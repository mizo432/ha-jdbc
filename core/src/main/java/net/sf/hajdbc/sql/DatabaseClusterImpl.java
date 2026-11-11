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

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.DatabaseClusterConfiguration;
import net.sf.hajdbc.DatabaseClusterConfigurationListener;
import net.sf.hajdbc.DatabaseClusterListener;
import net.sf.hajdbc.SynchronizationListener;
import net.sf.hajdbc.SynchronizationStrategy;
import net.sf.hajdbc.TransactionMode;
import net.sf.hajdbc.balancer.Balancer;
import net.sf.hajdbc.cache.DatabaseMetaDataCache;
import net.sf.hajdbc.codec.Decoder;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.durability.Durability;
import net.sf.hajdbc.io.InputSinkStrategy;
import net.sf.hajdbc.lock.LockManager;
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
import net.sf.hajdbc.tx.TransactionIdentifierFactory;

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
	private final DatabaseClusterLifecycleManager<Z, D> lifecycleManager;
	private final DatabaseClusterScheduler<Z, D> scheduler;
	private final DatabaseClusterActivationHandler<Z, D> activationHandler;
	private final DatabaseClusterOperations<Z, D> operations;

	// Package-private setters for lifecycle manager
	void setDecoder(Decoder decoder) { this.decoder = decoder; }
	void setLockManager(LockManager lockManager) { this.lockManager = lockManager; }
	void setStateManager(StateManager stateManager) { this.stateManager = stateManager; }
	void setBalancer(Balancer<Z, D> balancer) { this.balancer = balancer; }
	void setDialect(Dialect dialect) { this.dialect = dialect; }
	void setDurability(Durability<Z, D> durability) { this.durability = durability; }
	void setExecutor(ExecutorService executor) { this.executor = executor; }
	void setInputSinkStrategy(InputSinkStrategy<? extends Object> strategy) { this.sinkSourceFactory = strategy; }
	void setTransactionIdentifierFactory(TransactionIdentifierFactory<? extends Object> factory) { this.txIdentifierFactory = factory; }
	void setDatabaseMetaDataCache(DatabaseMetaDataCache<Z, D> cache) { this.databaseMetaDataCache = cache; }
	void setRegistrar(MBeanRegistrar<Z, D> registrar) { this.registrar = registrar; }
	void setActive(boolean active) { this.active = active; }

	// Package-private getters for lifecycle manager
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
		this.lifecycleManager = new DatabaseClusterLifecycleManager<>(this, configuration);
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
		
		this.lifecycleManager.start(this);
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
		this.lifecycleManager.stop(this);
	}

	boolean isAlive(D database, Level level)
	{
		return this.activationHandler.isAlive(database, level);
	}

	boolean activateWithStrategy(D database, SynchronizationStrategy strategy) throws SQLException, InterruptedException
	{
		return this.activationHandler.activate(database, strategy);
	}

}
