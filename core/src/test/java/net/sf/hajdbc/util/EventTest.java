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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventTest
{
	@Test
	public void getSource()
	{
		String source = "test-source";
		Event<String> event = new Event<>(source);
		Assertions.assertEquals(source, event.getSource());
	}
	
	@Test
	public void equalsWithSameSource()
	{
		String source = "test";
		Event<String> event1 = new Event<>(source);
		Event<String> event2 = new Event<>(source);
		
		Assertions.assertTrue(event1.equals(event2));
	}
	
	@Test
	public void equalsWithDifferentSource()
	{
		Event<String> event1 = new Event<>("source1");
		Event<String> event2 = new Event<>("source2");
		
		Assertions.assertFalse(event1.equals(event2));
	}
	
	@Test
	public void equalsWithNonEvent()
	{
		Event<String> event = new Event<>("source");
		
		Assertions.assertFalse(event.equals("not an event"));
		Assertions.assertFalse(event.equals(null));
		Assertions.assertFalse(event.equals(Integer.valueOf(1)));
	}
	
	@Test
	public void hashCodeConsistency()
	{
		String source = "test";
		Event<String> event1 = new Event<>(source);
		Event<String> event2 = new Event<>(source);
		
		Assertions.assertEquals(event1.hashCode(), event2.hashCode());
		Assertions.assertEquals(source.hashCode(), event1.hashCode());
	}
	
	@Test
	public void toStringDelegatesToSource()
	{
		String source = "custom-source";
		Event<String> event = new Event<>(source);
		
		Assertions.assertEquals(source, event.toString());
	}
	
	@Test
	public void serialization() throws IOException, ClassNotFoundException
	{
		SerializableSource source = new SerializableSource("data");
		Event<SerializableSource> event = new Event<>(source);
		
		// Serialize
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(event);
		}
		
		// Deserialize
		byte[] bytes = baos.toByteArray();
		@SuppressWarnings("unchecked")
		Event<SerializableSource> deserialized = (Event<SerializableSource>) 
			new java.io.ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
		
		Assertions.assertEquals(source, deserialized.getSource());
		Assertions.assertEquals(event, deserialized);
		Assertions.assertEquals(event.hashCode(), deserialized.hashCode());
	}
	
	@Test
	public void serializationWithIntegerSource() throws IOException, ClassNotFoundException
	{
		Integer source = 42;
		Event<Integer> event = new Event<>(source);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(event);
		}
		
		byte[] bytes = baos.toByteArray();
		@SuppressWarnings("unchecked")
		Event<Integer> deserialized = (Event<Integer>) 
			new java.io.ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
		
		Assertions.assertEquals(source, deserialized.getSource());
	}
	
	@Test
	public void equalsWithDifferentSourceType()
	{
		Event<String> event1 = new Event<>("test");
		Event<Integer> event2 = new Event<>(123);
		
		Assertions.assertFalse(event1.equals(event2));
	}
	
	@Test
	public void eventObjectSubclass()
	{
		Event<String> event = new Event<>("source");
		Assertions.assertTrue(event instanceof java.util.EventObject);
	}
	
	static class SerializableSource implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private final String data;
		
		SerializableSource(String data)
		{
			this.data = data;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			return this.data.equals(((SerializableSource) obj).data);
		}
		
		@Override
		public int hashCode()
		{
			return this.data.hashCode();
		}
		
		@Override
		public String toString()
		{
			return this.data;
		}
	}
}
