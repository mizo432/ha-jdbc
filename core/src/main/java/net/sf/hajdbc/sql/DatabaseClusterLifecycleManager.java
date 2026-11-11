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

import javax.management.JMException;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.DatabaseClusterConfiguration;
import net.sf.hajdbc.Locality;
import net.sf.hajdbc.balancer.Balancer;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.lock.LockManager;
import net.sf.hajdbc.lock.distributed.DistributedLockManager;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.management.MBeanRegistrar;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;
import net.sf.hajdbc.state.StateManager;
import net.sf.hajdbc.state.distributed.DistributedStateManager;
import net.sf.hajdbc.tx.SimpleTransactionIdentifierFactory;
import net.sf.hajdbc.tx.TransactionIdentifierFactory;
import net.sf.hajdbc.tx.UUIDTransactionIdentifierFactory;

/**
 * Manages the lifecycle (start/stop) of a database cluster.
 * @param <Z> the locator type
 * @param <D> the database type
 */
class DatabaseClusterLifecycleManager<Z, D extends Database<Z>>
{
	static final Messages messages = MessagesFactory.getMessages();
	static final Logger logger = LoggerFactory.getLogger(DatabaseClusterLifecycleManager.class);

	private final DatabaseCluster<Z, D> cluster;
	private final DatabaseClusterConfiguration<Z, D> configuration;

	DatabaseClusterLifecycleManager(DatabaseCluster<Z, D> cluster, DatabaseClusterConfiguration<Z, D> configuration)
	{
		this.cluster = cluster;
		this.configuration = configuration;
	}

	void start(DatabaseClusterImpl<Z, D> impl) throws SQLException
	{
		this.initializeComponents(impl);
		this.initializeDatabases(impl);
		this.initializeRecovery(impl);
		this.initializeCache(impl);
		this.registerMBeans(impl);
		
		impl.setActive(true);
		
		logger.log(Level.INFO, messages.start(this.cluster));
	}

	void stop(DatabaseClusterImpl<Z, D> impl)
	{
		logger.log(Level.INFO, messages.stop(this.cluster));
		
		impl.setActive(false);
		
		this.deactivateEmbeddedDatabases(impl);
		this.unregisterMBeans(impl);
		this.stopStateManager(impl);
		this.stopLockManager(impl);
		this.shutdownExecutor(impl);
		this.clearBalancer(impl);
	}

	private void initializeComponents(DatabaseClusterImpl<Z, D> impl) throws SQLException
	{
		CommandDispatcherFactory dispatcherFactory = this.configuration.getDispatcherFactory();
		
		impl.setDecoder(this.configuration.getDecoderFactory().createDecoder(this.cluster.getId()));
		LockManager lockManager = this.configuration.getLockManagerFactory().createLockManager();
		StateManager stateManager = this.configuration.getStateManagerFactory().createStateManager(this.cluster);
		
		if (dispatcherFactory != null)
		{
			try
			{
				lockManager = new DistributedLockManager(this.cluster, dispatcherFactory, this.configuration.getLockTimeoutMs());
				stateManager = new DistributedStateManager<>(this.cluster, dispatcherFactory);
			}
			catch (Exception e)
			{
				throw new SQLException(e);
			}
		}
		
		impl.setLockManager(lockManager);
		impl.setStateManager(stateManager);
		impl.setBalancer(this.configuration.getBalancerFactory().createBalancer(new java.util.TreeSet<D>()));
		impl.setDialect(this.configuration.getDialectFactory().createDialect());
		impl.setDurability(this.configuration.getDurabilityFactory().createDurability(this.cluster));
		impl.setExecutor(this.configuration.getExecutorProvider().getExecutor(this.configuration.getThreadFactory()));
		impl.setInputSinkStrategy(this.configuration.getInputSinkProvider().createInputSinkStrategy());
		impl.setTransactionIdentifierFactory((dispatcherFactory != null) ? new UUIDTransactionIdentifierFactory() : new SimpleTransactionIdentifierFactory());

		lockManager.start();
		stateManager.start();
	}

	private void initializeDatabases(DatabaseClusterImpl<Z, D> impl)
	{
		Set<String> databases = impl.getStateManager().getActiveDatabases();
		
		if (!databases.isEmpty())
		{
			messages.initialClusterState(this.cluster, databases);
			
			for (String databaseId: databases)
			{
				D database = this.cluster.getDatabase(databaseId);
				impl.getBalancer().add(database);
			}
		}
		else
		{
			messages.initialClusterStateEmpty(this.cluster);
			
			for (D database: this.configuration.getDatabaseMap().values())
			{
				if (impl.isAlive(database, Level.WARN))
				{
					impl.activate(database, impl.getStateManager());
				}
			}
		}
	}

	private void initializeRecovery(DatabaseClusterImpl<Z, D> impl)
	{
		var invokers = impl.getStateManager().recover();
		if (!invokers.isEmpty())
		{
			this.cluster.getDurability().recover(invokers);
		}
	}

	private void initializeCache(DatabaseClusterImpl<Z, D> impl)
	{
		var cache = this.configuration.getDatabaseMetaDataCacheFactory().createCache(this.cluster);
		impl.setDatabaseMetaDataCache(cache);
		
		try
		{
			impl.flushMetaDataCache();
		}
		catch (IllegalStateException e)
		{
			// Ignore - cache will initialize lazily.
		}
	}

	private void registerMBeans(DatabaseClusterImpl<Z, D> impl) throws SQLException
	{
		MBeanRegistrar<Z, D> registrar = this.configuration.getMBeanRegistrarFactory().createMBeanRegistrar();
		impl.setRegistrar(registrar);

		try
		{
			registrar.register(this.cluster);
			
			for (D database: this.configuration.getDatabaseMap().values())
			{
				registrar.register(this.cluster, database);
			}
		}
		catch (JMException e)
		{
			throw new SQLException(e);
		}
	}

	private void deactivateEmbeddedDatabases(DatabaseClusterImpl<Z, D> impl)
	{
		Balancer<Z, D> balancer = impl.getBalancer();
		if (balancer != null)
		{
			for (D database: balancer)
			{
				if (database.getLocality() == Locality.EMBEDDED)
				{
					impl.deactivate(database, impl.getStateManager());
				}
			}
		}
	}

	private void unregisterMBeans(DatabaseClusterImpl<Z, D> impl)
	{
		try
		{
			MBeanRegistrar<Z, D> registrar = impl.getRegistrar();
			if (registrar != null)
			{
				registrar.unregister(this.cluster);
				
				for (D database: this.configuration.getDatabaseMap().values())
				{
					registrar.unregister(this.cluster, database);
				}
			}
		}
		catch (Exception e)
		{
			logger.log(Level.WARN, e, "Failed to unregister MBeans");
		}
	}

	private void stopStateManager(DatabaseClusterImpl<Z, D> impl)
	{
		try
		{
			StateManager stateManager = impl.getStateManager();
			if (stateManager != null)
			{
				stateManager.stop();
			}
		}
		catch (Exception e)
		{
			logger.log(Level.WARN, e, "Failed to stop state manager");
		}
	}

	private void stopLockManager(DatabaseClusterImpl<Z, D> impl)
	{
		try
		{
			LockManager lockManager = impl.getLockManager();
			if (lockManager != null)
			{
				lockManager.stop();
			}
		}
		catch (Exception e)
		{
			logger.log(Level.WARN, e, "Failed to stop lock manager");
		}
	}

	private void shutdownExecutor(DatabaseClusterImpl<Z, D> impl)
	{
		var executor = impl.getExecutor();
		if (executor != null)
		{
			executor.shutdown();
			try
			{
				if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS))
				{
					executor.shutdownNow();
				}
			}
			catch (InterruptedException e)
			{
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	private void clearBalancer(DatabaseClusterImpl<Z, D> impl)
	{
		Balancer<Z, D> balancer = impl.getBalancer();
		if (balancer != null)
		{
			balancer.clear();
		}
	}
}
