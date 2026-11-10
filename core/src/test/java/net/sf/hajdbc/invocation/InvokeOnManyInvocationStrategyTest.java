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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
 * Tests for InvokeOnManyInvocationStrategy
 */
public class InvokeOnManyInvocationStrategyTest {

	@Test
	public void invokeWithAllResultsSucceed() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		SortedMap<MockDatabase, String> resultMap = new TreeMap<>();
		resultMap.put(db1, "result1");
		resultMap.put(db2, "result2");

		InvokeOnManyInvocationStrategy.ResultsCollector collector = mock(InvokeOnManyInvocationStrategy.ResultsCollector.class);
		SortedMap<MockDatabase, Exception> exceptionMap = new TreeMap<>();

		when(collector.collectResults(any(ProxyFactory.class), any(Invoker.class)))
			.thenReturn(new AbstractMap.SimpleImmutableEntry<>(resultMap, exceptionMap));

		InvokeOnManyInvocationStrategy strategy = new InvokeOnManyInvocationStrategy(collector);

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		SortedMap<MockDatabase, String> result = strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		});

		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(db1)).isEqualTo("result1");
		assertThat(result.get(db2)).isEqualTo("result2");
	}

	@Test
	public void invokeDeactivatesFailedDatabases() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		SortedMap<MockDatabase, String> resultMap = new TreeMap<>();
		resultMap.put(db1, "result1");

		SortedMap<MockDatabase, Exception> exceptionMap = new TreeMap<>();
		RuntimeException failureException = new RuntimeException("Database failure");
		exceptionMap.put(db2, failureException);

		InvokeOnManyInvocationStrategy.ResultsCollector collector = mock(InvokeOnManyInvocationStrategy.ResultsCollector.class);
		when(collector.collectResults(any(ProxyFactory.class), any(Invoker.class)))
			.thenReturn(new AbstractMap.SimpleImmutableEntry<>(resultMap, exceptionMap));

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(exceptionFactory.indicatesFailure(eq(failureException), eq(dialect))).thenReturn(true);
		when(cluster.deactivate(eq(db2), eq(stateManager))).thenReturn(true);

		InvokeOnManyInvocationStrategy strategy = new InvokeOnManyInvocationStrategy(collector);

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		});

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(db1)).isEqualTo("result1");
		verify(cluster).deactivate(eq(db2), eq(stateManager));
	}

	@Test
	public void invokeThrowsPrimaryExceptionWhenAllFail() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		SortedMap<MockDatabase, String> resultMap = new TreeMap<>();

		SortedMap<MockDatabase, Exception> exceptionMap = new TreeMap<>();
		RuntimeException primaryException = new RuntimeException("Primary failure");
		RuntimeException secondaryException = new RuntimeException("Secondary failure");
		exceptionMap.put(db1, primaryException);
		exceptionMap.put(db2, secondaryException);

		InvokeOnManyInvocationStrategy.ResultsCollector collector = mock(InvokeOnManyInvocationStrategy.ResultsCollector.class);
		when(collector.collectResults(any(ProxyFactory.class), any(Invoker.class)))
			.thenReturn(new AbstractMap.SimpleImmutableEntry<>(resultMap, exceptionMap));

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(exceptionFactory.indicatesFailure(eq(primaryException), eq(dialect))).thenReturn(false);
		when(exceptionFactory.indicatesFailure(eq(secondaryException), eq(dialect))).thenReturn(false);
		when(exceptionFactory.equals(eq(primaryException), eq(secondaryException))).thenReturn(false);
		when(cluster.deactivate(eq(db2), eq(stateManager))).thenReturn(true);

		InvokeOnManyInvocationStrategy strategy = new InvokeOnManyInvocationStrategy(collector);

		assertThatThrownBy(() -> strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		}))
			.isInstanceOf(RuntimeException.class);
	}

	@Test
	public void invokeWithPrimarySuccessAndSecondaryFailure() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		SortedMap<MockDatabase, String> resultMap = new TreeMap<>();
		resultMap.put(db1, "result1");

		SortedMap<MockDatabase, Exception> exceptionMap = new TreeMap<>();
		RuntimeException failureException = new RuntimeException("Database failure");
		exceptionMap.put(db2, failureException);

		InvokeOnManyInvocationStrategy.ResultsCollector collector = mock(InvokeOnManyInvocationStrategy.ResultsCollector.class);
		when(collector.collectResults(any(ProxyFactory.class), any(Invoker.class)))
			.thenReturn(new AbstractMap.SimpleImmutableEntry<>(resultMap, exceptionMap));

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);
		Dialect dialect = mock(Dialect.class);
		StateManager stateManager = mock(StateManager.class);

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getDialect()).thenReturn(dialect);
		when(cluster.getStateManager()).thenReturn(stateManager);
		when(exceptionFactory.indicatesFailure(eq(failureException), eq(dialect))).thenReturn(true);
		when(cluster.deactivate(eq(db2), eq(stateManager))).thenReturn(true);

		InvokeOnManyInvocationStrategy strategy = new InvokeOnManyInvocationStrategy(collector);

		SortedMap<MockDatabase, String> result = strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		});

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(db1)).isEqualTo("result1");
		verify(cluster).deactivate(eq(db2), eq(stateManager));
	}

	@Test
	public void invokeReturnsEmptyMapWhenNoExceptions() throws Exception {
		InvokeOnManyInvocationStrategy.ResultsCollector collector = mock(InvokeOnManyInvocationStrategy.ResultsCollector.class);
		SortedMap<MockDatabase, String> resultMap = new TreeMap<>();
		SortedMap<MockDatabase, Exception> exceptionMap = new TreeMap<>();

		when(collector.collectResults(any(ProxyFactory.class), any(Invoker.class)))
			.thenReturn(new AbstractMap.SimpleImmutableEntry<>(resultMap, exceptionMap));

		InvokeOnManyInvocationStrategy strategy = new InvokeOnManyInvocationStrategy(collector);

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		SortedMap<MockDatabase, String> result = strategy.invoke(factory, new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		});

		assertThat(result).isEmpty();
	}
}
