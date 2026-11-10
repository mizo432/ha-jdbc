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
 * 名前付きオブジェクトの{@link #hashCode()}、{@link #equals(Object)}、{@link #toString()}、{@link #compareTo(Object)}を実装します
 * @author Paul Ferraro
 * @param <N> 名前の型
 * @param <T> この型
 */
public abstract class AbstractNamed<N extends Comparable<N>, T extends Named<N, T>> implements Named<N, T>
{
	private final @NonNull N name;
	
	/**
	 * 名前を指定してコンストラクトします
	 * @param name 名前
	 */
	protected AbstractNamed(@NonNull N name)
	{
		this.name = name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NonNull N getName()
	{
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return this.name.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object)
	{
		return ((object != null) && this.getClass().isInstance(object)) ? this.name.equals(this.getClass().cast(object).getName()) : false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NonNull String toString()
	{
		return this.name.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(@NonNull T named)
	{
		return this.getName().compareTo(named.getName());
	}
}
