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
import java.util.Set;
import java.util.SortedMap;

import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.MockDatabase;
import net.sf.hajdbc.balancer.Balancer;
import net.sf.hajdbc.sql.ProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ExistingResultsCollector
 */
public class ExistingResultsCollectorTest {

	@Test
	public void collectResultsAllSucceed() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);

		Set<Map.Entry<MockDatabase, String>> entries = new HashSet<>(Arrays.asList(
			new AbstractMap.SimpleImmutableEntry<>(db1, "obj1"),
			new AbstractMap.SimpleImmutableEntry<>(db2, "obj2")
		));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(balancer.contains(db1)).thenReturn(true);
		when(balancer.contains(db2)).thenReturn(true);
		when(factory.entries()).thenReturn(entries);

		ExistingResultsCollector collector = new ExistingResultsCollector();

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result-" + database.getId();
			}
		};

		Map.Entry<SortedMap<MockDatabase, String>, SortedMap<MockDatabase, Exception>> result = collector.collectResults(factory, invoker);

		assertThat(result.getKey().size()).isEqualTo(2);
		assertThat(result.getKey().get(db1)).isEqualTo("result-1");
		assertThat(result.getKey().get(db2)).isEqualTo("result-2");
		assertThat(result.getValue()).isEmpty();
	}

	@Test
	public void collectResultsWithSomeFailures() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);

		RuntimeException failureException = new RuntimeException("Database failure");

		Set<Map.Entry<MockDatabase, String>> entries = new HashSet<>(Arrays.asList(
			new AbstractMap.SimpleImmutableEntry<>(db1, "obj1"),
			new AbstractMap.SimpleImmutableEntry<>(db2, "obj2")
		));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(balancer.contains(db1)).thenReturn(true);
		when(balancer.contains(db2)).thenReturn(true);
		when(exceptionFactory.createException(any(Exception.class))).thenReturn(failureException);
		when(factory.entries()).thenReturn(entries);

		ExistingResultsCollector collector = new ExistingResultsCollector();

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				if (database.getId().equals("1")) {
					return "result-1";
				}
				throw new RuntimeException("Database failure");
			}
		};

		Map.Entry<SortedMap<MockDatabase, String>, SortedMap<MockDatabase, Exception>> result = collector.collectResults(factory, invoker);

		assertThat(result.getKey().size()).isEqualTo(1);
		assertThat(result.getKey().get(db1)).isEqualTo("result-1");
		assertThat(result.getValue().size()).isEqualTo(1);
		assertThat(result.getValue().get(db2)).isSameAs(failureException);
	}

	@Test
	public void collectResultsSkipsConcurrentlyDeactivatedDatabase() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);

		Set<Map.Entry<MockDatabase, String>> entries = new HashSet<>(Arrays.asList(
			new AbstractMap.SimpleImmutableEntry<>(db1, "obj1"),
			new AbstractMap.SimpleImmutableEntry<>(db2, "obj2")
		));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(balancer.contains(db1)).thenReturn(true);
		when(balancer.contains(db2)).thenReturn(false); // db2 was concurrently deactivated
		when(factory.entries()).thenReturn(entries);

		ExistingResultsCollector collector = new ExistingResultsCollector();

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				if (database.getId().equals("1")) {
					return "result-1";
				}
				throw new RuntimeException("Database failure");
			}
		};

		Map.Entry<SortedMap<MockDatabase, String>, SortedMap<MockDatabase, Exception>> result = collector.collectResults(factory, invoker);

		assertThat(result.getKey().size()).isEqualTo(1);
		assertThat(result.getKey().get(db1)).isEqualTo("result-1");
		// db2's exception should be ignored since it was concurrently deactivated
		assertThat(result.getValue()).isEmpty();
	}

	@Test
	public void collectResultsEmptyEntries() throws Exception {
		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		when(factory.entries()).thenReturn(new HashSet<>());

		ExistingResultsCollector collector = new ExistingResultsCollector();

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				return "result";
			}
		};

		Map.Entry<SortedMap<MockDatabase, String>, SortedMap<MockDatabase, Exception>> result = collector.collectResults(factory, invoker);

		assertThat(result.getKey()).isEmpty();
		assertThat(result.getValue()).isEmpty();
	}

	@Test
	public void collectResultsAllFail() throws Exception {
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);

		RuntimeException failureException1 = new RuntimeException("Failure 1");
		RuntimeException failureException2 = new RuntimeException("Failure 2");

		Set<Map.Entry<MockDatabase, String>> entries = new HashSet<>(Arrays.asList(
			new AbstractMap.SimpleImmutableEntry<>(db1, "obj1"),
			new AbstractMap.SimpleImmutableEntry<>(db2, "obj2")
		));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(balancer.contains(db1)).thenReturn(true);
		when(balancer.contains(db2)).thenReturn(true);
		when(exceptionFactory.createException(any(Exception.class)))
			.thenReturn(failureException1)
			.thenReturn(failureException2);
		when(factory.entries()).thenReturn(entries);

		ExistingResultsCollector collector = new ExistingResultsCollector();

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>() {
			@Override
			public String invoke(MockDatabase database, String object) {
				throw new RuntimeException("Failure " + database.getId());
			}
		};

		Map.Entry<SortedMap<MockDatabase, String>, SortedMap<MockDatabase, Exception>> result = collector.collectResults(factory, invoker);

		assertThat(result.getKey()).isEmpty();
		assertThat(result.getValue().size()).isEqualTo(2);
		assertThat(result.getValue().get(db1)).isSameAs(failureException1);
		assertThat(result.getValue().get(db2)).isSameAs(failureException2);
	}
}
