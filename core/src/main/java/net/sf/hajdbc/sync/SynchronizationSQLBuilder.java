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
package net.sf.hajdbc.sync;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.util.Strings;

/**
 * Utility class for generating common SQL statements used in synchronization strategies.
 * Extracts duplicated SQL generation logic from {@link DifferentialSynchronizationStrategy}
 * and {@link FullSynchronizationStrategy}.
 * @author Paul Ferraro
 */
final class SynchronizationSQLBuilder
{
	private SynchronizationSQLBuilder()
	{
		// Utility class
	}
	
	/**
	 * Builds a SELECT statement for the specified table and columns.
	 * @param columns the columns to select
	 * @param tableName the normalized table name
	 * @return the SELECT SQL statement
	 */
	static String buildSelectSQL(Collection<String> columns, String tableName)
	{
		return String.format("SELECT %s FROM %s", Strings.join(columns, Strings.PADDED_COMMA), tableName); //$NON-NLS-1$
	}
	
	/**
	 * Builds a SELECT statement with ORDER BY clause for primary key columns.
	 * @param columns the columns to select
	 * @param tableName the normalized table name
	 * @param primaryKeyColumns the primary key columns for ordering
	 * @return the SELECT SQL statement with ORDER BY
	 */
	static String buildSelectOrderBySQL(Collection<String> columns, String tableName, Collection<String> primaryKeyColumns)
	{
		return String.format("SELECT %s FROM %s ORDER BY %s", Strings.join(columns, Strings.PADDED_COMMA), tableName, Strings.join(primaryKeyColumns, Strings.PADDED_COMMA)); //$NON-NLS-1$
	}
	
	/**
	 * Builds an INSERT statement for the specified table and columns.
	 * @param tableName the normalized table name
	 * @param columns the columns to insert
	 * @return the INSERT SQL statement
	 */
	static String buildInsertSQL(String tableName, Collection<String> columns)
	{
		return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, Strings.join(columns, Strings.PADDED_COMMA), Strings.join(Collections.nCopies(columns.size(), Strings.QUESTION), Strings.PADDED_COMMA)); //$NON-NLS-1$
	}
	
	/**
	 * Builds a DELETE statement with a WHERE clause for primary key columns.
	 * @param tableName the normalized table name
	 * @param primaryKeyColumns the primary key columns for the WHERE clause
	 * @return the DELETE SQL statement
	 */
	static String buildDeleteSQL(String tableName, Collection<String> primaryKeyColumns)
	{
		String whereClause = buildPrimaryKeyWhereClause(primaryKeyColumns);
		return String.format("DELETE FROM %s WHERE %s", tableName, whereClause); //$NON-NLS-1$
	}
	
	/**
	 * Builds an UPDATE statement for non-primary-key columns with a primary key WHERE clause.
	 * @param tableName the normalized table name
	 * @param nonPrimaryKeyColumns the columns to update
	 * @param primaryKeyColumns the primary key columns for the WHERE clause
	 * @return the UPDATE SQL statement
	 */
	static String buildUpdateSQL(String tableName, Collection<String> nonPrimaryKeyColumns, Collection<String> primaryKeyColumns)
	{
		String whereClause = buildPrimaryKeyWhereClause(primaryKeyColumns);
		return String.format("UPDATE %s SET %s = ? WHERE %s", tableName, Strings.join(nonPrimaryKeyColumns, " = ?, "), whereClause); //$NON-NLS-1$
	}
	
	/**
	 * Builds a SELECT statement to fetch non-primary-key columns by primary key.
	 * @param nonPrimaryKeyColumns the columns to select
	 * @param tableName the normalized table name
	 * @param primaryKeyColumns the primary key columns for the WHERE clause
	 * @return the SELECT SQL statement
	 */
	static String buildSelectByPKSQL(Collection<String> nonPrimaryKeyColumns, String tableName, Collection<String> primaryKeyColumns)
	{
		String whereClause = buildPrimaryKeyWhereClause(primaryKeyColumns);
		return String.format("SELECT %s FROM %s WHERE %s", Strings.join(nonPrimaryKeyColumns, Strings.PADDED_COMMA), tableName, whereClause); //$NON-NLS-1$
	}
	
	/**
	 * Builds a WHERE clause for primary key columns with parameter placeholders.
	 * E.g., for columns ["id", "name"], returns "id = ? AND name = ?".
	 * @param primaryKeyColumns the primary key columns
	 * @return the WHERE clause string
	 */
	static String buildPrimaryKeyWhereClause(Collection<String> primaryKeyColumns)
	{
		return Strings.join(new StringBuilder(), primaryKeyColumns, " = ? AND ").append(" = ?").toString(); //$NON-NLS-1$
	}
	
	/**
	 * Sets the column values from a ResultSet into a PreparedStatement for an insert operation.
	 * Handles null values appropriately.
	 * @param context the synchronization context
	 * @param table the table properties
	 * @param resultSet the result set to read from
	 * @param statement the prepared statement to write to
	 * @param columns the ordered list of columns
	 * @throws java.sql.SQLException if a database access error occurs
	 */
	static void setColumnValues(
			SynchronizationContext<?, ?> context, TableProperties table,
			ResultSet resultSet, PreparedStatement statement,
			List<String> columns) throws java.sql.SQLException
	{
		for (int i = 0; i < columns.size(); i++)
		{
			int index = i + 1;
			int type = context.getDialect().getColumnType(table.getColumnProperties(columns.get(i)));
			Object object = context.getSynchronizationSupport().getObject(resultSet, index, type);
			
			if (resultSet.wasNull())
			{
				statement.setNull(index, type);
			}
			else
			{
				statement.setObject(index, object, type);
			}
		}
	}
	
	/**
	 * Executes a batch of statements if there are remaining unexecuted items.
	 * @param statement the prepared statement
	 * @param count the total number of items added to the batch
	 * @param maxBatchSize the maximum batch size
	 * @throws java.sql.SQLException if a database access error occurs
	 */
	static void executeRemainingBatch(PreparedStatement statement, int count, int maxBatchSize) throws java.sql.SQLException
	{
		if ((count % maxBatchSize) > 0)
		{
			statement.executeBatch();
		}
	}
	
	/**
	 * Adds a batch entry and executes the batch if the maximum batch size is reached.
	 * @param statement the prepared statement
	 * @param count the current count of items added
	 * @param maxBatchSize the maximum batch size
	 * @return the incremented count
	 * @throws java.sql.SQLException if a database access error occurs
	 */
	static int addBatchAndExecuteIfFull(PreparedStatement statement, int count, int maxBatchSize) throws java.sql.SQLException
	{
		statement.addBatch();
		count += 1;
		
		if ((count % maxBatchSize) == 0)
		{
			statement.executeBatch();
			statement.clearBatch();
		}
		return count;
	}
}

