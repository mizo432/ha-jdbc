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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceLoadersTest
{
	@Test
	public void findServiceReturnsNullForUnknownService()
	{
		// No implementation registered for this interface
		ServiceLoadersTest.UnknownService service = ServiceLoaders.findService(UnknownService.class);
		Assertions.assertNull(service);
	}
	
	@Test
	public void findRequiredServiceThrowsForUnknownService()
	{
		Assertions.assertThrows(IllegalStateException.class, 
			() -> ServiceLoaders.findRequiredService(UnknownService.class));
	}
	
	@Test
	public void findServiceWithMatcherReturnsNullForUnknownService()
	{
		ServiceLoadersTest.UnknownService service = ServiceLoaders.findService(
			UnknownService.class, s -> true);
		Assertions.assertNull(service);
	}
	
	@Test
	public void findRequiredServiceWithMatcherThrowsForUnknownService()
	{
		Assertions.assertThrows(IllegalArgumentException.class, 
			() -> ServiceLoaders.findRequiredService(UnknownService.class, s -> true));
	}
	
	@Test
	public void findServiceFindsExistingService()
	{
		// JDKLogger is registered via ServiceLoader in the project
		net.sf.hajdbc.logging.LoggingProvider provider = ServiceLoaders.findService(
			net.sf.hajdbc.logging.LoggingProvider.class);
		// Should find the JDK logging provider
		Assertions.assertNotNull(provider);
	}
	
	public interface UnknownService
	{
		String getName();
	}
}
