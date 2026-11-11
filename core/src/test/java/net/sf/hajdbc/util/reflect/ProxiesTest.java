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
package net.sf.hajdbc.util.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProxiesTest
{
	interface TestInterface
	{
		String hello();
		int add(int a, int b);
	}

	@Test
	public void createProxy()
	{
		InvocationHandler handler = (proxy, method, args) -> {
			if ("hello".equals(method.getName())) {
				return "Hello, World!";
			}
			if ("add".equals(method.getName())) {
				return (int) args[0] + (int) args[1];
			}
			return null;
		};

		TestInterface proxy = Proxies.createProxy(TestInterface.class, handler);
		Assertions.assertNotNull(proxy);
		Assertions.assertTrue(proxy instanceof Proxy);
		Assertions.assertTrue(proxy instanceof TestInterface);
	}

	@Test
	public void proxyInvocation()
	{
		InvocationHandler handler = (proxy, method, args) -> {
			if ("hello".equals(method.getName())) {
				return "Hello, World!";
			}
			if ("add".equals(method.getName())) {
				return (int) args[0] + (int) args[1];
			}
			return null;
		};

		TestInterface proxy = Proxies.createProxy(TestInterface.class, handler);
		Assertions.assertEquals("Hello, World!", proxy.hello());
		Assertions.assertEquals(5, proxy.add(2, 3));
	}

	@Test
	public void proxyToString()
	{
		InvocationHandler handler = (proxy, method, args) -> {
			if ("toString".equals(method.getName())) {
				return "CustomProxy";
			}
			return null;
		};

		TestInterface proxy = Proxies.createProxy(TestInterface.class, handler);
		Assertions.assertEquals("CustomProxy", proxy.toString());
	}
}
