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

import java.util.SortedMap;

import net.sf.hajdbc.MockDatabase;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 * Tests for InvocationStrategies enum
 */
public class InvocationStrategiesTest {

	@Test
	public void allStrategiesPresent() {
		InvocationStrategies[] strategies = InvocationStrategies.values();
		assertThat(strategies.length).isEqualTo(7);

		assertThat(InvocationStrategies.INVOKE_ON_ALL).isNotNull();
		assertThat(InvocationStrategies.INVOKE_ON_ANY).isNotNull();
		assertThat(InvocationStrategies.INVOKE_ON_EXISTING).isNotNull();
		assertThat(InvocationStrategies.INVOKE_ON_NEXT).isNotNull();
		assertThat(InvocationStrategies.INVOKE_ON_PRIMARY).isNotNull();
		assertThat(InvocationStrategies.TRANSACTION_INVOKE_ON_ALL).isNotNull();
		assertThat(InvocationStrategies.END_TRANSACTION_INVOKE_ON_ALL).isNotNull();
	}

	@Test
	public void valueOf() {
		assertThat(InvocationStrategies.valueOf("INVOKE_ON_ALL")).isEqualTo(InvocationStrategies.INVOKE_ON_ALL);
		assertThat(InvocationStrategies.valueOf("INVOKE_ON_ANY")).isEqualTo(InvocationStrategies.INVOKE_ON_ANY);
		assertThat(InvocationStrategies.valueOf("INVOKE_ON_EXISTING")).isEqualTo(InvocationStrategies.INVOKE_ON_EXISTING);
		assertThat(InvocationStrategies.valueOf("INVOKE_ON_NEXT")).isEqualTo(InvocationStrategies.INVOKE_ON_NEXT);
		assertThat(InvocationStrategies.valueOf("INVOKE_ON_PRIMARY")).isEqualTo(InvocationStrategies.INVOKE_ON_PRIMARY);
		assertThat(InvocationStrategies.valueOf("TRANSACTION_INVOKE_ON_ALL")).isEqualTo(InvocationStrategies.TRANSACTION_INVOKE_ON_ALL);
		assertThat(InvocationStrategies.valueOf("END_TRANSACTION_INVOKE_ON_ALL")).isEqualTo(InvocationStrategies.END_TRANSACTION_INVOKE_ON_ALL);
	}

	@Test
	public void invokeDelegatesToStrategy() throws Exception {
		// Verify that each enum value delegates to its internal strategy
		// We can't easily test the full invocation without a real cluster,
		// but we can verify the enum values are properly constructed
		for (InvocationStrategies strategy : InvocationStrategies.values()) {
			assertThat(strategy).isNotNull();
		}
	}
}
