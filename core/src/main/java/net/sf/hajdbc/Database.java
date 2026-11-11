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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import net.sf.hajdbc.codec.Decoder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * @author  Paul Ferraro
 * @param <Z> connection source (e.g. Driver, DataSource, etc.)
 */
public interface Database<Z> extends Comparable<Database<Z>>
{
	/**
	 * Returns the unique identifier for this database.
	 * @return database id
	 */
	@NonNull String getId();

	/**
	 * Returns the connection source (e.g., Driver, DataSource) for this database.
	 * @return connection source
	 */
	@NonNull Z getConnectionSource();

	/**
	 * Returns the credentials for connecting to this database.
	 * @return credentials
	 */
	@NonNull Credentials getCredentials();

	/**
	 * Returns the weight of this database for load balancing.
	 * @return weight (1 or greater)
	 */
	int getWeight();

	/**
	 * Sets the weight of this database for load balancing.
	 * @param weight weight (1 or greater)
	 */
	void setWeight(int weight);

	/**
	 * Returns the locality (local or remote) of this database.
	 * @return locality
	 */
	@NonNull Locality getLocality();

	/**
	 * Connects to this database using the specified decoder.
	 * @param decoder codec decoder
	 * @return database connection
	 * @throws SQLException if connection fails
	 */
	@NonNull Connection connect(@NonNull Decoder decoder) throws SQLException;

	/**
	 * Returns the location string for this database.
	 * @return location
	 */
	@NonNull String getLocation();

	/**
	 * Returns the properties for this database.
	 * @return properties
	 */
	@NonNull Properties getProperties();
}
