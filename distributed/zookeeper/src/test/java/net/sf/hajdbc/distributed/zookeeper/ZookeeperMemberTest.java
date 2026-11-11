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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ZookeeperMemberTest
{
	@Test
	void testEqualsAndHashCode()
	{
		ZookeeperMember member1 = new ZookeeperMember("/cluster/members/node-1");
		ZookeeperMember member2 = new ZookeeperMember("/cluster/members/node-1");
		ZookeeperMember member3 = new ZookeeperMember("/cluster/members/node-2");
		
		assertThat(member1).isEqualTo(member2);
		assertThat(member1.hashCode()).isEqualTo(member2.hashCode());
		assertThat(member1).isNotEqualTo(member3);
	}
	
	@Test
	void testCompareTo()
	{
		ZookeeperMember member1 = new ZookeeperMember("/cluster/members/node-1");
		ZookeeperMember member2 = new ZookeeperMember("/cluster/members/node-2");
		
		assertThat(member1.compareTo(member2)).isLessThan(0);
		assertThat(member2.compareTo(member1)).isGreaterThan(0);
		assertThat(member1.compareTo(new ZookeeperMember("/cluster/members/node-1"))).isZero();
	}
	
	@Test
	void testToString()
	{
		ZookeeperMember member = new ZookeeperMember("/cluster/members/node-123");
		
		assertThat(member.toString()).isEqualTo("/cluster/members/node-123");
	}
	
	@Test
	void testGetNodePath()
	{
		ZookeeperMember member = new ZookeeperMember("/cluster/members/node-456");
		
		assertThat(member.getNodePath()).isEqualTo("/cluster/members/node-456");
	}
}
