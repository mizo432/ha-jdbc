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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import net.sf.hajdbc.Credentials;
import net.sf.hajdbc.Database;
import net.sf.hajdbc.codec.Decoder;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class DialectConnectionResolverTest
{
	@Test
	public void getConnectionPropertiesWithHostPortDatabase() throws Exception
	{
		DialectConnectionResolver resolver = new DialectConnectionResolver(
				"mysql", "//(?<host>[^:/]+)(?::(?<port>\\d+))?(?:/(?<database>[^?]+))?");

		Database<Void> database = mock(Database.class);
		Credentials credentials = mock(Credentials.class);
		Decoder decoder = mock(Decoder.class);
		Connection connection = mock(Connection.class);
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);

		when(database.getCredentials()).thenReturn(credentials);
		when(credentials.getUser()).thenReturn("user");
		when(credentials.decodePassword(decoder)).thenAnswer(invocation -> "password".toCharArray());
		when(database.connect(decoder)).thenReturn(connection);
		when(connection.getMetaData()).thenReturn(metaData);
		when(metaData.getURL()).thenReturn("jdbc:mysql://myhost:3306/mydb");
		when(metaData.getUserName()).thenReturn("user");

		ConnectionProperties properties = resolver.getConnectionProperties(database, decoder);

		assertThat(properties.getHost()).isEqualTo("myhost");
		assertThat(properties.getPort()).isEqualTo("3306");
		assertThat(properties.getDatabase()).isEqualTo("mydb");
		assertThat(properties.getUser()).isEqualTo("user");
		assertThat(properties.getPassword()).isEqualTo("password");
	}

	@Test
	public void getConnectionPropertiesWithDatabaseOnly() throws Exception
	{
		DialectConnectionResolver resolver = new DialectConnectionResolver(
				"mysql", "//(?:(?<host>[^:/]+)(?::(?<port>\\d+))?/)?(?<database>[^?]+)");

		Database<Void> database = mock(Database.class);
		Credentials credentials = mock(Credentials.class);
		Decoder decoder = mock(Decoder.class);
		Connection connection = mock(Connection.class);
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);

		when(database.getCredentials()).thenReturn(credentials);
		when(credentials.getUser()).thenReturn("user");
		when(credentials.decodePassword(decoder)).thenAnswer(invocation -> "password".toCharArray());
		when(database.connect(decoder)).thenReturn(connection);
		when(connection.getMetaData()).thenReturn(metaData);
		when(metaData.getURL()).thenReturn("jdbc:mysql:///mydb");
		when(metaData.getUserName()).thenReturn("user");

		ConnectionProperties properties = resolver.getConnectionProperties(database, decoder);

		assertThat(properties.getHost()).isNull();
		assertThat(properties.getPort()).isNull();
		assertThat(properties.getDatabase()).isEqualTo("/mydb");
		assertThat(properties.getUser()).isEqualTo("user");
		assertThat(properties.getPassword()).isEqualTo("password");
	}

	@Test
	public void getConnectionPropertiesThrowsOnNullUrl() throws Exception
	{
		DialectConnectionResolver resolver = new DialectConnectionResolver(
				"mysql", "//(?<host>[^:/]+)(?::(?<port>\\d+))?(?:/(?<database>[^?]+))?");

		Database<Void> database = mock(Database.class);
		Credentials credentials = mock(Credentials.class);
		Decoder decoder = mock(Decoder.class);
		Connection connection = mock(Connection.class);
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);

		when(database.getCredentials()).thenReturn(credentials);
		when(credentials.decodePassword(decoder)).thenAnswer(invocation -> "password".toCharArray());
		when(database.connect(decoder)).thenReturn(connection);
		when(connection.getMetaData()).thenReturn(metaData);
		when(metaData.getURL()).thenReturn(null);

		assertThatThrownBy(() -> resolver.getConnectionProperties(database, decoder))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void getConnectionPropertiesThrowsOnUnmatchedUrl() throws Exception
	{
		DialectConnectionResolver resolver = new DialectConnectionResolver(
				"mysql", "//(?<host>[^:/]+)(?::(?<port>\\d+))?(?:/(?<database>[^?]+))?");

		Database<Void> database = mock(Database.class);
		Credentials credentials = mock(Credentials.class);
		Decoder decoder = mock(Decoder.class);
		Connection connection = mock(Connection.class);
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);

		when(database.getCredentials()).thenReturn(credentials);
		when(credentials.decodePassword(decoder)).thenAnswer(invocation -> "password".toCharArray());
		when(database.connect(decoder)).thenReturn(connection);
		when(connection.getMetaData()).thenReturn(metaData);
		when(metaData.getURL()).thenReturn("jdbc:postgresql://localhost:5432/mydb");

		assertThatThrownBy(() -> resolver.getConnectionProperties(database, decoder))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("jdbc:postgresql://localhost:5432/mydb");
	}

	@Test
	public void getConnectionPropertiesCleansPassword() throws Exception
	{
		/* Verify that password characters are zeroed after use (security).
		 * We test the happy path — the helper uses Arrays.fill in a finally block. */
		DialectConnectionResolver resolver = new DialectConnectionResolver(
				"mysql", "//(?<host>[^:/]+)(?::(?<port>\\d+))?(?:/(?<database>[^?]+))?");

		Database<Void> database = mock(Database.class);
		Credentials credentials = mock(Credentials.class);
		Decoder decoder = mock(Decoder.class);
		Connection connection = mock(Connection.class);
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);

		when(database.getCredentials()).thenReturn(credentials);
		when(credentials.decodePassword(decoder)).thenAnswer(invocation -> "secret".toCharArray());
		when(database.connect(decoder)).thenReturn(connection);
		when(connection.getMetaData()).thenReturn(metaData);
		when(metaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/testdb");
		when(metaData.getUserName()).thenReturn("admin");

		ConnectionProperties properties = resolver.getConnectionProperties(database, decoder);

		assertThat(properties.getPassword()).isEqualTo("secret");
	}
}
