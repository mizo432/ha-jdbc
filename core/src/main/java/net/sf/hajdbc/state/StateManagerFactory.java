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
package net.sf.hajdbc.state;

import java.io.Serializable;
import java.sql.SQLException;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.Identifiable;

/**
 * Factory for creating {@link StateManager} instances.
 * <p>
 * This factory is a core component of the HA-JDBC architecture, registered via
 * {@link DatabaseClusterConfiguration#getStateManagerFactory()}. The state manager
 * persists the activation/deactivation state of the database cluster, enabling
 * failover and recovery. In a distributed setup, a {@link net.sf.hajdbc.state.distributed.DistributedStateManager}
 * is used to synchronize state across nodes.
 * </p>
 *
 * @see StateManager
 * @see DatabaseClusterConfiguration#getStateManagerFactory()
 * @see net.sf.hajdbc.state.sql.SQLStateManagerFactory
 * @see net.sf.hajdbc.state.distributed.DistributedStateManager
 */
public interface StateManagerFactory extends Identifiable, Serializable
{
	/**
	 * Creates a new state manager for the specified database cluster.
	 * @param cluster the database cluster
	 * @return new state manager
	 * @throws SQLException if state manager creation fails
	 */
	<Z, D extends Database<Z>> StateManager createStateManager(DatabaseCluster<Z, D> cluster) throws SQLException;
}
