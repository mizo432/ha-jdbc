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

import java.sql.SQLException;
import java.util.UUID;

import net.sf.hajdbc.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 *
 */
public class InvokerEventTest
{
	@Test
	public void serializationNoResult()
	{
		InvokerEvent event1 = new InvokerEventImpl(UUID.randomUUID(), Durability.Phase.COMMIT, "1");
		InvokerEvent event2 = Objects.deserialize(Objects.serialize(event1), InvokerEvent.class);
		assertThat(event2).isEqualTo(event1);
		assertThat(event2.getTransactionId()).isEqualTo(event1.getTransactionId());
		assertThat(event2.getPhase()).isEqualTo(event1.getPhase());
		assertThat(event2.getDatabaseId()).isEqualTo(event1.getDatabaseId());
	}

	@Test
	public void serializationWithResult()
	{
		InvokerEvent event1 = new InvokerEventImpl(UUID.randomUUID(), Durability.Phase.COMMIT, "1");
		event1.setResult(new InvokerResultImpl(100));
		InvokerEvent event2 = Objects.deserialize(Objects.serialize(event1), InvokerEvent.class);
		assertThat(event2).isEqualTo(event1);
		assertThat(event2.getTransactionId()).isEqualTo(event1.getTransactionId());
		assertThat(event2.getPhase()).isEqualTo(event1.getPhase());
		assertThat(event2.getDatabaseId()).isEqualTo(event1.getDatabaseId());
		assertThat(event2.getResult().getValue()).isEqualTo(event1.getResult().getValue());
	}

	@Test
	public void serializationWithException()
	{
		InvokerEvent event1 = new InvokerEventImpl(UUID.randomUUID(), Durability.Phase.COMMIT, "1");
		event1.setResult(new InvokerResultImpl(new SQLException()));
		InvokerEvent event2 = Objects.deserialize(Objects.serialize(event1), event1.getClass());
		assertThat(event2).isEqualTo(event1);
		assertThat(event2.getTransactionId()).isEqualTo(event1.getTransactionId());
		assertThat(event2.getPhase()).isEqualTo(event1.getPhase());
		assertThat(event2.getDatabaseId()).isEqualTo(event1.getDatabaseId());
		assertThat(event2.getResult().getException()).isNotNull();
	}

	@Test
	public void equals() {
		UUID txId = UUID.randomUUID();
		InvokerEvent event1 = new InvokerEventImpl(txId, Durability.Phase.COMMIT, "1");
		InvokerEvent event2 = new InvokerEventImpl(txId, Durability.Phase.COMMIT, "1");
		assertThat(event2).isEqualTo(event1);
		
		event1.setResult(new InvokerResultImpl(0));
		assertThat(event2).isEqualTo(event1);
		
		event2.setResult(new InvokerResultImpl(new Exception()));
		assertThat(event2).isEqualTo(event1);
		
		event2 = new InvokerEventImpl(txId, Durability.Phase.COMMIT, "2");
		assertThat(event2).isNotEqualTo(event1);
		
		event2 = new InvokerEventImpl(txId, Durability.Phase.ROLLBACK, "1");
		assertThat(event2).isNotEqualTo(event1);
		
		event2 = new InvokerEventImpl(UUID.randomUUID(), Durability.Phase.COMMIT, "1");
		assertThat(event2).isNotEqualTo(event1);
	}
}
