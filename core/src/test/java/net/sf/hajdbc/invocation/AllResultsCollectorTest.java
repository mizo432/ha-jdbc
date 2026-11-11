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

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.MockDatabase;
import net.sf.hajdbc.balancer.Balancer;
import net.sf.hajdbc.sql.ProxyFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link AllResultsCollector}.
 */
public class AllResultsCollectorTest
{
	@Test
	public void collectResultsAllSucceed() throws Exception
	{
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);

		Set<MockDatabase> activeDatabases = new TreeSet<>(java.util.Arrays.asList(db1, db2));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(balancer.getActiveDatabases()).thenReturn(activeDatabases);
		when(factory.get(db1)).thenReturn("obj1");
		when(factory.get(db2)).thenReturn("obj2");

		AllResultsCollector.ExecutorProvider provider = new AllResultsCollector.ExecutorProvider()
		{
			@Override
			public <Z, D extends Database<Z>> ExecutorService getExecutor(DatabaseCluster<Z, D> cluster)
			{
				return Executors.newFixedThreadPool(2);
			}
		};
		AllResultsCollector collector = new AllResultsCollector(provider);

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>()
		{
			@Override
			public String invoke(MockDatabase database, String object)
			{
				return "result-" + database.getId();
			}
		};

		Map.Entry<SortedMap<MockDatabase, String>, SortedMap<MockDatabase, Exception>> result = collector.collectResults(factory, invoker);

		Assertions.assertEquals(2, result.getKey().size());
		Assertions.assertEquals("result-1", result.getKey().get(db1));
		Assertions.assertEquals("result-2", result.getKey().get(db2));
		Assertions.assertTrue(result.getValue().isEmpty());
	}

	@Test
	public void collectResultsWithSomeFailures() throws Exception
	{
		MockDatabase db1 = new MockDatabase("1");
		MockDatabase db2 = new MockDatabase("2");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);

		RuntimeException failureException = new RuntimeException("Database failure");

		Set<MockDatabase> activeDatabases = new TreeSet<>(java.util.Arrays.asList(db1, db2));

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(balancer.getActiveDatabases()).thenReturn(activeDatabases);
		when(factory.get(db1)).thenReturn("obj1");
		when(factory.get(db2)).thenReturn("obj2");
		when(exceptionFactory.createException(any(Exception.class))).thenReturn(failureException);

		AllResultsCollector.ExecutorProvider provider = new AllResultsCollector.ExecutorProvider()
		{
			@Override
			public <Z, D extends Database<Z>> ExecutorService getExecutor(DatabaseCluster<Z, D> cluster)
			{
				return Executors.newFixedThreadPool(2);
			}
		};
		AllResultsCollector collector = new AllResultsCollector(provider);

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>()
		{
			@Override
			public String invoke(MockDatabase database, String object)
			{
				if (database.getId().equals("2"))
				{
					throw new RuntimeException("Database failure");
				}
				return "result-" + database.getId();
			}
		};

		Map.Entry<SortedMap<MockDatabase, String>, SortedMap<MockDatabase, Exception>> result = collector.collectResults(factory, invoker);

		Assertions.assertEquals(1, result.getKey().size());
		Assertions.assertEquals("result-1", result.getKey().get(db1));
		Assertions.assertEquals(1, result.getValue().size());
		Assertions.assertTrue(result.getValue().containsKey(db2));
		Assertions.assertSame(failureException, result.getValue().get(db2));
	}

	@Test
	public void collectResultsSingleDatabase() throws Exception
	{
		MockDatabase db1 = new MockDatabase("1");

		ProxyFactory<Void, MockDatabase, String, Exception> factory = mock(ProxyFactory.class);
		DatabaseCluster<Void, MockDatabase> cluster = mock(DatabaseCluster.class);
		Balancer<Void, MockDatabase> balancer = mock(Balancer.class);
		ExceptionFactory<Exception> exceptionFactory = mock(ExceptionFactory.class);

		Set<MockDatabase> activeDatabases = java.util.Collections.singleton(db1);

		when(factory.getDatabaseCluster()).thenReturn(cluster);
		when(factory.getExceptionFactory()).thenReturn(exceptionFactory);
		when(cluster.getBalancer()).thenReturn(balancer);
		when(balancer.getActiveDatabases()).thenReturn(activeDatabases);
		when(factory.get(db1)).thenReturn("obj1");

		AllResultsCollector.ExecutorProvider provider = new AllResultsCollector.ExecutorProvider()
		{
			@Override
			public <Z, D extends Database<Z>> ExecutorService getExecutor(DatabaseCluster<Z, D> cluster)
			{
				return Executors.newFixedThreadPool(1);
			}
		};
		AllResultsCollector collector = new AllResultsCollector(provider);

		Invoker<Void, MockDatabase, String, String, Exception> invoker = new Invoker<Void, MockDatabase, String, String, Exception>()
		{
			@Override
			public String invoke(MockDatabase database, String object)
			{
				return "result-" + database.getId();
			}
		};

		Map.Entry<SortedMap<MockDatabase, String>, SortedMap<MockDatabase, Exception>> result = collector.collectResults(factory, invoker);

		Assertions.assertEquals(1, result.getKey().size());
		Assertions.assertEquals("result-1", result.getKey().get(db1));
		Assertions.assertTrue(result.getValue().isEmpty());
	}
}
