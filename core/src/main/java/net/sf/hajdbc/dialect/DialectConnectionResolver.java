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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.codec.Decoder;

/**
 * Helper that resolves connection properties from a database connection.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectConnectionResolver
{
	private final Pattern urlPattern;

	DialectConnectionResolver(String vendorPattern, String locatorPattern)
	{
		this.urlPattern = Pattern.compile(String.format("jdbc\\:%s\\:%s", vendorPattern, locatorPattern));
	}

	/**
	 * Resolve connection properties (host, port, database, user, password) from a database connection.
	 */
	<Z, D extends Database<Z>> ConnectionProperties getConnectionProperties(D database, Decoder decoder) throws SQLException
	{
		char[] passwordChars = null;
		try
		{
			passwordChars = database.getCredentials().decodePassword(decoder);
			final String password = new String(passwordChars);
			try (Connection connection = database.connect(decoder))
			{
				DatabaseMetaData metaData = connection.getMetaData();
				String url = metaData.getURL();

				if (url == null)
				{
					throw new UnsupportedOperationException();
				}

				Matcher matcher = this.urlPattern.matcher(url);

				if (!matcher.find())
				{
					throw new UnsupportedOperationException(url);
				}

				final String host = matcher.group("host");
				final String port = matcher.group("port");
				final String databaseName = matcher.group("database");
				final String user = metaData.getUserName();

				return new ConnectionProperties()
				{
					@Override
					public String getHost()
					{
						return host;
					}

					@Override
					public String getPort()
					{
						return port;
					}

					@Override
					public String getDatabase()
					{
						return databaseName;
					}

					@Override
					public String getUser()
					{
						return user;
					}

					@Override
					public String getPassword()
					{
						return password;
					}
				};
			}
		}
		finally
		{
			if (passwordChars != null)
			{
				Arrays.fill(passwordChars, (char) 0);
			}
		}
	}
}
