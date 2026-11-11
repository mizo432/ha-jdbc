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
package net.sf.hajdbc.distributed.jgroups;

import static org.assertj.core.api.Assertions.assertThat;

import org.jgroups.Address;
import org.jgroups.util.UUID;
import org.junit.jupiter.api.Test;

class AddressMemberTest {

    @Test
    void testGetAddress() {
        Address address = new UUID();
        AddressMember member = new AddressMember(address);
        assertThat(member.getAddress()).isSameAs(address);
    }

    @Test
    void testEqualsSameAddress() {
        Address address = new UUID();
        AddressMember member1 = new AddressMember(address);
        AddressMember member2 = new AddressMember(address);
        assertThat(member1).isEqualTo(member2);
        assertThat(member2).isEqualTo(member1);
    }

    @Test
    void testEqualsDifferentAddress() {
        UUID uuid1 = new UUID();
        UUID uuid2 = new UUID();
        while (uuid1.equals(uuid2)) {
            uuid2 = new UUID();
        }
        AddressMember member1 = new AddressMember(uuid1);
        AddressMember member2 = new AddressMember(uuid2);
        assertThat(member1).isNotEqualTo(member2);
    }

    @Test
    void testEqualsNull() {
        AddressMember member = new AddressMember(new UUID());
        assertThat(member).isNotEqualTo(null);
    }

    @Test
    void testEqualsOtherType() {
        AddressMember member = new AddressMember(new UUID());
        assertThat(member).isNotEqualTo("string");
    }

    @Test
    void testHashCodeSameAddress() {
        Address address = new UUID();
        AddressMember member1 = new AddressMember(address);
        AddressMember member2 = new AddressMember(address);
        assertThat(member1.hashCode()).isEqualTo(member2.hashCode());
        assertThat(member1.hashCode()).isEqualTo(address.hashCode());
    }

    @Test
    void testToString() {
        Address address = new UUID();
        AddressMember member = new AddressMember(address);
        assertThat(member.toString()).isEqualTo(address.toString());
    }

    @Test
    void testCompareToSameAddress() {
        Address address = new UUID();
        AddressMember member1 = new AddressMember(address);
        AddressMember member2 = new AddressMember(address);
        assertThat(member1.compareTo(member2)).isZero();
    }

    @Test
    void testCompareToDifferentAddress() {
        UUID uuid1 = new UUID();
        UUID uuid2 = new UUID();
        while (uuid1.equals(uuid2)) {
            uuid2 = new UUID();
        }
        AddressMember member1 = new AddressMember(uuid1);
        AddressMember member2 = new AddressMember(uuid2);
        int result = member1.compareTo(member2);
        assertThat(result).isNotZero();
        assertThat(member2.compareTo(member1)).isEqualTo(-result);
    }
}
