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
package net.sf.hajdbc.xml;

import java.io.Serializable;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Factory for creating XML stream sources and results.
 * <p>
 * This factory is used by {@link XMLDatabaseClusterConfigurationFactory} to read and write
 * XML configuration files. It abstracts the underlying XML parsing implementation,
 * allowing for different XML formats (DOM, SAX, StAX) to be used.
 * </p>
 *
 * @see XMLDatabaseClusterConfigurationFactory
 * @see javax.xml.transform.Source
 * @see javax.xml.transform.Result
 */
public interface XMLStreamFactory extends Serializable
{
	/**
	 * Creates an XML source for reading configuration.
	 * @return XML source
	 */
	Source createSource();

	/**
	 * Creates an XML result for writing configuration.
	 * @return XML result
	 */
	Result createResult();
}
