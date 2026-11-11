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

import net.sf.hajdbc.configuration.PropertiesBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Database builder interface.
 * @author Paul Ferraro
 * @param <Z> connection source (e.g., Driver, DataSource, etc.)
 * @param <D> database implementation
 */
public interface DatabaseBuilder<Z, D extends Database<Z>> extends PropertiesBuilder<D>
{
	/**
	 * Maximum size of database ID
	 */
	int ID_MAX_SIZE = 64;

	/**
	 * Sets the connection source.
	 * @param connectionSource the connection source
	 * @return this builder
	 */
	@NonNull DatabaseBuilder<Z, D> connectionSource(@NonNull Z connectionSource);

	/**
	 * Sets the database location.
	 * @param location the location
	 * @return this builder
	 */
	@NonNull DatabaseBuilder<Z, D> location(@NonNull String location);

	/**
	 * Sets the credentials.
	 * @param user the username
	 * @param password the password
	 * @return this builder
	 */
	@NonNull DatabaseBuilder<Z, D> credentials(@Nullable String user, @Nullable String password);

	/**
	 * Sets the weight.
	 * @param weight the weight
	 * @return this builder
	 */
	@NonNull DatabaseBuilder<Z, D> weight(int weight);

	/**
	 * Sets the locality.
	 * @param locality the locality
	 * @return this builder
	 */
	@NonNull DatabaseBuilder<Z, D> locality(@NonNull Locality locality);
}
