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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;

/**
 * Scheduled task that detects failed databases and deactivates them.
 * @param <Z> the locator type
 * @param <D> the database type
 */
class DatabaseClusterFailureDetectionTask<Z, D extends Database<Z>> implements Runnable
{
	static final Messages messages = MessagesFactory.getMessages();
	static final Logger logger = LoggerFactory.getLogger(DatabaseClusterFailureDetectionTask.class);

	private final DatabaseClusterImpl<Z, D> cluster;

	DatabaseClusterFailureDetectionTask(DatabaseClusterImpl<Z, D> cluster)
	{
		this.cluster = cluster;
	}

	@Override
	public void run()
	{
		if (!this.cluster.getStateManager().isEnabled()) return;

		Set<D> databases = this.cluster.getBalancer();

		int size = databases.size();

		if ((size > 1) || this.cluster.configuration.isEmptyClusterAllowed())
		{
			List<D> deadList = new ArrayList<>(size);

			for (D database: databases)
			{
				if (!this.cluster.isAlive(database, Level.WARN))
				{
					deadList.add(database);
				}
			}

			if ((deadList.size() < size) || this.cluster.configuration.isEmptyClusterAllowed())
			{
				for (D database: deadList)
				{
					if (this.cluster.deactivate(database, this.cluster.getStateManager()))
					{
						logger.log(Level.ERROR, messages.deactivated(this.cluster, database));
					}
				}
			}
		}
	}
}
