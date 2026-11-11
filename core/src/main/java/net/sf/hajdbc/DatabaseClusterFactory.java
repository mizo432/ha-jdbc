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
package net.sf.hajdbc;

import java.sql.SQLException;

import org.jspecify.annotations.NonNull;


/**
 * Database cluster factory interface.
 * @author Paul Ferraro
 * @param <Z> connection source (e.g., Driver, DataSource, etc.)
 * @param <D> database implementation
 */
public interface DatabaseClusterFactory<Z, D extends Database<Z>>
{
	/**
	 * Creates a database cluster.
	 * @param id cluster identifier
	 * @param factory configuration factory
	 * @param builder configuration builder
	 * @return database cluster
	 * @throws SQLException if cluster creation fails
	 */
	<B extends DatabaseBuilder<Z, D>> @NonNull DatabaseCluster<Z, D> createDatabaseCluster(@NonNull String id, @NonNull DatabaseClusterConfigurationFactory<Z, D> factory, @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> builder) throws SQLException;
}
