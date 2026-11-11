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

import net.sf.hajdbc.Database;
import org.jspecify.annotations.NonNull;

/**
 * A factory for creating child proxies, where each child is derived from a parent SQL object.
 * <p>
 * This interface uses 6 type parameters to support the hierarchical proxy pattern:
 * </p>
 * <ul>
 *   <li>{@code Z} - The root SQL object type (e.g., {@code Driver}, {@code DataSource})</li>
 *   <li>{@code D} - The database type, extending {@link Database}&lt;{@code Z}&gt;</li>
 *   <li>{@code P} - The parent object type (e.g., {@code Connection} for a {@code Statement} child)</li>
 *   <li>{@code PE} - The exception type thrown when creating the parent (typically {@code SQLException})</li>
 *   <li>{@code T} - The child object type (e.g., {@code Statement}, {@code ResultSet})</li>
 *   <li>{@code E} - The exception type thrown by the child object (typically {@code SQLException})</li>
 * </ul>
 * <p>
 * Note: In most cases, {@code PE} and {@code E} are both {@code SQLException}. The only exceptions
 * are {@code OutputProxyFactory} (where {@code E} is {@code IOException}) and
 * {@code XAResourceProxyFactory} (where {@code E} is {@code XAException}).
 * </p>
 *
 * @author Paul Ferraro
 * @param <Z> the root SQL object type
 * @param <D> the database type, extending Database&lt;Z&gt;
 * @param <P> the parent object type
 * @param <PE> the exception type thrown when accessing the parent
 * @param <T> the child object type
 * @param <E> the exception type thrown by the child object
 */
public interface ChildProxyFactory<Z, D extends Database<Z>, P, PE extends Exception, T, E extends Exception> extends ProxyFactory<Z, D, T, E>
{
	@NonNull ProxyFactory<Z, D, P, PE> getParent();
	
	@NonNull P getParentProxy();

	void remove();
	
	void close(@NonNull D database);

	void close(@NonNull D database, @NonNull T object) throws E;
}
