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
package net.sf.hajdbc.util.ref;

import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReferenceMapTest
{
	private ReferenceMap<String, String> createMap(ReferenceFactory factory)
	{
		return new ReferenceMap<>(new HashMap<>(), factory);
	}

	@Test
	public void isEmptyOnNewMap()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		Assertions.assertTrue(map.isEmpty());
		Assertions.assertEquals(0, map.size());
	}

	@Test
	public void putAndGet()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		Assertions.assertEquals("value1", map.get("key1"));
		Assertions.assertFalse(map.isEmpty());
		Assertions.assertEquals(1, map.size());
	}

	@Test
	public void putReturnsPreviousValue()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		Assertions.assertNull(map.put("key1", "value1"));
		Assertions.assertEquals("value1", map.put("key1", "value2"));
		Assertions.assertEquals("value2", map.get("key1"));
	}

	@Test
	public void remove()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		Assertions.assertEquals("value1", map.remove("key1"));
		Assertions.assertNull(map.get("key1"));
		Assertions.assertTrue(map.isEmpty());
	}

	@Test
	public void removeNonExistentKey()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		Assertions.assertNull(map.remove("nonexistent"));
	}

	@Test
	public void containsKey()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		Assertions.assertTrue(map.containsKey("key1"));
		Assertions.assertFalse(map.containsKey("key2"));
	}

	@Test
	public void containsValue()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		Assertions.assertTrue(map.containsValue("value1"));
		Assertions.assertFalse(map.containsValue("value2"));
	}

	@Test
	public void keySet()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		map.put("key2", "value2");
		Set<String> keys = map.keySet();
		Assertions.assertEquals(2, keys.size());
		Assertions.assertTrue(keys.contains("key1"));
		Assertions.assertTrue(keys.contains("key2"));
	}

	@Test
	public void values()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		map.put("key2", "value2");
		Set<String> values = new HashSet<>();
		for (String v : map.values()) {
			values.add(v);
		}
		Assertions.assertTrue(values.contains("value1"));
		Assertions.assertTrue(values.contains("value2"));
	}

	@Test
	public void entrySet()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		map.put("key2", "value2");
		Set<Map.Entry<String, String>> entries = map.entrySet();
		Assertions.assertEquals(2, entries.size());
		boolean foundKey1 = false;
		boolean foundKey2 = false;
		for (Map.Entry<String, String> entry : entries) {
			if ("key1".equals(entry.getKey())) {
				Assertions.assertEquals("value1", entry.getValue());
				foundKey1 = true;
			}
			if ("key2".equals(entry.getKey())) {
				Assertions.assertEquals("value2", entry.getValue());
				foundKey2 = true;
			}
		}
		Assertions.assertTrue(foundKey1);
		Assertions.assertTrue(foundKey2);
	}

	@Test
	public void entrySetSetValue()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if ("key1".equals(entry.getKey())) {
				Assertions.assertEquals("value1", entry.setValue("newValue"));
			}
		}
		Assertions.assertEquals("newValue", map.get("key1"));
	}

	@Test
	public void entrySetAddThrowsUnsupportedOperationException()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			map.entrySet().add(Map.entry("key2", "value2"));
		});
	}

	@Test
	public void clear()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.clear();
		Assertions.assertTrue(map.isEmpty());
		Assertions.assertEquals(0, map.size());
	}

	@Test
	public void putAll()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		Map<String, String> source = new HashMap<>();
		source.put("key1", "value1");
		source.put("key2", "value2");
		map.putAll(source);
		Assertions.assertEquals(2, map.size());
		Assertions.assertEquals("value1", map.get("key1"));
		Assertions.assertEquals("value2", map.get("key2"));
	}

	@Test
	public void softReferenceMap()
	{
		ReferenceMap<String, String> map = createMap(SoftReferenceFactory.getInstance());
		map.put("key1", "value1");
		Assertions.assertEquals("value1", map.get("key1"));
	}

	@Test
	public void weakReferenceGarbageCollected()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		String value = new String("value1");
		map.put("key1", value);
		Assertions.assertEquals("value1", map.get("key1"));
		// Clear the strong reference and force GC
		value = null;
		System.gc();
		// The value may be garbage collected, so get may return null
		// This is expected behavior for weak references
	}

	@Test
	public void valuesContains()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		map.put("key2", "value2");
		Assertions.assertTrue(map.values().contains("value1"));
		Assertions.assertFalse(map.values().contains("value3"));
	}

	@Test
	public void valuesIteratorRemove()
	{
		ReferenceMap<String, String> map = createMap(WeakReferenceFactory.getInstance());
		map.put("key1", "value1");
		map.put("key2", "value2");
		java.util.Iterator<String> it = map.values().iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
		Assertions.assertTrue(map.isEmpty());
	}
}
