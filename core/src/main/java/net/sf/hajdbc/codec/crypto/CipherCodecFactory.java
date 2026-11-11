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
package net.sf.hajdbc.codec.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;

import net.sf.hajdbc.codec.Codec;
import net.sf.hajdbc.codec.CodecFactory;
import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;
import net.sf.hajdbc.util.Strings;
import net.sf.hajdbc.util.SystemProperties;

/**
 * Uses a shared key stored in a keystore to decrypt passwords in configuration files.
 * To generate a Base64-encoded encrypted password for use in configuration files, use the following command:<br/>
 * <p><code>java -classpath ha-jdbc.jar net.sf.hajdbc.codec.crypto.CipherCodecFactory [password]</code></p>
 * The following system properties can be used to customize key and/or keystore properties:
 * <table>
 * 	<tr>
 * 		<th>Property</th>
 * 		<th>Default</th>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.keystore.file</td>
 * 		<td>~/.keystore</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.keystore.type</td>
 * 		<td>jceks</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.keystore.password</td>
 * 		<td><em>None</em></td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.keystore.password.file</td>
 * 		<td><em>None</em></td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.key.alias</td>
 * 		<td>ha-jdbc</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.key.password</td>
 * 		<td><em>Required</em><td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.key.password.file</td>
 * 		<td><em>None</em></td>
 * 	</tr>
 * </table>
 * Password properties (<code>ha-jdbc.keystore.password</code> and <code>ha-jdbc.key.password</code>) are resolved
 * in the following order of precedence (highest first):
 * <ol>
 * 	<li>File-based: contents of the file specified by <code>ha-jdbc.keystore.password.file</code> system property</li>
 * 	<li>Environment variable: <code>HA_JDBC_KEYSTORE_PASSWORD</code> environment variable</li>
 * 	<li>System property: <code>ha-jdbc.keystore.password</code></li>
 * </ol>
 * The file-based and environment variable methods are recommended to avoid exposing passwords in command-line history or process lists.
 * @author Paul Ferraro
 */
public class CipherCodecFactory implements CodecFactory
{
	private static final long serialVersionUID = -4409167180573651279L;
	private static final Logger log = LoggerFactory.getLogger(CipherCodecFactory.class);
	
	public static final String DEFAULT_KEYSTORE_FILE = String.format("%s/.keystore", SystemProperties.getSystemProperty("user.home"));
	public static final String DEFAULT_KEY_ALIAS = "ha-jdbc";

	enum Property
	{
		KEYSTORE_FILE("keystore.file", DEFAULT_KEYSTORE_FILE),
		KEYSTORE_TYPE("keystore.type", "jceks"),
		KEYSTORE_PASSWORD("keystore.password", null),
		KEYSTORE_PASSWORD_FILE("keystore.password.file", null),
		KEY_ALIAS("key.alias", DEFAULT_KEY_ALIAS),
		KEY_PASSWORD("key.password", null),
		KEY_PASSWORD_FILE("key.password.file", null);
		
		final String nameFormat;
		final String name;
		final String defaultValue;
		
		private Property(String name, String defaultValue)
		{
			this.nameFormat = "ha-jdbc.{0}." + name;
			this.name = "ha-jdbc." + name;
			this.defaultValue = defaultValue;
		}
	}
	
	/**
	 * Returns the environment variable name corresponding to a password-related property.
	 * Example: keystore.password → HA_JDBC_KEYSTORE_PASSWORD
	 */
	private static String getEnvironmentVariableName(Property property)
	{
		// "keystore.password" → "HA_JDBC_KEYSTORE_PASSWORD"
		String base = property.name.replace("ha-jdbc.", "").replace(".", "_").toUpperCase();
		return "HA_JDBC_" + base;
	}
	
	private static String getProperty(String id, Property property)
	{
		String value = SystemProperties.getSystemProperty(MessageFormat.format(property.nameFormat, id));
		
		if (value != null) return value;
		
		String pattern = SystemProperties.getSystemProperty(property.name, property.defaultValue);
		
		if (pattern == null) return null;
		
		return MessageFormat.format(pattern, id);
	}
	
	/**
	 * Retrieves a sensitive property (e.g., password).
	 * The order of precedence (highest first) is:
	 * <ol>
	 * 	<li>File-based: contents of the file specified by the property with .file suffix</li>
	 * 	<li>Environment variable: environment variable with HA_JDBC_ prefix</li>
	 * 	<li>System property: the normal property name</li>
	 * </ol>
	 */
	private static String getPropertySecure(String id, Property passwordProperty, Property fileProperty)
	{
		// 1. Try file-based password
		String filePath = getProperty(id, fileProperty);
		if (filePath != null && !filePath.isEmpty())
		{
			String filePassword = readPasswordFromFile(filePath);
			if (filePassword != null)
			{
				return filePassword;
			}
		}
		
		// 2. Try environment variable
		String envName = getEnvironmentVariableName(passwordProperty);
		String envValue = SystemProperties.getEnvironmentVariable(envName);
		if (envValue != null && !envValue.isEmpty())
		{
			return envValue;
		}
		
		// 3. Try system property (existing behavior)
		return getProperty(id, passwordProperty);
	}
	
	/**
	 * Reads a password from a file.
	 * Returns the trimmed contents of the file.
	 * Returns null for empty or whitespace-only content.
	 * Checks file permissions and logs a warning if readable/writable by others.
	 */
	private static String readPasswordFromFile(String filePath)
	{
		try
		{
			File file = new File(filePath);
			if (!file.exists() || !file.isFile())
			{
				return null;
			}
			// Check file permissions for security
			checkPasswordFilePermissions(file);
			String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
			return content.isEmpty() ? null : content;
		}
		catch (IOException e)
		{
			log.log(Level.WARN, e, "Failed to read password file: {0}", filePath);
			return null;
		}
	}
	
	/**
	 * Checks the permissions of a password file.
	 * Logs a warning if the file is readable/writable by others.
	 */
	private static void checkPasswordFilePermissions(File file)
	{
		if (!file.canRead())
		{
			log.log(Level.WARN, "Password file is not readable: {0}", file.getAbsolutePath());
			return;
		}
		
		// Check POSIX file permissions if available, regardless of whether the current user can write
		try
		{
			java.nio.file.Path path = file.toPath();
			if (Files.isReadable(path))
			{
				// Check POSIX file permissions if available
				try
				{
					java.nio.file.attribute.PosixFileAttributes fileAttrs = 
						java.nio.file.Files.readAttributes(path, java.nio.file.attribute.PosixFileAttributes.class);
					java.util.Set<java.nio.file.attribute.PosixFilePermission> perms = fileAttrs.permissions();
					
					// Check if others have read or write permissions
					boolean othersCanRead = perms.contains(java.nio.file.attribute.PosixFilePermission.OTHERS_READ);
					boolean othersCanWrite = perms.contains(java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE);
					
					if (othersCanRead || othersCanWrite)
					{
						log.log(Level.WARN, "Password file has insecure permissions (others can read/write): {0}. Recommended: 600 (rw-------)", file.getAbsolutePath());
					}
				}
				catch (UnsupportedOperationException e)
				{
					// POSIX file attributes not supported on this filesystem (e.g., Windows)
					// Skip detailed permission check
				}
			}
		}
		catch (IOException e)
		{
			// Ignore permission check errors
			log.log(Level.TRACE, e, "Failed to check password file permissions: {0}", file.getAbsolutePath());
		}
	}
	
	@Override
	public String getId()
	{
		return "?";
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.codec.CodecFactory#createCodec(java.lang.String)
	 */
	@Override
	public Codec createCodec(String clusterId) throws SQLException
	{
		String type = getProperty(clusterId, Property.KEYSTORE_TYPE);
		File file = new File(getProperty(clusterId, Property.KEYSTORE_FILE));
		String password = getPropertySecure(clusterId, Property.KEYSTORE_PASSWORD, Property.KEYSTORE_PASSWORD_FILE);

		String keyAlias = getProperty(clusterId, Property.KEY_ALIAS);
		String keyPassword = getPropertySecure(clusterId, Property.KEY_PASSWORD, Property.KEY_PASSWORD_FILE);
		
		try
		{
			char[] storePassword = null;
			char[] keyPass = null;
			try
			{
				storePassword = (password != null) ? password.toCharArray() : null;
				keyPass = (keyPassword != null) ? keyPassword.toCharArray() : null;

				KeyStore store = KeyStore.getInstance(type);

				try (InputStream input = new FileInputStream(file))
				{
					store.load(input, storePassword);
				}

				return new CipherCodec(store.getKey(keyAlias, keyPass));
			}
			finally
			{
				if (storePassword != null)
				{
					Arrays.fill(storePassword, (char) 0);
				}
				if (keyPass != null)
				{
					Arrays.fill(keyPass, (char) 0);
				}
			}
		}
		catch (GeneralSecurityException e)
		{
			throw new SQLException(e);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
	}
	
	public static void main(String... args)
	{
		if (args.length != 2)
		{
			System.err.println(String.format("Usage:%s\tjava %s <cluster-id> <password-to-encrypt>", Strings.NEW_LINE, CipherCodecFactory.class.getName()));
			System.exit(1);
			return;
		}
		
		String clusterId = args[0];
		String value = args[1];
		
		try
		{
			Codec codec = new CipherCodecFactory().createCodec(clusterId);

			System.out.println(codec.encode(value));
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
		}
	}
}
