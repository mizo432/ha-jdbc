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
import java.net.URL;

/**
 * @author Paul Ferraro
 */
public class URLXMLStreamFactory implements XMLStreamFactory
{
	private static final long serialVersionUID = -3911432025271185584L;
	
	private final URL url;
	
	public URLXMLStreamFactory(URL url)
	{
		this.url = url;
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.xml.XMLStreamFactory#createInputStream()
	 */
	@Override
	public InputStream createInputStream()
	{
		try
		{
			return this.url.openStream();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.xml.XMLStreamFactory#createOutputStream()
	 */
	@Override
	public OutputStream createOutputStream()
	{
		try
		{
			return this.url.openConnection().getOutputStream();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.url.toString();
	}
}
