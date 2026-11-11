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
package net.sf.hajdbc.messages.simple;

import net.sf.hajdbc.messages.Messages;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleMessagesTest
{
	private final Messages messages = new SimpleMessages();

	@Test
	public void serviceNotFound()
	{
		String message = messages.serviceNotFound(String.class);
		Assertions.assertNotNull(message);
		Assertions.assertTrue(message.contains("java.lang.String"));
	}

	@Test
	public void noDatabases()
	{
		String message = messages.noDatabases();
		Assertions.assertNotNull(message);
		Assertions.assertFalse(message.isEmpty());
	}

	@Test
	public void noLocation()
	{
		String message = messages.noLocation("db1");
		Assertions.assertNotNull(message);
		Assertions.assertFalse(message.isEmpty());
	}

	@Test
	public void invalidLocation()
	{
		String message = messages.invalidLocation("db1", "jdbc:h2:mem:test");
		Assertions.assertNotNull(message);
		Assertions.assertFalse(message.isEmpty());
	}

	@Test
	public void resourceNotFound()
	{
		String message = messages.resourceNotFound("config.xml");
		Assertions.assertNotNull(message);
		Assertions.assertFalse(message.isEmpty());
	}

	@Test
	public void noSyncStrategies()
	{
		String message = messages.noSyncStrategies();
		Assertions.assertNotNull(message);
		Assertions.assertFalse(message.isEmpty());
	}

	@Test
	public void noEmbeddedDriverFound()
	{
		String message = messages.noEmbeddedDriverFound();
		Assertions.assertNotNull(message);
		Assertions.assertFalse(message.isEmpty());
	}
}
