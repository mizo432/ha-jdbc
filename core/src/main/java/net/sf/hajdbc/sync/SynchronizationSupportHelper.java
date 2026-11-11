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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;

/**
 * Helper for constraint batch operations in {@link SynchronizationSupportImpl}.
 * @author Paul Ferraro
 */
final class SynchronizationSupportHelper
{
	private static final Logger logger = LoggerFactory.getLogger(SynchronizationSupport.class);
	
	private SynchronizationSupportHelper()
	{
		// Utility class
	}
	
	/**
	 * Common pattern: get connection, set auto-commit, batch SQL, execute, restore auto-commit.
	 */
	static <Z, D extends Database<Z>> void executeBatch(SynchronizationContext<Z, D> context, Collection<String> sqlStatements) throws SQLException
	{
		Connection connection = context.getConnection(context.getTargetDatabase());
		boolean autoCommit = connection.getAutoCommit();
		try
		{
			connection.setAutoCommit(true);
			
			try (Statement statement = connection.createStatement())
			{
				for (String sql: sqlStatements)
				{
					logger.log(Level.DEBUG, sql);
					statement.addBatch(sql);
				}
				statement.executeBatch();
			}
		}
		finally
		{
			connection.setAutoCommit(autoCommit);
		}
	}
	
	/**
	 * Builds SQL statements for dropping foreign keys.
	 */
	static <Z, D extends Database<Z>> Collection<String> buildDropForeignKeySQL(Dialect dialect, Collection<TableProperties> tables) throws SQLException
	{
		return buildSQL(dialect, tables, table -> {
			Collection<String> result = new java.util.ArrayList<>();
			for (ForeignKeyConstraint constraint: table.getForeignKeyConstraints())
			{
				result.add(dialect.getDropForeignKeyConstraintSQL(constraint));
			}
			return result;
		});
	}
	
	/**
	 * Builds SQL statements for creating foreign keys.
	 */
	static <Z, D extends Database<Z>> Collection<String> buildCreateForeignKeySQL(Dialect dialect, Collection<TableProperties> tables) throws SQLException
	{
		return buildSQL(dialect, tables, table -> {
			Collection<String> result = new java.util.ArrayList<>();
			for (ForeignKeyConstraint constraint: table.getForeignKeyConstraints())
			{
				result.add(dialect.getCreateForeignKeyConstraintSQL(constraint));
			}
			return result;
		});
	}
	
	/**
	 * Builds SQL statements for dropping unique constraints.
	 */
	static <Z, D extends Database<Z>> Collection<String> buildDropUniqueConstraintSQL(Dialect dialect, Collection<TableProperties> tables) throws SQLException
	{
		return buildSQL(dialect, tables, table -> {
			Collection<String> result = new java.util.ArrayList<>();
			for (UniqueConstraint constraint: table.getUniqueConstraints())
			{
				result.add(dialect.getDropUniqueConstraintSQL(constraint));
			}
			return result;
		});
	}
	
	/**
	 * Builds SQL statements for creating unique constraints.
	 */
	static <Z, D extends Database<Z>> Collection<String> buildCreateUniqueConstraintSQL(Dialect dialect, Collection<TableProperties> tables) throws SQLException
	{
		return buildSQL(dialect, tables, table -> {
			Collection<String> result = new java.util.ArrayList<>();
			for (UniqueConstraint constraint: table.getUniqueConstraints())
			{
				result.add(dialect.getCreateUniqueConstraintSQL(constraint));
			}
			return result;
		});
	}
	
	private interface TableSQLBuilder
	{
		Collection<String> build(TableProperties table) throws SQLException;
	}
	
	private static Collection<String> buildSQL(Dialect dialect, Collection<TableProperties> tables, TableSQLBuilder builder) throws SQLException
	{
		Collection<String> result = new java.util.ArrayList<>();
		for (TableProperties table: tables)
		{
			result.addAll(builder.build(table));
		}
		return result;
	}
}
