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
package net.sf.hajdbc.dialect;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DumpRestoreSupport;
import net.sf.hajdbc.codec.Decoder;
import net.sf.hajdbc.util.Processes;

/**
 * Helper that provides the common dump/restore pattern for CLI-based database tools.
 * Subclasses override template methods to customize tool name, arguments, and password
 * environment variable.
 * @author Paul Ferraro
 */
public abstract class DialectDumpRestore implements DumpRestoreSupport
{
	/**
	 * Returns the password file path used to skip password prompts.
	 * @return the password file, or {@code null} if not applicable
	 */
	protected abstract File getPasswordFile();

	/**
	 * Returns the name of the dump CLI tool (e.g., "pg_dump", "mysqldump").
	 * @return the dump tool name
	 */
	protected abstract String dumpToolName();

	/**
	 * Returns the name of the restore CLI tool (e.g., "pg_restore", "mysql").
	 * @return the restore tool name
	 */
	protected abstract String restoreToolName();

	/**
	 * Returns the environment variable name for the password (e.g., "PGPASSWORD", "MYSQL_PWD").
	 * @return the password environment variable name
	 */
	protected abstract String passwordEnvironmentVariable();

	/**
	 * Returns the command-line argument prefix for the user (e.g., "--username=" or "--user=").
	 * @return the user argument prefix
	 */
	protected abstract String userArgumentPrefix();

	/**
	 * Configures the dump command with database-specific arguments.
	 * The host, port, and user arguments are already added by {@link #dump(Database, Decoder, File, boolean)}.
	 * @param args the command arguments list to append to
	 * @param properties the connection properties
	 * @param file the output file
	 * @param dataOnly true if only data should be dumped
	 */
	protected abstract void configureDumpCommand(List<String> args, ConnectionProperties properties, File file, boolean dataOnly);

	/**
	 * Configures the restore command with database-specific arguments.
	 * The host, port, and user arguments are already added by {@link #restore(Database, Decoder, File, boolean)}.
	 * @param args the command arguments list to append to
	 * @param properties the connection properties
	 * @param file the input file
	 * @param dataOnly true if only data should be restored
	 */
	protected abstract void configureRestoreCommand(List<String> args, ConnectionProperties properties, File file, boolean dataOnly);

	/**
	 * Gets the connection properties for the specified database.
	 * @param database the database
	 * @param decoder the decoder
	 * @return the connection properties
	 * @throws SQLException if connection properties cannot be retrieved
	 */
	protected abstract ConnectionProperties getConnectionProperties(Database<?> database, Decoder decoder) throws SQLException;

	/**
	 * Dumps the database using the configured CLI tool.
	 */
	@Override
	public <Z, D extends Database<Z>> void dump(D database, Decoder decoder, File file, boolean dataOnly) throws Exception
	{
		ConnectionProperties properties = getConnectionProperties(database, decoder);
		ProcessBuilder builder = new ProcessBuilder(dumpToolName());
		List<String> args = builder.command();
		addConnectionArgs(args, properties);
		configureDumpCommand(args, properties, file, dataOnly);
		Processes.run(setPassword(builder, properties));
	}

	/**
	 * Restores the database using the configured CLI tool.
	 */
	@Override
	public <Z, D extends Database<Z>> void restore(D database, Decoder decoder, File file, boolean dataOnly) throws Exception
	{
		ConnectionProperties properties = getConnectionProperties(database, decoder);
		ProcessBuilder builder = new ProcessBuilder(restoreToolName());
		configureRestoreProcessBuilder(builder, properties, file, dataOnly);
		List<String> args = builder.command();
		addConnectionArgs(args, properties);
		configureRestoreCommand(args, properties, file, dataOnly);
		Processes.run(setPassword(builder, properties));
	}

	/**
	 * Configures the restore ProcessBuilder before arguments are added.
	 * Override this to set up process-level configuration (e.g., redirectInput).
	 * @param builder the process builder
	 * @param properties the connection properties
	 * @param file the input file
	 * @param dataOnly true if only data should be restored
	 */
	protected void configureRestoreProcessBuilder(ProcessBuilder builder, ConnectionProperties properties, File file, boolean dataOnly)
	{
		// Default: no special configuration
	}

	/**
	 * Adds common connection arguments (host, port, user) to the command.
	 */
	private void addConnectionArgs(List<String> args, ConnectionProperties properties)
	{
		if (properties.getHost() != null)
		{
			args.add("--host=" + properties.getHost());
		}
		if (properties.getPort() != null)
		{
			args.add("--port=" + properties.getPort());
		}
		args.add(this.userArgumentPrefix() + properties.getUser());
	}

	/**
	 * Sets the password environment variable if needed.
	 */
	private ProcessBuilder setPassword(ProcessBuilder builder, ConnectionProperties properties)
	{
		String password = properties.getPassword();
		File passwordFile = this.getPasswordFile();
		if ((password != null) && (passwordFile == null || !passwordFile.exists()))
		{
			Processes.environment(builder).put(this.passwordEnvironmentVariable(), password);
		}
		return builder;
	}
}
