package net.sf.hajdbc.distributed.jgroups;

import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.hajdbc.distributed.Command;
import net.sf.hajdbc.distributed.CommandDispatcher;
import net.sf.hajdbc.distributed.CommandResponse;
import net.sf.hajdbc.distributed.Member;
import net.sf.hajdbc.distributed.MembershipListener;
import net.sf.hajdbc.distributed.Stateful;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.util.ObjectInputStream;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

public class JGroupsCommandDispatcher<C> implements CommandDispatcher<C> {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String id;
	private final long timeout;
	private final C context;
	private final Stateful stateful;
	private final MembershipListener membershipListener;
	private JChannel channel;
	private AddressMember localMember;
	private final AtomicReference<View> currentView = new AtomicReference<>();
	private final Map<Address, CommandResponse<?>> pendingResponses = new ConcurrentHashMap<>();

	public JGroupsCommandDispatcher(String id, JChannel channel, long timeout, C context, Stateful stateful, MembershipListener membershipListener) {
		this.id = id;
		this.channel = channel;
		this.timeout = timeout;
		this.context = context;
		this.stateful = stateful;
		this.membershipListener = membershipListener;
	}

	@Override
	public void start() {
		this.channel.setReceiver(new Receiver() {
			@Override
			public void viewAccepted(View newView) {
				JGroupsCommandDispatcher.this.onViewAccepted(newView);
			}
			@Override
			public void receive(Message msg) {
				JGroupsCommandDispatcher.this.onReceive(msg);
			}
		});
		try {
			this.channel.connect(this.id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.localMember = new AddressMember(this.channel.getAddress());
		this.currentView.set(this.channel.getView());
		try {
			this.fetchState();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void onViewAccepted(View newView) {
		View oldView = this.currentView.getAndSet(newView);
		if (oldView != null) {
			List<Address> oldMembers = oldView.getMembers();
			List<Address> newMembers = newView.getMembers();
			for (Address address : newMembers) {
				if (!oldMembers.contains(address) && this.membershipListener != null) {
					this.membershipListener.added(new AddressMember(address));
				}
			}
			for (Address address : oldMembers) {
				if (!newMembers.contains(address) && this.membershipListener != null) {
					this.membershipListener.removed(new AddressMember(address));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void onReceive(Message msg) {
		try {
			Object data = msg.getObject();
			if (data instanceof Command) {
				Command<Object, C> command = (Command<Object, C>) data;
				this.logger.log(Level.DEBUG, "Processing command: {0}", command);
				Object result = command.execute(this.context);
				Message response = new org.jgroups.ObjectMessage(msg.getSrc(), result);
				this.channel.send(response);
			}
		} catch (Exception e) {
			this.logger.log(Level.ERROR, "Error processing message: " + e.getMessage(), e);
		}
	}

	private void fetchState() throws Exception {
		View view = this.currentView.get();
		if (view != null && !view.getMembers().isEmpty()) {
			Address coordinator = view.getMembers().get(0);
			if (!coordinator.equals(this.channel.getAddress())) {
				Message request = new org.jgroups.ObjectMessage(coordinator, "STATE_TRANSFER");
				this.channel.send(request);
			}
		}
	}

	@Override
	public void stop() {
		if (this.channel != null && this.channel.isConnected()) {
			this.channel.close();
		}
	}

	@Override
	public <R> Map<Member, CommandResponse<R>> executeAll(Command<R, C> command, Member... excludedMembers) throws Exception {
		List<Member> excluded = Arrays.asList(excludedMembers);
		View view = this.currentView.get();
		Map<Member, CommandResponse<R>> results = new TreeMap<>();
		if (view == null) return results;
		for (Address address : view.getMembers()) {
			AddressMember addressMember = new AddressMember(address);
			if (addressMember.equals(this.localMember)) {
				R result = command.execute(this.context);
				results.put(addressMember, new SimpleCommandResponse<>(result));
			} else if (!excluded.contains(addressMember)) {
				Message msg = new org.jgroups.ObjectMessage(address, command);
				this.channel.send(msg);
				CommandResponse<R> response = this.waitForResponse(addressMember);
				results.put(addressMember, response);
			}
		}
		return results;
	}

	@Override
	public <R> CommandResponse<R> execute(Command<R, C> command, Member member) throws Exception {
		if (!(member instanceof AddressMember)) {
			throw new IllegalArgumentException("Member must be an AddressMember: " + member.getClass().getName());
		}
		AddressMember addressMember = (AddressMember) member;
		if (addressMember.equals(this.localMember)) {
			R result = command.execute(this.context);
			return new SimpleCommandResponse<>(result);
		}
		Message msg = new org.jgroups.ObjectMessage(addressMember.getAddress(), command);
		this.channel.send(msg);
		return this.waitForResponse(addressMember);
	}

	@SuppressWarnings("unchecked")
	private <R> CommandResponse<R> waitForResponse(AddressMember member) throws Exception {
		long deadline = System.currentTimeMillis() + this.timeout;
		while (System.currentTimeMillis() < deadline) {
			CommandResponse<R> response = (CommandResponse<R>) this.pendingResponses.remove(member.getAddress());
			if (response != null) return response;
			TimeUnit.MILLISECONDS.sleep(100);
		}
		return new TimeoutCommandResponse<>();
	}

	@Override
	public AddressMember getLocal() {
		return this.localMember;
	}

	@Override
	public AddressMember getCoordinator() {
		View view = this.currentView.get();
		if (view == null || view.getMembers().isEmpty()) return null;
		return new AddressMember(view.getMembers().get(0));
	}

	public void getState(OutputStream output) throws Exception {
		try (ObjectOutput out = new ObjectOutputStream(output)) {
			this.stateful.writeState(out);
			out.flush();
		}
	}

	public void setState(InputStream input) throws Exception {
		try (ObjectInputStream inputStream = new ObjectInputStream(input, Stateful.class.getClassLoader())) {
			this.stateful.readState(inputStream);
		}
	}

	private static class SimpleCommandResponse<R> implements CommandResponse<R> {
		private final R result;
		SimpleCommandResponse(R result) { this.result = result; }
		@Override
		public R get() { return this.result; }
	}

	private static class TimeoutCommandResponse<R> implements CommandResponse<R> {
		@Override
		public R get() throws ExecutionException {
			throw new ExecutionException(new java.util.concurrent.TimeoutException("Command execution timed out"));
		}
	}
}
