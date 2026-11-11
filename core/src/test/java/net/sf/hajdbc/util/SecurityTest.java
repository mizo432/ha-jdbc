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
package net.sf.hajdbc.util;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SecurityTest
{
	@Test
	public void runPrivilegedAction()
	{
		PrivilegedAction<String> action = () -> "hello";
		String result = Security.run(action);
		Assertions.assertEquals("hello", result);
	}
	
	@Test
	public void runPrivilegedActionReturnsNull()
	{
		PrivilegedAction<String> action = () -> null;
		String result = Security.run(action);
		Assertions.assertNull(result);
	}
	
	@Test
	public void runPrivilegedExceptionAction() throws Exception
	{
		PrivilegedExceptionAction<Integer> action = () -> 42;
		Integer result = Security.run(action);
		Assertions.assertEquals(42, result);
	}
	
	@Test
	public void runPrivilegedExceptionActionThrowsException()
	{
		PrivilegedExceptionAction<Void> action = () -> {
			throw new RuntimeException("expected");
		};
		
		Assertions.assertThrows(RuntimeException.class, () -> Security.run(action));
	}
	
	@Test
	public void runPrivilegedExceptionActionWithExceptionClass() throws IOException
	{
		PrivilegedExceptionAction<String> action = () -> "success";
		String result = Security.run(action, IOException.class);
		Assertions.assertEquals("success", result);
	}
	
	@Test
	public void runPrivilegedExceptionActionWithExceptionClassThrowsCheckedException()
	{
		PrivilegedExceptionAction<Void> action = () -> {
			throw new IOException("expected");
		};
		
		Assertions.assertThrows(IOException.class, () -> Security.run(action, IOException.class));
	}
	
	@Test
	public void runPrivilegedExceptionActionWithExceptionClassCastsException()
	{
		PrivilegedExceptionAction<Void> action = () -> {
			throw new IllegalArgumentException("expected");
		};
		
		Assertions.assertThrows(ClassCastException.class, () -> Security.run(action, IOException.class));
	}
	
	@Test
	public void runPrivilegedActionWithInteger()
	{
		PrivilegedAction<Integer> action = () -> 100;
		Integer result = Security.run(action);
		Assertions.assertEquals(100, result);
	}
	
	@Test
	public void runPrivilegedActionWithBoolean()
	{
		PrivilegedAction<Boolean> action = () -> true;
		Boolean result = Security.run(action);
		Assertions.assertTrue(result);
	}
}
