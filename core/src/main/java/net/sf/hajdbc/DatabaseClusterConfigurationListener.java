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

import org.jspecify.annotations.NonNull;

/**
 * Listener interface for monitoring database cluster configuration changes.
 * @author Paul Ferraro
 * @param <Z> connection source (e.g., Driver, DataSource, etc.)
 * @param <D> database implementation
 */
public interface DatabaseClusterConfigurationListener<Z, D extends Database<Z>>
{
	/**
	 * Called when a database is added.
	 * @param database the added database
	 * @param configuration database cluster configuration
	 */
	void added(@NonNull D database, @NonNull DatabaseClusterConfiguration<Z, D> configuration);
	
	/**
	 * Called when a database is removed.
	 * @param database the removed database
	 * @param configuration database cluster configuration
	 */
	void removed(@NonNull D database, @NonNull DatabaseClusterConfiguration<Z, D> configuration);
}
