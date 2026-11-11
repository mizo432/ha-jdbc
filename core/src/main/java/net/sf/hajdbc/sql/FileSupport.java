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
package net.sf.hajdbc.sql;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;

/**
 * Provides temporary file support for serializing stream data.
 * Files created by this object are deleted when {@link #close()} is called.
 * 
 * @author  Paul Ferraro
 * @version $Revision: 1612 $
 * @since   1.0
 */
public interface FileSupport<E extends Throwable> extends Closeable
{
	/**
	 * Creates a file from the given binary input stream.
	 * @param inputStream binary stream of data
	 * @return temporary file
	 * @throws SQLException if an IO error occurs
	 */
	File createFile(InputStream inputStream) throws E;
	
	/**
	 * Creates a file from the given character input stream.
	 * @param reader character stream of data
	 * @return temporary file
	 * @throws SQLException if an IO error occurs
	 */
	File createFile(Reader reader) throws E;

	/**
	 * Returns a reader for the given file.
	 * @param file temporary file
	 * @return reader
	 * @throws SQLException if an IO error occurs
	 */
	Reader getReader(File file) throws E;
	
	/**
	 * Returns an input stream for the given file.
	 * @param file temporary file
	 * @return input stream
	 * @throws SQLException if an IO error occurs
	 */
	InputStream getInputStream(File file) throws E;
}
