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

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleInvocationHandlerTest
{
	interface TestInterface
	{
		String hello();
		int add(int a, int b);
		void throwException() throws RuntimeException;
	}

	static class TestImpl implements TestInterface
	{
		@Override
		public String hello()
		{
			return "Hello from impl";
		}

		@Override
		public int add(int a, int b)
		{
			return a + b;
		}

		@Override
		public void throwException()
		{
			throw new RuntimeException("Test exception");
		}
	}

	@Test
	public void invokeMethod()
	{
		TestImpl target = new TestImpl();
		SimpleInvocationHandler handler = new SimpleInvocationHandler(target);
		TestInterface proxy = Proxies.createProxy(TestInterface.class, handler);

		Assertions.assertEquals("Hello from impl", proxy.hello());
	}

	@Test
	public void invokeMethodWithParameters()
	{
		TestImpl target = new TestImpl();
		SimpleInvocationHandler handler = new SimpleInvocationHandler(target);
		TestInterface proxy = Proxies.createProxy(TestInterface.class, handler);

		Assertions.assertEquals(5, proxy.add(2, 3));
	}

	@Test
	public void invokeMethodThrowingException()
	{
		TestImpl target = new TestImpl();
		SimpleInvocationHandler handler = new SimpleInvocationHandler(target);
		TestInterface proxy = Proxies.createProxy(TestInterface.class, handler);

		Assertions.assertThrows(RuntimeException.class, () -> {
			proxy.throwException();
		});
	}

	@Test
	public void proxyIsInstance()
	{
		TestImpl target = new TestImpl();
		SimpleInvocationHandler handler = new SimpleInvocationHandler(target);
		TestInterface proxy = Proxies.createProxy(TestInterface.class, handler);

		Assertions.assertTrue(proxy instanceof Proxy);
		Assertions.assertTrue(proxy instanceof TestInterface);
	}
}
