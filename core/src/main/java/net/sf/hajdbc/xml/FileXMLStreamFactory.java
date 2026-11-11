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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Alternative to {@link URLXMLStreamFactory} and the file:// protocol, since default JDK file url stream handler
 * does not support output.
 * 
 * @author Paul Ferraro
 */
public class FileXMLStreamFactory implements XMLStreamFactory
{
	private static final long serialVersionUID = -8857228563490452629L;
	
	private final File file;
	
	public FileXMLStreamFactory(File file)
	{
		this.file = file;
	}
	
	public FileXMLStreamFactory(URL url)
	{
		try
		{
			this.file = new File(url.toURI());
		}
		catch (URISyntaxException e)
		{
			throw new IllegalArgumentException(e);
		}
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
			return new FileInputStream(this.file);
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
			return new FileOutputStream(this.file);
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
		return this.file.getPath();
	}
}
