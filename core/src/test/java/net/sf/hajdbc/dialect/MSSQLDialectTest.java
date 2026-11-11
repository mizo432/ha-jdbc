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

import net.sf.hajdbc.ColumnProperties;
import net.sf.hajdbc.Credentials;
import net.sf.hajdbc.Database;
import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.SequenceProperties;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.codec.Decoder;
import net.sf.hajdbc.dialect.mssql.MSSQLDialectFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Paul Ferraro
 */
public class MSSQLDialectTest extends StandardDialectTest
{
	@Override
	protected DialectFactory createDialectFactory()
	{
		return new MSSQLDialectFactory();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getSequenceSupport()
	 */
	@Override
	public void getSequenceSupport()
	{
		assertThat(this.dialect.getSequenceSupport()).isSameAs(this.dialect);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getIdentityColumnSupport()
	 */
	@Override
	public void getIdentityColumnSupport()
	{
		assertThat(this.dialect.getIdentityColumnSupport()).isSameAs(this.dialect);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getTruncateTableSQL()
	 */
	@Override
	public void getTruncateTableSQL() throws SQLException
	{
		TableProperties table = mock(TableProperties.class);
		QualifiedName name = mock(QualifiedName.class);

		when(table.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("table");

		String result = this.dialect.getTruncateTableSQL(table);

		assertThat(result).isEqualTo("TRUNCATE TABLE table");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getCreateForeignKeyConstraintSQL()
	 */
	@Override
	public void getCreateForeignKeyConstraintSQL() throws SQLException
	{
		QualifiedName table = mock(QualifiedName.class);
		QualifiedName foreignTable = mock(QualifiedName.class);
		ForeignKeyConstraint constraint = mock(ForeignKeyConstraint.class);

		when(table.getDDLName()).thenReturn("table");
		when(foreignTable.getDDLName()).thenReturn("foreign_table");
		when(constraint.getName()).thenReturn("name");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("column1", "column2"));
		when(constraint.getForeignTable()).thenReturn(foreignTable);
		when(constraint.getForeignColumnList()).thenReturn(Arrays.asList("foreign_column1", "foreign_column2"));
		when(constraint.getDeferrability()).thenReturn(DatabaseMetaData.importedKeyInitiallyDeferred);
		when(constraint.getDeleteRule()).thenReturn(DatabaseMetaData.importedKeyCascade);
		when(constraint.getUpdateRule()).thenReturn(DatabaseMetaData.importedKeyRestrict);

		String result = this.dialect.getCreateForeignKeyConstraintSQL(constraint);

		assertThat(result).isEqualTo("ALTER TABLE table WITH NOCHECK ADD CONSTRAINT name FOREIGN KEY (column1, column2) REFERENCES foreign_table (foreign_column1, foreign_column2) ON DELETE CASCADE ON UPDATE NO ACTION");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getDropForeignKeyConstraintSQL()
	 */
	@Override
	public void getDropForeignKeyConstraintSQL() throws SQLException
	{
		QualifiedName table = mock(QualifiedName.class);
		QualifiedName foreignTable = mock(QualifiedName.class);
		ForeignKeyConstraint constraint = mock(ForeignKeyConstraint.class);

		when(table.getDDLName()).thenReturn("table");
		when(foreignTable.getDDLName()).thenReturn("foreign_table");
		when(constraint.getName()).thenReturn("name");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("column1", "column2"));
		when(constraint.getForeignTable()).thenReturn(foreignTable);
		when(constraint.getForeignColumnList()).thenReturn(Arrays.asList("foreign_column1", "foreign_column2"));
		when(constraint.getDeferrability()).thenReturn(DatabaseMetaData.importedKeyInitiallyDeferred);
		when(constraint.getDeleteRule()).thenReturn(DatabaseMetaData.importedKeyCascade);
		when(constraint.getUpdateRule()).thenReturn(DatabaseMetaData.importedKeyRestrict);

		String result = this.dialect.getDropForeignKeyConstraintSQL(constraint);

		assertThat(result).isEqualTo("ALTER TABLE table DROP CONSTRAINT name");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#evaluateCurrentDate()
	 */
	@Override
	public void evaluateCurrentDate()
	{
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());

		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATE FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT GETDATE() FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT GETDATE ( ) FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CCURRENT_DATE FROM test", date)).isEqualTo("SELECT CCURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATES FROM test", date)).isEqualTo("SELECT CURRENT_DATES FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_TIME FROM test", date)).isEqualTo("SELECT CURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_TIMESTAMP FROM test", date)).isEqualTo("SELECT CURRENT_TIMESTAMP FROM test");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#evaluateCurrentTime()
	 */
	@Override
	public void evaluateCurrentTime()
	{
		java.sql.Time time = new java.sql.Time(System.currentTimeMillis());

		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME(2) FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME ( 2 ) FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT GETDATE() FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT GETDATE ( ) FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME FROM test", time)).isEqualTo("SELECT LOCALTIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_DATE FROM test", time)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIMESTAMP FROM test", time)).isEqualTo("SELECT CURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIMESTAMP FROM test", time)).isEqualTo("SELECT LOCALTIMESTAMP FROM test");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#evaluateCurrentTimestamp()
	 */
	@Override
	public void evaluateCurrentTimestamp()
	{
		java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());

		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP(2) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP ( 2 ) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT GETDATE() FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT GETDATE ( ) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT SYSDATETIME() FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT SYSDATETIME ( ) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP FROM test", timestamp)).isEqualTo("SELECT LOCALTIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_DATE FROM test", timestamp)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIME FROM test", timestamp)).isEqualTo("SELECT CURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIME FROM test", timestamp)).isEqualTo("SELECT LOCALTIME FROM test");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#evaluateRand()
	 */
	@Override
	public void evaluateRand()
	{
		assertThat(this.dialect.evaluateRand("SELECT RAND(42) FROM test")).matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test");
		assertThat(this.dialect.evaluateRand("SELECT RAND ( 42 ) FROM test")).matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test");
		assertThat(this.dialect.evaluateRand("SELECT RAND() FROM test")).matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test");
		assertThat(this.dialect.evaluateRand("SELECT RAND ( ) FROM test")).matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test");
		assertThat(this.dialect.evaluateRand("SELECT RAND FROM test")).isEqualTo("SELECT RAND FROM test");
		assertThat(this.dialect.evaluateRand("SELECT OPERAND() FROM test")).isEqualTo("SELECT OPERAND() FROM test");
		assertThat(this.dialect.evaluateRand("SELECT 1 FROM test")).isEqualTo("SELECT 1 FROM test");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getAlterIdentityColumnSQL()
	 */
	@Override
	public void getAlterIdentityColumnSQL() throws SQLException
	{
		TableProperties table = mock(TableProperties.class);
		ColumnProperties column = mock(ColumnProperties.class);
		QualifiedName name = mock(QualifiedName.class);

		when(table.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("table");
		when(column.getName()).thenReturn("column");

		String result = this.dialect.getIdentityColumnSupport().getAlterIdentityColumnSQL(table, column, 1000L);

		assertThat(result).isEqualTo("DBCC CHECKIDENT (table, RESEED, 1000)");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getAlterSequenceSQL()
	 */
	@Override
	public void getAlterSequenceSQL() throws SQLException
	{
		SequenceProperties sequence = mock(SequenceProperties.class);
		QualifiedName name = mock(QualifiedName.class);

		when(sequence.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("sequence");
		when(sequence.getIncrement()).thenReturn(1);

		String result = this.dialect.getSequenceSupport().getAlterSequenceSQL(sequence, 1000L);

		assertThat(result).isEqualTo("ALTER SEQUENCE sequence RESTART WITH 1000");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getNextSequenceValueSQL()
	 */
	@Override
	public void getNextSequenceValueSQL() throws SQLException
	{
		QualifiedName name = mock(QualifiedName.class);
		SequenceProperties sequence = mock(SequenceProperties.class);

		when(sequence.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("sequence");

		String result = this.dialect.getSequenceSupport().getNextSequenceValueSQL(sequence);

		assertThat(result).isEqualTo("SELECT NEXT VALUE FOR sequence");
	}

	/**
	 * Test MSSQL-specific connection properties parsing.
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getConnectionProperties()
	 */
	@Override
	public void getConnectionProperties() throws SQLException
	{
		Database<Void> database = mock(Database.class);
		Credentials credentials = mock(Credentials.class);
		Decoder decoder = mock(Decoder.class);
		Connection connection = mock(Connection.class);
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		String host = "myhost";
		String port = "1433";
		String databaseName = "mydb";
		String user = "user";
		String password = "password";

		when(database.getCredentials()).thenReturn(credentials);
		when(credentials.getUser()).thenReturn(user);
		when(credentials.decodePassword(decoder)).thenAnswer(invocation -> password.toCharArray());
		when(database.connect(decoder)).thenReturn(connection);
		when(connection.getMetaData()).thenReturn(metaData);
		when(metaData.getUserName()).thenReturn(user);

		when(metaData.getURL()).thenReturn(String.format("jdbc:sqlserver://%s:%s;databaseName=%s", host, port, databaseName));

		ConnectionProperties properties = this.dialect.getConnectionProperties(database, decoder);

		assertThat(properties.getHost()).isEqualTo(host);
		assertThat(properties.getPort()).isEqualTo(port);
		assertThat(properties.getDatabase()).isEqualTo(databaseName);
		assertThat(properties.getUser()).isSameAs(user);
		assertThat(properties.getPassword()).isEqualTo(password);
	}
}
