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

import org.jspecify.annotations.NonNull;

/**
 * Configuration for a database cluster.
 * This interface extends four sub-interfaces that isolate distinct configuration concerns:
 * <ul>
 *   <li>{@link DatabaseClusterFactoryConfiguration} — factory instances for cluster components</li>
 *   <li>{@link DatabaseClusterTemporalEvaluationConfiguration} — temporal SQL evaluation settings</li>
 *   <li>{@link DatabaseClusterDetectionConfiguration} — database feature detection settings</li>
 *   <li>{@link DatabaseClusterSchedulingConfiguration} — scheduling and operational settings</li>
 * </ul>
 * Components should depend on the smallest sub-interface they need rather than this aggregate interface.
 *
 * @author Paul Ferraro
 * @param <Z> database connection source
 * @param <D> database descriptor
 */
public interface DatabaseClusterConfiguration<Z, D extends Database<Z>> extends
    DatabaseClusterFactoryConfiguration,
    DatabaseClusterTemporalEvaluationConfiguration,
    DatabaseClusterDetectionConfiguration,
    DatabaseClusterSchedulingConfiguration
{
	/**
	 * Returns the databases of this cluster indexed by identifier.
	 * @return map of databases
	 * @throws IllegalArgumentException if no database exists with the specified identifier
	 */
	@NonNull ConcurrentMap<String, D> getDatabaseMap();

	/**
	 * Returns the synchronization strategies indexed by name.
	 * @return map of synchronization strategies
	 */
	@NonNull Map<String, SynchronizationStrategy> getSynchronizationStrategyMap();

	/**
	 * Returns the default synchronization strategy name.
	 * @return default synchronization strategy name
	 */
	@NonNull String getDefaultSynchronizationStrategy();

	/**
	 * Returns the transaction mode for this cluster.
	 * @return transaction mode
	 */
	@NonNull TransactionMode getTransactionMode();

	/**
	 * Returns the executor service provider for async operations.
	 * @return executor service provider
	 */
	@NonNull ExecutorServiceProvider getExecutorProvider();
}
