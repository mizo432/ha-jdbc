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
import java.util.Collection;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Database properties interface.
 * @author Paul Ferraro
 */
public interface DatabaseProperties
{
	/**
	 * Returns a collection of table properties.
	 * @return collection of table properties
	 * @throws SQLException if an error occurs
	 */
	@NonNull Collection<TableProperties> getTables() throws SQLException;

	/**
	 * Returns whether SELECT FOR UPDATE is supported.
	 * @return true if supported
	 * @throws SQLException if an error occurs
	 */
	boolean supportsSelectForUpdate() throws SQLException;
	
	/**
	 * Returns whether locators update copies.
	 * @return true if locators update copies
	 * @throws SQLException if an error occurs
	 */
	boolean locatorsUpdateCopy() throws SQLException;
	
	/**
	 * Finds properties for the given table name.
	 * @param table table name
	 * @return table properties
	 * @throws SQLException if an error occurs
	 */
	@Nullable TableProperties findTable(@NonNull String table) throws SQLException;
	
	/**
	 * Returns a collection of sequence properties.
	 * @return collection of sequence properties
	 * @throws SQLException if an error occurs
	 */
	@NonNull Collection<SequenceProperties> getSequences() throws SQLException;
	
	/**
	 * Finds properties for the given sequence name.
	 * @param sequence sequence name
	 * @return sequence properties
	 * @throws SQLException if an error occurs
	 */
	@Nullable SequenceProperties findSequence(@NonNull String sequence) throws SQLException;
	
	/**
	 * Finds a type name matching the given precision and types.
	 * @param precision precision
	 * @param types list of types
	 * @return type name
	 * @throws SQLException if an error occurs
	 */
	@Nullable String findType(int precision, int... types) throws SQLException;
}
