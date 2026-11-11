package net.sf.hajdbc.distributed.jgroups;

import net.sf.hajdbc.distributed.Member;
import org.jgroups.Address;

public class AddressMember implements Member {
	private static final long serialVersionUID = -5777399287019796606L;
	private final Address address;

	public AddressMember(Address address) {
		this.address = address;
	}

	public Address getAddress() {
		return this.address;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof AddressMember)) return false;
		return this.address.equals(((AddressMember) object).address);
	}

	@Override
	public int hashCode() {
		return this.address.hashCode();
	}

	@Override
	public String toString() {
		return this.address.toString();
	}

	@Override
	public int compareTo(Member member) {
		return this.address.compareTo(((AddressMember) member).address);
	}
}
