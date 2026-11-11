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
package net.sf.hajdbc.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.SynchronizationStrategy;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;
import net.sf.hajdbc.state.DatabaseEvent;
import net.sf.hajdbc.sync.SynchronizationContext;
import net.sf.hajdbc.sync.SynchronizationContextImpl;

/**
 * Handles database activation with synchronization and health checking.
 * @param <Z> the locator type
 * @param <D> the database type
 */
class DatabaseClusterActivationHandler<Z, D extends Database<Z>>
{
	static final Messages messages = MessagesFactory.getMessages();
	static final Logger logger = LoggerFactory.getLogger(DatabaseClusterActivationHandler.class);

	private final DatabaseClusterImpl<Z, D> cluster;

	DatabaseClusterActivationHandler(DatabaseClusterImpl<Z, D> cluster)
	{
		this.cluster = cluster;
	}

	/**
	 * Checks if the specified database is alive by attempting a connection.
	 * @param database the database to check
	 * @param level the log level for error messages
	 * @return true if the database is alive, false otherwise
	 */
	boolean isAlive(D database, Level level)
	{
		try (Connection connection = database.connect(this.cluster.getDecoder()))
		{
			return this.cluster.getDialect().isValid(connection);
		}
		catch (SQLException e)
		{
			logger.log(level, e);
			return false;
		}
	}

	/**
	 * Activates the specified database using the given synchronization strategy.
	 * @param database the database to activate
	 * @param strategy the synchronization strategy to use
	 * @return true if the database was activated, false if it was already active
	 * @throws SQLException if synchronization fails
	 * @throws InterruptedException if interrupted while waiting for lock
	 */
	boolean activate(D database, SynchronizationStrategy strategy) throws SQLException, InterruptedException
	{
		if (!this.isAlive(database, Level.DEBUG)) return false;

		Lock lock = this.cluster.getLockManager().writeLock(null);

		lock.lockInterruptibly();

		try
		{
			if (this.cluster.getBalancer().contains(database)) return false;

			if (!this.cluster.getBalancer().isEmpty())
			{
				try (SynchronizationContext<Z, D> context = new SynchronizationContextImpl<>(this.cluster, database))
				{
					DatabaseEvent event = new DatabaseEvent(database);

					logger.log(Level.INFO, messages.synchronizationBegin(this.cluster, database, strategy));

					for (var listener : this.cluster.getSynchronizationListeners())
					{
						listener.beforeSynchronization(event);
					}

					strategy.synchronize(context);

					logger.log(Level.INFO, messages.synchronizationEnd(this.cluster, database, strategy));

					for (var listener : this.cluster.getSynchronizationListeners())
					{
						listener.afterSynchronization(event);
					}
				}
			}

			return this.cluster.activate(database, this.cluster.getStateManager());
		}
		finally
		{
			lock.unlock();
		}
	}
}
