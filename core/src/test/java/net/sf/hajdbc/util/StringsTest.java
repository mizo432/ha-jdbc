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
package net.sf.hajdbc.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 * Tests for Strings utility class
 */
public class StringsTest {

	@Test
	public void joinEmptyCollection() {
		String result = Strings.join(Collections.<String>emptyList(), ",");
		assertThat(result).isEmpty();
	}

	@Test
	public void joinSingleElement() {
		String result = Strings.join(Collections.singletonList("a"), ",");
		assertThat(result).isEqualTo("a");
	}

	@Test
	public void joinMultipleElements() {
		List<String> list = Arrays.asList("a", "b", "c");
		String result = Strings.join(list, ",");
		assertThat(result).isEqualTo("a,b,c");
	}

	@Test
	public void joinWithCustomDelimiter() {
		List<String> list = Arrays.asList("a", "b", "c");
		String result = Strings.join(list, " - ");
		assertThat(result).isEqualTo("a - b - c");
	}

	@Test
	public void joinArray() {
		String[] array = new String[] { "a", "b", "c" };
		String result = Strings.join(array, ",");
		assertThat(result).isEqualTo("a,b,c");
	}

	@Test
	public void joinEmptyArray() {
		String[] array = new String[] {};
		String result = Strings.join(array, ",");
		assertThat(result).isEmpty();
	}

	@Test
	public void joinWithBuilder() {
		List<String> list = Arrays.asList("a", "b", "c");
		StringBuilder builder = new StringBuilder("prefix:");
		String result = Strings.join(builder, list, ",").toString();
		assertThat(result).isEqualTo("prefix:a,b,c");
	}

	@Test
	public void joinArrayWithBuilder() {
		String[] array = new String[] { "a", "b", "c" };
		StringBuilder builder = new StringBuilder("prefix:");
		String result = Strings.join(builder, array, ",").toString();
		assertThat(result).isEqualTo("prefix:a,b,c");
	}

	@Test
	public void joinWithEmptyDelimiter() {
		List<String> list = Arrays.asList("a", "b", "c");
		String result = Strings.join(list, "");
		assertThat(result).isEqualTo("abc");
	}

	@Test
	public void constants() {
		assertThat(Strings.ANY).isEqualTo("%");
		assertThat(Strings.COMMA).isEqualTo(",");
		assertThat(Strings.DASH).isEqualTo("-");
		assertThat(Strings.DOT).isEqualTo(".");
		assertThat(Strings.EMPTY).isEmpty();
		assertThat(Strings.PADDED_COMMA).isEqualTo(", ");
		assertThat(Strings.QUESTION).isEqualTo("?");
		assertThat(Strings.UNDERSCORE).isEqualTo("_");
		assertThat(Strings.TAB).isEqualTo("\t");
	}

	@Test
	public void joinWithNullElements() {
		List<String> list = Arrays.asList("a", null, "c");
		String result = Strings.join(list, ",");
		assertThat(result).isEqualTo("a,null,c");
	}
}
