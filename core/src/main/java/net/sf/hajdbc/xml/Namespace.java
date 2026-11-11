/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2014  Paul Ferraro
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
package net.sf.hajdbc.xml;

/**
 * XML namespace versions supported by HA-JDBC.
 * XSD validation is no longer used; Jackson 3 XML handles schema validation via POJO binding.
 * @author Paul Ferraro
 */
public enum Namespace
{
	VERSION_3_0(3, 0),
	VERSION_3_1(3, 1),
	;

	public static final Namespace CURRENT_VERSION = VERSION_3_1;

	private final int major;
	private final int minor;

	private Namespace(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}

	public String getURI() {
		return String.format("urn:ha-jdbc:cluster:%d.%d", this.major, this.minor);
	}
}
