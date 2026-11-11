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
package net.sf.hajdbc.dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sf.hajdbc.ColumnProperties;
import net.sf.hajdbc.ColumnPropertiesFactory;
import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.ForeignKeyConstraintFactory;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.QualifiedNameFactory;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.UniqueConstraintFactory;

/**
 * Accesses database metadata through DatabaseMetaData for vendor-specific schemas.
 * This interface isolates metadata access concerns from other dialect responsibilities,
 * following the Interface Segregation Principle.
 *
 * @author Paul Ferraro
 * @since 3.2
 */
public interface DialectMetadataContract
{
	/**
	 * Returns the data type of the specified column in the given schema and table.
	 * This method is used to fix JDBC driver type mapping quirks.
	 * @param properties column metadata
	 * @return the JDBC data type of this column
	 * @throws SQLException
	 */
	int getColumnType(ColumnProperties properties) throws SQLException;

	/**
	 * Returns the schema search path.
	 * @param metaData database metadata
	 * @return list of schema names
	 * @throws SQLException
	 * @since 2.0
	 */
	List<String> getDefaultSchemas(DatabaseMetaData metaData) throws SQLException;

	/**
	 * Returns all tables in this database mapped by schema.
	 * @param metaData DatabaseMetaData implementation
	 * @return map from schema name to collection of table names
	 * @throws SQLException if an error occurs accessing DatabaseMetaData
	 */
	Collection<QualifiedName> getTables(DatabaseMetaData metaData, QualifiedNameFactory factory) throws SQLException;

	/**
	 * Returns the columns of the specified table.
	 * @param metaData DatabaseMetaData implementation
	 * @param table schema-qualified table name
	 * @return map from column name to column properties
	 * @throws SQLException if an error occurs accessing DatabaseMetaData
	 */
	Map<String, ColumnProperties> getColumns(DatabaseMetaData metaData, QualifiedName table, ColumnPropertiesFactory factory) throws SQLException;

	/**
	 * Returns the primary key of the specified table.
	 * @param metaData DatabaseMetaData implementation
	 * @param table schema-qualified table name
	 * @return unique constraint
	 * @throws SQLException if an error occurs accessing DatabaseMetaData
	 */
	UniqueConstraint getPrimaryKey(DatabaseMetaData metaData, QualifiedName table, UniqueConstraintFactory factory) throws SQLException;

	/**
	 * Returns the foreign key constraints of the specified table.
	 * @param metaData DatabaseMetaData implementation
	 * @param table schema-qualified table name
	 * @return collection of foreign key constraints
	 * @throws SQLException if an error occurs accessing DatabaseMetaData
	 */
	Collection<ForeignKeyConstraint> getForeignKeyConstraints(DatabaseMetaData metaData, QualifiedName table, ForeignKeyConstraintFactory factory) throws SQLException;

	/**
	 * Returns the unique constraints of the specified table - excluding the table's primary key.
	 * @param metaData DatabaseMetaData implementation
	 * @param table qualified table name
	 * @param primaryKey the primary key of this table
	 * @return collection of unique constraints
	 * @throws SQLException if an error occurs accessing DatabaseMetaData
	 */
	Collection<UniqueConstraint> getUniqueConstraints(DatabaseMetaData metaData, QualifiedName table, UniqueConstraint primaryKey, UniqueConstraintFactory factory) throws SQLException;

	/**
	 * Identifies ID columns from the given collection of columns.
	 * @param columns table columns
	 * @return collection of column names
	 * @throws SQLException
	 */
	Collection<String> getIdentityColumns(Collection<ColumnProperties> columns) throws SQLException;

	/**
	 * Returns the mapping from standard JDBC types to native types.
	 * @param metaData database metadata
	 * @return map of JDBC types
	 * @throws SQLException
	 */
	Map<Integer, Map.Entry<String, Integer>> getTypes(DatabaseMetaData metaData) throws SQLException;
}

