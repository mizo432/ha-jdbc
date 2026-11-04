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

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.Set;
import java.util.TreeMap;

import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.MockDatabase;
import net.sf.hajdbc.balancer.Balancer;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.sql.ProxyFactory;
import net.sf.hajdbc.state.StateManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for InvokeOnAnyInvocationStrategy
 */
public class InvokeOnAnyInvocationStrategyTest {

	@Test
	public void invokeSucceedsOnFirstDatabase() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		Set<Map.Entry<MockDatabase, String>> entries = new HashSet<>(Arrays.asList(
			new AbstractMap.SimpleImmutableEntry<>(db1, "obj1"),
			new AbstractMap.SimpleImmutableEntry<>(db2, "obj2")
		));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(balancer.contains(db1)).thenReturn(true);
		when(balancer.contains(db2)).thenReturn(true);
		when(factory.entries()).thenReturn(entries);

		InvokeOnOneInvocationStrategy fallbackStrategy = mock(InvokeOnOneInvocationStrategy.class);

		InvokeOnAnyInvocationStrategy strategy = new InvokeOnAnyInvocationStrategy(fallbackStrategy);

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result-" + database.getId();
			}
		};

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, invoker);

		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals("result-1", result.get(db1));
		verify(fallbackStrategy, never()).invoke(any(ProxyFactory.class), any(Invoker.class));
	}

	@Test
	public void invokeFallsBackWhenAllDatabasesFail() throws Exception {
		MockDatabase db1 = new MockDatabase("1");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);

		RuntimeException failureException = new RuntimeException("Database failure");

		Set<Map.Entry<MockDatabase, String>> entries = new HashSet<>(Arrays.asList(
			new AbstractMap.SimpleImmutableEntry<>(db1, "obj1")
		));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(balancer.contains(db1)).thenReturn(true);
		when(balancer.size()).thenReturn(1); // Only 1 database, so exception is thrown
		when(exceptionFactory.createException(any(Exception.class))).thenReturn(failureException);
		when(exceptionFactory.indicatesFailure(eq(failureException), eq(dialect))).thenReturn(true);
		when(factory.entries()).thenReturn(entries);

		InvokeOnOneInvocationStrategy fallbackStrategy = mock(InvokeOnOneInvocationStrategy.class);

		InvokeOnAnyInvocationStrategy strategy = new InvokeOnAnyInvocationStrategy(fallbackStrategy);

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				throw new RuntimeException("Database failure");
			}
		};

		try {
			strategy.invoke(factory, invoker);
			Assertions.fail("Expected exception");
		} catch (RuntimeException e) {
			Assertions.assertEquals("Database failure", e.getMessage());
		}
	}

	@Test
	public void invokeDeactivatesFailedDatabaseAndTriesNext() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);

		RuntimeException failureException = new RuntimeException("Database failure");

		Set<Map.Entry<MockDatabase, String>> entries = new HashSet<>(Arrays.asList(
			new AbstractMap.SimpleImmutableEntry<>(db1, "obj1"),
			new AbstractMap.SimpleImmutableEntry<>(db2, "obj2")
		));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(balancer.contains(db1)).thenReturn(true);
		when(balancer.contains(db2)).thenReturn(true);
		when(balancer.size()).thenReturn(2); // More than 1, so deactivate and try next
		when(exceptionFactory.createException(any(Exception.class))).thenReturn(failureException);
		when(exceptionFactory.indicatesFailure(eq(failureException), eq(dialect))).thenReturn(true);
		when(cluster.deactivate(eq(db1), eq(stateManager))).thenReturn(true);
		when(factory.entries()).thenReturn(entries);

		InvokeOnOneInvocationStrategy fallbackStrategy = mock(InvokeOnOneInvocationStrategy.class);

		InvokeOnAnyInvocationStrategy strategy = new InvokeOnAnyInvocationStrategy(fallbackStrategy);

		final boolean[] firstCall = {true};
		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				if (database.getId().equals("1") && firstCall[0]) {
					firstCall[0] = false;
					throw new RuntimeException("Database failure");
				}
				return "result-" + database.getId();
			}
		};

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, invoker);

		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals("result-2", result.get(db2));
		verify(cluster).deactivate(eq(db1), eq(stateManager));
	}

	@Test
	public void invokeSkipsInactiveDatabase() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		Set<Map.Entry<MockDatabase, String>> entries = new HashSet<>(Arrays.asList(
			new AbstractMap.SimpleImmutableEntry<>(db1, "obj1"),
			new AbstractMap.SimpleImmutableEntry<>(db2, "obj2")
		));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(balancer.contains(db1)).thenReturn(false); // db1 is inactive
		when(balancer.contains(db2)).thenReturn(true);
		when(factory.entries()).thenReturn(entries);

		InvokeOnOneInvocationStrategy fallbackStrategy = mock(InvokeOnOneInvocationStrategy.class);

		InvokeOnAnyInvocationStrategy strategy = new InvokeOnAnyInvocationStrategy(fallbackStrategy);

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result-" + database.getId();
			}
		};

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, invoker);

		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals("result-2", result.get(db2));
		verify(fallbackStrategy, never()).invoke(any(ProxyFactory.class), any(Invoker.class));
	}

	@Test
	public void invokeDelegatesToFallbackWhenNoExistingDatabases() throws Exception {
		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(factory.entries()).thenReturn(new HashSet<>()); // No existing databases

		MockDatabase db1 = new MockDatabase("1");
		SortedMap<MockDatabase, String> fallbackResult = new TreeMap<>();
		fallbackResult.put(db1, "fallback-result");

		InvokeOnOneInvocationStrategy fallbackStrategy = mock(InvokeOnOneInvocationStrategy.class);
		when(fallbackStrategy.invoke(eq(factory), any(Invoker.class))).thenReturn(fallbackResult);

		InvokeOnAnyInvocationStrategy strategy = new InvokeOnAnyInvocationStrategy(fallbackStrategy);

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		};

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, invoker);

		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals("fallback-result", result.get(db1));
		verify(fallbackStrategy).invoke(eq(factory), any(Invoker.class));
	}
}
