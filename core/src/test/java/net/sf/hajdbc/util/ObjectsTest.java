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

import java.io.Serializable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ObjectsTest
{
	@Test
	public void equalsWithNulls()
	{
		Assertions.assertTrue(Objects.equals(null, null));
		Assertions.assertFalse(Objects.equals(null, "test"));
		Assertions.assertFalse(Objects.equals("test", null));
	}
	
	@Test
	public void equalsWithStrings()
	{
		Assertions.assertTrue(Objects.equals("test", "test"));
		Assertions.assertFalse(Objects.equals("test", "other"));
	}
	
	@Test
	public void equalsWithByteArrays()
	{
		byte[] a = {1, 2, 3};
		byte[] b = {1, 2, 3};
		byte[] c = {1, 2, 4};
		
		Assertions.assertTrue(Objects.equals(a, b));
		Assertions.assertFalse(Objects.equals(a, c));
	}
	
	@Test
	public void equalsWithIntArrays()
	{
		int[] a = {1, 2, 3};
		int[] b = {1, 2, 3};
		int[] c = {1, 2, 4};
		
		Assertions.assertTrue(Objects.equals(a, b));
		Assertions.assertFalse(Objects.equals(a, c));
	}
	
	@Test
	public void equalsWithBooleanArrays()
	{
		boolean[] a = {true, false, true};
		boolean[] b = {true, false, true};
		boolean[] c = {true, false, false};
		
		Assertions.assertTrue(Objects.equals(a, b));
		Assertions.assertFalse(Objects.equals(a, c));
	}
	
	@Test
	public void equalsWithDoubleArrays()
	{
		double[] a = {1.0, 2.0, 3.0};
		double[] b = {1.0, 2.0, 3.0};
		double[] c = {1.0, 2.0, 3.1};
		
		Assertions.assertTrue(Objects.equals(a, b));
		Assertions.assertFalse(Objects.equals(a, c));
	}
	
	@Test
	public void equalsWithFloatArrays()
	{
		float[] a = {1.0f, 2.0f, 3.0f};
		float[] b = {1.0f, 2.0f, 3.0f};
		float[] c = {1.0f, 2.0f, 3.1f};
		
		Assertions.assertTrue(Objects.equals(a, b));
		Assertions.assertFalse(Objects.equals(a, c));
	}
	
	@Test
	public void equalsWithLongArrays()
	{
		long[] a = {1L, 2L, 3L};
		long[] b = {1L, 2L, 3L};
		long[] c = {1L, 2L, 4L};
		
		Assertions.assertTrue(Objects.equals(a, b));
		Assertions.assertFalse(Objects.equals(a, c));
	}
	
	@Test
	public void equalsWithShortArrays()
	{
		short[] a = {1, 2, 3};
		short[] b = {1, 2, 3};
		short[] c = {1, 2, 4};
		
		Assertions.assertTrue(Objects.equals(a, b));
		Assertions.assertFalse(Objects.equals(a, c));
	}
	
	@Test
	public void equalsWithCharArrays()
	{
		char[] a = {'a', 'b', 'c'};
		char[] b = {'a', 'b', 'c'};
		char[] c = {'a', 'b', 'd'};
		
		Assertions.assertTrue(Objects.equals(a, b));
		Assertions.assertFalse(Objects.equals(a, c));
	}
	
	@Test
	public void equalsWithObjectArrays()
	{
		String[] a = {"a", "b", "c"};
		String[] b = {"a", "b", "c"};
		String[] c = {"a", "b", "d"};
		
		Assertions.assertTrue(Objects.equals(a, b));
		Assertions.assertFalse(Objects.equals(a, c));
	}
	
	@Test
	public void serializeNull()
	{
		Assertions.assertNull(Objects.serialize(null));
	}
	
	@Test
	public void serializeString()
	{
		String original = "test";
		byte[] serialized = Objects.serialize(original);
		
		Assertions.assertNotNull(serialized);
		Assertions.assertFalse(serialized.length == 0);
	}
	
	@Test
	public void serializeDeserializeString()
	{
		String original = "test";
		byte[] serialized = Objects.serialize(original);
		String deserialized = Objects.deserialize(serialized, String.class);
		
		Assertions.assertEquals(original, deserialized);
	}
	
	@Test
	public void serializeDeserializeInteger()
	{
		Integer original = 42;
		byte[] serialized = Objects.serialize(original);
		Integer deserialized = Objects.deserialize(serialized, Integer.class);
		
		Assertions.assertEquals(original, deserialized);
	}
	
	@Test
	public void deserializeNull()
	{
		Assertions.assertNull(Objects.deserialize(null, String.class));
	}
	
	@Test
	public void serializeDeserializeSerializable()
	{
		TestObject original = new TestObject("test", 123);
		byte[] serialized = Objects.serialize(original);
		TestObject deserialized = Objects.deserialize(serialized, TestObject.class);
		
		Assertions.assertEquals(original.getName(), deserialized.getName());
		Assertions.assertEquals(original.getValue(), deserialized.getValue());
	}
	
	public static class TestObject implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private final String name;
		private final int value;
		
		public TestObject(String name, int value)
		{
			this.name = name;
			this.value = value;
		}
		
		public String getName()
		{
			return name;
		}
		
		public int getValue()
		{
			return value;
		}
	}
}
