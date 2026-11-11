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
import java.lang.ref.WeakReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WeakReferenceFactoryTest
{
	@Test
	public void getInstance()
	{
		ReferenceFactory factory1 = WeakReferenceFactory.getInstance();
		ReferenceFactory factory2 = WeakReferenceFactory.getInstance();
		Assertions.assertSame(factory1, factory2);
	}

	@Test
	public void createReference()
	{
		ReferenceFactory factory = WeakReferenceFactory.getInstance();
		String value = "test";
		Reference<String> reference = factory.createReference(value);
		Assertions.assertNotNull(reference);
		Assertions.assertTrue(reference instanceof WeakReference);
		Assertions.assertEquals("test", reference.get());
	}

	@Test
	public void createReferenceWithNull()
	{
		ReferenceFactory factory = WeakReferenceFactory.getInstance();
		Reference<String> reference = factory.createReference(null);
		Assertions.assertNotNull(reference);
		Assertions.assertNull(reference.get());
	}
}
