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
package net.sf.hajdbc.distributed.zookeeper;

import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
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
import net.sf.hajdbc.util.Objects;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Zookeeperベースのコマンドディスパッチャーです。
 * Zookeeperのephemeralノードでメンバー管理し、DistributedQueueでコマンドを分散実行します。
 * 
 * @author Paul Ferraro
 * @param <C> コマンドコンテキストの型
 */
public class ZookeeperCommandDispatcher<C> implements CommandDispatcher<C>
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final String MEMBERS_SUFFIX = "/members";
	private static final String COMMANDS_SUFFIX = "/commands";
	private static final String RESPONSES_SUFFIX = "/responses";
	private static final String STATE_SUFFIX = "/state";
	
	private final String id;
	private final long timeout;
	private final C context;
	private final Stateful stateful;
	private final MembershipListener membershipListener;
	
	private final String connectionString;
	private int baseSleepTime;
	private int maxRetries;
	
	private CuratorFramework client;
	private PathChildrenCache memberCache;
	private String localNodePath;
	private final AtomicReference<List<String>> memberPaths = new AtomicReference<>(Collections.emptyList());
	
	/**
	 * Constructs a new ZookeeperCommandDispatcher.
	 * @param id the cluster id
	 * @param connectionString Zookeeper connection string
	 * @param timeout the command timeout in milliseconds
	 * @param context the execution context
	 * @param stateful the state transfer handler
	 * @param membershipListener notified of membership changes
	 */
	public ZookeeperCommandDispatcher(String id, String connectionString, long timeout,
			C context, Stateful stateful, MembershipListener membershipListener)
	{
		this.id = id;
		this.connectionString = connectionString;
		this.timeout = timeout;
		this.context = context;
		this.stateful = stateful;
		this.membershipListener = membershipListener;
		this.baseSleepTime = 1000;
		this.maxRetries = 5;
	}

	public void setBaseSleepTime(int baseSleepTime)
	{
		this.baseSleepTime = baseSleepTime;
	}

	public void setMaxRetries(int maxRetries)
	{
		this.maxRetries = maxRetries;
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
			this.client = CuratorFrameworkFactory.builder()
				.connectString(this.connectionString)
				.sessionTimeoutMs((int)this.timeout)
				.retryPolicy(new ExponentialBackoffRetry(this.baseSleepTime, this.maxRetries))
				.namespace(this.id)
				.build();
			this.client.start();
			
			// Wait for connection
 		if (!this.client.blockUntilConnected((int)this.timeout, TimeUnit.MILLISECONDS))
			{
				throw new SQLException("Failed to connect to Zookeeper at '" + this.connectionString + "' within timeout");
			}
			
			// Ensure base paths exist
			ensurePath(this.client, MEMBERS_SUFFIX);
			ensurePath(this.client, COMMANDS_SUFFIX);
			ensurePath(this.client, RESPONSES_SUFFIX);
			ensurePath(this.client, STATE_SUFFIX);
			
			// Register this member as ephemeral node
			String nodeName = "node-" + System.currentTimeMillis() + "-";
			this.localNodePath = this.client.create()
				.withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
				.forPath(MEMBERS_SUFFIX + "/" + nodeName);
			
			// Fetch initial state from coordinator if exists
			this.fetchState();
			
			// Start member cache for membership changes
			this.memberCache = new PathChildrenCache(this.client, MEMBERS_SUFFIX, true);
			this.memberCache.getListenable().addListener((curator, event) ->
			{
				List<String> current = this.memberPaths.get();
				List<String> updated = new ArrayList<>();
				for (ChildData child : this.memberCache.getCurrentData())
				{
					updated.add(child.getPath());
				}
				
				// Detect added members
				for (String path : updated)
				{
					if (!current.contains(path) && !path.equals(this.localNodePath))
					{
						if (this.membershipListener != null)
						{
							this.membershipListener.added(new ZookeeperMember(path));
						}
					}
				}
				
				// Detect removed members
				for (String path : current)
				{
					if (!updated.contains(path) && !path.equals(this.localNodePath))
					{
						if (this.membershipListener != null)
						{
							this.membershipListener.removed(new ZookeeperMember(path));
						}
					}
				}
				
				this.memberPaths.set(updated);
			});
			this.memberCache.start(StartMode.POST_INITIALIZED_EVENT);
			
			// Start command listener thread
			this.startCommandListener();
			
		}
		catch (Exception e)
		{
			throw new SQLException("Failed to start ZookeeperCommandDispatcher: " + e.getMessage(), e);
		}
	}
	
	private void ensurePath(CuratorFramework client, String path) throws Exception
	{
		try
		{
			if (client.checkExists().forPath(path) == null)
			{
				client.create().forPath(path);
			}
		}
		catch (KeeperException.NodeExistsException e)
		{
			// Another node created it first
		}
	}
	
	private void fetchState() throws Exception
	{
		byte[] data = this.client.getData().forPath(STATE_SUFFIX);
		if (data != null && data.length > 0)
		{
			try (ObjectInputStream input = new ObjectInputStream(new java.io.ByteArrayInputStream(data), Stateful.class.getClassLoader()))
			{
				this.stateful.readState(input);
			}
		}
	}
	
	private final Object commandLock = new Object();
	private final Map<String, CommandResponse<?>> pendingResponses = new ConcurrentHashMap<>();
	
	private void startCommandListener()
	{
		Thread listener = new Thread(() ->
		{
			try
			{
 			while (!Thread.currentThread().isInterrupted() && this.client != null && this.client.getState() == CuratorFrameworkState.STARTED)
				{
					List<String> commands = this.client.getChildren().forPath(COMMANDS_SUFFIX);
					for (String cmdName : commands)
					{
						String cmdPath = COMMANDS_SUFFIX + "/" + cmdName;
						try
						{
							byte[] data = this.client.getData().forPath(cmdPath);
							if (data != null)
							{
								Command<Object, C> command = Objects.deserialize(data, Command.class);
								// Only process commands not targeted at a specific member, or targeted at us
								byte[] targetData = this.client.getData().forPath(cmdPath + "/target");
								String targetStr = targetData != null ? new String(targetData) : null;
								if (targetStr == null || targetStr.equals(this.localNodePath))
								{
									this.logger.log(Level.DEBUG, "Processing command: {0}", command);
									Object result = command.execute(this.context);
									// Store response
									String respPath = this.client.create()
										.withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
										.forPath(RESPONSES_SUFFIX + "/" + cmdName + "-resp-");
									this.client.setData().forPath(respPath, Objects.serialize(result));
									// Delete processed command
									this.client.delete().forPath(cmdPath + "/target");
									this.client.delete().forPath(cmdPath);
								}
							}
						}
						catch (KeeperException.NoNodeException e)
						{
							// Already processed
						}
					}
					Thread.sleep(100);
				}
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			catch (Exception e)
			{
				this.logger.log(Level.ERROR, "Command listener error: " + e.getMessage(), e);
			}
		});
		listener.setDaemon(true);
		listener.setName("zookeeper-command-listener-" + this.id);
		listener.start();
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
			if (this.memberCache != null)
			{
				this.memberCache.close();
			}
 		if (this.client != null && this.client.getState() == CuratorFrameworkState.STARTED)
			{
				if (this.localNodePath != null)
				{
					try
					{
						this.client.delete().forPath(this.localNodePath);
					}
					catch (Exception e)
					{
						// Ignore
					}
				}
				this.client.close();
			}
		}
		catch (Exception e)
		{
			this.logger.log(Level.ERROR, "Error stopping ZookeeperCommandDispatcher: " + e.getMessage(), e);
		}
	}

	@Override
	public <R> Map<Member, CommandResponse<R>> executeAll(Command<R, C> command, Member... excludedMembers) throws Exception
	{
		List<Member> excluded = Arrays.asList(excludedMembers);
		List<String> members = this.getCurrentMembers();
		
		Map<Member, CommandResponse<R>> results = new TreeMap<>();
		
		for (String memberPath : members)
		{
			if (memberPath.equals(this.localNodePath))
			{
				// Execute locally
				R result = command.execute(this.context);
				results.put(new ZookeeperMember(memberPath), new SimpleCommandResponse<>(result));
			}
			else if (!excluded.contains(new ZookeeperMember(memberPath)))
			{
 			// Submit command for remote execution
 			String cmdPath = this.client.create()
					.withMode(CreateMode.EPHEMERAL)
					.forPath(COMMANDS_SUFFIX + "/", Objects.serialize(command));
				this.client.create().forPath(cmdPath + "/target", (byte[])null);
				
 			// Wait for response with polling
 			long deadline = System.currentTimeMillis() + this.timeout;
 			boolean found = false;
 			while (System.currentTimeMillis() < deadline)
 			{
 				List<String> responses = this.client.getChildren().forPath(RESPONSES_SUFFIX);
 				for (String respName : responses)
 				{
 					if (respName.startsWith(cmdPath.substring(cmdPath.lastIndexOf('/') + 1) + "-resp-"))
 					{
 						byte[] data = this.client.getData().forPath(RESPONSES_SUFFIX + "/" + respName);
 						Object rawResult = Objects.deserialize(data, Object.class);
 						results.put(new ZookeeperMember(memberPath), new SimpleCommandResponse<>((R) rawResult));
 						this.client.delete().forPath(RESPONSES_SUFFIX + "/" + respName);
 						found = true;
 						break;
 					}
 				}
 				if (found) break;
 				Thread.sleep(100);
 			}
 			if (!found)
 			{
 				results.put(new ZookeeperMember(memberPath), new TimeoutCommandResponse<>());
 			}
			}
		}
		
		return results;
	}
	
	@Override
	public <R> CommandResponse<R> execute(Command<R, C> command, Member member) throws Exception
	{
		if (!(member instanceof ZookeeperMember))
		{
			throw new IllegalArgumentException("Member must be a ZookeeperMember: " + member.getClass().getName());
		}
		
		String targetPath = ((ZookeeperMember) member).getNodePath();
		
		if (targetPath.equals(this.localNodePath))
		{
			R result = command.execute(this.context);
			return new SimpleCommandResponse<>(result);
		}
		
		// Submit command for specific member
		String cmdPath = this.client.create()
			.withMode(CreateMode.EPHEMERAL)
			.forPath(COMMANDS_SUFFIX + "/", Objects.serialize(command));
		this.client.create().forPath(cmdPath + "/target", targetPath.getBytes());
		
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Object> resultRef = new AtomicReference<>();
		AtomicReference<Throwable> errorRef = new AtomicReference<>();
		
		// Poll for response
		long deadline = System.currentTimeMillis() + this.timeout;
		while (System.currentTimeMillis() < deadline)
		{
			List<String> responses = this.client.getChildren().forPath(RESPONSES_SUFFIX);
			for (String respName : responses)
			{
				if (respName.startsWith(cmdPath.substring(cmdPath.lastIndexOf('/') + 1) + "-resp-"))
				{
					byte[] data = this.client.getData().forPath(RESPONSES_SUFFIX + "/" + respName);
						resultRef.set(Objects.deserialize(data, Object.class));
					this.client.delete().forPath(RESPONSES_SUFFIX + "/" + respName);
					latch.countDown();
					break;
				}
			}
			if (resultRef.get() != null) break;
			Thread.sleep(100);
		}
		
		if (errorRef.get() != null)
		{
			return new ExceptionCommandResponse<>(new ExecutionException(errorRef.get()));
		}
		if (resultRef.get() != null)
		{
			return new SimpleCommandResponse<>((R) resultRef.get());
		}
		return new TimeoutCommandResponse<>();
	}
	
	private List<String> getCurrentMembers() throws Exception
	{
		List<String> members = new ArrayList<>();
		List<String> children = this.client.getChildren().forPath(MEMBERS_SUFFIX);
		for (String child : children)
		{
			members.add(MEMBERS_SUFFIX + "/" + child);
		}
		return members;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.distributed.CommandDispatcher#getLocal()
	 */
	@Override
	public ZookeeperMember getLocal()
	{
		return new ZookeeperMember(this.localNodePath);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.distributed.CommandDispatcher#getCoordinator()
	 */
	@Override
	public ZookeeperMember getCoordinator()
	{
		try
		{
			List<String> members = this.getCurrentMembers();
			if (members.isEmpty()) return null;
			Collections.sort(members);
			return new ZookeeperMember(members.get(0));
		}
		catch (Exception e)
		{
			return null;
		}
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
	
	// Publish state to Zookeeper
	public void publishState() throws Exception
	{
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		this.getState(baos);
		this.client.setData().forPath(STATE_SUFFIX, baos.toByteArray());
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
			throw new ExecutionException(new java.util.concurrent.TimeoutException("Command execution timed out"));
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
}
