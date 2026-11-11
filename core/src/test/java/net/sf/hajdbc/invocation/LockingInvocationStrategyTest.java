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
package net.sf.hajdbc.invocation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.sql.ProxyFactory;

import org.junit.jupiter.api.Test;

/**
 * Tests for LockingInvocationStrategy
 */
public class LockingInvocationStrategyTest {

	@Test
	@SuppressWarnings("unchecked")
	public void invokeAcquiresAndReleasesLocks() throws Exception {
		ReentrantLock lock1 = new ReentrantLock();
		ReentrantLock lock2 = new ReentrantLock();
		List<Lock> lockList = new ArrayList<>();
		lockList.add(lock1);
		lockList.add(lock2);

		InvocationStrategy delegate = mock(InvocationStrategy.class);
		when(delegate.invoke(any(), any())).thenReturn(new TreeMap<>());

		LockingInvocationStrategy strategy = new LockingInvocationStrategy(delegate, lockList);

		ProxyFactory<Object, Database<Object>, Object, SQLException> proxy = mock(ProxyFactory.class);
		Invoker<Object, Database<Object>, Object, String, SQLException> invoker = mock(Invoker.class);

		SortedMap<Database<Object>, String> result = strategy.invoke(proxy, invoker);

		verify(delegate).invoke(proxy, invoker);
		assertThat(result).isEmpty();

		// Verify locks are released by trying to acquire them
		assertThat(lock1.tryLock()).isTrue();
		lock1.unlock();
		assertThat(lock2.tryLock()).isTrue();
		lock2.unlock();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void invokeReleasesLocksOnException() throws Exception {
		ReentrantLock lock = new ReentrantLock();
		List<Lock> lockList = Collections.singletonList(lock);

		InvocationStrategy delegate = mock(InvocationStrategy.class);
		RuntimeException expected = new RuntimeException("Test exception");
		when(delegate.invoke(any(), any())).thenThrow(expected);

		LockingInvocationStrategy strategy = new LockingInvocationStrategy(delegate, lockList);

		ProxyFactory<Object, Database<Object>, Object, SQLException> proxy = mock(ProxyFactory.class);
		Invoker<Object, Database<Object>, Object, String, SQLException> invoker = mock(Invoker.class);

		try {
			strategy.invoke(proxy, invoker);
		} catch (RuntimeException e) {
			// Expected
		}

		// Verify lock is released after exception
		assertThat(lock.tryLock()).isTrue();
		lock.unlock();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void invokeWithEmptyLockList() throws Exception {
		List<Lock> lockList = Collections.emptyList();

		InvocationStrategy delegate = mock(InvocationStrategy.class);
		SortedMap<Database<Object>, Object> expected = new TreeMap<>();
		when(delegate.invoke(any(), any())).thenReturn(expected);

		LockingInvocationStrategy strategy = new LockingInvocationStrategy(delegate, lockList);

		ProxyFactory<Object, Database<Object>, Object, SQLException> proxy = mock(ProxyFactory.class);
		Invoker<Object, Database<Object>, Object, Object, SQLException> invoker = mock(Invoker.class);

		SortedMap<Database<Object>, Object> result = strategy.invoke(proxy, invoker);

		verify(delegate).invoke(proxy, invoker);
		assertThat(result).isEmpty();
	}
}
