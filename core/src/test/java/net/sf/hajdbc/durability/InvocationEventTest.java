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

import java.util.UUID;

import net.sf.hajdbc.ExceptionType;
import net.sf.hajdbc.util.Objects;

import org.junit.Test;

public class InvocationEventTest
{
	@Test
	public void serialization()
	{
		InvocationEvent event1 = new InvocationEventImpl(10, Durability.Phase.COMMIT, ExceptionType.SQL);
		InvocationEvent event2 = Objects.deserialize(Objects.serialize(event1), InvocationEvent.class);
		assertThat(event2).isEqualTo(event1);
		assertThat(event2.getTransactionId()).isEqualTo(event1.getTransactionId());
		assertThat(event2.getPhase()).isEqualTo(event1.getPhase());
		assertThat(event2.getExceptionType()).isEqualTo(event1.getExceptionType());
	}

	@Test
	public void equals() {
		UUID txId = UUID.randomUUID();
		InvocationEvent event1 = new InvocationEventImpl(txId, Durability.Phase.COMMIT, ExceptionType.SQL);
		InvocationEvent event2 = new InvocationEventImpl(txId, Durability.Phase.COMMIT, ExceptionType.SQL);
		assertThat(event2).isEqualTo(event1);
		
		event2 = new InvocationEventImpl(txId, Durability.Phase.COMMIT, ExceptionType.XA);
		assertThat(event2).isEqualTo(event1);
		
		event2 = new InvocationEventImpl(txId, Durability.Phase.ROLLBACK, ExceptionType.SQL);
		assertThat(event2).isNotEqualTo(event1);
		
		event2 = new InvocationEventImpl(UUID.randomUUID(), Durability.Phase.COMMIT, ExceptionType.SQL);
		assertThat(event2).isNotEqualTo(event1);
	}
}
