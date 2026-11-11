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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleStaticRegistryTest
{
	@Test
	public void getExistingKey()
	{
		Map<String, Integer> map = new HashMap<>();
		map.put("one", 1);
		map.put("two", 2);
		
		SimpleStaticRegistry<String, Integer> registry = new SimpleStaticRegistry<>(map);
		
		Assertions.assertEquals(1, registry.get("one"));
		Assertions.assertEquals(2, registry.get("two"));
	}
	
	@Test
	public void getNonExistingKeyWithoutFactory()
	{
		Map<String, Integer> map = new HashMap<>();
		map.put("one", 1);
		
		SimpleStaticRegistry<String, Integer> registry = new SimpleStaticRegistry<>(map);
		
		Assertions.assertNull(registry.get("missing"));
	}
	
	@Test
	public void getNonExistingKeyWithFactory()
	{
		Map<String, Integer> map = new HashMap<>();
		map.put("one", 1);
		
		SimpleStaticRegistry<String, Integer> registry = new SimpleStaticRegistry<>(map, key -> "Key not found: " + key);
		
		IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> registry.get("missing"));
		Assertions.assertEquals("Key not found: missing", ex.getMessage());
	}
	
	@Test
	public void getExistingKeyWithFactory()
	{
		Map<String, Integer> map = new HashMap<>();
		map.put("one", 1);
		
		SimpleStaticRegistry<String, Integer> registry = new SimpleStaticRegistry<>(map, key -> "Key not found: " + key);
		
		Assertions.assertEquals(1, registry.get("one"));
	}
	
	@Test
	public void emptyRegistry()
	{
		Map<String, String> map = new HashMap<>();
		SimpleStaticRegistry<String, String> registry = new SimpleStaticRegistry<>(map);
		
		Assertions.assertNull(registry.get("any"));
	}
	
	@Test
	public void getWithNullValue()
	{
		Map<String, String> map = new HashMap<>();
		map.put("key", null);
		
		SimpleStaticRegistry<String, String> registry = new SimpleStaticRegistry<>(map);
		
		Assertions.assertNull(registry.get("key"));
	}
	
	@Test
	public void getWithNullValueAndFactory()
	{
		Map<String, String> map = new HashMap<>();
		map.put("key", null);
		
		SimpleStaticRegistry<String, String> registry = new SimpleStaticRegistry<>(map, k -> "not found");
		
		// When value is explicitly null in map and factory is present, it throws
		Assertions.assertThrows(IllegalArgumentException.class, () -> registry.get("key"));
	}
	
	@Test
	public void integerKeys()
	{
		Map<Integer, String> map = new HashMap<>();
		map.put(1, "one");
		map.put(2, "two");
		
		SimpleStaticRegistry<Integer, String> registry = new SimpleStaticRegistry<>(map);
		
		Assertions.assertEquals("one", registry.get(1));
		Assertions.assertEquals("two", registry.get(2));
		Assertions.assertNull(registry.get(3));
	}
	
	@Test
	public void implementsStaticRegistryInterface()
	{
		Map<String, Integer> map = new HashMap<>();
		StaticRegistry<String, Integer> registry = new SimpleStaticRegistry<>(map);
		Assertions.assertNotNull(registry);
	}
}
