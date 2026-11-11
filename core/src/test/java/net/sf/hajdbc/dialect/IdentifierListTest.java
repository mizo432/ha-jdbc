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
package net.sf.hajdbc.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import net.sf.hajdbc.IdentifierNormalizer;

import org.junit.jupiter.api.Test;

/**
 * Tests for IdentifierList
 */
public class IdentifierListTest {

	@Test
	public void addNormalizesIdentifier() {
		IdentifierNormalizer normalizer = mock(IdentifierNormalizer.class);
		when(normalizer.normalize("test")).thenReturn("TEST");

		IdentifierList list = new IdentifierList(normalizer);
		list.add("test");

		assertThat(list).containsExactly("TEST");
	}

	@Test
	public void addAtIndexNormalizesIdentifier() {
		IdentifierNormalizer normalizer = mock(IdentifierNormalizer.class);
		when(normalizer.normalize("first")).thenReturn("FIRST");
		when(normalizer.normalize("second")).thenReturn("SECOND");

		IdentifierList list = new IdentifierList(normalizer);
		list.add("first");
		list.add(1, "second");

		assertThat(list).containsExactly("FIRST", "SECOND");
	}

	@Test
	public void addFirstNormalizesIdentifier() {
		IdentifierNormalizer normalizer = mock(IdentifierNormalizer.class);
		when(normalizer.normalize("first")).thenReturn("FIRST");
		when(normalizer.normalize("second")).thenReturn("SECOND");

		IdentifierList list = new IdentifierList(normalizer);
		list.add("second");
		list.addFirst("first");

		assertThat(list).containsExactly("FIRST", "SECOND");
	}

	@Test
	public void addLastNormalizesIdentifier() {
		IdentifierNormalizer normalizer = mock(IdentifierNormalizer.class);
		when(normalizer.normalize("first")).thenReturn("FIRST");
		when(normalizer.normalize("second")).thenReturn("SECOND");

		IdentifierList list = new IdentifierList(normalizer);
		list.add("first");
		list.addLast("second");

		assertThat(list).containsExactly("FIRST", "SECOND");
	}

	@Test
	public void addAllNormalizesIdentifiers() {
		IdentifierNormalizer normalizer = mock(IdentifierNormalizer.class);
		when(normalizer.normalize("a")).thenReturn("A");
		when(normalizer.normalize("b")).thenReturn("B");
		when(normalizer.normalize("c")).thenReturn("C");

		IdentifierList list = new IdentifierList(normalizer);
		list.addAll(Arrays.asList("a", "b", "c"));

		assertThat(list).containsExactly("A", "B", "C");
	}

	@Test
	public void addAllAtIndexNormalizesIdentifiers() {
		IdentifierNormalizer normalizer = mock(IdentifierNormalizer.class);
		when(normalizer.normalize("a")).thenReturn("A");
		when(normalizer.normalize("b")).thenReturn("B");

		IdentifierList list = new IdentifierList(normalizer);
		list.addAll(0, Arrays.asList("a", "b"));

		assertThat(list).containsExactly("A", "B");
	}

	@Test
	public void addAllWithEmptyCollection() {
		IdentifierNormalizer normalizer = mock(IdentifierNormalizer.class);

		IdentifierList list = new IdentifierList(normalizer);
		boolean result = list.addAll(Collections.emptyList());

		// addAll returns false when no elements were added (i != index)
		assertThat(result).isFalse();
		assertThat(list).isEmpty();
	}

	@Test
	public void serialization() {
		IdentifierNormalizer normalizer = mock(IdentifierNormalizer.class);

		IdentifierList list = new IdentifierList(normalizer);
		assertThat(list).isInstanceOf(java.io.Serializable.class);
	}
}
