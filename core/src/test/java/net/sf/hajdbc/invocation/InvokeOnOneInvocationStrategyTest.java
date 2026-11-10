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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.MockDatabase;
import net.sf.hajdbc.balancer.Balancer;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.sql.ProxyFactory;
import net.sf.hajdbc.state.StateManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for InvokeOnOneInvocationStrategy
 */
public class InvokeOnOneInvocationStrategyTest {

	@Test
	public void invokeWithPrimarySelector() throws Exception {
		MockDatabase database = new MockDatabase("1");
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		when(cluster.getBalancer()).thenReturn(balancer);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);

		when(balancer.primary()).thenReturn(database);
		when(balancer.contains(database)).thenReturn(true);
		when(balancer.invoke(any(Invoker.class), eq(database), eq("obj"))).thenReturn("result");
		when(factory.get(database)).thenReturn("obj");

		InvokeOnOneInvocationStrategy strategy = new InvokeOnOneInvocationStrategy(new PrimaryDatabaseSelector());

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		});

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(database)).isEqualTo("result");
	}

	@Test
	public void invokeWithNextSelector() throws Exception {
		MockDatabase database = new MockDatabase("1");
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		when(cluster.getBalancer()).thenReturn(balancer);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);

		when(balancer.next()).thenReturn(database);
		when(balancer.contains(database)).thenReturn(true);
		when(balancer.invoke(any(Invoker.class), eq(database), eq("obj"))).thenReturn("result");
		when(factory.get(database)).thenReturn("obj");

		InvokeOnOneInvocationStrategy strategy = new InvokeOnOneInvocationStrategy(new NextDatabaseSelector());

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		});

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(database)).isEqualTo("result");
	}

	@Test
	public void invokeDeactivatesFailedDatabase() throws Exception {
		MockDatabase database1 = new MockDatabase("1");
		MockDatabase database2 = new MockDatabase("2");
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		RuntimeException failureException = new RuntimeException("Database failure");

		when(cluster.getBalancer()).thenReturn(balancer);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(cluster.deactivate(eq(database1), eq(stateManager))).thenReturn(true);
		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);

		when(exceptionFactory.createException(any(Exception.class))).thenReturn(failureException);
		when(exceptionFactory.indicatesFailure(eq(failureException), eq(dialect))).thenReturn(true);

		// First call returns database1 which fails, second call returns database2 which succeeds
		when(balancer.next()).thenReturn(database1, database2);
		when(balancer.contains(database1)).thenReturn(true);
		when(balancer.contains(database2)).thenReturn(true);
		when(balancer.invoke(any(Invoker.class), eq(database1), eq("obj1")))
			.thenThrow(new RuntimeException("Database failure"));
		when(balancer.invoke(any(Invoker.class), eq(database2), eq("obj2"))).thenReturn("result");
		when(factory.get(database1)).thenReturn("obj1");
		when(factory.get(database2)).thenReturn("obj2");

		InvokeOnOneInvocationStrategy strategy = new InvokeOnOneInvocationStrategy(new NextDatabaseSelector());

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				if (database.getId().equals("1")) {
					throw new RuntimeException("Database failure");
				}
				return "result";
			}
		});

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(database2)).isEqualTo("result");
		verify(cluster).deactivate(eq(database1), eq(stateManager));
	}

	@Test
	public void invokeThrowsWhenNoActiveDatabases() throws Exception {
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);

		RuntimeException noDatabasesException = new RuntimeException("No active databases");

		when(cluster.getBalancer()).thenReturn(balancer);
		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(balancer.next()).thenReturn(null);
		when(exceptionFactory.createException(anyString())).thenReturn(noDatabasesException);

		InvokeOnOneInvocationStrategy strategy = new InvokeOnOneInvocationStrategy(new NextDatabaseSelector());

		assertThatThrownBy(() -> strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		}))
			.isInstanceOf(RuntimeException.class);
	}

	@Test
	public void invokeSkipsConcurrentlyDeactivatedDatabase() throws Exception {
		MockDatabase database1 = new MockDatabase("1");
		MockDatabase database2 = new MockDatabase("2");
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		when(cluster.getBalancer()).thenReturn(balancer);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);

		// database1 fails and is concurrently deactivated (not in balancer anymore)
		when(balancer.next()).thenReturn(database1, database2);
		when(balancer.contains(database1)).thenReturn(false); // concurrently deactivated
		when(balancer.contains(database2)).thenReturn(true);
		when(balancer.invoke(any(Invoker.class), eq(database1), eq("obj1")))
			.thenThrow(new RuntimeException("Database failure"));
		when(balancer.invoke(any(Invoker.class), eq(database2), eq("obj2"))).thenReturn("result");
		when(factory.get(database1)).thenReturn("obj1");
		when(factory.get(database2)).thenReturn("obj2");

		InvokeOnOneInvocationStrategy strategy = new InvokeOnOneInvocationStrategy(new NextDatabaseSelector());

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				if (database.getId().equals("1")) {
					throw new RuntimeException("Database failure");
				}
				return "result";
			}
		});

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(database2)).isEqualTo("result");
	}
}
