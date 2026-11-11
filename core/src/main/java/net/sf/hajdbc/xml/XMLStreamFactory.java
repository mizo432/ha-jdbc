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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Factory for creating input and output streams for XML configuration.
 * <p>
 * This factory is used by {@link XMLDatabaseClusterConfigurationFactory} to read and write
 * XML configuration files using Jackson 3 XML.
 * </p>
 *
 * @see XMLDatabaseClusterConfigurationFactory
 */
public interface XMLStreamFactory extends Serializable
{
	/**
	 * Creates an input stream for reading configuration.
	 * @return input stream
	 */
	InputStream createInputStream();

	/**
	 * Creates an output stream for writing configuration.
	 * @return output stream
	 */
	OutputStream createOutputStream();
}
