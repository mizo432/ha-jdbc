/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2013  Paul Ferraro
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

import java.util.Map;
import java.util.Set;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.invocation.Invoker;

/**
 * A factory for creating proxies to SQL objects across a database cluster.
 * <p>
 * This interface uses 4 type parameters:
 * </p>
 * <ul>
 *   <li>{@code Z} - The root SQL object type (e.g., {@code Driver}, {@code DataSource})</li>
 *   <li>{@code D} - The database type, extending {@link Database}&lt;{@code Z}&gt;</li>
 *   <li>{@code T} - The SQL object type being proxied (e.g., {@code Connection}, {@code Statement})</li>
 *   <li>{@code E} - The exception type thrown by the proxied object (typically {@code SQLException})</li>
 * </ul>
 *
 * @author Paul Ferraro
 * @param <Z> the root SQL object type
 * @param <D> the database type, extending Database&lt;Z&gt;
 * @param <T> the SQL object type being proxied
 * @param <E> the exception type thrown by the proxied object
 */
public interface ProxyFactory<Z, D extends Database<Z>, T, E extends Exception>
{
	T get(D database);
	
	Set<Map.Entry<D, T>> entries();

	DatabaseCluster<Z, D> getDatabaseCluster();
	
	ExceptionFactory<E> getExceptionFactory();
	
	void record(Invoker<Z, D, T, ?, E> invoker);

	void replay(D database, T object) throws E;

	void addChild(ChildProxyFactory<Z, D, T, E, ?, ? extends Exception> child);

	void removeChild(ChildProxyFactory<Z, D, T, E, ?, ? extends Exception> child);
	
	void removeChildren();
	
	T createProxy();
}