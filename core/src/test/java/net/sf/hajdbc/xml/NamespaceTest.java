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
package net.sf.hajdbc.xml;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.validation.Schema;

import org.junit.jupiter.api.Test;

/**
 * Tests for Namespace enum
 */
public class NamespaceTest {

	@Test
	public void enumValues() {
		Namespace[] namespaces = Namespace.values();
		assertThat(namespaces).hasSize(2);
		assertThat(namespaces).containsExactly(Namespace.VERSION_3_0, Namespace.VERSION_3_1);
	}

	@Test
	public void currentVersion() {
		assertThat(Namespace.CURRENT_VERSION).isEqualTo(Namespace.VERSION_3_1);
	}

	@Test
	public void version30Uri() {
		assertThat(Namespace.VERSION_3_0.getURI()).isEqualTo("urn:ha-jdbc:cluster:3.0");
	}

	@Test
	public void version31Uri() {
		assertThat(Namespace.VERSION_3_1.getURI()).isEqualTo("urn:ha-jdbc:cluster:3.1");
	}

	@Test
	public void version30Schema() {
		Schema schema = Namespace.VERSION_3_0.getSchema();
		assertThat(schema).isNotNull();
	}

	@Test
	public void version31Schema() {
		Schema schema = Namespace.VERSION_3_1.getSchema();
		assertThat(schema).isNotNull();
	}

	@Test
	public void version30ReaderFactory() {
		assertThat(Namespace.VERSION_3_0.getReaderFactory()).isNotNull();
	}

	@Test
	public void version31ReaderFactory() {
		assertThat(Namespace.VERSION_3_1.getReaderFactory()).isNotNull();
	}

	@Test
	public void valueOf() {
		assertThat(Namespace.valueOf("VERSION_3_0")).isEqualTo(Namespace.VERSION_3_0);
		assertThat(Namespace.valueOf("VERSION_3_1")).isEqualTo(Namespace.VERSION_3_1);
	}
}
