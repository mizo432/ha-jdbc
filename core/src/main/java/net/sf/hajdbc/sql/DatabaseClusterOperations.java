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
import java.util.TreeSet;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseClusterConfiguration;
import net.sf.hajdbc.DatabaseClusterConfigurationListener;
import net.sf.hajdbc.SynchronizationStrategy;
import net.sf.hajdbc.Version;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.management.Description;
import net.sf.hajdbc.management.MBeanRegistrar;
import net.sf.hajdbc.management.ManagedAttribute;
import net.sf.hajdbc.management.ManagedOperation;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;

/**
 * JMX managed operations for a database cluster.
 * @param <Z> the locator type
 * @param <D> the database type
 */
class DatabaseClusterOperations<Z, D extends Database<Z>>
{
	static final Messages messages = MessagesFactory.getMessages();
	static final Logger logger = LoggerFactory.getLogger(DatabaseClusterOperations.class);

	private final DatabaseClusterImpl<Z, D> cluster;
	private final DatabaseClusterConfiguration<Z, D> configuration;
	private final DatabaseClusterActivationHandler<Z, D> activationHandler;

	DatabaseClusterOperations(DatabaseClusterImpl<Z, D> cluster,
	                          DatabaseClusterConfiguration<Z, D> configuration,
	                          DatabaseClusterActivationHandler<Z, D> activationHandler)
	{
		this.cluster = cluster;
		this.configuration = configuration;
		this.activationHandler = activationHandler;
	}

	/**
	 * Deactivates the specified database.
	 * @param databaseId a database identifier
	 * @throws IllegalArgumentException if no database exists with the specified identifier.
	 */
	@ManagedOperation
	public void deactivate(String databaseId)
	{
		this.cluster.deactivate(this.cluster.getDatabase(databaseId), this.cluster.getStateManager());
	}

	/**
	 * Synchronizes, using the default strategy, and reactivates the specified database.
	 * @param databaseId a database identifier
	 * @throws IllegalArgumentException if no database exists with the specified identifier.
	 * @throws IllegalStateException if synchronization fails.
	 */
	@ManagedOperation
	public void activate(String databaseId)
	{
		this.activate(databaseId, this.configuration.getDefaultSynchronizationStrategy());
	}

	/**
	 * Synchronizes, using the specified strategy, and reactivates the specified database.
	 * @param databaseId a database identifier
	 * @param strategyId the identifer of a synchronization strategy
	 * @throws IllegalArgumentException if no database exists with the specified identifier, or no synchronization strategy exists with the specified identifier.
	 * @throws IllegalStateException if synchronization fails.
	 */
	@ManagedOperation
	public void activate(String databaseId, String strategyId)
	{
		SynchronizationStrategy strategy = this.configuration.getSynchronizationStrategyMap().get(strategyId);

		if (strategy == null)
		{
			throw new IllegalArgumentException(messages.invalidSyncStrategy(strategyId, this.configuration.getSynchronizationStrategyMap().keySet()));
		}

		D database = this.cluster.getDatabase(databaseId);
		try
		{
			if (this.activationHandler.activate(database, strategy))
			{
				logger.log(Level.INFO, messages.activated(this.cluster, database));
			}
		}
		catch (SQLException e)
		{
			logger.log(Level.WARN, e, messages.activationFailed(this.cluster, database));

			SQLException exception = e.getNextException();

			while (exception != null)
			{
				logger.log(Level.ERROR, exception);

				exception = exception.getNextException();
			}

			throw new IllegalStateException("Activation failed for " + database, e);
		}
		catch (InterruptedException e)
		{
			logger.log(Level.WARN, e);

			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Checks if the specified database is alive.
	 * @param databaseId database identifier
	 * @return true if the database is alive, false otherwise
	 * @throws IllegalArgumentException if no database exists with the specified identifier.
	 */
	@ManagedOperation
	public boolean isAlive(String databaseId)
	{
		return this.activationHandler.isAlive(this.cluster.getDatabase(databaseId), Level.WARN);
	}

	/**
	 * Returns the collection of active databases in this cluster.
	 * @return list of database identifiers
	 */
	@ManagedAttribute
	public Set<String> getActiveDatabases()
	{
		Set<String> databases = new TreeSet<>();

		for (D database : this.cluster.getBalancer())
		{
			databases.add(database.getId());
		}

		return databases;
	}

	/**
	 * Returns the collection of inactive databases in this cluster.
	 * @return collection of database identifiers
	 */
	@ManagedAttribute
	public Set<String> getInactiveDatabases()
	{
		Set<String> databases = new TreeSet<>(this.configuration.getDatabaseMap().keySet());

		for (D database : this.cluster.getBalancer())
		{
			databases.remove(database.getId());
		}

		return databases;
	}

	/**
	 * Return the current HA-JDBC version
	 * @return the current version
	 */
	@ManagedAttribute
	public String getVersion()
	{
		return Version.CURRENT.toString();
	}

	/**
	 * Removes the specified database from the cluster.
	 * @param databaseId a database identifier
	 * @throws IllegalStateException if database is still active.
	 */
	@ManagedOperation
	public void remove(String databaseId)
	{
		D database = this.cluster.getDatabase(databaseId);

		if (this.cluster.getBalancer().contains(database))
		{
			throw new IllegalStateException(messages.stillActive(this.cluster, database));
		}

		MBeanRegistrar<Z, D> registrar = this.cluster.getRegistrar();
		registrar.unregister(this.cluster, database);

		this.configuration.getDatabaseMap().remove(databaseId);

		for (DatabaseClusterConfigurationListener<Z, D> listener : this.cluster.getConfigurationListeners())
		{
			listener.removed(database, this.configuration);
		}
	}

	/**
	 * Flushes this cluster's cache of DatabaseMetaData.
	 */
	@ManagedOperation
	@Description("Flushes this cluster's cache of database meta data")
	public void flushMetaDataCache()
	{
		try
		{
			this.cluster.getDatabaseMetaDataCache().flush();
		}
		catch (SQLException e)
		{
			throw new IllegalStateException("Failed to flush metadata cache", e);
		}
	}
}
