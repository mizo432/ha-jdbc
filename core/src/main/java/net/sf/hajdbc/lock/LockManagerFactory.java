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
package net.sf.hajdbc.lock;

import java.io.Serializable;

import net.sf.hajdbc.Identifiable;

/**
 * Factory for creating {@link LockManager} instances.
 * <p>
 * This factory is a core component of the HA-JDBC architecture, registered via
 * {@link net.sf.hajdbc.DatabaseClusterConfiguration#getLockManagerFactory()}. The lock manager
 * provides distributed locking to ensure that only one node activates a database at a time,
 * preventing split-brain scenarios. In a distributed setup, a
 * {@link net.sf.hajdbc.lock.distributed.DistributedLockManager} backed by Zookeeper is used.
 * </p>
 *
 * @see LockManager
 * @see net.sf.hajdbc.DatabaseClusterConfiguration#getLockManagerFactory()
 * @see net.sf.hajdbc.lock.distributed.DistributedLockManager
 */
public interface LockManagerFactory extends Identifiable, Serializable
{
	/**
	 * Creates a new lock manager.
	 * @return new lock manager
	 */
	LockManager createLockManager();
}
