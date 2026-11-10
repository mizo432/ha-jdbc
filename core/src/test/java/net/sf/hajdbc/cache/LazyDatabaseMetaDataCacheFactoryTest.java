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
package net.sf.hajdbc.cache;

import net.sf.hajdbc.cache.lazy.LazyDatabaseMetaDataCache;
import net.sf.hajdbc.cache.lazy.LazyDatabaseMetaDataCacheFactory;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class LazyDatabaseMetaDataCacheFactoryTest
{
	@Test
	public void getId()
	{
		LazyDatabaseMetaDataCacheFactory factory = new LazyDatabaseMetaDataCacheFactory();
		assertThat(factory.getId()).isEqualTo("lazy");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void createCache()
	{
		LazyDatabaseMetaDataCacheFactory factory = new LazyDatabaseMetaDataCacheFactory();
		Object cache = factory.createCache(null);
		assertThat(cache).isInstanceOf(LazyDatabaseMetaDataCache.class);
	}
}
