package net.sf.hajdbc.distributed.jgroups;

import net.sf.hajdbc.distributed.CommandDispatcher;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.distributed.MembershipListener;
import net.sf.hajdbc.distributed.Stateful;
import org.jgroups.JChannel;

public class JGroupsCommandDispatcherFactory implements CommandDispatcherFactory {
	private static final long serialVersionUID = 5135621114239237376L;
	public static final long DEFAULT_TIMEOUT = 60000;
	public static final String DEFAULT_STACK = "udp.xml";
	
	private String stack = DEFAULT_STACK;
	private long timeout = DEFAULT_TIMEOUT;
	private String name;

	@Override
	public String getId() {
		return "jgroups";
	}

	@Override
	public <C> CommandDispatcher<C> createCommandDispatcher(String id, C context, Stateful stateful, MembershipListener membershipListener) throws Exception {
		JChannel channel = new JChannel(this.stack);
		if (this.name != null) {
			channel.setName(this.name);
		}
		return new JGroupsCommandDispatcher<>(id, channel, this.timeout, context, stateful, membershipListener);
	}

	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }
	public String getStack() { return this.stack; }
	public void setStack(String stack) { this.stack = stack; }
	public long getTimeout() { return this.timeout; }
	public void setTimeout(long timeout) { this.timeout = timeout; }
}
