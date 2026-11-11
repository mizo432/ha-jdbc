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
package net.sf.hajdbc.balancer;

import java.util.Iterator;
import java.util.Set;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.invocation.Invoker;

/**
 * A collection of active databases and a strategy for determining the next balancing target.
 * Unlike {@link Set}, this interface provides explicit methods for managing active databases,
 * preventing unintended operations (such as {@code clear()}, {@code removeAll()}, {@code retainAll()})
 * that could bypass the cluster's activation/deactivation logic.
 * 
 * @author Paul Ferraro
 *
 * @param <Z> database connection source
 * @param <D> database descriptor
 */
public interface Balancer<Z, D extends Database<Z>> extends Iterable<D>
{
	/**
	 * Adds the specified database as active.
	 * @param database a database descriptor
	 * @return true if the database was added, false if it was already active
	 */
	boolean add(D database);

	/**
	 * Removes the specified database from the active set.
	 * @param database a database descriptor
	 * @return true if the database was removed, false if it was not active
	 */
	boolean remove(D database);

	/**
	 * Returns whether the specified database is active.
	 * @param database a database descriptor
	 * @return true if the database is active
	 */
	boolean contains(D database);

	/**
	 * Returns whether no databases are active.
	 * @return true if no databases are active
	 */
	boolean isEmpty();

	/**
	 * Returns the number of active databases.
	 * @return number of active databases
	 */
	int size();

	/**
	 * Returns an iterator over the active databases.
	 * @return iterator
	 */
	Iterator<D> iterator();

	/**
	 * Returns a snapshot of the active databases as an unmodifiable set.
	 * @return unmodifiable set of active databases
	 */
	Set<D> getActiveDatabases();

	/**
	 * Clears all active databases.
	 */
	void clear();

	/**
	 * Returns the primary database.
	 * The database returned by this method must be consistent across all nodes.
	 * @return primary database
	 */
	D primary();
	
	/**
	 * Returns the non-primary databases.
	 * @return iterable collection of databases
	 */
	Iterable<D> backups();
	
	/**
	 * Returns the next database from this balancer.
	 * @return next database
	 */
	D next();

	/**
	 * Executes the given invoker on the given object for the specified database, and may notify the balancer.
	 * @param <T> invoker target object type
	 * @param <R> invoker return type
	 * @param <E> invoker exception type
	 * @param invoker invoker
	 * @param database database
	 * @param object target object
	 * @return execution result
	 * @throws E exception
	 */
	<T, R, E extends Exception> R invoke(Invoker<Z, D, T, R, E> invoker, D database, T object) throws E;
}
