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

public class AbstractPoolProviderTest
{
	@Test
	public void getProvidedClass()
	{
		AbstractPoolProvider<String, Exception> provider = new AbstractPoolProvider<>(String.class, Exception.class) {
			@Override
			public String create() { return "test"; }
			@Override
			public void close(String object) {}
			@Override
			public boolean isValid(String item) { return true; }
		};
		
		assertThat(provider.getProvidedClass()).isEqualTo(String.class);
	}
	
	@Test
	public void getExceptionClass()
	{
		AbstractPoolProvider<String, RuntimeException> provider = new AbstractPoolProvider<>(String.class, RuntimeException.class) {
			@Override
			public String create() { return "test"; }
			@Override
			public void close(String object) {}
			@Override
			public boolean isValid(String item) { return true; }
		};
		
		assertThat(provider.getExceptionClass()).isEqualTo(RuntimeException.class);
	}
}
