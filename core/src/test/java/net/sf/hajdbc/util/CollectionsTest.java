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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CollectionsTest
{
	@Test
	public void emptySortedSet()
	{
		verify(Collections.<Integer>emptySortedSet(), new TreeSet<Integer>(), 0, 1);
	}
	
	@Test
	public void singletonSortedSet()
	{
		SortedSet<Integer> set = new TreeSet<>();
		set.add(1);
		
		verify(Collections.<Integer>singletonSortedSet(1), set, 0, 1);
	}
	
	private static <T> void verify(SortedSet<T> immutableSet, SortedSet<T> mutableSet, T value1, T value2)
	{
		try
		{
			immutableSet.add(value1);
			Assertions.fail();
		}
		catch (UnsupportedOperationException e)
		{
		}

		try
		{
			immutableSet.addAll(java.util.Collections.singleton(value1));
			Assertions.fail();
		}
		catch (UnsupportedOperationException e)
		{
		}

		try
		{
			immutableSet.clear();
			
			Assertions.assertTrue(mutableSet.isEmpty());
		}
		catch (UnsupportedOperationException e)
		{
			Assertions.assertFalse(mutableSet.isEmpty());
		}
		
		Assertions.assertEquals(immutableSet.contains(value1), mutableSet.contains(value1));
		Assertions.assertEquals(immutableSet.contains(value2), mutableSet.contains(value2));
		
		Assertions.assertEquals(immutableSet.containsAll(java.util.Collections.emptySet()), mutableSet.containsAll(java.util.Collections.emptySet()));
		Assertions.assertEquals(immutableSet.containsAll(java.util.Collections.singleton(0)), mutableSet.containsAll(java.util.Collections.singleton(0)));
		Assertions.assertEquals(immutableSet.containsAll(java.util.Collections.singleton(1)), mutableSet.containsAll(java.util.Collections.singleton(1)));
		
		Iterator<T> iterator = immutableSet.iterator();

		if (!mutableSet.isEmpty())
		{
			Assertions.assertTrue(iterator.hasNext());
			
			Assertions.assertEquals(iterator.next(), mutableSet.iterator().next());			
		}

		Assertions.assertFalse(iterator.hasNext());
		
		try
		{
			iterator.next();
			Assertions.fail();
		}
		catch (NoSuchElementException e)
		{
		}
		
		Assertions.assertEquals(immutableSet.isEmpty(), mutableSet.isEmpty());
		
		try
		{
			immutableSet.remove(value1);
		}
		catch (UnsupportedOperationException e)
		{
		}
		
		try
		{
			immutableSet.removeAll(java.util.Collections.singleton(value1));
		}
		catch (UnsupportedOperationException e)
		{
		}
		
		try
		{
			immutableSet.retainAll(java.util.Collections.singleton(value1));
		}
		catch (UnsupportedOperationException e)
		{
		}
		
		Assertions.assertEquals(immutableSet.size(), mutableSet.size());
		
		Assertions.assertArrayEquals(immutableSet.toArray(), mutableSet.toArray());
		
		Assertions.assertSame(immutableSet.comparator(), mutableSet.comparator());
		
		if (!mutableSet.isEmpty())
		{
			Assertions.assertEquals(immutableSet.first(), mutableSet.first());
			Assertions.assertEquals(immutableSet.last(), mutableSet.last());
		}
		else
		{
			try
			{
				immutableSet.first();
				Assertions.fail();
			}
			catch (NoSuchElementException e)
			{
			}
			
			try
			{
				immutableSet.last();
				Assertions.fail();
			}
			catch (NoSuchElementException e)
			{
			}
		}
		
		Assertions.assertEquals(immutableSet.headSet(value1), mutableSet.headSet(value1));
		Assertions.assertEquals(immutableSet.headSet(value2), mutableSet.headSet(value2));

		Assertions.assertEquals(immutableSet.subSet(value1, value2), mutableSet.subSet(value1, value2));
		
		Assertions.assertEquals(immutableSet.tailSet(value1), mutableSet.tailSet(value1));
		Assertions.assertEquals(immutableSet.tailSet(value2), mutableSet.tailSet(value2));
	}
	
	@Test
	public void emptySortedMap()
	{
		verify(Collections.<Integer, String>emptySortedMap(), new TreeMap<Integer, String>(), 0, 1, "");
	}
	
	@Test
	public void singletonSortedMap()
	{
		SortedMap<Integer, String> map = new TreeMap<>();
		map.put(1, "");
		
		verify(Collections.<Integer, String>singletonSortedMap(1, ""), map, 0, 1, "");
	}
	
	private static <K, V> void verify(SortedMap<K, V> immutableMap, SortedMap<K, V> mutableMap, K key1, K key2, V value)
	{
		try
		{
			immutableMap.clear();
			
			Assertions.assertTrue(mutableMap.isEmpty());
		}
		catch (UnsupportedOperationException e)
		{
			Assertions.assertFalse(mutableMap.isEmpty());
		}
		
		Assertions.assertEquals(immutableMap.containsKey(key1), mutableMap.containsKey(key1));
		Assertions.assertEquals(immutableMap.containsKey(key2), mutableMap.containsKey(key2));

		Assertions.assertEquals(immutableMap.containsValue(value), mutableMap.containsValue(value));
		Assertions.assertEquals(immutableMap.containsValue(null), mutableMap.containsValue(null));
		
		Assertions.assertEquals(immutableMap.entrySet(), mutableMap.entrySet());
		
		Assertions.assertEquals(immutableMap.get(key1), mutableMap.get(key1));
		Assertions.assertEquals(immutableMap.get(key2), mutableMap.get(key2));
		
		Assertions.assertEquals(immutableMap.isEmpty(), mutableMap.isEmpty());
		
		Assertions.assertEquals(immutableMap.keySet(), mutableMap.keySet());
		
		try
		{
			immutableMap.put(key1, value);
			Assertions.fail();
		}
		catch (UnsupportedOperationException e)
		{
		}

		try
		{
			immutableMap.putAll(java.util.Collections.singletonMap(key1, value));
			Assertions.fail();
		}
		catch (UnsupportedOperationException e)
		{
		}

		try
		{
			immutableMap.remove(key1);
		}
		catch (UnsupportedOperationException e)
		{
		}
		
		Assertions.assertEquals(immutableMap.size(), mutableMap.size());
		
		// Is it OK that this fails?
		// Assertions.assertEquals(immutableMap.values(), mutableMap.values());
		Assertions.assertEquals(immutableMap.values().size(), mutableMap.values().size());
		if (!mutableMap.isEmpty())
		{
			Assertions.assertEquals(immutableMap.values().iterator().next(), mutableMap.values().iterator().next());
		}
		
		Assertions.assertSame(immutableMap.comparator(), mutableMap.comparator());
		
		if (!mutableMap.isEmpty())
		{
			Assertions.assertEquals(immutableMap.firstKey(), mutableMap.firstKey());
			Assertions.assertEquals(immutableMap.lastKey(), mutableMap.lastKey());
		}
		else
		{
			try
			{
				immutableMap.firstKey();
				Assertions.fail();
			}
			catch (NoSuchElementException e)
			{
			}
			
			try
			{
				immutableMap.lastKey();
				Assertions.fail();
			}
			catch (NoSuchElementException e)
			{
			}
		}
		
		Assertions.assertEquals(immutableMap.headMap(key1), mutableMap.headMap(key1));
		Assertions.assertEquals(immutableMap.headMap(key2), mutableMap.headMap(key2));

		Assertions.assertEquals(immutableMap.subMap(key1, key2), mutableMap.subMap(key1, key2));
		
		Assertions.assertEquals(immutableMap.tailMap(key1), mutableMap.tailMap(key1));
		Assertions.assertEquals(immutableMap.tailMap(key2), mutableMap.tailMap(key2));
	}
}
