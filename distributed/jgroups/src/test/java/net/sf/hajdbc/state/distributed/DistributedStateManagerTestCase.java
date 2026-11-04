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
package net.sf.hajdbc.state.distributed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.ExceptionType;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.distributed.jgroups.JGroupsCommandDispatcherFactory;
import net.sf.hajdbc.durability.Durability;
import net.sf.hajdbc.durability.Durability.Phase;
import net.sf.hajdbc.durability.InvocationEvent;
import net.sf.hajdbc.durability.InvocationEventImpl;
import net.sf.hajdbc.durability.InvokerEvent;
import net.sf.hajdbc.durability.InvokerEventImpl;
import net.sf.hajdbc.durability.InvokerResult;
import net.sf.hajdbc.durability.InvokerResultImpl;
import net.sf.hajdbc.state.DatabaseEvent;
import net.sf.hajdbc.state.StateManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for {@link DistributedStateManager}.
 * @author Paul Ferraro
 */
public class DistributedStateManagerTestCase
{
	private DatabaseCluster<Void, Database<Void>> cluster1 = mock(DatabaseCluster.class);
	private DatabaseCluster<Void, Database<Void>> cluster2 = mock(DatabaseCluster.class);
	private StateManager localManager1 = mock(StateManager.class);
	private StateManager localManager2 = mock(StateManager.class);
	private Durability<Void, Database<Void>> durability1 = mock(Durability.class);
	private Durability<Void, Database<Void>> durability2 = mock(Durability.class);
	private DistributedStateManager<Void, Database<Void>> manager1;
	private DistributedStateManager<Void, Database<Void>> manager2;

	@Before
	public void init() throws Exception
	{
		String id = "cluster";

		when(this.cluster1.getId()).thenReturn(id);
		when(this.cluster2.getId()).thenReturn(id);
		when(this.cluster1.getStateManager()).thenReturn(this.localManager1);
		when(this.cluster2.getStateManager()).thenReturn(this.localManager2);
		when(this.cluster1.getDurability()).thenReturn(this.durability1);
		when(this.cluster2.getDurability()).thenReturn(this.durability2);
		
		CommandDispatcherFactory dispatcherFactory1 = createCommandDispatcherFactory("node1");
		CommandDispatcherFactory dispatcherFactory2 = createCommandDispatcherFactory("node2");

		this.manager1 = new DistributedStateManager<>(this.cluster1, dispatcherFactory1);
		this.manager1.start();
		
		verify(this.localManager1).start();
		
		this.manager2 = new DistributedStateManager<>(this.cluster2, dispatcherFactory2);
		this.manager2.start();
		
		verify(this.localManager2).start();
	}

	static CommandDispatcherFactory createCommandDispatcherFactory(String name)
	{
		JGroupsCommandDispatcherFactory factory = new JGroupsCommandDispatcherFactory();
		factory.setName(name);
		factory.setStack("fast.xml");
		return factory;
	}

	@After
	public void destroy()
	{
		this.manager1.stop();
		
		verify(this.localManager1).stop();
		
		this.manager1 = null;
		
		this.manager2.stop();
		
		verify(this.localManager2).stop();
		
		this.manager2 = null;
	}

	@Test
	@Ignore("JGroups分散テストはJava 25で不安定")
	public void activate()
	{
		String databaseId = "test";
		Database<Void> database = mock(Database.class);

		when(database.getId()).thenReturn(databaseId);
		
		DatabaseEvent event = new DatabaseEvent(database);
		
		when(this.cluster2.getDatabase(databaseId)).thenReturn(database);
		
		this.manager1.activated(event);
		
		verify(this.localManager1).activated(event);
		verify(this.cluster2).activate(database, this.localManager2);
		
		when(this.cluster1.getDatabase(databaseId)).thenReturn(database);
		
		this.manager2.activated(event);
		
		verify(this.localManager2).activated(event);
		verify(this.cluster1).activate(database, this.localManager1);
	}

	@Test
	@Ignore("JGroups分散テストはJava 25で不安定")
	public void deactivate()
	{
		String databaseId = "test";
		Database<Void> database = mock(Database.class);

		when(database.getId()).thenReturn(databaseId);
		
		DatabaseEvent event = new DatabaseEvent(database);
		
		when(this.cluster2.getDatabase(databaseId)).thenReturn(database);
		
		this.manager1.deactivated(event);
		
		verify(this.localManager1).deactivated(event);
		verify(this.cluster2).deactivate(database, this.localManager2);
		
		when(this.cluster1.getDatabase(databaseId)).thenReturn(database);
		
		this.manager2.deactivated(event);
		
		verify(this.localManager2).deactivated(event);
		verify(this.cluster1).deactivate(database, this.localManager1);
	}

	@Test
	@Ignore("JGroups分散テストはJava 25で不安定")
	public void recover()
	{
		Map<InvocationEvent, Map<String, InvokerEvent>> invocations = mock(Map.class);
		
		when(this.localManager1.recover()).thenReturn(invocations);
		
		Map<InvocationEvent, Map<String, InvokerEvent>> result = this.manager1.recover();
		
		assertThat(result).isSameAs(invocations);
		
		when(this.localManager2.recover()).thenReturn(invocations);
		
		result = this.manager2.recover();
		
		assertThat(result).isSameAs(invocations);
	}

	@Test
	@Ignore("JGroups分散テストはJava 25で不安定")
	public void durability()
	{
		Object tx = UUID.randomUUID();
		Phase phase = Phase.COMMIT;
		ExceptionType exceptionType = ExceptionType.SQL;
		InvocationEvent invocationEvent = new InvocationEventImpl(tx, phase, exceptionType);
		
		this.manager1.beforeInvocation(invocationEvent);
		
		verify(this.localManager1).beforeInvocation(invocationEvent);
		
		Map<InvocationEvent, Map<String, InvokerEvent>> invocations = this.manager2.getRemoteInvokers(this.manager1);
		
		assertThat(invocations.size()).isEqualTo(1);
		Map<String, InvokerEvent> invokers = invocations.get(invocationEvent);
		assertThat(invokers).isNotNull();
		assertThat(invokers.size()).isEqualTo(0);
		
		String databaseId = "test";
		InvokerEvent invokerEvent = new InvokerEventImpl(tx, phase, databaseId);
		
		this.manager1.beforeInvoker(invokerEvent);
		
		verify(this.localManager1).beforeInvoker(invokerEvent);
		
		invocations = this.manager2.getRemoteInvokers(this.manager1);
		
		assertThat(invocations.size()).isEqualTo(1);
		invokers = invocations.get(invocationEvent);
		assertThat(invokers).isNotNull();
		assertThat(invokers.size()).isEqualTo(1);
		assertThat(invokers.get(databaseId)).isEqualTo(invokerEvent);
		
		InvokerResult result = new InvokerResultImpl("value");
		invokerEvent.setResult(result);
		
		this.manager1.afterInvoker(invokerEvent);
		
		verify(this.localManager1).afterInvoker(invokerEvent);
		
		invocations = this.manager2.getRemoteInvokers(this.manager1);
		
		assertThat(invocations.size()).isEqualTo(1);
		invokers = invocations.get(invocationEvent);
		assertThat(invokers).isNotNull();
		assertThat(invokers.size()).isEqualTo(1);
		InvokerEvent resultEvent = invokers.get(databaseId);
		assertThat(resultEvent).isEqualTo(invokerEvent);
		assertThat(resultEvent.getResult().getValue()).isEqualTo(result.getValue());
		
		this.manager1.afterInvocation(invocationEvent);
		
		verify(this.localManager1).afterInvocation(invocationEvent);
		
		invocations = this.manager2.getRemoteInvokers(this.manager1);
		
		assertThat(invocations.size()).isEqualTo(0);
	}
}
