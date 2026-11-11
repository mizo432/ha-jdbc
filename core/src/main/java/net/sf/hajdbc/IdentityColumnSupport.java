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
import org.jspecify.annotations.Nullable;

/**
 * Identity column support interface.
 * @author Paul Ferraro
 */
public interface IdentityColumnSupport
{
	/**
	 * Parses the table name from an INSERT SQL statement that may contain identity columns.
	 * @param sql SQL statement
	 * @return table name, or null if the SQL statement is not an INSERT or this dialect does not support identity columns
	 * @throws SQLException if an error occurs
	 * @since 2.0
	 */
	@Nullable String parseInsertTable(@NonNull String sql) throws SQLException;
	
	/**
	 * Returns the SQL statement to reset the current value of an identity column.
	 * @param table table properties
	 * @param column column properties
	 * @param value value
	 * @return SQL statement
	 * @throws SQLException if an error occurs
	 * @since 2.0.2
	 */
	@NonNull String getAlterIdentityColumnSQL(@NonNull TableProperties table, @NonNull ColumnProperties column, long value) throws SQLException;
}
