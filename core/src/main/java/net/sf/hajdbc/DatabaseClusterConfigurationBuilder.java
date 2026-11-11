/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2014  Paul Ferraro
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
package net.sf.hajdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.sf.hajdbc.balancer.BalancerFactory;
import net.sf.hajdbc.balancer.load.LoadBalancerFactory;
import net.sf.hajdbc.cache.DatabaseMetaDataCacheFactory;
import net.sf.hajdbc.cache.eager.EagerDatabaseMetaDataCacheFactory;
import net.sf.hajdbc.codec.DecoderFactory;
import net.sf.hajdbc.codec.MultiplexingDecoderFactory;
import net.sf.hajdbc.configuration.Builder;
import net.sf.hajdbc.configuration.ServiceBuilder;
import net.sf.hajdbc.configuration.SimpleBuilder;
import net.sf.hajdbc.configuration.SimpleServiceBuilder;
import net.sf.hajdbc.dialect.DialectFactory;
import net.sf.hajdbc.dialect.StandardDialectFactory;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.durability.DurabilityFactory;
import net.sf.hajdbc.durability.coarse.CoarseDurabilityFactory;
import net.sf.hajdbc.io.InputSinkProvider;
import net.sf.hajdbc.io.file.FileInputSinkProvider;
import net.sf.hajdbc.lock.LockManagerFactory;
import net.sf.hajdbc.lock.semaphore.SemaphoreLockManagerFactory;
import net.sf.hajdbc.management.DefaultMBeanRegistrarFactory;
import net.sf.hajdbc.management.MBeanRegistrarFactory;
import net.sf.hajdbc.messages.Messages;
import net.sf.hajdbc.messages.MessagesFactory;
import net.sf.hajdbc.sql.DefaultExecutorServiceProvider;
import net.sf.hajdbc.sql.TransactionModeEnum;
import net.sf.hajdbc.state.StateManagerFactory;
import net.sf.hajdbc.state.sql.SQLStateManagerFactory;
import net.sf.hajdbc.util.concurrent.cron.CronExpression;
import net.sf.hajdbc.util.concurrent.cron.CronExpressionBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DatabaseClusterConfigurationBuilder<Z, D extends Database<Z>, B extends DatabaseBuilder<Z, D>> implements Builder<DatabaseClusterConfiguration<Z, D>>
{
	private static final Messages messages = MessagesFactory.getMessages();

	private final DatabaseBuilderFactory<Z, D, B> factory;
	
	private final List<Builder<SynchronizationStrategy>> synchronizationStrategyBuilders = new LinkedList<>();
	private final List<Builder<D>> databaseBuilders = new LinkedList<>();

	private Builder<CommandDispatcherFactory> commandDispatcherFactoryBuilder;
	private Builder<StateManagerFactory> stateManagerFactoryBuilder = new SimpleBuilder<>(new SQLStateManagerFactory());
	private Builder<LockManagerFactory> lockManagerFactoryBuilder = new SimpleBuilder<>(new SemaphoreLockManagerFactory());
	private Builder<BalancerFactory> balancerFactoryBuilder = new SimpleBuilder<>(new LoadBalancerFactory());
	private Builder<DialectFactory> dialectFactoryBuilder = new SimpleBuilder<>(new StandardDialectFactory());
	private Builder<DurabilityFactory> durabilityFactoryBuilder = new SimpleBuilder<>(new CoarseDurabilityFactory());
	private Builder<InputSinkProvider> inputSinkProviderBuilder = new SimpleBuilder<>(new FileInputSinkProvider());
	private Builder<DatabaseMetaDataCacheFactory> metaDataCacheFactoryBuilder = new SimpleBuilder<>(new EagerDatabaseMetaDataCacheFactory());

	private Builder<DecoderFactory> decoderFactoryBuilder = new SimpleBuilder<>(new MultiplexingDecoderFactory());
	private Builder<MBeanRegistrarFactory> mbeanRegistrarFactoryBuilder = new SimpleBuilder<>(new DefaultMBeanRegistrarFactory());
	private Builder<ThreadFactory> threadFactoryBuilder = new SimpleBuilder<>(Executors.defaultThreadFactory());
	private Builder<ExecutorServiceProvider> executorProviderBuilder = new SimpleBuilder<>(new DefaultExecutorServiceProvider());

	private CronExpressionBuilder autoActivateScheduleBuilder = new CronExpressionBuilder();
	private CronExpressionBuilder failureDetectScheduleBuilder = new CronExpressionBuilder();
	
	private String defaultSynchronizationStrategy;
	private TransactionMode transactionMode = TransactionModeEnum.SERIAL;
	private boolean evalCurrentDate = false;
	private boolean evalCurrentTime = false;
	private boolean evalCurrentTimestamp = false;
	private boolean evalRand = false;
	private boolean detectIdentityColumns = false;
	private boolean detectSequences = false;
	private boolean allowEmptyCluster = false;
	private long lockTimeoutMs = 5 * 60 * 1000; // 5 minutes default
	
	protected DatabaseClusterConfigurationBuilder(DatabaseBuilderFactory<Z, D, B> factory)
	{
		this.factory = factory;
	}

	public @NonNull ServiceBuilder<CommandDispatcherFactory> distributable(@NonNull String id)
	{
		ServiceBuilder<CommandDispatcherFactory> builder = new ServiceBuilder<>(CommandDispatcherFactory.class, id);
		this.commandDispatcherFactoryBuilder = builder;
		return builder;
	}
	
	public <T extends Builder<CommandDispatcherFactory>> @NonNull T distributable(@NonNull Class<T> builderClass)
	{
		try
		{
			T builder = builderClass.newInstance();
			this.commandDispatcherFactoryBuilder = builder;
			return builder;
		}
		catch (IllegalAccessException | InstantiationException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> distributable(@Nullable CommandDispatcherFactory factory)
	{
		this.commandDispatcherFactoryBuilder = new SimpleBuilder<>(factory);
		return this;
	}

	public @NonNull ServiceBuilder<StateManagerFactory> state(@NonNull String id)
	{
		ServiceBuilder<StateManagerFactory> builder = new ServiceBuilder<>(StateManagerFactory.class, id);
		this.stateManagerFactoryBuilder = builder;
		return builder;
	}

	public <T extends Builder<StateManagerFactory>> @NonNull T state(@NonNull Class<T> builderClass)
	{
		try
		{
			T builder = builderClass.newInstance();
			this.stateManagerFactoryBuilder = builder;
			return builder;
		}
		catch (IllegalAccessException | InstantiationException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> state(@NonNull StateManagerFactory factory)
	{
		this.stateManagerFactoryBuilder = new SimpleBuilder<>(factory);
		return this;
	}

	public @NonNull ServiceBuilder<LockManagerFactory> lock(@NonNull String id)
	{
		ServiceBuilder<LockManagerFactory> builder = new ServiceBuilder<>(LockManagerFactory.class, id);
		this.lockManagerFactoryBuilder = builder;
		return builder;
	}

	public <T extends Builder<LockManagerFactory>> @NonNull T lock(@NonNull Class<T> builderClass)
	{
		try
		{
			T builder = builderClass.newInstance();
			this.lockManagerFactoryBuilder = builder;
			return builder;
		}
		catch (IllegalAccessException | InstantiationException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> lock(@NonNull LockManagerFactory factory)
	{
		this.lockManagerFactoryBuilder = new SimpleBuilder<>(factory);
		return this;
	}

	public @NonNull ServiceBuilder<SynchronizationStrategy> addSynchronizationStrategy(@NonNull String id)
	{
		ServiceBuilder<SynchronizationStrategy> builder = new ServiceBuilder<>(SynchronizationStrategy.class, id);
		this.synchronizationStrategyBuilders.add(builder);
		return builder;
	}

	public <T extends Builder<SynchronizationStrategy>> @NonNull T addSynchronizationStrategy(@NonNull Class<T> builderClass)
	{
		try
		{
			T builder = builderClass.newInstance();
			this.synchronizationStrategyBuilders.add(builder);
			return builder;
		}
		catch (IllegalAccessException | InstantiationException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> addSynchronizationStrategy(@NonNull SynchronizationStrategy strategy)
	{
		this.synchronizationStrategyBuilders.add(new SimpleBuilder<>(strategy));
		return this;
	}

	public @NonNull B addDatabase(@NonNull String id)
	{
		B builder = this.factory.createBuilder(id);
		this.databaseBuilders.add(builder);
		return builder;
	}
	
	public <T extends Builder<ExecutorServiceProvider>> @NonNull T executor(@NonNull Class<T> builderClass)
	{
		try
		{
			T builder = builderClass.newInstance();
			this.executorProviderBuilder = builder;
			return builder;
		}
		catch (IllegalAccessException | InstantiationException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> executor(@NonNull ExecutorServiceProvider provider)
	{
		this.executorProviderBuilder = new SimpleBuilder<>(provider);
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> balancer(@NonNull String id)
	{
		this.balancerFactoryBuilder = new SimpleServiceBuilder<>(BalancerFactory.class, id);
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> balancer(@NonNull BalancerFactory factory)
	{
		this.balancerFactoryBuilder = new SimpleBuilder<>(factory);
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> dialect(@NonNull String id)
	{
		this.dialectFactoryBuilder = new SimpleServiceBuilder<>(DialectFactory.class, id);
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> dialect(@NonNull DialectFactory factory)
	{
		this.dialectFactoryBuilder = new SimpleBuilder<>(factory);
		return this;
	}
	
	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> durability(@NonNull String id)
	{
		this.durabilityFactoryBuilder = new SimpleServiceBuilder<>(DurabilityFactory.class, id);
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> durability(@NonNull DurabilityFactory factory)
	{
		this.durabilityFactoryBuilder = new SimpleBuilder<>(factory);
		return this;
	}
	
	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> metaDataCache(@NonNull String id)
	{
		this.metaDataCacheFactoryBuilder = new SimpleServiceBuilder<>(DatabaseMetaDataCacheFactory.class, id);
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> metaDataCache(@NonNull DatabaseMetaDataCacheFactory factory)
	{
		this.metaDataCacheFactoryBuilder = new SimpleBuilder<>(factory);
		return this;
	}
	
	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> inputSink(@NonNull String id)
	{
		this.inputSinkProviderBuilder = new SimpleServiceBuilder<>(InputSinkProvider.class, id);
		return this;
	}
	
	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> inputSink(@NonNull InputSinkProvider factory)
	{
		this.inputSinkProviderBuilder = new SimpleBuilder<>(factory);
		return this;
	}
	
	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> decoder(@NonNull DecoderFactory factory)
	{
		this.decoderFactoryBuilder = new SimpleBuilder<>(factory);
		return this;
	}
	
	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> mbeanRegistrar(@NonNull MBeanRegistrarFactory factory)
	{
		this.mbeanRegistrarFactoryBuilder = new SimpleBuilder<>(factory);
		return this;
	}
	
	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> defaultSynchronizationStrategy(@NonNull String id)
	{
		this.defaultSynchronizationStrategy = id;
		return this;
	}
	
	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> transactionMode(@NonNull TransactionMode transactionMode)
	{
		this.transactionMode = transactionMode;
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> autoActivateSchedule(@NonNull String schedule)
	{
		this.autoActivateScheduleBuilder.expression(schedule);
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> failureDetectSchedule(@NonNull String schedule)
	{
		this.failureDetectScheduleBuilder.expression(schedule);
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> evalCurrentDate(boolean enabled)
	{
		this.evalCurrentDate = enabled;
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> evalCurrentTime(boolean enabled)
	{
		this.evalCurrentTime = enabled;
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> evalCurrentTimestamp(boolean enabled)
	{
		this.evalCurrentTimestamp = enabled;
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> evalRand(boolean enabled)
	{
		this.evalRand = enabled;
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> detectIdentityColumns(boolean enabled)
	{
		this.detectIdentityColumns = enabled;
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> detectSequences(boolean enabled)
	{
		this.detectSequences = enabled;
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> allowEmptyCluster(boolean enabled)
	{
		this.allowEmptyCluster = enabled;
		return this;
	}

	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> lockTimeoutMs(long lockTimeoutMs)
	{
		this.lockTimeoutMs = lockTimeoutMs;
		return this;
	}

	@Override
	public @NonNull DatabaseClusterConfigurationBuilder<Z, D, B> read(@NonNull DatabaseClusterConfiguration<Z, D> configuration)
	{
		return this;
	}

	@Override
	public @NonNull DatabaseClusterConfiguration<Z, D> build() throws SQLException
	{
		final CommandDispatcherFactory commandDispatcherFactory = (this.commandDispatcherFactoryBuilder != null) ? this.commandDispatcherFactoryBuilder.build() : null;
		final StateManagerFactory stateManagerFactory = this.stateManagerFactoryBuilder.build();
		final LockManagerFactory lockManagerFactory = this.lockManagerFactoryBuilder.build();
		final BalancerFactory balancerFactory = this.balancerFactoryBuilder.build();
		final DialectFactory dialectFactory = this.dialectFactoryBuilder.build();
		final DurabilityFactory durabilityFactory = this.durabilityFactoryBuilder.build();
		final InputSinkProvider inputSinkProvider = this.inputSinkProviderBuilder.build();
		final DatabaseMetaDataCacheFactory metaDataCacheFactory = this.metaDataCacheFactoryBuilder.build();
		final DecoderFactory decoderFactory = this.decoderFactoryBuilder.build();
		final MBeanRegistrarFactory mbeanRegistrarFactory = this.mbeanRegistrarFactoryBuilder.build();
		final ThreadFactory threadFactory = this.threadFactoryBuilder.build();
		final ExecutorServiceProvider executorServiceProvider = this.executorProviderBuilder.build();
		final CronExpression autoActivateSchedule = this.autoActivateScheduleBuilder.build();
		final CronExpression failureDetectSchedule = this.failureDetectScheduleBuilder.build();
		
		final String defaultSynchronizationStrategy = this.defaultSynchronizationStrategy;
		final TransactionMode transactionMode = this.transactionMode;
		final boolean evalCurrentDate = this.evalCurrentDate;
		final boolean evalCurrentTime = this.evalCurrentTime;
		final boolean evalCurrentTimestamp = this.evalCurrentTimestamp;
		final boolean evalRand = this.evalRand;
		final boolean detectIdentityColumns = this.detectIdentityColumns;
		final boolean detectSequences = this.detectSequences;
		final boolean allowEmptyCluster = this.allowEmptyCluster;
		final long lockTimeoutMs = this.lockTimeoutMs;
		
		if (this.synchronizationStrategyBuilders.isEmpty())
		{
			throw new SQLException(messages.noSyncStrategies());
		}
		
		final Map<String, SynchronizationStrategy> syncStrategies = new HashMap<>();
		for (Builder<SynchronizationStrategy> builder: this.synchronizationStrategyBuilders)
		{
			SynchronizationStrategy strategy = builder.build();
			syncStrategies.put(strategy.getId(), strategy);
		}
		
		if (!syncStrategies.containsKey(defaultSynchronizationStrategy))
		{
			throw new SQLException(messages.invalidSyncStrategy(defaultSynchronizationStrategy, syncStrategies.keySet()));
		}
		
		if (this.databaseBuilders.isEmpty())
		{
			throw new SQLException(messages.noDatabases());
		}
		
		final ConcurrentMap<String, D> databases = new ConcurrentHashMap<>();
		for (Builder<D> builder: this.databaseBuilders)
		{
			D database = builder.build();
			databases.put(database.getId(), database);
		}
		
		return new DatabaseClusterConfiguration<Z, D>()
		{
			@Override
			public CommandDispatcherFactory getDispatcherFactory()
			{
				return commandDispatcherFactory;
			}

			@Override
			public ConcurrentMap<String, D> getDatabaseMap()
			{
				return databases;
			}

			@Override
			public Map<String, SynchronizationStrategy> getSynchronizationStrategyMap()
			{
				return syncStrategies;
			}

			@Override
			public String getDefaultSynchronizationStrategy()
			{
				return defaultSynchronizationStrategy;
			}

			@Override
			public BalancerFactory getBalancerFactory()
			{
				return balancerFactory;
			}

			@Override
			public TransactionMode getTransactionMode()
			{
				return transactionMode;
			}

			@Override
			public ExecutorServiceProvider getExecutorProvider()
			{
				return executorServiceProvider;
			}

			@Override
			public DialectFactory getDialectFactory()
			{
				return dialectFactory;
			}

			@Override
			public StateManagerFactory getStateManagerFactory()
			{
				return stateManagerFactory;
			}

			@Override
			public DatabaseMetaDataCacheFactory getDatabaseMetaDataCacheFactory()
			{
				return metaDataCacheFactory;
			}

			@Override
			public DurabilityFactory getDurabilityFactory()
			{
				return durabilityFactory;
			}

			@Override
			public LockManagerFactory getLockManagerFactory()
			{
				return lockManagerFactory;
			}

			@Override
			public boolean isSequenceDetectionEnabled()
			{
				return detectSequences;
			}

			@Override
			public boolean isIdentityColumnDetectionEnabled()
			{
				return detectIdentityColumns;
			}

			@Override
			public boolean isCurrentDateEvaluationEnabled()
			{
				return evalCurrentDate;
			}

			@Override
			public boolean isCurrentTimeEvaluationEnabled()
			{
				return evalCurrentTime;
			}

			@Override
			public boolean isCurrentTimestampEvaluationEnabled()
			{
				return evalCurrentTimestamp;
			}

			@Override
			public boolean isRandEvaluationEnabled()
			{
				return evalRand;
			}

			@Override
			public CronExpression getFailureDetectionExpression()
			{
				return failureDetectSchedule;
			}

			@Override
			public CronExpression getAutoActivationExpression()
			{
				return autoActivateSchedule;
			}

			@Override
			public ThreadFactory getThreadFactory()
			{
				return threadFactory;
			}

			@Override
			public DecoderFactory getDecoderFactory()
			{
				return decoderFactory;
			}

			@Override
			public MBeanRegistrarFactory getMBeanRegistrarFactory()
			{
				return mbeanRegistrarFactory;
			}

			@Override
			public boolean isEmptyClusterAllowed()
			{
				return allowEmptyCluster;
			}

 		@Override
 		public InputSinkProvider getInputSinkProvider()
 		{
 			return inputSinkProvider;
 		}

 		@Override
 		public long getLockTimeoutMs()
 		{
 			return lockTimeoutMs;
 		}
 	};
 }
}
