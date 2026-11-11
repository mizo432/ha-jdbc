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
package net.sf.hajdbc.state;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import java.util.Set;

import net.sf.hajdbc.durability.Durability;
import net.sf.hajdbc.durability.InvocationEvent;
import net.sf.hajdbc.durability.InvocationEventImpl;
import net.sf.hajdbc.state.simple.SimpleStateManager;

import org.junit.jupiter.api.Test;

public class SimpleStateManagerTest
{
	@Test
	public void getActiveDatabasesInitiallyEmpty()
	{
		SimpleStateManager manager = new SimpleStateManager();
		assertThat(manager.getActiveDatabases()).isEmpty();
	}
	
	@Test
	public void activatedAddsDatabase()
	{
		SimpleStateManager manager = new SimpleStateManager();
		manager.activated(new DatabaseEvent(new TestDatabase("db1")));
		
		assertThat(manager.getActiveDatabases()).containsExactly("db1");
	}
	
	@Test
	public void deactivatedRemovesDatabase()
	{
		SimpleStateManager manager = new SimpleStateManager();
		manager.activated(new DatabaseEvent(new TestDatabase("db1")));
		manager.deactivated(new DatabaseEvent(new TestDatabase("db1")));
		
		assertThat(manager.getActiveDatabases()).isEmpty();
	}
	
	@Test
	public void setActiveDatabases()
	{
		SimpleStateManager manager = new SimpleStateManager();
		manager.setActiveDatabases(Set.of("db1", "db2"));
		
		assertThat(manager.getActiveDatabases()).containsExactlyInAnyOrder("db1", "db2");
	}
	
	@Test
	public void setActiveDatabasesReplacesExisting()
	{
		SimpleStateManager manager = new SimpleStateManager();
		manager.activated(new DatabaseEvent(new TestDatabase("db1")));
		manager.setActiveDatabases(Set.of("db2"));
		
		assertThat(manager.getActiveDatabases()).containsExactly("db2");
	}
	
	@Test
	public void isEnabledAlwaysTrue()
	{
		SimpleStateManager manager = new SimpleStateManager();
		assertThat(manager.isEnabled()).isTrue();
	}
	
	@Test
	public void recoverReturnsEmptyMapInitially()
	{
		SimpleStateManager manager = new SimpleStateManager();
		assertThat(manager.recover()).isEmpty();
	}
	
	@Test
	public void beforeInvocationAddsToRecovery()
	{
		SimpleStateManager manager = new SimpleStateManager();
		InvocationEvent event = new InvocationEventImpl(1, Durability.Phase.COMMIT, net.sf.hajdbc.ExceptionType.SQL);
		manager.beforeInvocation(event);
		
		assertThat(manager.recover()).hasSize(1);
		assertThat(manager.recover()).containsKey(event);
	}
	
	@Test
	public void afterInvocationRemovesFromRecovery()
	{
		SimpleStateManager manager = new SimpleStateManager();
		InvocationEvent event = new InvocationEventImpl(1, Durability.Phase.COMMIT, net.sf.hajdbc.ExceptionType.SQL);
		manager.beforeInvocation(event);
		manager.afterInvocation(event);
		
		assertThat(manager.recover()).isEmpty();
	}
	
	
	@Test
	public void startAndStop()
	{
		SimpleStateManager manager = new SimpleStateManager();
		// Should not throw
		manager.start();
		manager.stop();
	}
	
	// Test helper
	
	private static class TestDatabase implements net.sf.hajdbc.Database<Object> {
		private final String id;
		
		TestDatabase(String id) { this.id = id; }
		
		@Override
		public String getId() { return id; }
		@Override
		public Object getConnectionSource() { return null; }
		@Override
		public net.sf.hajdbc.Credentials getCredentials() { return null; }
		@Override
		public int getWeight() { return 1; }
		@Override
		public void setWeight(int weight) {}
		@Override
		public net.sf.hajdbc.Locality getLocality() { return net.sf.hajdbc.Locality.LOCAL; }
		@Override
		public java.sql.Connection connect(net.sf.hajdbc.codec.Decoder decoder) { return null; }
		@Override
		public String getLocation() { return ""; }
		@Override
		public Properties getProperties() { return new Properties(); }
		@Override
		public int compareTo(net.sf.hajdbc.Database<Object> o) { return 0; }
	}
}
