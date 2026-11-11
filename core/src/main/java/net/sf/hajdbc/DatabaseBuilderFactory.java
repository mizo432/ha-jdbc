/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2014  Paul Ferraro
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

import org.jspecify.annotations.NonNull;

/**
 * Factory for creating {@link DatabaseBuilder} instances.
 * <p>
 * This factory is used during configuration to create database builders for each database
 * in the cluster. Each builder constructs a single {@link Database} instance with its
 * connection source, location, properties, and credentials. The builder pattern allows
 * programmatic configuration of databases before they are added to the cluster.
 * </p>
 * 
 * @param <Z> database connection source
 * @param <D> database descriptor
 * @param <B> database builder
 * @see DatabaseBuilder
 * @see DatabaseClusterConfigurationBuilder
 */
public interface DatabaseBuilderFactory<Z, D extends Database<Z>, B extends DatabaseBuilder<Z, D>>
{
	/**
	 * Creates a new database builder with the specified identifier.
	 * @param id unique database identifier within the cluster
	 * @return new database builder
	 */
	@NonNull B createBuilder(@NonNull String id);
}
