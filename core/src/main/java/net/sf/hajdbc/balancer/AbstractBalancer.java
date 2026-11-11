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

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.hajdbc.Database;

/**
 * Thread-safe abstract balancer implementation that implements most of the Balancer interface,
 * except for {@link Balancer#next()}, {@link Balancer#add(net.sf.hajdbc.Database)},
 * {@link Balancer#remove(net.sf.hajdbc.Database)}, and {@link Balancer#clear()}.
 * Calls to {@link #iterator()} are non-blocking.
 * 
 * @author Paul Ferraro
 * @param <Z> database connection source
 * @param <D> database descriptor
 */
public abstract class AbstractBalancer<Z, D extends Database<Z>> implements Balancer<Z, D>
{
	protected abstract Set<D> getDatabases();

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.balancer.Balancer#backups()
	 */
	@Override
	public Iterable<D> backups()
	{
		Iterator<D> databases = this.getDatabases().iterator();
		
		if (!databases.hasNext() || ((databases.next() != null) && !databases.hasNext())) return Collections.emptySet();
		
		D database = databases.next();
		
		if (!databases.hasNext()) return Collections.singleton(database);
		
		SortedSet<D> backups = new TreeSet<>();
		
		backups.add(database);
		
		do
		{
			backups.add(databases.next());
		}
		while (databases.hasNext());

		return backups;
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.balancer.Balancer#iterator()
	 */
	@Override
	public Iterator<D> iterator()
	{
		return this.getDatabases().iterator();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.balancer.Balancer#contains(net.sf.hajdbc.Database)
	 */
	@Override
	public boolean contains(D database)
	{
		return this.getDatabases().contains(database);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.balancer.Balancer#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return this.getDatabases().isEmpty();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.balancer.Balancer#size()
	 */
	@Override
	public int size()
	{
		return this.getDatabases().size();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.balancer.Balancer#getActiveDatabases()
	 */
	@Override
	public Set<D> getActiveDatabases()
	{
		return Collections.unmodifiableSet(this.getDatabases());
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof Balancer)) return false;
		if (this == object) return true;
		
		@SuppressWarnings("unchecked")
		Balancer<Z, D> other = (Balancer<Z, D>) object;
		
		return this.getDatabases().equals(other.getActiveDatabases());
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.getDatabases().hashCode();
	}

	@Override
	public String toString()
	{
		return this.getDatabases().toString();
	}
}
