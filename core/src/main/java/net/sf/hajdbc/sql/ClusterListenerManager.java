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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseClusterConfigurationListener;
import net.sf.hajdbc.DatabaseClusterListener;
import net.sf.hajdbc.SynchronizationListener;

/**
 * Manages listeners for a database cluster.
 * @param <Z> the locator type
 * @param <D> the database type
 */
class ClusterListenerManager<Z, D extends Database<Z>>
{
	private final List<DatabaseClusterConfigurationListener<Z, D>> configurationListeners = new CopyOnWriteArrayList<>();
	private final List<DatabaseClusterListener> clusterListeners = new CopyOnWriteArrayList<>();
	private final List<SynchronizationListener> synchronizationListeners = new CopyOnWriteArrayList<>();

	void addConfigurationListener(DatabaseClusterConfigurationListener<Z, D> listener)
	{
		this.configurationListeners.add(listener);
	}

	void addListener(DatabaseClusterListener listener)
	{
		this.clusterListeners.add(listener);
	}

	void addSynchronizationListener(SynchronizationListener listener)
	{
		this.synchronizationListeners.add(listener);
	}

	void removeConfigurationListener(DatabaseClusterConfigurationListener<Z, D> listener)
	{
		this.configurationListeners.remove(listener);
	}

	void removeListener(DatabaseClusterListener listener)
	{
		this.clusterListeners.remove(listener);
	}

	void removeSynchronizationListener(SynchronizationListener listener)
	{
		this.synchronizationListeners.remove(listener);
	}

	List<DatabaseClusterConfigurationListener<Z, D>> getConfigurationListeners()
	{
		return this.configurationListeners;
	}

	List<DatabaseClusterListener> getClusterListeners()
	{
		return this.clusterListeners;
	}

	List<SynchronizationListener> getSynchronizationListeners()
	{
		return this.synchronizationListeners;
	}
}
