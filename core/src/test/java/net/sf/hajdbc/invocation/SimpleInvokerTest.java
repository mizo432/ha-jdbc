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

import java.lang.reflect.Method;

import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.ExceptionType;
import net.sf.hajdbc.MockDatabase;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.durability.Durability;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for SimpleInvoker
 */
public class SimpleInvokerTest {

	private static ExceptionFactory<Exception> createExceptionFactory() {
		return new ExceptionFactory<Exception>() {
			@Override
			public Class<Exception> getTargetClass() { return Exception.class; }
			@Override
			public Exception createException(Throwable e) { return new Exception(e); }
			@Override
			public Exception createException(String message) { return new Exception(message); }
			@Override
			public boolean indicatesFailure(Exception e, Dialect dialect) { return false; }
			@Override
			public boolean equals(Exception e1, Exception e2) { return e1.equals(e2); }
			@Override
			public ExceptionType getType() { return ExceptionType.SQL; }
			@Override
			public boolean correctHeuristic(Exception e, Durability.Phase phase) { return false; }
		};
	}

	@Test
	public void getMethod() throws Exception {
		Method method = String.class.getMethod("toString");
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker =
			new SimpleInvoker<>(method, new Object[0], createExceptionFactory());

		Assertions.assertSame(method, invoker.getMethod());
	}

	@Test
	public void getParameters() throws Exception {
		Method method = String.class.getMethod("toString");
		Object[] params = new Object[] { "arg1", "arg2" };
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker =
			new SimpleInvoker<>(method, params, createExceptionFactory());

		Assertions.assertSame(params, invoker.getParameters());
	}

	@Test
	public void invoke() throws Exception {
		Method method = String.class.getMethod("toString");
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker =
			new SimpleInvoker<>(method, new Object[0], createExceptionFactory());

		String result = invoker.invoke(new MockDatabase("1"), "test");
		Assertions.assertEquals("test", result);
	}

	@Test
	public void equalsSameMethod() throws Exception {
		Method method = String.class.getMethod("toString");
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker1 =
			new SimpleInvoker<>(method, new Object[0], createExceptionFactory());
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker2 =
			new SimpleInvoker<>(method, new Object[0], createExceptionFactory());

		Assertions.assertEquals(invoker1, invoker2);
	}

	@Test
	public void equalsDifferentMethod() throws Exception {
		Method method1 = String.class.getMethod("toString");
		Method method2 = String.class.getMethod("length");

		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker1 =
			new SimpleInvoker<>(method1, new Object[0], createExceptionFactory());
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker2 =
			new SimpleInvoker<>(method2, new Object[0], createExceptionFactory());

		Assertions.assertNotEquals(invoker1, invoker2);
	}

	@Test
	public void equalsNull() throws Exception {
		Method method = String.class.getMethod("toString");
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker =
			new SimpleInvoker<>(method, new Object[0], createExceptionFactory());

		Assertions.assertNotEquals(invoker, null);
	}

	@Test
	public void equalsDifferentType() throws Exception {
		Method method = String.class.getMethod("toString");
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker =
			new SimpleInvoker<>(method, new Object[0], createExceptionFactory());

		Assertions.assertNotEquals(invoker, "string");
	}

	@Test
	public void hashCodeConsistent() throws Exception {
		Method method = String.class.getMethod("toString");
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker =
			new SimpleInvoker<>(method, new Object[0], createExceptionFactory());

		Assertions.assertEquals(method.hashCode(), invoker.hashCode());
	}

	@Test
	public void toStringReturnsMethodString() throws Exception {
		Method method = String.class.getMethod("toString");
		SimpleInvoker<Void, MockDatabase, String, String, Exception> invoker =
			new SimpleInvoker<>(method, new Object[0], createExceptionFactory());

		Assertions.assertEquals(method.toString(), invoker.toString());
	}
}
