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
package net.sf.hajdbc.invocation;

import net.sf.hajdbc.Database;

/**
 * Represents a method invocation against a SQL object in a database.
 * <p>
 * This interface uses 5 type parameters to support the proxy pattern across
 * multiple databases in a cluster. The type parameters follow a consistent
 * convention throughout HA-JDBC:
 * </p>
 * <ul>
 *   <li>{@code Z} - The root SQL object type (e.g., {@code Driver}, {@code DataSource})</li>
 *   <li>{@code D} - The database type, which extends {@link Database} parameterized by {@code Z}</li>
 *   <li>{@code T} - The target object type on which the method is invoked (e.g., {@code Connection}, {@code Statement})</li>
 *   <li>{@code R} - The return type of the invoked method</li>
 *   <li>{@code E} - The exception type that may be thrown (typically {@code SQLException})</li>
 * </ul>
 * <p>
 * Example: {@code Invoker<DataSource, DataSourceDatabase, Connection, Statement, SQLException>}
 * represents invoking {@code Connection.createStatement()} where the result is a {@code Statement}.
 * </p>
 *
 * @author Paul Ferraro
 * @param <Z> the root SQL object type (e.g., Driver, DataSource)
 * @param <D> the database type, extending Database<Z>
 * @param <T> the target object type on which the method is invoked
 * @param <R> the return type of the invoked method
 * @param <E> the exception type that may be thrown
 */
public interface Invoker<Z, D extends Database<Z>, T, R, E extends Exception>
{
	/**
	 * Invokes an action against the specified database on the specified SQL object
	 * @param database a database
	 * @param object an SQL object
	 * @return the invocation result
	 * @throws Exception
	 */
	R invoke(D database, T object) throws E;
}
