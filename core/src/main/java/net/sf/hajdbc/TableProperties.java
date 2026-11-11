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
 * Table properties interface.
 * @author Paul Ferraro
 */
public interface TableProperties extends Named<QualifiedName, TableProperties>
{
	/**
	 * Returns a collection of column names.
	 * @return collection of column names
	 * @throws SQLException if an error occurs
	 */
	@NonNull Collection<String> getColumns() throws SQLException;
	
	/**
	 * Returns the properties of the specified column.
	 * @param column column name
	 * @return column properties
	 * @throws SQLException if an error occurs
	 */
	@NonNull ColumnProperties getColumnProperties(@NonNull String column) throws SQLException;
	
	/**
	 * Returns the primary key constraint.
	 * @return primary key constraint
	 * @throws SQLException if an error occurs
	 */
	@Nullable UniqueConstraint getPrimaryKey() throws SQLException;
	
	/**
	 * Returns a collection of foreign key constraints.
	 * @return collection of foreign key constraints
	 * @throws SQLException if an error occurs
	 */
	@NonNull Collection<ForeignKeyConstraint> getForeignKeyConstraints() throws SQLException;
	
	/**
	 * Returns a collection of unique constraints.
	 * @return collection of unique constraints
	 * @throws SQLException if an error occurs
	 */
	@NonNull Collection<UniqueConstraint> getUniqueConstraints() throws SQLException;
	
	/**
	 * Returns a collection of identity column names.
	 * @return collection of identity column names
	 * @throws SQLException if an error occurs
	 */
	@NonNull Collection<String> getIdentityColumns() throws SQLException;
}
