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
package net.sf.hajdbc.util;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilesTest
{
	@Test
	public void createTempFile() throws IOException
	{
		File tempFile = Files.createTempFile(".tmp");
		
		Assertions.assertNotNull(tempFile);
		Assertions.assertTrue(tempFile.exists());
		Assertions.assertTrue(tempFile.getName().startsWith(Files.TEMP_FILE_PREFIX));
		Assertions.assertTrue(tempFile.getName().endsWith(".tmp"));
		
		tempFile.delete();
	}
	
	@Test
	public void createTempFileWithDifferentSuffix() throws IOException
	{
		File tempFile = Files.createTempFile(".dat");
		
		Assertions.assertNotNull(tempFile);
		Assertions.assertTrue(tempFile.exists());
		Assertions.assertTrue(tempFile.getName().endsWith(".dat"));
		
		tempFile.delete();
	}
	
	@Test
	public void deleteExistingFile() throws IOException
	{
		File tempFile = Files.createTempFile(".tmp");
		Assertions.assertTrue(tempFile.exists());
		
		Files.delete(tempFile);
		Assertions.assertFalse(tempFile.exists());
	}
	
	@Test
	public void deleteNonExistingFile()
	{
		File nonExistingFile = new File("/tmp/non-existing-file-" + System.currentTimeMillis() + ".tmp");
		
		// Should not throw exception
		Files.delete(nonExistingFile);
	}
}
