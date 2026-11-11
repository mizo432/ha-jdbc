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
package net.sf.hajdbc.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class AbstractLoggerTest
{
	@Test
	public void formatWithNoArgs()
	{
		assertThat(AbstractLogger.format("test message")).isEqualTo("test message");
	}
	
	@Test
	public void formatWithOneArg()
	{
		assertThat(AbstractLogger.format("Hello {0}", "World")).isEqualTo("Hello World");
	}
	
	@Test
	public void formatWithMultipleArgs()
	{
		assertThat(AbstractLogger.format("{0} and {1}", "A", "B")).isEqualTo("A and B");
	}
	
	@Test
	public void formatWithRepeatedArgs()
	{
		assertThat(AbstractLogger.format("{0} {0}", "A")).isEqualTo("A A");
	}
	
	@Test
	public void formatWithEmptyPattern()
	{
		assertThat(AbstractLogger.format("")).isEmpty();
	}
	
	@Test
	public void logWithArgsDelegatesToLogWithThrowable()
	{
		// Verify that log(Level, String, Object...) delegates to log(Level, Throwable, String, Object...)
		// with null throwable
		TestLogger logger = new TestLogger();
		logger.log(Level.INFO, "test {0}", "arg1");
		
		assertThat(logger.getLastLevel()).isEqualTo(Level.INFO);
		assertThat(logger.getLastThrowable()).isNull();
		assertThat(logger.getLastPattern()).isEqualTo("test {0}");
		assertThat(logger.getLastArgs()).containsExactly("arg1");
	}
	
	@Test
	public void logWithThrowableDelegatesToLogWithPattern()
	{
		// Verify that log(Level, Throwable) delegates to log(Level, Throwable, String, Object...)
		TestLogger logger = new TestLogger();
		RuntimeException ex = new RuntimeException("test error");
		logger.log(Level.ERROR, ex);
		
		assertThat(logger.getLastLevel()).isEqualTo(Level.ERROR);
		assertThat(logger.getLastThrowable()).isSameAs(ex);
		assertThat(logger.getLastPattern()).isEqualTo("test error");
		assertThat(logger.getLastArgs()).isEmpty();
	}
	
	// Test implementation that captures calls
	
	private static class TestLogger extends AbstractLogger {
		private Level lastLevel;
		private Throwable lastThrowable;
		private String lastPattern;
		private Object[] lastArgs = new Object[0];
		
		@Override
		public void log(Level level, Throwable e, String pattern, Object... args)
		{
			this.lastLevel = level;
			this.lastThrowable = e;
			this.lastPattern = pattern;
			this.lastArgs = args;
		}
		
		public Level getLastLevel() { return lastLevel; }
		public Throwable getLastThrowable() { return lastThrowable; }
		public String getLastPattern() { return lastPattern; }
		public Object[] getLastArgs() { return lastArgs; }
	}
}
