/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2012  Paul Ferraro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.hajdbc.distributed.pekko;

import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

import org.apache.pekko.actor.Address;
import org.apache.pekko.actor.AddressFromURIString;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.cluster.Cluster;
import org.apache.pekko.cluster.ClusterEvent;
import org.apache.pekko.cluster.MemberStatus;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Apache Pekkoベースのコマンドディスパッチャーです。
 * Pekko Clusterでメンバー管理し、Actor間のメッセージ送信でコマンドを分散実行します。
 * 
 * @author Paul Ferraro
 * @param <C> コマンドコンテキストの型
 */
public class PekkoCommandDispatcher<C> implements CommandDispatcher<C>
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final String id;
	private final long timeout;
	private final C context;
	private final Stateful stateful;
	private final MembershipListener membershipListener;
	
	private final String systemName;
	private final int port;
	private final List<String> seedNodes;
	
	private ActorSystem<ExecuteCommand> system;
	private Address localAddress;

	/**
	 * Constructs a new PekkoCommandDispatcher.
	 * @param id the cluster id
	 * @param systemName the Pekko ActorSystem name
	 * @param port the port for this node
	 * @param timeout the command timeout in milliseconds
	 * @param seedNodes the seed nodes for cluster joining
	 * @param context the execution context
	 * @param stateful the state transfer handler
	 * @param membershipListener notified of membership changes
	 */
	public PekkoCommandDispatcher(String id, String systemName, int port, long timeout,
			List<String> seedNodes, C context, Stateful stateful, MembershipListener membershipListener)
	{
		this.id = id;
		this.systemName = systemName;
		this.port = port;
		this.timeout = timeout;
		this.seedNodes = seedNodes != null ? seedNodes : Collections.emptyList();
		this.context = context;
		this.stateful = stateful;
		this.membershipListener = membershipListener;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.Lifecycle#start()
	 */
	@Override
	public void start() throws SQLException
	{
		try
		{
			// Create configuration
 		String config = String.format(
 			"pekko.actor.provider = cluster%n" +
 			"pekko.remote.artery.canonical.hostname = \"0.0.0.0\"%n" +
 			"pekko.remote.artery.canonical.port = %d%n" +
 			"pekko.cluster.seed-nodes = [%s]%n" +
 			"pekko.cluster.split-brain-resolver.provider-class = \"org.apache.pekko.cluster.sbr.SplitBrainResolverProvider\"%n",
 			this.port,
 			formatSeedNodes()
 		);
			
			// Create Config from string
			Config parsedConfig = ConfigFactory.parseString(config);
			
			// Create ActorSystem with the command handler as root behavior
			this.system = ActorSystem.create(
				CommandHandler.create(this.context, this.stateful), 
				this.systemName, parsedConfig);
			
			// Get local address
			this.localAddress = Cluster.get(this.system.classicSystem()).selfAddress();
			
			// Subscribe to cluster events
			Cluster.get(this.system.classicSystem()).subscribe(
				this.system.classicSystem().deadLetters(),
				ClusterEvent.initialStateAsEvents(),
				ClusterEvent.MemberJoined.class,
				ClusterEvent.MemberUp.class,
				ClusterEvent.MemberRemoved.class,
				ClusterEvent.MemberLeft.class,
				ClusterEvent.MemberExited.class
			);
			
			// Join seed nodes if configured
			if (!this.seedNodes.isEmpty())
			{
				Cluster classicCluster = Cluster.get(this.system.classicSystem());
				List<Address> addresses = new ArrayList<>();
				for (String seed : this.seedNodes)
				{
					addresses.add(AddressFromURIString.parse(seed));
				}
				classicCluster.joinSeedNodes(addresses);
			}
			
		}
		catch (Exception e)
		{
			throw new SQLException("Failed to start PekkoCommandDispatcher: " + e.getMessage(), e);
		}
	}
	
	private String formatSeedNodes()
	{
		if (this.seedNodes.isEmpty())
		{
			// Default to self as seed node
			return String.format("\"pekko://%s@127.0.0.1:%d\"", this.systemName, this.port);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.seedNodes.size(); i++)
		{
			if (i > 0) sb.append(", ");
			sb.append("\"").append(this.seedNodes.get(i)).append("\"");
		}
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.Lifecycle#stop()
	 */
	@Override
	public void stop()
	{
		try
		{
			if (this.system != null)
			{
				Cluster.get(this.system.classicSystem()).leave(this.localAddress);
				this.system.terminate();
			}
		}
		catch (Exception e)
		{
			this.logger.log(Level.ERROR, "Error stopping PekkoCommandDispatcher: " + e.getMessage(), e);
		}
	}

	@Override
	public <R> Map<Member, CommandResponse<R>> executeAll(Command<R, C> command, Member... excludedMembers) throws Exception
	{
		List<Member> excluded = Arrays.asList(excludedMembers);
		List<PekkoMember> currentMembers = getCurrentMembers();
		
		Map<Member, CommandResponse<R>> results = new TreeMap<>();
		
		for (PekkoMember member : currentMembers)
		{
			if (member.getAddress().equals(this.localAddress))
			{
				// Execute locally
				R result = command.execute(this.context);
				results.put(member, new SimpleCommandResponse<>(result));
			}
			else if (!excluded.contains(member))
			{
				// Send command to remote member
				CompletionStage<Object> future = sendCommand(member, command);
				try
				{
					Object result = future.toCompletableFuture().get(this.timeout, TimeUnit.MILLISECONDS);
					if (result instanceof Exception)
					{
						results.put(member, new ExceptionCommandResponse<>(new ExecutionException((Exception) result)));
					}
					else
					{
						results.put(member, new SimpleCommandResponse<>((R) result));
					}
				}
				catch (TimeoutException e)
				{
					results.put(member, new TimeoutCommandResponse<>());
				}
				catch (ExecutionException e)
				{
					results.put(member, new ExceptionCommandResponse<>(e));
				}
			}
		}
		
		return results;
	}
	
	@Override
	public <R> CommandResponse<R> execute(Command<R, C> command, Member member) throws Exception
	{
		if (!(member instanceof PekkoMember))
		{
			throw new IllegalArgumentException("Member must be a PekkoMember: " + member.getClass().getName());
		}
		
		PekkoMember pekkoMember = (PekkoMember) member;
		
		if (pekkoMember.getAddress().equals(this.localAddress))
		{
			R result = command.execute(this.context);
			return new SimpleCommandResponse<>(result);
		}
		
		// Send command to remote member
		CompletionStage<Object> future = sendCommand(pekkoMember, command);
		try
		{
			Object result = future.toCompletableFuture().get(this.timeout, TimeUnit.MILLISECONDS);
			if (result instanceof Exception)
			{
				return new ExceptionCommandResponse<>(new ExecutionException((Exception) result));
			}
			return new SimpleCommandResponse<>((R) result);
		}
		catch (TimeoutException e)
		{
			return new TimeoutCommandResponse<>();
		}
		catch (ExecutionException e)
		{
			return new ExceptionCommandResponse<>(e);
		}
	}
	
	private <R> CompletionStage<Object> sendCommand(PekkoMember target, Command<R, C> command)
	{
		// Ask the root actor (which is our CommandHandler) to execute the command
		// The root actor's name is the system name
		return AskPattern.ask(
			this.system,
			replyTo -> new ExecuteCommand(command, replyTo),
			java.time.Duration.ofMillis(this.timeout),
			this.system.scheduler()
		);
	}
	
	private List<PekkoMember> getCurrentMembers()
	{
		if (this.system == null) return Collections.emptyList();
		
		Cluster cluster = Cluster.get(this.system.classicSystem());
		ClusterEvent.CurrentClusterState state = cluster.state();
		
		List<PekkoMember> result = new ArrayList<>();
		for (org.apache.pekko.cluster.Member member : state.getMembers())
		{
			if (member.status() == MemberStatus.up())
			{
				result.add(new PekkoMember(member.address()));
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.distributed.CommandDispatcher#getLocal()
	 */
	@Override
	public PekkoMember getLocal()
	{
		return new PekkoMember(this.localAddress);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.distributed.CommandDispatcher#getCoordinator()
	 */
	@Override
	public PekkoMember getCoordinator()
	{
		if (this.system == null) return null;
		
		Cluster cluster = Cluster.get(this.system.classicSystem());
		ClusterEvent.CurrentClusterState state = cluster.state();
		
		// The leader is the oldest UP member
		for (org.apache.pekko.cluster.Member member : state.getMembers())
		{
			if (member.status() == MemberStatus.up())
			{
				return new PekkoMember(member.address());
			}
		}
		return null;
	}

	public void getState(OutputStream output) throws Exception
	{
		try (ObjectOutput out = new ObjectOutputStream(output))
		{
			this.stateful.writeState(out);
			out.flush();
		}
	}

	public void setState(InputStream input) throws Exception
	{
		try (ObjectInputStream inputStream = new ObjectInputStream(input, Stateful.class.getClassLoader()))
		{
			this.stateful.readState(inputStream);
		}
	}
	
	// Publish state
	public void publishState() throws Exception
	{
		// State is managed locally in Pekko
		// Can be extended with distributed data if needed
	}

	private static class SimpleCommandResponse<R> implements CommandResponse<R>
	{
		private final R result;
		
		SimpleCommandResponse(R result)
		{
			this.result = result;
		}
		
		@Override
		public R get()
		{
			return this.result;
		}
	}

	private static class TimeoutCommandResponse<R> implements CommandResponse<R>
	{
		@Override
		public R get() throws ExecutionException
		{
			throw new ExecutionException(new TimeoutException("Command execution timed out"));
		}
	}

	private static class ExceptionCommandResponse<R> implements CommandResponse<R>
	{
		private final ExecutionException exception;
		
		ExceptionCommandResponse(ExecutionException exception)
		{
			this.exception = exception;
		}
		
		@Override
		public R get() throws ExecutionException
		{
			throw this.exception;
		}
	}
	
	/**
	 * Execute command message.
	 */
	public static final class ExecuteCommand implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		private final Command<?, ?> command;
		private final ActorRef<Object> replyTo;
		
		ExecuteCommand(Command<?, ?> command, ActorRef<Object> replyTo)
		{
			this.command = command;
			this.replyTo = replyTo;
		}
		
		public Command<?, ?> getCommand()
		{
			return this.command;
		}
		
		public ActorRef<Object> getReplyTo()
		{
			return this.replyTo;
		}
	}
	
	/**
	 * Command handler actor behavior.
	 */
	public static class CommandHandler<C> extends AbstractBehavior<ExecuteCommand>
	{
		private final C context;
		private final Stateful stateful;
		
		public CommandHandler(ActorContext<ExecuteCommand> context, C ctx, Stateful stateful)
		{
			super(context);
			this.context = ctx;
			this.stateful = stateful;
		}
		
		public static <C> Behavior<ExecuteCommand> create(C ctx, Stateful stateful)
		{
			return Behaviors.setup(context -> new CommandHandler<>(context, ctx, stateful));
		}
		
		@Override
		public Receive<ExecuteCommand> createReceive()
		{
			return newReceiveBuilder()
				.onMessage(ExecuteCommand.class, this::onExecuteCommand)
				.build();
		}
		
		private Behavior<ExecuteCommand> onExecuteCommand(ExecuteCommand request)
		{
			try
			{
				@SuppressWarnings("unchecked")
				Command<Object, C> cmd = (Command<Object, C>) request.getCommand();
				Object result = cmd.execute(this.context);
				request.getReplyTo().tell(result);
			}
			catch (Exception e)
			{
				request.getReplyTo().tell(e);
			}
			return this;
		}
	}
}
