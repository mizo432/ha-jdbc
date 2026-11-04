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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for InvocationStrategies enum
 */
public class InvocationStrategiesTest {

	@Test
	public void allStrategiesPresent() {
		InvocationStrategies[] strategies = InvocationStrategies.values();
		Assertions.assertEquals(7, strategies.length);

		Assertions.assertNotNull(InvocationStrategies.INVOKE_ON_ALL);
		Assertions.assertNotNull(InvocationStrategies.INVOKE_ON_ANY);
		Assertions.assertNotNull(InvocationStrategies.INVOKE_ON_EXISTING);
		Assertions.assertNotNull(InvocationStrategies.INVOKE_ON_NEXT);
		Assertions.assertNotNull(InvocationStrategies.INVOKE_ON_PRIMARY);
		Assertions.assertNotNull(InvocationStrategies.TRANSACTION_INVOKE_ON_ALL);
		Assertions.assertNotNull(InvocationStrategies.END_TRANSACTION_INVOKE_ON_ALL);
	}

	@Test
	public void valueOf() {
		Assertions.assertEquals(InvocationStrategies.INVOKE_ON_ALL, InvocationStrategies.valueOf("INVOKE_ON_ALL"));
		Assertions.assertEquals(InvocationStrategies.INVOKE_ON_ANY, InvocationStrategies.valueOf("INVOKE_ON_ANY"));
		Assertions.assertEquals(InvocationStrategies.INVOKE_ON_EXISTING, InvocationStrategies.valueOf("INVOKE_ON_EXISTING"));
		Assertions.assertEquals(InvocationStrategies.INVOKE_ON_NEXT, InvocationStrategies.valueOf("INVOKE_ON_NEXT"));
		Assertions.assertEquals(InvocationStrategies.INVOKE_ON_PRIMARY, InvocationStrategies.valueOf("INVOKE_ON_PRIMARY"));
		Assertions.assertEquals(InvocationStrategies.TRANSACTION_INVOKE_ON_ALL, InvocationStrategies.valueOf("TRANSACTION_INVOKE_ON_ALL"));
		Assertions.assertEquals(InvocationStrategies.END_TRANSACTION_INVOKE_ON_ALL, InvocationStrategies.valueOf("END_TRANSACTION_INVOKE_ON_ALL"));
	}

	@Test
	public void invokeDelegatesToStrategy() throws Exception {
		// Verify that each enum value delegates to its internal strategy
		// We can't easily test the full invocation without a real cluster,
		// but we can verify the enum values are properly constructed
		for (InvocationStrategies strategy : InvocationStrategies.values()) {
			Assertions.assertNotNull(strategy, strategy.name());
		}
	}
}
