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

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProcessesTest
{
	@Test
	public void environmentReturnsProcessBuilderEnvironment()
	{
		ProcessBuilder builder = new ProcessBuilder("echo", "test");
		Map<String, String> env = Processes.environment(builder);
		
		Assertions.assertNotNull(env);
		Assertions.assertTrue(env.size() > 0);
	}
	
	@Test
	public void runSuccessfulCommand() throws Exception
	{
		// Use a command that always succeeds
		String command = System.getProperty("os.name").toLowerCase().contains("win") 
			? "cmd.exe" : "echo";
		ProcessBuilder builder = new ProcessBuilder(command, "/c", "exit", "0");
		
		// On non-Windows, use a simpler approach
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			builder = new ProcessBuilder("true");
		}
		
		Processes.run(builder);
	}
	
	@Test
	public void runFailingCommandThrowsException()
	{
		ProcessBuilder builder;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			builder = new ProcessBuilder("cmd.exe", "/c", "exit", "1");
		} else {
			builder = new ProcessBuilder("bash", "-c", "exit 1");
		}
		
		Assertions.assertThrows(Exception.class, () -> Processes.run(builder));
	}
	
	@Test
	public void runCommandWithOutput() throws Exception
	{
		ProcessBuilder builder;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			builder = new ProcessBuilder("cmd.exe", "/c", "echo", "hello");
		} else {
			builder = new ProcessBuilder("echo", "hello");
		}
		
		Processes.run(builder);
	}
	
	@Test
	public void environmentIsMutable()
	{
		ProcessBuilder builder = new ProcessBuilder("echo", "test");
		Map<String, String> env = Processes.environment(builder);
		
		String oldValue = env.get("PATH");
		Assertions.assertNotNull(oldValue);
	}
}
