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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.management.JMException;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.DatabaseClusterConfiguration;
import net.sf.hajdbc.DatabaseClusterConfigurationListener;
import net.sf.hajdbc.DatabaseClusterListener;
import net.sf.hajdbc.Locality;
import net.sf.hajdbc.SynchronizationListener;
import net.sf.hajdbc.SynchronizationStrategy;
import net.sf.hajdbc.TransactionMode;
import net.sf.hajdbc.balancer.Balancer;
import net.sf.hajdbc.cache.DatabaseMetaDataCache;
import net.sf.hajdbc.codec.Decoder;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.durability.Durability;
import net.sf.hajdbc.io.InputSinkStrategy;
import net.sf.hajdbc.lock.LockManager;
import net.sf.hajdbc.lock.distributed.DistributedLockManager;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.management.MBean;
import net.sf.hajdbc.management.MBeanRegistrar;
import net.sf.hajdbc.management.ManagedAttribute;
import net.sf.hajdbc.management.ManagedOperation;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;
import net.sf.hajdbc.state.DatabaseEvent;
import net.sf.hajdbc.state.StateManager;
import net.sf.hajdbc.state.distributed.DistributedStateManager;
import net.sf.hajdbc.tx.SimpleTransactionIdentifierFactory;
import net.sf.hajdbc.tx.TransactionIdentifierFactory;
import net.sf.hajdbc.tx.UUIDTransactionIdentifierFactory;

/**
 * @author Paul Ferraro
 */
@MBean
public class DatabaseClusterImpl<Z, D extends Database<Z>> implements DatabaseCluster<Z, D>
{
	static final Messages messages = MessagesFactory.getMessages();
	static final Logger logger = LoggerFactory.getLogger(DatabaseClusterImpl.class);
	
	private final String id;
	
	final DatabaseClusterConfiguration<Z, D> configuration;
	
	private Balancer<Z, D> balancer;
	private Dialect dialect;
	private Durability<Z, D> durability;
	private DatabaseMetaDataCache<Z, D> databaseMetaDataCache;
	private ExecutorService executor;
	private Decoder decoder;
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "同期は一貫して管理されている")
	private LockManager lockManager;
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "同期は一貫して管理されている")
	private StateManager stateManager;
	private InputSinkStrategy<? extends Object> sinkSourceFactory;
	private TransactionIdentifierFactory<? extends Object> txIdentifierFactory;
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "同期は一貫して管理されている")
	private MBeanRegistrar<Z, D> registrar;
	
	private boolean active = false;

	private final ClusterListenerManager<Z, D> listenerManager = new ClusterListenerManager<>();
	private final LifecycleManager lifecycleManager;
	private final DatabaseClusterScheduler<Z, D> scheduler;
	private final DatabaseClusterActivationHandler<Z, D> activationHandler;
	private final DatabaseClusterOperations<Z, D> operations;

	// Package-private getter for lifecycle manager
	MBeanRegistrar<Z, D> getRegistrar() { return this.registrar; }

	// Package-private getters for activation handler and operations
	List<SynchronizationListener> getSynchronizationListeners() { return this.listenerManager.getSynchronizationListeners(); }
	List<DatabaseClusterConfigurationListener<Z, D>> getConfigurationListeners() { return this.listenerManager.getConfigurationListeners(); }
	
	public DatabaseClusterImpl(String id, DatabaseClusterConfiguration<Z, D> configuration)
	{
		this(id, configuration, null);
	}
	
	public DatabaseClusterImpl(String id, DatabaseClusterConfiguration<Z, D> configuration, DatabaseClusterConfigurationListener<Z, D> listener)
	{
		this.id = id;
		this.configuration = configuration;
		this.lifecycleManager = new LifecycleManager();
		this.scheduler = new DatabaseClusterScheduler<>(this, configuration);
		this.activationHandler = new DatabaseClusterActivationHandler<>(this);
		this.operations = new DatabaseClusterOperations<>(this, configuration, this.activationHandler);
		
		if (listener != null)
		{
			this.listenerManager.addConfigurationListener(listener);
		}
	}

	/**
	 * Deactivates the specified database.
	 * @param databaseId a database identifier
	 * @throws IllegalArgumentException if no database exists with the specified identifier.
	 */
	@ManagedOperation
	public void deactivate(String databaseId)
	{
		this.operations.deactivate(databaseId);
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
		this.operations.activate(databaseId);
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
		this.operations.activate(databaseId, strategyId);
	}
	
	/**
	 * 指定されたデータベースが応答しているかどうかを判定します
	 * @param databaseId データベース識別子
	 * @return データベースが生きている場合はtrue、そうでない場合はfalse
	 * @throws IllegalArgumentException 指定された識別子を持つデータベースが存在しない場合。
	 */
	@ManagedOperation
	public boolean isAlive(String databaseId)
	{
		return this.operations.isAlive(databaseId);
	}
	
	/**
	 * このクラスタのアクティブなデータベースのコレクションを返します。
	 * @return データベース識別子のリスト
	 */
	@ManagedAttribute
	public Set<String> getActiveDatabases()
	{
		return this.operations.getActiveDatabases();
	}
	
	/**
	 * このクラスタの非アクティブなデータベースのコレクションを返します。
	 * @return データベース識別子のコレクション
	 */
	@ManagedAttribute
	public Set<String> getInactiveDatabases()
	{
		return this.operations.getInactiveDatabases();
	}
	
	/**
	 * Return the current HA-JDBC version
	 * @return the current version
	 */
	@ManagedAttribute
	public String getVersion()
	{
		return this.operations.getVersion();
	}

	/**
	 * Removes the specified database from the cluster.
	 * @param databaseId a database identifier
	 * @throws IllegalStateException if database is still active.
	 */
	@ManagedOperation
	public void remove(String databaseId)
	{
		this.operations.remove(databaseId);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getId()
	 */
	@ManagedAttribute
	@Override
	public String getId()
	{
		return this.id;
	}

	@Override
	public String toString()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#isActive()
	 */
	@ManagedAttribute
	@Override
	public boolean isActive()
	{
		return this.active;
	}
	
	/**
	 * Returns the set of synchronization strategies available to this cluster.
	 * @return a set of synchronization strategy identifiers
	 */
	@ManagedAttribute
	public Set<String> getSynchronizationStrategies()
	{
		return new TreeSet<>(this.configuration.getSynchronizationStrategyMap().keySet());
	}
	
	/**
	 * Returns the default synchronization strategy used by this cluster.
	 * @return a synchronization strategy identifier
	 */
	@ManagedAttribute
	public String getDefaultSynchronizationStrategy()
	{
		return this.configuration.getDefaultSynchronizationStrategy();
	}

	/**
	 * Flushes this cluster's cache of DatabaseMetaData.
	 */
	@ManagedOperation
	public void flushMetaDataCache()
	{
		this.operations.flushMetaDataCache();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#addConfigurationListener(net.sf.hajdbc.DatabaseClusterConfigurationListener)
	 */
	@ManagedOperation
	@Override
	public void addConfigurationListener(DatabaseClusterConfigurationListener<Z, D> listener)
	{
		this.listenerManager.addConfigurationListener(listener);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#addListener(net.sf.hajdbc.DatabaseClusterListener)
	 */
	@ManagedOperation
	@Override
	public void addListener(DatabaseClusterListener listener)
	{
		this.listenerManager.addListener(listener);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#addSynchronizationListener(net.sf.hajdbc.SynchronizationListener)
	 */
	@ManagedOperation
	@Override
	public void addSynchronizationListener(SynchronizationListener listener)
	{
		this.listenerManager.addSynchronizationListener(listener);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#removeConfigurationListener(net.sf.hajdbc.DatabaseClusterConfigurationListener)
	 */
	@ManagedOperation
	@Override
	public void removeConfigurationListener(DatabaseClusterConfigurationListener<Z, D> listener)
	{
		this.listenerManager.removeConfigurationListener(listener);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#removeListener(net.sf.hajdbc.DatabaseClusterListener)
	 */
	@ManagedOperation
	@Override
	public void removeListener(DatabaseClusterListener listener)
	{
		this.listenerManager.removeListener(listener);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#removeSynchronizationListener(net.sf.hajdbc.SynchronizationListener)
	 */
	@ManagedOperation
	@Override
	public void removeSynchronizationListener(SynchronizationListener listener)
	{
		this.listenerManager.removeSynchronizationListener(listener);
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#activate(net.sf.hajdbc.Database, net.sf.hajdbc.state.StateManager)
	 */
	@Override
	public boolean activate(D database, StateManager manager)
	{
		boolean added = this.balancer.add(database);
		
		if (added)
		{
/*
			database.setActive(true);
			
			if (database.isDirty())
			{
				database.clean();
			}
*/
			DatabaseEvent event = new DatabaseEvent(database);

			manager.activated(event);
			
			for (DatabaseClusterListener listener: this.listenerManager.getClusterListeners())
			{
				listener.activated(event);
			}
		}
		
		return added;
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#deactivate(net.sf.hajdbc.Database, net.sf.hajdbc.state.StateManager)
	 */
	@Override
	public boolean deactivate(D database, StateManager manager)
	{
		boolean removed = this.balancer.remove(database);
		
		if (removed)
		{
//			database.setActive(false);
			
			DatabaseEvent event = new DatabaseEvent(database);

			manager.deactivated(event);
			
			for (DatabaseClusterListener listener: this.listenerManager.getClusterListeners())
			{
				listener.deactivated(event);
			}
		}
		
		return removed;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getBalancer()
	 */
	@Override
	public Balancer<Z, D> getBalancer()
	{
		return this.balancer;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getDatabase(java.lang.String)
	 */
	@Override
	public D getDatabase(String id)
	{
		D database = this.configuration.getDatabaseMap().get(id);
		
		if (database == null)
		{
			throw new IllegalArgumentException(messages.invalidDatabase(this, id));
		}
		
		return database;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getDatabaseMetaDataCache()
	 */
	@Override
	public DatabaseMetaDataCache<Z, D> getDatabaseMetaDataCache()
	{
		return this.databaseMetaDataCache;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getDialect()
	 */
	@Override
	public Dialect getDialect()
	{
		return this.dialect;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getDurability()
	 */
	@Override
	public Durability<Z, D> getDurability()
	{
		return this.durability;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getLockManager()
	 */
	@Override
	public LockManager getLockManager()
	{
		return this.lockManager;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getExecutor()
	 */
	@Override
	public ExecutorService getExecutor()
	{
		return this.executor;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getTransactionMode()
	 */
	@Override
	public TransactionMode getTransactionMode()
	{
		return this.configuration.getTransactionMode();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getStateManager()
	 */
	@Override
	public StateManager getStateManager()
	{
		return this.stateManager;
	}

	@Override
	public ThreadFactory getThreadFactory()
	{
		return this.configuration.getThreadFactory();
	}

	@Override
	public Decoder getDecoder()
	{
		return this.decoder;
	}

	@Override
	public InputSinkStrategy<? extends Object> getInputSinkStrategy()
	{
		return this.sinkSourceFactory;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#getTransactionIdentifierFactory()
	 */
	@Override
	public TransactionIdentifierFactory<? extends Object> getTransactionIdentifierFactory()
	{
		return this.txIdentifierFactory;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#isCurrentDateEvaluationEnabled()
	 */
	@Override
	public boolean isCurrentDateEvaluationEnabled()
	{
		return this.configuration.isCurrentDateEvaluationEnabled();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#isCurrentTimeEvaluationEnabled()
	 */
	@Override
	public boolean isCurrentTimeEvaluationEnabled()
	{
		return this.configuration.isCurrentTimeEvaluationEnabled();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#isCurrentTimestampEvaluationEnabled()
	 */
	@Override
	public boolean isCurrentTimestampEvaluationEnabled()
	{
		return this.configuration.isCurrentTimestampEvaluationEnabled();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#isIdentityColumnDetectionEnabled()
	 */
	@Override
	public boolean isIdentityColumnDetectionEnabled()
	{
		return this.configuration.isIdentityColumnDetectionEnabled();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#isRandEvaluationEnabled()
	 */
	@Override
	public boolean isRandEvaluationEnabled()
	{
		return this.configuration.isRandEvaluationEnabled();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.DatabaseCluster#isSequenceDetectionEnabled()
	 */
	@Override
	public boolean isSequenceDetectionEnabled()
	{
		return this.configuration.isSequenceDetectionEnabled();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.Lifecycle#start()
	 */
	@Override
	public synchronized void start() throws SQLException
	{
		if (this.active) return;
		
		this.lifecycleManager.start();
		this.scheduler.initialize();
		
		logger.log(Level.INFO, messages.start(this));
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.Lifecycle#stop()
	 */
	@Override
	public synchronized void stop()
	{
		logger.log(Level.INFO, messages.stop(this));
		
		this.active = false;
		
		this.scheduler.shutdown();
		this.lifecycleManager.stop();
	}

	boolean isAlive(D database, Level level)
	{
		return this.activationHandler.isAlive(database, level);
	}

	boolean activateWithStrategy(D database, SynchronizationStrategy strategy) throws SQLException, InterruptedException
	{
		return this.activationHandler.activate(database, strategy);
	}

	/**
	 * Private inner class that manages the lifecycle (start/stop) of this database cluster.
	 * As an inner class, it has direct access to all private fields of DatabaseClusterImpl,
	 * eliminating the need for package-private setters that break encapsulation.
	 */
	private class LifecycleManager
	{
		private final Logger logger = LoggerFactory.getLogger(LifecycleManager.class);

		void start() throws SQLException
		{
			try
			{
				initializeComponents();
				initializeDatabases();
				initializeRecovery();
				initializeCache();
				registerMBeans();

				DatabaseClusterImpl.this.active = true;

				logger.log(Level.INFO, messages.start(DatabaseClusterImpl.this));
			}
			catch (SQLException e)
			{
				cleanupOnFailure();
				throw e;
			}
		}

		void stop()
		{
			logger.log(Level.INFO, messages.stop(DatabaseClusterImpl.this));

			DatabaseClusterImpl.this.active = false;

			deactivateEmbeddedDatabases();
			unregisterMBeans();
			stopStateManager();
			stopLockManager();
			shutdownExecutor();
			clearBalancer();
		}

		private void cleanupOnFailure()
		{
			logger.log(Level.WARN, "Initialization failed - cleaning up resources");

			unregisterMBeans();
			shutdownExecutor();
			stopStateManager();
			stopLockManager();
			clearBalancer();
		}

		private void initializeComponents() throws SQLException
		{
			CommandDispatcherFactory dispatcherFactory = DatabaseClusterImpl.this.configuration.getDispatcherFactory();

			DatabaseClusterImpl.this.decoder = DatabaseClusterImpl.this.configuration.getDecoderFactory().createDecoder(DatabaseClusterImpl.this.getId());
			LockManager lockManager = DatabaseClusterImpl.this.configuration.getLockManagerFactory().createLockManager();
			StateManager stateManager = DatabaseClusterImpl.this.configuration.getStateManagerFactory().createStateManager(DatabaseClusterImpl.this);

			if (dispatcherFactory != null)
			{
				try
				{
					lockManager = new DistributedLockManager(DatabaseClusterImpl.this, dispatcherFactory, DatabaseClusterImpl.this.configuration.getLockTimeoutMs());
					stateManager = new DistributedStateManager<>(DatabaseClusterImpl.this, dispatcherFactory);
				}
				catch (Exception e)
				{
					throw new SQLException(e);
				}
			}

			DatabaseClusterImpl.this.lockManager = lockManager;
			DatabaseClusterImpl.this.stateManager = stateManager;
			DatabaseClusterImpl.this.balancer = DatabaseClusterImpl.this.configuration.getBalancerFactory().createBalancer(new java.util.TreeSet<D>());
			DatabaseClusterImpl.this.dialect = DatabaseClusterImpl.this.configuration.getDialectFactory().createDialect();
			DatabaseClusterImpl.this.durability = DatabaseClusterImpl.this.configuration.getDurabilityFactory().createDurability(DatabaseClusterImpl.this);
			DatabaseClusterImpl.this.executor = DatabaseClusterImpl.this.configuration.getExecutorProvider().getExecutor(DatabaseClusterImpl.this.configuration.getThreadFactory());
			DatabaseClusterImpl.this.sinkSourceFactory = DatabaseClusterImpl.this.configuration.getInputSinkProvider().createInputSinkStrategy();
			DatabaseClusterImpl.this.txIdentifierFactory = (dispatcherFactory != null) ? new UUIDTransactionIdentifierFactory() : new SimpleTransactionIdentifierFactory();

			lockManager.start();
			stateManager.start();
		}

		private void initializeDatabases()
		{
			Set<String> databases = DatabaseClusterImpl.this.stateManager.getActiveDatabases();

			if (!databases.isEmpty())
			{
				messages.initialClusterState(DatabaseClusterImpl.this, databases);

				for (String databaseId: databases)
				{
					D database = DatabaseClusterImpl.this.getDatabase(databaseId);
					DatabaseClusterImpl.this.balancer.add(database);
				}
			}
			else
			{
				messages.initialClusterStateEmpty(DatabaseClusterImpl.this);

				for (D database: DatabaseClusterImpl.this.configuration.getDatabaseMap().values())
				{
					if (DatabaseClusterImpl.this.isAlive(database, Level.WARN))
					{
						DatabaseClusterImpl.this.activate(database, DatabaseClusterImpl.this.stateManager);
					}
				}
			}
		}

		private void initializeRecovery()
		{
			var invokers = DatabaseClusterImpl.this.stateManager.recover();
			if (!invokers.isEmpty())
			{
				DatabaseClusterImpl.this.getDurability().recover(invokers);
			}
		}

		private void initializeCache()
		{
			var cache = DatabaseClusterImpl.this.configuration.getDatabaseMetaDataCacheFactory().createCache(DatabaseClusterImpl.this);
			DatabaseClusterImpl.this.databaseMetaDataCache = cache;

			try
			{
				DatabaseClusterImpl.this.flushMetaDataCache();
			}
			catch (IllegalStateException e)
			{
				// Ignore - cache will initialize lazily.
			}
		}

		private void registerMBeans() throws SQLException
		{
			MBeanRegistrar<Z, D> registrar = DatabaseClusterImpl.this.configuration.getMBeanRegistrarFactory().createMBeanRegistrar();
			DatabaseClusterImpl.this.registrar = registrar;

			try
			{
				registrar.register(DatabaseClusterImpl.this);

				for (D database: DatabaseClusterImpl.this.configuration.getDatabaseMap().values())
				{
					registrar.register(DatabaseClusterImpl.this, database);
				}
			}
			catch (JMException e)
			{
				throw new SQLException(e);
			}
		}

		private void deactivateEmbeddedDatabases()
		{
			Balancer<Z, D> balancer = DatabaseClusterImpl.this.balancer;
			if (balancer != null)
			{
				for (D database: balancer)
				{
					if (database.getLocality() == Locality.EMBEDDED)
					{
						DatabaseClusterImpl.this.deactivate(database, DatabaseClusterImpl.this.stateManager);
					}
				}
			}
		}

		private void unregisterMBeans()
		{
			try
			{
				MBeanRegistrar<Z, D> registrar = DatabaseClusterImpl.this.registrar;
				if (registrar != null)
				{
					registrar.unregister(DatabaseClusterImpl.this);

					for (D database: DatabaseClusterImpl.this.configuration.getDatabaseMap().values())
					{
						registrar.unregister(DatabaseClusterImpl.this, database);
					}
				}
			}
			catch (Exception e)
			{
				logger.log(Level.WARN, e, "Failed to unregister MBeans");
			}
		}

		private void stopStateManager()
		{
			try
			{
				StateManager stateManager = DatabaseClusterImpl.this.stateManager;
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

		private void stopLockManager()
		{
			try
			{
				LockManager lockManager = DatabaseClusterImpl.this.lockManager;
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

		private void shutdownExecutor()
		{
			var executor = DatabaseClusterImpl.this.executor;
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

		private void clearBalancer()
		{
			Balancer<Z, D> balancer = DatabaseClusterImpl.this.balancer;
			if (balancer != null)
			{
				balancer.clear();
			}
		}
	}
}
