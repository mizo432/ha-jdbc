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
package net.sf.hajdbc.pool;

/**
 * Factory for creating object pools.
 * <p>
 * This factory is used internally by HA-JDBC to manage connection pools. It abstracts
 * the underlying pooling implementation, allowing different pool providers to be used.
 * </p>
 *
 * @see Pool
 * @see PoolProvider
 */
public interface PoolFactory
{
	/**
	 * Creates a pool using the specified provider.
	 * @param <T> pooled object type
	 * @param <E> exception type thrown by the provider
	 * @param provider a pool provider
	 * @return a new pool
	 */
	<T, E extends Exception> Pool<T, E> createPool(PoolProvider<T, E> provider);
}
