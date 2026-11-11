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
package net.sf.hajdbc;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;

import net.sf.hajdbc.balancer.BalancerFactory;
import net.sf.hajdbc.cache.DatabaseMetaDataCacheFactory;
import net.sf.hajdbc.codec.DecoderFactory;
import net.sf.hajdbc.dialect.DialectFactory;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.durability.DurabilityFactory;
import net.sf.hajdbc.io.InputSinkProvider;
import net.sf.hajdbc.lock.LockManagerFactory;
import net.sf.hajdbc.management.MBeanRegistrarFactory;
import net.sf.hajdbc.state.StateManagerFactory;
import net.sf.hajdbc.util.concurrent.cron.CronExpression;
import org.jspecify.annotations.NonNull;

/**
 * @author Paul Ferraro
 */
public interface DatabaseClusterConfiguration<Z, D extends Database<Z>>
{
	@NonNull CommandDispatcherFactory getDispatcherFactory();
	
	/**
	 * Returns the databases of this cluster indexed by identifier.
	 * @return map of databases
	 * @throws IllegalArgumentException if no database exists with the specified identifier
	 */
	@NonNull ConcurrentMap<String, D> getDatabaseMap();
	
	@NonNull Map<String, SynchronizationStrategy> getSynchronizationStrategyMap();
	
	@NonNull String getDefaultSynchronizationStrategy();
	
	/**
	 * Returns the Balancer implementation used by this database cluster.
	 * @return the <code>Balancer</code> implementation
	 */
	@NonNull BalancerFactory getBalancerFactory();

	@NonNull TransactionMode getTransactionMode();
	
	@NonNull ExecutorServiceProvider getExecutorProvider();
	
	/**
	 * Returns the Dialect that can return database vendor-specific values.
	 * @return the <code>Dialect</code> implementation
	 */
	@NonNull DialectFactory getDialectFactory();
	
	/**
	 * Returns the StateManager for persisting the state of the database cluster.
	 * @return the StateManager implementation
	 */
	@NonNull StateManagerFactory getStateManagerFactory();
	
	/**
	 * Returns the DatabaseMetaData cache.
	 * @return the <code>DatabaseMetaDataCache</code> implementation
	 */
	@NonNull DatabaseMetaDataCacheFactory getDatabaseMetaDataCacheFactory();

	@NonNull DurabilityFactory getDurabilityFactory();

	@NonNull LockManagerFactory getLockManagerFactory();
	
	/**
	 * Indicates whether or not sequence detection is enabled for this cluster.
	 * @return true, if sequence detection is enabled, false otherwise.
	 */
	boolean isSequenceDetectionEnabled();
	
	/**
	 * Indicates whether or not identity column detection is enabled for this cluster.
	 * @return true, if identity column detection is enabled, false otherwise.
	 */
	boolean isIdentityColumnDetectionEnabled();
	
	/**
	 * Indicates whether or not non-deterministic CURRENT_DATE SQL functions will be evaluated to deterministic static values.
	 * @return true, if temporal SQL replacement is enabled, false otherwise.
	 */
	boolean isCurrentDateEvaluationEnabled();
	
	/**
	 * Indicates whether or not non-deterministic CURRENT_TIME functions will be evaluated to deterministic static values.
	 * @return true, if temporal SQL replacement is enabled, false otherwise.
	 */
	boolean isCurrentTimeEvaluationEnabled();
	
	/**
	 * Indicates whether or not non-deterministic CURRENT_TIMESTAMP functions will be evaluated to deterministic static values.
	 * @return true, if temporal SQL replacement is enabled, false otherwise.
	 */
	boolean isCurrentTimestampEvaluationEnabled();
	
	/**
	 * Indicates whether or not non-deterministic RAND() functions will be replaced by evaluated to static values.
	 * @return true, if temporal SQL replacement is enabled, false otherwise.
	 */
	boolean isRandEvaluationEnabled();

	@NonNull CronExpression getFailureDetectionExpression();
	
	@NonNull CronExpression getAutoActivationExpression();
	
	@NonNull ThreadFactory getThreadFactory();
	
	@NonNull DecoderFactory getDecoderFactory();
	
	@NonNull MBeanRegistrarFactory getMBeanRegistrarFactory();
	
	boolean isEmptyClusterAllowed();

	@NonNull InputSinkProvider getInputSinkProvider();

	/**
	 * Returns the distributed lock acquisition timeout in milliseconds.
	 * @return lock timeout in milliseconds
	 */
	long getLockTimeoutMs();
}
