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
package net.sf.hajdbc.sql;

import java.lang.reflect.Proxy;
import java.sql.Connection;

import net.sf.hajdbc.util.reflect.Proxies;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link ConnectionInvocationHandler}.
 */
public class ConnectionInvocationHandlerTest
{

	@Test
	public void getProxyFactoryReturnsCorrectType() throws Exception
	{
		ConnectionProxyFactory<java.sql.Driver, DriverDatabase, java.sql.Driver> proxyFactory = mock(ConnectionProxyFactory.class);
		
		ConnectionInvocationHandler<java.sql.Driver, DriverDatabase, java.sql.Driver> handler = 
			new ConnectionInvocationHandler<>(proxyFactory);
		
		Assertions.assertSame(proxyFactory, handler.getProxyFactory());
	}
	
	@Test
	public void createProxyReturnsConnectionProxy() throws Exception
	{
		ConnectionProxyFactory<java.sql.Driver, DriverDatabase, java.sql.Driver> proxyFactory = mock(ConnectionProxyFactory.class);
		
		ConnectionInvocationHandler<java.sql.Driver, DriverDatabase, java.sql.Driver> handler = 
			new ConnectionInvocationHandler<>(proxyFactory);
		
		Connection connection = Proxies.createProxy(Connection.class, handler);
		
		Assertions.assertNotNull(connection);
		Assertions.assertTrue(Proxy.isProxyClass(connection.getClass()));
	}
	
	@Test
	public void getReturnsUnderlyingConnection() throws Exception
	{
		Connection underlyingConnection = mock(Connection.class);
		ConnectionProxyFactory<java.sql.Driver, DriverDatabase, java.sql.Driver> proxyFactory = mock(ConnectionProxyFactory.class);
		DriverDatabase database = mock(DriverDatabase.class);
		
		when(proxyFactory.get(database)).thenReturn(underlyingConnection);
		
		ConnectionInvocationHandler<java.sql.Driver, DriverDatabase, java.sql.Driver> handler = 
			new ConnectionInvocationHandler<>(proxyFactory);
		
		Assertions.assertSame(underlyingConnection, handler.getProxyFactory().get(database));
	}
}
