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

import net.sf.hajdbc.balancer.BalancerFactory;
import net.sf.hajdbc.dialect.DialectFactory;
import net.sf.hajdbc.durability.DurabilityFactory;
import net.sf.hajdbc.lock.LockManagerFactory;
import net.sf.hajdbc.state.StateManagerFactory;
import net.sf.hajdbc.cache.DatabaseMetaDataCacheFactory;
import net.sf.hajdbc.codec.DecoderFactory;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.management.MBeanRegistrarFactory;
import org.jspecify.annotations.NonNull;

/**
 * Provides factory instances for creating database cluster components.
 * This interface isolates factory configuration from other configuration concerns,
 * following the Interface Segregation Principle.
 *
 * @author Paul Ferraro
 * @since 3.2
 */
public interface DatabaseClusterFactoryConfiguration
{
	/**
	 * Returns the command dispatcher factory for distributed communication.
	 * @return command dispatcher factory
	 */
	@NonNull CommandDispatcherFactory getDispatcherFactory();

	/**
	 * Returns the balancer factory for load balancing.
	 * @return balancer factory
	 */
	@NonNull BalancerFactory getBalancerFactory();

	/**
	 * Returns the dialect factory for database vendor-specific SQL.
	 * @return dialect factory
	 */
	@NonNull DialectFactory getDialectFactory();

	/**
	 * Returns the state manager factory for persisting cluster state.
	 * @return state manager factory
	 */
	@NonNull StateManagerFactory getStateManagerFactory();

	/**
	 * Returns the database metadata cache factory.
	 * @return metadata cache factory
	 */
	@NonNull DatabaseMetaDataCacheFactory getDatabaseMetaDataCacheFactory();

	/**
	 * Returns the durability factory for invocation persistence.
	 * @return durability factory
	 */
	@NonNull DurabilityFactory getDurabilityFactory();

	/**
	 * Returns the lock manager factory for distributed locking.
	 * @return lock manager factory
	 */
	@NonNull LockManagerFactory getLockManagerFactory();

	/**
	 * Returns the decoder factory for state deserialization.
	 * @return decoder factory
	 */
	@NonNull DecoderFactory getDecoderFactory();

	/**
	 * Returns the MBean registrar factory for JMX management.
	 * @return MBean registrar factory
	 */
	@NonNull MBeanRegistrarFactory getMBeanRegistrarFactory();
}

