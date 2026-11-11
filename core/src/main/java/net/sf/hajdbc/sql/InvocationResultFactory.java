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

import java.util.SortedMap;

import net.sf.hajdbc.Database;

/**
 * Factory for creating and comparing invocation results.
 * <p>
 * This factory is used by the proxy factory hierarchy to aggregate results from
 * multiple database invocations. It compares results from primary and backup databases
 * to detect inconsistencies, and creates a consolidated result from all invocations.
 * </p>
 *
 * @param <Z> database connection source
 * @param <D> database descriptor
 * @param <R> result type
 */
public interface InvocationResultFactory<Z, D extends Database<Z>, R>
{
	/**
	 * Compares results from primary and backup databases to detect inconsistencies.
	 * @param primaryResult result from the primary database
	 * @param backupResult result from the backup database
	 * @return true if the results differ
	 */
	boolean differs(R primaryResult, R backupResult);

	/**
	 * Creates a consolidated result from all database invocations.
	 * @param results map of database to result
	 * @return consolidated result
	 */
	R createResult(SortedMap<D, R> results);
}