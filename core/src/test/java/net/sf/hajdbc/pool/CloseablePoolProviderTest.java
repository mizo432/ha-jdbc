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
package net.sf.hajdbc.pool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class CloseablePoolProviderTest
{
	@Test
	public void closeDelegatesToAutoCloseable() throws Exception
	{
		TestCloseable item = new TestCloseable();
		
		CloseablePoolProvider<TestCloseable, Exception> provider = new CloseablePoolProvider<>(TestCloseable.class, Exception.class) {
			@Override
			public TestCloseable create() { return new TestCloseable(); }
			@Override
			public boolean isValid(TestCloseable item) { return true; }
		};
		
		provider.close(item);
		
		assertThat(item.isClosed()).isTrue();
	}
	
	@Test
	public void closeHandlesExceptionGracefully() throws Exception
	{
		ThrowingCloseable item = new ThrowingCloseable();
		
		CloseablePoolProvider<ThrowingCloseable, Exception> provider = new CloseablePoolProvider<>(ThrowingCloseable.class, Exception.class) {
			@Override
			public ThrowingCloseable create() { return new ThrowingCloseable(); }
			@Override
			public boolean isValid(ThrowingCloseable item) { return true; }
		};
		
		// Should not throw - exception is logged
		provider.close(item);
	}
	
	@Test
	public void getProvidedClass()
	{
		CloseablePoolProvider<TestCloseable, Exception> provider = new CloseablePoolProvider<>(TestCloseable.class, Exception.class) {
			@Override
			public TestCloseable create() { return new TestCloseable(); }
			@Override
			public boolean isValid(TestCloseable item) { return true; }
		};
		
		assertThat(provider.getProvidedClass()).isEqualTo(TestCloseable.class);
	}
	
	@Test
	public void getExceptionClass()
	{
		CloseablePoolProvider<TestCloseable, RuntimeException> provider = new CloseablePoolProvider<>(TestCloseable.class, RuntimeException.class) {
			@Override
			public TestCloseable create() { return new TestCloseable(); }
			@Override
			public boolean isValid(TestCloseable item) { return true; }
		};
		
		assertThat(provider.getExceptionClass()).isEqualTo(RuntimeException.class);
	}
	
	// Test helpers
	
	public static class TestCloseable implements AutoCloseable {
		private boolean closed = false;
		
		@Override
		public void close() { this.closed = true; }
		
		public boolean isClosed() { return closed; }
	}
	
	public static class ThrowingCloseable implements AutoCloseable {
		@Override
		public void close() throws Exception { throw new Exception("Close failed"); }
	}
}
