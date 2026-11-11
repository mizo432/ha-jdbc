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
package net.sf.hajdbc.durability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class DurabilityPhaseTest
{
	@Test
	public void phaseValues()
	{
		Durability.Phase[] phases = Durability.Phase.values();
		assertThat(phases).hasSize(4);
		assertThat(phases).containsExactly(
			Durability.Phase.PREPARE,
			Durability.Phase.COMMIT,
			Durability.Phase.ROLLBACK,
			Durability.Phase.FORGET
		);
	}
	
	@Test
	public void phaseValueOf()
	{
		assertThat(Durability.Phase.valueOf("PREPARE")).isEqualTo(Durability.Phase.PREPARE);
		assertThat(Durability.Phase.valueOf("COMMIT")).isEqualTo(Durability.Phase.COMMIT);
		assertThat(Durability.Phase.valueOf("ROLLBACK")).isEqualTo(Durability.Phase.ROLLBACK);
		assertThat(Durability.Phase.valueOf("FORGET")).isEqualTo(Durability.Phase.FORGET);
	}
}
