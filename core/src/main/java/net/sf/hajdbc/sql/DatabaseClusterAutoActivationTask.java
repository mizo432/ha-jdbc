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

import java.sql.SQLException;
import java.util.Set;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;

/**
 * Scheduled task that attempts to automatically activate inactive databases.
 * @param <Z> the locator type
 * @param <D> the database type
 */
class DatabaseClusterAutoActivationTask<Z, D extends Database<Z>> implements Runnable
{
	static final Messages messages = MessagesFactory.getMessages();
	static final Logger logger = LoggerFactory.getLogger(DatabaseClusterAutoActivationTask.class);

	private final DatabaseClusterImpl<Z, D> cluster;

	DatabaseClusterAutoActivationTask(DatabaseClusterImpl<Z, D> cluster)
	{
		this.cluster = cluster;
	}

	@Override
	public void run()
	{
		if (!this.cluster.getStateManager().isEnabled()) return;

		try
		{
			Set<D> activeDatabases = this.cluster.getBalancer();

			if (!activeDatabases.isEmpty())
			{
				for (D database: this.cluster.configuration.getDatabaseMap().values())
				{
					if (!activeDatabases.contains(database))
					{
						try
						{
							if (this.cluster.activate(database, this.cluster.configuration.getSynchronizationStrategyMap()
									.get(this.cluster.configuration.getDefaultSynchronizationStrategy())))
							{
								logger.log(Level.INFO, messages.activated(this.cluster, database));
							}
						}
						catch (SQLException e)
						{
							logger.log(Level.DEBUG, e);
						}
					}
				}
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
}
