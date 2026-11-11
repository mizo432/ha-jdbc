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
package net.sf.hajdbc.sql.serial;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;

import org.junit.jupiter.api.Test;

/**
 * Tests for SerialLocatorFactories enum
 */
public class SerialLocatorFactoriesTest {

	@Test
	public void enumValues() {
		SerialLocatorFactories[] factories = SerialLocatorFactories.values();
		assertThat(factories).hasSize(5);
		assertThat(factories).containsExactly(
			SerialLocatorFactories.ARRAY,
			SerialLocatorFactories.BLOB,
			SerialLocatorFactories.CLOB,
			SerialLocatorFactories.NCLOB,
			SerialLocatorFactories.REF
		);
	}

	@Test
	public void arrayFactory() {
		assertThat(SerialLocatorFactories.ARRAY.getSerialFactory()).isNotNull();
	}

	@Test
	public void blobFactory() {
		assertThat(SerialLocatorFactories.BLOB.getSerialFactory()).isNotNull();
	}

	@Test
	public void clobFactory() {
		assertThat(SerialLocatorFactories.CLOB.getSerialFactory()).isNotNull();
	}

	@Test
	public void nclobFactory() {
		assertThat(SerialLocatorFactories.NCLOB.getSerialFactory()).isNotNull();
	}

	@Test
	public void refFactory() {
		assertThat(SerialLocatorFactories.REF.getSerialFactory()).isNotNull();
	}

	@Test
	public void findArray() {
		assertThat(SerialLocatorFactories.find(Array.class)).isNotNull();
	}

	@Test
	public void findBlob() {
		assertThat(SerialLocatorFactories.find(Blob.class)).isNotNull();
	}

	@Test
	public void findClob() {
		assertThat(SerialLocatorFactories.find(Clob.class)).isNotNull();
	}

	@Test
	public void findNClob() {
		assertThat(SerialLocatorFactories.find(NClob.class)).isNotNull();
	}

	@Test
	public void findRef() {
		assertThat(SerialLocatorFactories.find(Ref.class)).isNotNull();
	}

	@Test
	public void findUnknownClass() {
		assertThat(SerialLocatorFactories.find(String.class)).isNull();
	}

	@Test
	public void valueOf() {
		assertThat(SerialLocatorFactories.valueOf("ARRAY")).isEqualTo(SerialLocatorFactories.ARRAY);
		assertThat(SerialLocatorFactories.valueOf("BLOB")).isEqualTo(SerialLocatorFactories.BLOB);
		assertThat(SerialLocatorFactories.valueOf("CLOB")).isEqualTo(SerialLocatorFactories.CLOB);
		assertThat(SerialLocatorFactories.valueOf("NCLOB")).isEqualTo(SerialLocatorFactories.NCLOB);
		assertThat(SerialLocatorFactories.valueOf("REF")).isEqualTo(SerialLocatorFactories.REF);
	}
}
