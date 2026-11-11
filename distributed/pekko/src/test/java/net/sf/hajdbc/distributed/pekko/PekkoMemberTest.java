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
package net.sf.hajdbc.distributed.pekko;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.pekko.actor.Address;
import org.apache.pekko.actor.AddressFromURIString;
import org.junit.jupiter.api.Test;

class PekkoMemberTest
{
	@Test
	void testEqualsAndHashCode()
	{
		Address address1 = AddressFromURIString.parse("pekko://ha-jdbc@127.0.0.1:25520");
		Address address2 = AddressFromURIString.parse("pekko://ha-jdbc@127.0.0.1:25520");
		Address address3 = AddressFromURIString.parse("pekko://ha-jdbc@127.0.0.1:25521");
		
		PekkoMember member1 = new PekkoMember(address1);
		PekkoMember member2 = new PekkoMember(address2);
		PekkoMember member3 = new PekkoMember(address3);
		
		assertThat(member1).isEqualTo(member2);
		assertThat(member1.hashCode()).isEqualTo(member2.hashCode());
		assertThat(member1).isNotEqualTo(member3);
	}
	
	@Test
	void testCompareTo()
	{
		Address address1 = AddressFromURIString.parse("pekko://ha-jdbc@127.0.0.1:25520");
		Address address2 = AddressFromURIString.parse("pekko://ha-jdbc@127.0.0.1:25521");
		
		PekkoMember member1 = new PekkoMember(address1);
		PekkoMember member2 = new PekkoMember(address2);
		
		assertThat(member1.compareTo(member2)).isLessThan(0);
		assertThat(member2.compareTo(member1)).isGreaterThan(0);
		assertThat(member1.compareTo(new PekkoMember(address1))).isZero();
	}
	
	@Test
	void testToString()
	{
		Address address = AddressFromURIString.parse("pekko://ha-jdbc@127.0.0.1:25520");
		PekkoMember member = new PekkoMember(address);
		
		assertThat(member.toString()).isEqualTo("pekko://ha-jdbc@127.0.0.1:25520");
	}
	
	@Test
	void testGetAddress()
	{
		Address address = AddressFromURIString.parse("pekko://ha-jdbc@192.168.1.100:25520");
		PekkoMember member = new PekkoMember(address);
		
		assertThat(member.getAddress()).isEqualTo(address);
	}
}
