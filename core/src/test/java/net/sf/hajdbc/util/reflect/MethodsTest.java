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

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Set;

import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.ExceptionType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MethodsTest
{
	@Test
	public void findMethods()
	{
		Set<Method> methods = Methods.findMethods(String.class, "length");
		Assertions.assertFalse(methods.isEmpty());
		Assertions.assertTrue(methods.stream().anyMatch(m -> "length".equals(m.getName())));
	}

	@Test
	public void findMethodsWithMultiplePatterns()
	{
		Set<Method> methods = Methods.findMethods(String.class, "length", "charAt");
		Assertions.assertFalse(methods.isEmpty());
		Assertions.assertTrue(methods.stream().anyMatch(m -> "length".equals(m.getName())));
		Assertions.assertTrue(methods.stream().anyMatch(m -> "charAt".equals(m.getName())));
	}

	@Test
	public void findMethodsWithNoMatch()
	{
		Set<Method> methods = Methods.findMethods(String.class, "nonexistent");
		Assertions.assertTrue(methods.isEmpty());
	}

	@Test
	public void getMethod()
	{
		Method method = Methods.getMethod(String.class, "length");
		Assertions.assertNotNull(method);
		Assertions.assertEquals("length", method.getName());
	}

	@Test
	public void getMethodWithParameters()
	{
		Method method = Methods.getMethod(String.class, "charAt", int.class);
		Assertions.assertNotNull(method);
		Assertions.assertEquals("charAt", method.getName());
	}

	@Test
	public void getMethodNotFound()
	{
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Methods.getMethod(String.class, "nonexistent");
		});
	}

	@Test
	public void findMethod()
	{
		Method method = Methods.findMethod(String.class, "length");
		Assertions.assertNotNull(method);
		Assertions.assertEquals("length", method.getName());
	}

	@Test
	public void findMethodNotFound()
	{
		Method method = Methods.findMethod(String.class, "nonexistent");
		Assertions.assertNull(method);
	}

	@Test
	public void findMethodByClassName()
	{
		Method method = Methods.findMethod("java.lang.String", "length");
		Assertions.assertNotNull(method);
		Assertions.assertEquals("length", method.getName());
	}

	@Test
	public void findMethodByClassNameNotFound()
	{
		Method method = Methods.findMethod("nonexistent.Class", "length");
		Assertions.assertNull(method);
	}

	@Test
	public void findMethodByClassNameMethodNotFound()
	{
		Method method = Methods.findMethod("java.lang.String", "nonexistent");
		Assertions.assertNull(method);
	}

	@Test
	public void invoke() throws SQLException
	{
		ExceptionFactory<SQLException> factory = new SQLExceptionFactory();
		Method method = Methods.getMethod(String.class, "length");
		Integer result = Methods.invoke(method, factory, "hello");
		Assertions.assertEquals(5, result);
	}

	@Test
	public void invokeWithParameters() throws SQLException
	{
		ExceptionFactory<SQLException> factory = new SQLExceptionFactory();
		Method method = Methods.getMethod(String.class, "charAt", int.class);
		Character result = Methods.invoke(method, factory, "hello", 0);
		Assertions.assertEquals('h', result);
	}

	private static class SQLExceptionFactory implements ExceptionFactory<SQLException>
	{
		@Override
		public Class<SQLException> getTargetClass()
		{
			return SQLException.class;
		}

		@Override
		public SQLException createException(Throwable e)
		{
			return new SQLException(e);
		}

		@Override
		public SQLException createException(String message)
		{
			return new SQLException(message);
		}

		@Override
		public boolean equals(SQLException exception1, SQLException exception2)
		{
			return exception1.equals(exception2);
		}

		@Override
		public boolean indicatesFailure(SQLException exception, net.sf.hajdbc.dialect.Dialect dialect)
		{
			return true;
		}

		@Override
		public ExceptionType getType()
		{
			return ExceptionType.SQL;
		}

		@Override
		public boolean correctHeuristic(SQLException exception, net.sf.hajdbc.durability.Durability.Phase phase)
		{
			return true;
		}
	}
}
