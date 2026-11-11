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

public class LevelTest
{
	@Test
	public void enumValues()
	{
		Level[] levels = Level.values();
		assertThat(levels).hasSize(5);
		assertThat(levels).containsExactly(Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE);
	}
	
	@Test
	public void valueOf()
	{
		assertThat(Level.valueOf("ERROR")).isEqualTo(Level.ERROR);
		assertThat(Level.valueOf("WARN")).isEqualTo(Level.WARN);
		assertThat(Level.valueOf("INFO")).isEqualTo(Level.INFO);
		assertThat(Level.valueOf("DEBUG")).isEqualTo(Level.DEBUG);
		assertThat(Level.valueOf("TRACE")).isEqualTo(Level.TRACE);
	}
}
