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

import java.io.Serializable;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.Identifiable;

/**
 * Factory for creating {@link DatabaseMetaDataCache} instances.
 * <p>
 * This factory is a core component of the HA-JDBC architecture, registered via
 * {@link net.sf.hajdbc.DatabaseClusterConfiguration#getDatabaseMetaDataCacheFactory()}.
 * The cache stores database metadata (tables, columns, constraints) to avoid repeated
 * calls to {@link java.sql.DatabaseMetaData}. Several implementations are available:
 * eager (loads all metadata at startup), lazy (loads on first access), and simple (no caching).
 * </p>
 *
 * @see DatabaseMetaDataCache
 * @see net.sf.hajdbc.DatabaseClusterConfiguration#getDatabaseMetaDataCacheFactory()
 * @see net.sf.hajdbc.cache.eager.EagerDatabaseMetaDataCacheFactory
 * @see net.sf.hajdbc.cache.lazy.LazyDatabaseMetaDataCacheFactory
 */
public interface DatabaseMetaDataCacheFactory extends Identifiable, Serializable
{
	/**
	 * Creates a new metadata cache for the specified database cluster.
	 * @param cluster the database cluster
	 * @return new metadata cache
	 */
	<Z, D extends Database<Z>> DatabaseMetaDataCache<Z, D> createCache(DatabaseCluster<Z, D> cluster);
}
