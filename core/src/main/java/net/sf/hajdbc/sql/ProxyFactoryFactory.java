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

import net.sf.hajdbc.Database;
import net.sf.hajdbc.invocation.Invoker;

/**
 * A factory for creating proxy factories.
 * <p>
 * This interface uses 6 type parameters to support creating child proxy factories:
 * </p>
 * <ul>
 *   <li>{@code Z} - The root SQL object type (e.g., {@code Driver}, {@code DataSource})</li>
 *   <li>{@code D} - The database type, extending {@link Database}&lt;{@code Z}&gt;</li>
 *   <li>{@code P} - The parent object type</li>
 *   <li>{@code PE} - The exception type thrown when accessing the parent</li>
 *   <li>{@code R} - The result object type (child)</li>
 *   <li>{@code RE} - The exception type thrown by the result object</li>
 * </ul>
 *
 * @author Paul Ferraro
 * @param <Z> the root SQL object type
 * @param <D> the database type, extending Database&lt;Z&gt;
 * @param <P> the parent object type
 * @param <PE> the exception type thrown when accessing the parent
 * @param <R> the result object type (child)
 * @param <RE> the exception type thrown by the result object
 */
public interface ProxyFactoryFactory<Z, D extends Database<Z>, P, PE extends Exception, R, RE extends Exception>
{
	 ProxyFactory<Z, D, R, RE> createProxyFactory(P parentProxy, ProxyFactory<Z, D, P, PE> parent, Invoker<Z, D, P, R, PE> invoker, Map<D, R> objects);
}
