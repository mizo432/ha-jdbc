/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2014  Paul Ferraro
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
package net.sf.hajdbc.lock.distributed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.distributed.jgroups.JGroupsCommandDispatcherFactory;
import net.sf.hajdbc.lock.LockManager;
import net.sf.hajdbc.lock.semaphore.SemaphoreLockManager;

public class DistributedLockManagerTest
{
	LockManager manager1;
	LockManager manager2;

	@BeforeEach
	public void init() throws Exception
	{
		String id = "cluster";
		DatabaseCluster<?, ?> cluster1 = mock(DatabaseCluster.class);
		DatabaseCluster<?, ?> cluster2 = mock(DatabaseCluster.class);
		LockManager lockManager1 = new SemaphoreLockManager(false);
		LockManager lockManager2 = new SemaphoreLockManager(false);
		CommandDispatcherFactory dispatcherFactory1 = createCommandDispatcherFactory("node1");
		CommandDispatcherFactory dispatcherFactory2 = createCommandDispatcherFactory("node2");

		when(cluster1.getId()).thenReturn(id);
		when(cluster1.getLockManager()).thenReturn(lockManager1);
		when(cluster2.getId()).thenReturn(id);
		when(cluster2.getLockManager()).thenReturn(lockManager2);

		this.manager1 = new DistributedLockManager(cluster1, dispatcherFactory1);
		this.manager1.start();
		this.manager2 = new DistributedLockManager(cluster2, dispatcherFactory2);
		this.manager2.start();
	}

	static CommandDispatcherFactory createCommandDispatcherFactory(String name)
	{
		JGroupsCommandDispatcherFactory factory = new JGroupsCommandDispatcherFactory();
		factory.setName(name);
		factory.setStack("fast.xml");
		return factory;
	}

	@AfterEach
	public void destroy()
	{
		this.manager1.stop();
		this.manager1 = null;
		this.manager2.stop();
		this.manager2 = null;
	}

	@Test
	@Disabled("JGroups分散テストはJava 25で不安定")
	public void simple()
	{
		test(this.manager1);
		test(this.manager2);
	}

	private static void test(LockManager manager)
	{
		test(manager.readLock("1"));
		test(manager.readLock(null));
		test(manager.writeLock("1"));
		test(manager.writeLock(null));
	}

	private static void test(Lock lock)
	{
		assertThat(lock.tryLock()).isTrue();
		lock.unlock();
	}

	@Test
	@Disabled("JGroups分散テストはJava 25で不安定")
	public void concurrent() throws Exception
	{
		final LockManager manager = this.manager1;
		ExecutorService executor = Executors.newFixedThreadPool(10);
		try
		{
			Future<?>[] futures = new Future[10];
			for (int i = 0; i < futures.length; i++)
			{
				futures[i] = executor.submit(() ->
				{
					Lock lock = manager.writeLock("1");
					lock.lock();
					try
					{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					finally
					{
						lock.unlock();
					}
				});
			}
			for (Future<?> future : futures)
			{
				future.get(10, TimeUnit.SECONDS);
			}
		}
		finally
		{
			executor.shutdown();
		}
	}
}
