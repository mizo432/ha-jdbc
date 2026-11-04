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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for Strings utility class
 */
public class StringsTest {

	@Test
	public void joinEmptyCollection() {
		String result = Strings.join(Collections.<String>emptyList(), ",");
		Assertions.assertEquals("", result);
	}

	@Test
	public void joinSingleElement() {
		String result = Strings.join(Collections.singletonList("a"), ",");
		Assertions.assertEquals("a", result);
	}

	@Test
	public void joinMultipleElements() {
		List<String> list = Arrays.asList("a", "b", "c");
		String result = Strings.join(list, ",");
		Assertions.assertEquals("a,b,c", result);
	}

	@Test
	public void joinWithCustomDelimiter() {
		List<String> list = Arrays.asList("a", "b", "c");
		String result = Strings.join(list, " - ");
		Assertions.assertEquals("a - b - c", result);
	}

	@Test
	public void joinArray() {
		String[] array = new String[] { "a", "b", "c" };
		String result = Strings.join(array, ",");
		Assertions.assertEquals("a,b,c", result);
	}

	@Test
	public void joinEmptyArray() {
		String[] array = new String[] {};
		String result = Strings.join(array, ",");
		Assertions.assertEquals("", result);
	}

	@Test
	public void joinWithBuilder() {
		List<String> list = Arrays.asList("a", "b", "c");
		StringBuilder builder = new StringBuilder("prefix:");
		String result = Strings.join(builder, list, ",").toString();
		Assertions.assertEquals("prefix:a,b,c", result);
	}

	@Test
	public void joinArrayWithBuilder() {
		String[] array = new String[] { "a", "b", "c" };
		StringBuilder builder = new StringBuilder("prefix:");
		String result = Strings.join(builder, array, ",").toString();
		Assertions.assertEquals("prefix:a,b,c", result);
	}

	@Test
	public void joinWithEmptyDelimiter() {
		List<String> list = Arrays.asList("a", "b", "c");
		String result = Strings.join(list, "");
		Assertions.assertEquals("abc", result);
	}

	@Test
	public void constants() {
		Assertions.assertEquals("%", Strings.ANY);
		Assertions.assertEquals(",", Strings.COMMA);
		Assertions.assertEquals("-", Strings.DASH);
		Assertions.assertEquals(".", Strings.DOT);
		Assertions.assertEquals("", Strings.EMPTY);
		Assertions.assertEquals(", ", Strings.PADDED_COMMA);
		Assertions.assertEquals("?", Strings.QUESTION);
		Assertions.assertEquals("_", Strings.UNDERSCORE);
		Assertions.assertEquals("\t", Strings.TAB);
	}

	@Test
	public void joinWithNullElements() {
		List<String> list = Arrays.asList("a", null, "c");
		String result = Strings.join(list, ",");
		Assertions.assertEquals("a,null,c", result);
	}
}
