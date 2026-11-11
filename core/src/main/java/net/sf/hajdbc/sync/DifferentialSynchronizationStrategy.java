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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.ExceptionType;
import net.sf.hajdbc.SynchronizationStrategy;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;
import net.sf.hajdbc.util.Objects;
import net.sf.hajdbc.util.Strings;

/**
 * Database-independent synchronization strategy that only updates differences between two databases.
 * This strategy is best used when there are <em>few</em> differences between the active database and the inactive database (i.e. barely out of sync).
 * The following algorithm is used:
 * <ol>
 *  <li>Drop the foreign keys on the inactive database (to avoid integrity constraint violations)</li>
 *  <li>For each database table:
 *   <ol>
 *    <li>Drop the unique constraints on the table (to avoid integrity constraint violations)</li>
 *    <li>Find the primary key(s) of the table</li>
 *    <li>Query all rows in the inactive database table, sorting by the primary key(s)</li>
 *    <li>Query all rows on the active database table</li>
 *    <li>For each row in table:
 *     <ol>
 *      <li>If primary key of the rows are the same, determine whether or not row needs to be updated</li>
 *      <li>Otherwise, determine whether row should be deleted, or a new row is to be inserted</li>
 *     </ol>
 *    </li>
 *    <li>Re-create the unique constraints on the table (to avoid integrity constraint violations)</li>
 *   </ol>
 *  </li>
 *  <li>Re-create the foreign keys on the inactive database</li>
 *  <li>Synchronize sequences</li>
 * </ol>
 * @author  Paul Ferraro
 */
public class DifferentialSynchronizationStrategy implements SynchronizationStrategy, TableSynchronizationStrategy
{
	private static final long serialVersionUID = -2785092229503649831L;

	static final Messages messages = MessagesFactory.getMessages();
	static final Logger logger = LoggerFactory.getLogger(DifferentialSynchronizationStrategy.class);

	private final SynchronizationStrategy strategy = new PerTableSynchronizationStrategy(this);
	private int fetchSize = 0;
	private int maxBatchSize = 100;
	private Pattern versionPattern = null;
	
	@Override
	public String getId()
	{
		return "diff";
	}

	@Override
	public <Z, D extends Database<Z>> void synchronize(SynchronizationContext<Z, D> context) throws SQLException
	{
		this.strategy.synchronize(context);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.SynchronizationStrategy#init(net.sf.hajdbc.DatabaseCluster)
	 */
	@Override
	public <Z, D extends Database<Z>> void init(DatabaseCluster<Z, D> cluster)
	{
		this.strategy.init(cluster);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.SynchronizationStrategy#destroy(net.sf.hajdbc.DatabaseCluster)
	 */
	@Override
	public <Z, D extends Database<Z>> void destroy(DatabaseCluster<Z, D> cluster)
	{
		this.strategy.destroy(cluster);
	}

	@Override
	public <Z, D extends Database<Z>> void dropConstraints(SynchronizationContext<Z, D> context) throws SQLException
	{
		SynchronizationSupport support = context.getSynchronizationSupport();
		support.dropForeignKeys();
		support.dropUniqueConstraints();
	}

	@Override
	public <Z, D extends Database<Z>> void restoreConstraints(SynchronizationContext<Z, D> context) throws SQLException
	{
		SynchronizationSupport support = context.getSynchronizationSupport();
		support.restoreUniqueConstraints();
		support.restoreForeignKeys();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.SynchronizationStrategy#synchronize(net.sf.hajdbc.sync.SynchronizationContext)
	 */
	@Override
	public <Z, D extends Database<Z>> void synchronize(SynchronizationContext<Z, D> context, TableProperties table) throws SQLException
	{
		String tableName = table.getName().getDMLName();
		
		UniqueConstraint primaryKey = table.getPrimaryKey();
		
		if (primaryKey == null)
		{
			throw new SQLException(messages.primaryKeyRequired(this, table));
		}
		
		List<String> primaryKeyColumns = primaryKey.getColumnList();
		
		Collection<String> columns = table.getColumns();
		
		List<String> nonPrimaryKeyColumns = new ArrayList<>(columns.size());
		List<String> versionColumns = new ArrayList<>(columns.size());
		
		for (String column: columns)
		{
			if (!primaryKeyColumns.contains(column))
			{
				// Try to find a version column
				if ((this.versionPattern != null) && this.versionPattern.matcher(column).matches())
				{
					versionColumns.add(column);
				}
				
				nonPrimaryKeyColumns.add(column);
			}
		}
		
		// List of columns for select statement - starting with primary key
		List<String> allColumns = new ArrayList<>(columns.size());
		allColumns.addAll(primaryKeyColumns);
		allColumns.addAll(nonPrimaryKeyColumns);

		List<String> selectColumns = allColumns;
		if (!versionColumns.isEmpty())
		{
			selectColumns = new ArrayList<>(primaryKeyColumns.size() + versionColumns.size());
			selectColumns.addAll(primaryKeyColumns);
			selectColumns.addAll(versionColumns);
		}
		
		// Retrieve table rows in primary key order
		final String selectSQL = String.format("SELECT %s FROM %s ORDER BY %s", Strings.join(selectColumns, Strings.PADDED_COMMA), tableName, Strings.join(primaryKeyColumns, Strings.PADDED_COMMA)); //$NON-NLS-1$
		String primaryKeyWhereClause = Strings.join(new StringBuilder(), primaryKeyColumns, " = ? AND ").append(" = ?").toString(); //$NON-NLS-1$
		String selectAllSQL = !versionColumns.isEmpty() ? String.format("SELECT %s FROM %s WHERE %s", Strings.join(nonPrimaryKeyColumns, Strings.PADDED_COMMA), tableName, primaryKeyWhereClause) : null;
		String deleteSQL = String.format("DELETE FROM %s WHERE %s", tableName, primaryKeyWhereClause);
		String insertSQL = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, Strings.join(allColumns, Strings.PADDED_COMMA), Strings.join(Collections.nCopies(allColumns.size(), Strings.QUESTION), Strings.PADDED_COMMA)); //$NON-NLS-1$
		String updateSQL = !nonPrimaryKeyColumns.isEmpty() ? String.format("UPDATE %s SET %s = ? WHERE %s", tableName, Strings.join(nonPrimaryKeyColumns, " = ?, "), primaryKeyWhereClause) : null;
		
		Connection targetConnection = context.getConnection(context.getTargetDatabase());
		Connection sourceConnection = context.getConnection(context.getSourceDatabase());
		
		try (final Statement targetStatement = targetConnection.createStatement())
		{
			targetStatement.setFetchSize(this.fetchSize);
			
			Callable<ResultSet> callable = new Callable<ResultSet>()
			{
				@Override
				public ResultSet call() throws SQLException
				{
					logger.log(Level.DEBUG, selectSQL);
					return targetStatement.executeQuery(selectSQL);
				}
			};
			
			Future<ResultSet> future = context.getExecutor().submit(callable);
			
			try (Statement sourceStatement = sourceConnection.createStatement())
			{
				sourceStatement.setFetchSize(this.fetchSize);
				
				try (ResultSet sourceResultSet = sourceStatement.executeQuery(selectSQL))
				{
					try (ResultSet targetResultSet = future.get())
					{
						if (selectAllSQL != null)
						{
							logger.log(Level.DEBUG, selectAllSQL);
						}
						try (PreparedStatement selectAllStatement = (selectAllSQL != null) ? targetConnection.prepareStatement(selectAllSQL) : null)
						{
							logger.log(Level.DEBUG, deleteSQL);
							try (PreparedStatement deleteStatement = targetConnection.prepareStatement(deleteSQL))
							{
								logger.log(Level.DEBUG, insertSQL);
								try (PreparedStatement insertStatement = targetConnection.prepareStatement(insertSQL))
								{
									if (updateSQL != null)
									{
										logger.log(Level.DEBUG, updateSQL);
									}
									try (PreparedStatement updateStatement = (updateSQL != null) ? sourceConnection.prepareStatement(updateSQL) : null)
									{
										int[] counts = synchronizeRows(
												context, table,
												allColumns, primaryKeyColumns, nonPrimaryKeyColumns, selectColumns, versionColumns,
												sourceResultSet, targetResultSet,
												selectAllStatement, deleteStatement, insertStatement, updateStatement,
												this.maxBatchSize);

										logger.log(Level.INFO, messages.insertCount(table, counts[0]));
										logger.log(Level.INFO, messages.updateCount(table, counts[1]));
										logger.log(Level.INFO, messages.deleteCount(table, counts[2]));
									}
								}
							}
						}
					}
				}
			}
 		catch (InterruptedException e)
 		{
 			Thread.currentThread().interrupt();
 			throw new SQLException("Synchronization interrupted", e);
 		}
 		catch (ExecutionException e)
 		{
 			Throwable cause = e.getCause();
 			if (cause instanceof SQLException)
 			{
 				throw (SQLException) cause;
 			}
 			throw ExceptionType.SQL.<SQLException>getExceptionFactory().createException(cause != null ? cause : e);
 		}
		}
	}
	
	/**
	 * Synchronize rows between source and target result sets using a merge-sort style algorithm.
	 * Returns an array of [insertCount, updateCount, deleteCount].
	 */
	private static <Z, D extends Database<Z>> int[] synchronizeRows(
			SynchronizationContext<Z, D> context, TableProperties table,
			List<String> allColumns, List<String> primaryKeyColumns, List<String> nonPrimaryKeyColumns,
			List<String> selectColumns, List<String> versionColumns,
			ResultSet sourceResultSet, ResultSet targetResultSet,
			PreparedStatement selectAllStatement, PreparedStatement deleteStatement,
			PreparedStatement insertStatement, PreparedStatement updateStatement,
			int maxBatchSize) throws SQLException
	{
		boolean hasMoreSourceResults = sourceResultSet.next();
		boolean hasMoreTargetResults = targetResultSet.next();

		int insertCount = 0;
		int updateCount = 0;
		int deleteCount = 0;

		while (hasMoreSourceResults || hasMoreTargetResults)
		{
			int compare = comparePrimaryKey(hasMoreSourceResults, hasMoreTargetResults,
					primaryKeyColumns.size(), sourceResultSet, targetResultSet);

			if (compare > 0)
			{
				deleteCount = batchDelete(context, table, allColumns, primaryKeyColumns,
						targetResultSet, deleteStatement, deleteCount, maxBatchSize);
			}
			else if (compare < 0)
			{
				insertCount = batchInsert(context, table, allColumns, primaryKeyColumns,
						versionColumns, sourceResultSet, selectAllStatement, insertStatement,
						insertCount, maxBatchSize);
			}
			else if (updateStatement != null)
			{
				updateCount = batchUpdate(context, table, allColumns, primaryKeyColumns,
						nonPrimaryKeyColumns, selectColumns, versionColumns,
						sourceResultSet, targetResultSet, selectAllStatement, updateStatement,
						updateCount, maxBatchSize);
			}

			if (hasMoreSourceResults && (compare <= 0))
			{
				hasMoreSourceResults = sourceResultSet.next();
			}

			if (hasMoreTargetResults && (compare >= 0))
			{
				hasMoreTargetResults = targetResultSet.next();
			}
		}

		// Execute remaining batches
		if ((deleteCount % maxBatchSize) > 0)
		{
			deleteStatement.executeBatch();
		}
		if ((insertCount % maxBatchSize) > 0)
		{
			insertStatement.executeBatch();
		}
		if (updateStatement != null && (updateCount % maxBatchSize) > 0)
		{
			updateStatement.executeBatch();
		}

		return new int[] { insertCount, updateCount, deleteCount };
	}

	/**
	 * Compare primary key values between source and target result sets.
	 * Returns: positive if target PK > source PK (target-only row, needs delete),
	 *          negative if source PK > target PK (source-only row, needs insert),
	 *          zero if primary keys match (may need update).
	 */
	private static int comparePrimaryKey(boolean hasMoreSourceResults, boolean hasMoreTargetResults,
			int primaryKeyCount, ResultSet sourceResultSet, ResultSet targetResultSet) throws SQLException
	{
		if (!hasMoreSourceResults)
		{
			return 1;
		}
		else if (!hasMoreTargetResults)
		{
			return -1;
		}

		for (int i = 1; i <= primaryKeyCount; ++i)
		{
			Object sourceObject = sourceResultSet.getObject(i);
			Object targetObject = targetResultSet.getObject(i);
			int compare = compare(sourceObject, targetObject);
			if (compare != 0)
			{
				return compare;
			}
		}
		return 0;
	}

	/**
	 * Batch a delete operation for a row that exists in target but not in source.
	 */
	private static <Z, D extends Database<Z>> int batchDelete(
			SynchronizationContext<Z, D> context, TableProperties table,
			List<String> allColumns, List<String> primaryKeyColumns,
			ResultSet targetResultSet, PreparedStatement deleteStatement,
			int deleteCount, int maxBatchSize) throws SQLException
	{
		deleteStatement.clearParameters();

		for (int i = 1; i <= primaryKeyColumns.size(); ++i)
		{
			int type = context.getDialect().getColumnType(table.getColumnProperties(allColumns.get(i - 1)));
			deleteStatement.setObject(i, targetResultSet.getObject(i), type);
		}

		deleteStatement.addBatch();
		deleteCount += 1;

		if ((deleteCount % maxBatchSize) == 0)
		{
			deleteStatement.executeBatch();
			deleteStatement.clearBatch();
		}
		return deleteCount;
	}

	/**
	 * Batch an insert operation for a row that exists in source but not in target.
	 */
	private static <Z, D extends Database<Z>> int batchInsert(
			SynchronizationContext<Z, D> context, TableProperties table,
			List<String> allColumns, List<String> primaryKeyColumns,
			List<String> versionColumns, ResultSet sourceResultSet,
			PreparedStatement selectAllStatement, PreparedStatement insertStatement,
			int insertCount, int maxBatchSize) throws SQLException
	{
		insertStatement.clearParameters();

		// Set primary key columns
		for (int i = 1; i <= primaryKeyColumns.size(); ++i)
		{
			int type = context.getDialect().getColumnType(table.getColumnProperties(allColumns.get(i - 1)));
			insertStatement.setObject(i, sourceResultSet.getObject(i), type);
		}

		if (versionColumns.isEmpty())
		{
			// No version columns: read non-PK columns directly from source result set
			for (int i = primaryKeyColumns.size() + 1; i <= allColumns.size(); ++i)
			{
				int type = context.getDialect().getColumnType(table.getColumnProperties(allColumns.get(i - 1)));
				Object object = context.getSynchronizationSupport().getObject(sourceResultSet, i, type);

				if (sourceResultSet.wasNull())
				{
					insertStatement.setNull(i, type);
				}
				else
				{
					insertStatement.setObject(i, object, type);
				}
			}
		}
		else
		{
			// Version columns present: fetch full row from target to get non-version columns
			if (selectAllStatement != null)
			{
				selectAllStatement.clearParameters();
				for (int i = 1; i <= primaryKeyColumns.size(); ++i)
				{
					int type = context.getDialect().getColumnType(table.getColumnProperties(allColumns.get(i - 1)));
					selectAllStatement.setObject(i, sourceResultSet.getObject(i), type);
				}

				try (ResultSet selectAllResultSet = selectAllStatement.executeQuery())
				{
					if (!selectAllResultSet.next())
					{
						throw new IllegalStateException("Row not found for insert synchronization in table: " + table.getName());
					}
					for (int i = primaryKeyColumns.size() + 1; i <= allColumns.size(); ++i)
					{
						int type = context.getDialect().getColumnType(table.getColumnProperties(allColumns.get(i - 1)));
						Object object = context.getSynchronizationSupport().getObject(selectAllResultSet, i - primaryKeyColumns.size(), type);

						if (selectAllResultSet.wasNull())
						{
							insertStatement.setNull(i, type);
						}
						else
						{
							insertStatement.setObject(i, object, type);
						}
					}
				}
			}
		}

		insertStatement.addBatch();
		insertCount += 1;

		if ((insertCount % maxBatchSize) == 0)
		{
			insertStatement.executeBatch();
			insertStatement.clearBatch();
		}
		return insertCount;
	}

	/**
	 * Batch an update operation for a row that exists in both source and target but has differences.
	 */
	private static <Z, D extends Database<Z>> int batchUpdate(
			SynchronizationContext<Z, D> context, TableProperties table,
			List<String> allColumns, List<String> primaryKeyColumns,
			List<String> nonPrimaryKeyColumns, List<String> selectColumns,
			List<String> versionColumns,
			ResultSet sourceResultSet, ResultSet targetResultSet,
			PreparedStatement selectAllStatement, PreparedStatement updateStatement,
			int updateCount, int maxBatchSize) throws SQLException
	{
		updateStatement.clearParameters();

		boolean updated = false;

		// Compare version/select columns to determine if update is needed
		for (int i = primaryKeyColumns.size() + 1; i <= selectColumns.size(); ++i)
		{
			int type = context.getDialect().getColumnType(table.getColumnProperties(selectColumns.get(i - 1)));

			Object sourceObject = context.getSynchronizationSupport().getObject(sourceResultSet, i, type);
			Object targetObject = context.getSynchronizationSupport().getObject(targetResultSet, i, type);

			int index = i - primaryKeyColumns.size();

			if (sourceResultSet.wasNull())
			{
				updateStatement.setNull(index, type);
				updated |= !targetResultSet.wasNull();
			}
			else
			{
				updateStatement.setObject(index, sourceObject, type);
				updated |= targetResultSet.wasNull();
				updated |= !Objects.equals(sourceObject, targetObject);
			}
		}

		if (updated)
		{
			// If version columns are present, fetch non-version columns from target
			if (selectAllStatement != null)
			{
				selectAllStatement.clearParameters();
				for (int i = 1; i <= primaryKeyColumns.size(); ++i)
				{
					int type = context.getDialect().getColumnType(table.getColumnProperties(allColumns.get(i - 1)));
					selectAllStatement.setObject(i, sourceResultSet.getObject(i), type);
				}

				try (ResultSet selectAllResultSet = selectAllStatement.executeQuery())
				{
					if (!selectAllResultSet.next())
					{
						throw new IllegalStateException("Row not found for update synchronization in table: " + table.getName());
					}
					for (int i = primaryKeyColumns.size() + 1; i <= allColumns.size(); ++i)
					{
						int type = context.getDialect().getColumnType(table.getColumnProperties(allColumns.get(i - 1)));
						int index = i - primaryKeyColumns.size();

						Object object = context.getSynchronizationSupport().getObject(selectAllResultSet, index, type);

						if (selectAllResultSet.wasNull())
						{
							updateStatement.setNull(index, type);
						}
						else
						{
							updateStatement.setObject(index, object, type);
						}
					}
				}
			}

			// Set primary key WHERE clause
			for (int i = 1; i <= primaryKeyColumns.size(); ++i)
			{
				int type = context.getDialect().getColumnType(table.getColumnProperties(allColumns.get(i - 1)));
				updateStatement.setObject(i + nonPrimaryKeyColumns.size(), targetResultSet.getObject(i), type);
			}

			updateStatement.addBatch();
			updateCount += 1;

			if ((updateCount % maxBatchSize) == 0)
			{
				updateStatement.executeBatch();
				updateStatement.clearBatch();
			}
		}
		return updateCount;
	}

	private static int compare(Object object1, Object object2)
	{
		@SuppressWarnings("unchecked")
		Comparable<Object> comparable = (Comparable<Object>) object1;
		
		return comparable.compareTo(object2);
	}

	/**
	 * @return the fetchSize.
	 */
	public int getFetchSize()
	{
		return this.fetchSize;
	}

	/**
	 * @param fetchSize the fetchSize to set.
	 */
	public void setFetchSize(int fetchSize)
	{
		this.fetchSize = fetchSize;
	}

	/**
	 * @return Returns the maxBatchSize.
	 */
	public int getMaxBatchSize()
	{
		return this.maxBatchSize;
	}

	/**
	 * @param maxBatchSize The maxBatchSize to set.
	 */
	public void setMaxBatchSize(int maxBatchSize)
	{
		this.maxBatchSize = maxBatchSize;
	}

	/**
	 * @return the versionPattern
	 */
	public String getVersionPattern()
	{
		return (this.versionPattern != null) ? this.versionPattern.pattern() : null;
	}

	/**
	 * @param versionPattern the versionPattern to set
	 */
	public void setVersionPattern(String versionPattern)
	{
		this.versionPattern = (versionPattern != null) ? Pattern.compile(versionPattern, Pattern.CASE_INSENSITIVE) : null;
	}
}
