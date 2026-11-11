/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2014  Paul Ferraro
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
package net.sf.hajdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LocalityTest
{
	@Test
	public void enumValues()
	{
		Locality[] localities = Locality.values();
		assertThat(localities).hasSize(3);
		assertThat(localities).containsExactly(Locality.REMOTE, Locality.LOCAL, Locality.EMBEDDED);
	}
	
	@Test
	public void isLocal()
	{
		assertThat(Locality.REMOTE.isLocal()).isFalse();
		assertThat(Locality.LOCAL.isLocal()).isTrue();
		assertThat(Locality.EMBEDDED.isLocal()).isTrue();
	}
	
	@Test
	public void toStringReturnsLowerCase()
	{
		assertThat(Locality.REMOTE.toString()).isEqualTo("remote");
		assertThat(Locality.LOCAL.toString()).isEqualTo("local");
		assertThat(Locality.EMBEDDED.toString()).isEqualTo("embedded");
	}
	
	@Test
	public void valueOf()
	{
		assertThat(Locality.valueOf("REMOTE")).isEqualTo(Locality.REMOTE);
		assertThat(Locality.valueOf("LOCAL")).isEqualTo(Locality.LOCAL);
		assertThat(Locality.valueOf("EMBEDDED")).isEqualTo(Locality.EMBEDDED);
	}
}
