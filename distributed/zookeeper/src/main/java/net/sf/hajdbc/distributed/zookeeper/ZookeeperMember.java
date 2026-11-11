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
package net.sf.hajdbc.distributed.zookeeper;

import net.sf.hajdbc.distributed.Member;

/**
 * Zookeeperのephemeralノードパスでグループメンバーを識別します。
 * 
 * @author Paul Ferraro
 */
public class ZookeeperMember implements Member
{
	private static final long serialVersionUID = -5777399287019796607L;
	
	private final String nodePath;
	
	/**
	 * Constructs a new ZookeeperMember
	 * @param nodePath the ephemeral node path identifying this member
	 */
	public ZookeeperMember(String nodePath)
	{
		this.nodePath = nodePath;
	}

	public String getNodePath()
	{
		return this.nodePath;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof ZookeeperMember)) return false;

		ZookeeperMember member = (ZookeeperMember) object;
		
		return this.nodePath.equals(member.nodePath);
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.nodePath.hashCode();
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.nodePath;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Member member)
	{
		return this.nodePath.compareTo(((ZookeeperMember) member).nodePath);
	}
}
