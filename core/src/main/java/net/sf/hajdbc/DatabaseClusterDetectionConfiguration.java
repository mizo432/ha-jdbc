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

/**
 * Configures detection of database features for synchronization.
 * This interface isolates detection settings from other configuration concerns,
 * following the Interface Segregation Principle.
 *
 * @author Paul Ferraro
 * @since 3.2
 */
public interface DatabaseClusterDetectionConfiguration
{
	/**
	 * Indicates whether sequence detection is enabled for this cluster.
	 * @return true if sequence detection is enabled
	 */
	boolean isSequenceDetectionEnabled();

	/**
	 * Indicates whether identity column detection is enabled for this cluster.
	 * @return true if identity column detection is enabled
	 */
	boolean isIdentityColumnDetectionEnabled();
}

