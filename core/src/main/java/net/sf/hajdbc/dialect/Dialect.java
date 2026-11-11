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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAException;

import net.sf.hajdbc.ColumnProperties;
import net.sf.hajdbc.ColumnPropertiesFactory;
import net.sf.hajdbc.Database;
import net.sf.hajdbc.DumpRestoreSupport;
import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.ForeignKeyConstraintFactory;
import net.sf.hajdbc.IdentifierNormalizer;
import net.sf.hajdbc.IdentityColumnSupport;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.QualifiedNameFactory;
import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.TriggerSupport;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.UniqueConstraintFactory;
import net.sf.hajdbc.codec.Decoder;


/**
 * Encapsulates database vendor-specific SQL syntax.
 * 
 * @author  Paul Ferraro
 * @since   1.1
 */
public interface Dialect
{
	/**
	 * Returns the SQL statement used to truncate a table.
	 * @param properties table metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	String getTruncateTableSQL(TableProperties properties) throws SQLException;
	
	/**
	 * Returns the SQL statement used to create a foreign key constraint.
	 * @param constraint foreign key constraint metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	String getCreateForeignKeyConstraintSQL(ForeignKeyConstraint constraint) throws SQLException;

	/**
	 * Returns the SQL statement used to drop a foreign key constraint.
	 * @param constraint foreign key constraint metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	String getDropForeignKeyConstraintSQL(ForeignKeyConstraint constraint) throws SQLException;

	/**
	 * Returns the SQL statement used to create a unique constraint.
	 * @param constraint unique constraint metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	String getCreateUniqueConstraintSQL(UniqueConstraint constraint) throws SQLException;

	/**
	 * Returns the SQL statement used to drop a unique constraint.
	 * @param constraint unique constraint metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	String getDropUniqueConstraintSQL(UniqueConstraint constraint) throws SQLException;
	
	/**
	 * Determines if the given SQL is a SELECT ... FOR UPDATE statement.
	 * @param sql SQL statement
	 * @return true if SELECT ... FOR UPDATE, false otherwise
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	boolean isSelectForUpdate(String sql) throws SQLException;
	
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
	 * Replaces the non-deterministic CURRENT_DATE function with a deterministic static value.
	 * @param sql SQL statement
	 * @param date the date to replace with
	 * @return an equivalent deterministic SQL statement
	 * @throws SQLException
	 * @since 2.0.2
	 */
	String evaluateCurrentDate(String sql, java.sql.Date date);
	
	/**
	 * Replaces the non-deterministic CURRENT_TIME function with a deterministic static value.
	 * @param sql SQL statement
	 * @param time the time to replace with
	 * @return an equivalent deterministic SQL statement
	 * @throws SQLException
	 * @since 2.0.2
	 */
	String evaluateCurrentTime(String sql, java.sql.Time time);
	
	/**
	 * Replaces the non-deterministic CURRENT_TIMESTAMP function with a deterministic static value.
	 * @param sql SQL statement
	 * @param timestamp the timestamp to replace with
	 * @return an equivalent deterministic SQL statement
	 * @throws SQLException
	 * @since 2.0.2
	 */
	String evaluateCurrentTimestamp(String sql, java.sql.Timestamp timestamp);
	
	/**
	 * Replaces the non-deterministic RAND() function with a deterministic static value.
	 * @param sql SQL statement
	 * @return an equivalent deterministic SQL statement
	 * @throws SQLException
	 * @since 2.0.2
	 */
	String evaluateRand(String sql);
	
	/**
	 * Determines if the given exception indicates a fatal error.
	 * @param e the exception
	 * @return true if the exception indicates a fatal error, false otherwise
	 */
	boolean indicatesFailure(SQLException e);
	
	/**
	 * Determines if the given exception indicates a fatal error.
	 * @param e the exception
	 * @return true if the exception indicates a fatal error, false otherwise
	 */
	boolean indicatesFailure(XAException e);

	SequenceSupport getSequenceSupport();
	
	IdentityColumnSupport getIdentityColumnSupport();
	
	DumpRestoreSupport getDumpRestoreSupport();
	
	TriggerSupport getTriggerSupport();
	
	String getCreateSchemaSQL(String schema);
	
	String getDropSchemaSQL(String schema);
	
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

	IdentifierNormalizer createIdentifierNormalizer(DatabaseMetaData metaData) throws SQLException;
	
	QualifiedNameFactory createQualifiedNameFactory(DatabaseMetaData metaData, IdentifierNormalizer normalizer) throws SQLException;
	
	ColumnPropertiesFactory createColumnPropertiesFactory(IdentifierNormalizer normalizer);
	
	ForeignKeyConstraintFactory createForeignKeyConstraintFactory(QualifiedNameFactory factory);
	
	UniqueConstraintFactory createUniqueConstraintFactory(IdentifierNormalizer normalizer);
	
	boolean isValid(Connection connection) throws SQLException;
	
	<Z, D extends Database<Z>> ConnectionProperties getConnectionProperties(D database, Decoder decoder) throws SQLException;
}
