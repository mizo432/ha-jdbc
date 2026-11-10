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
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.sql.SQLRecoverableException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLTransientConnectionException;
import java.sql.SQLTransientException;
import java.sql.SQLWarning;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.sql.rowset.RowSetWarning;
import javax.sql.rowset.serial.SerialException;
import javax.sql.rowset.spi.SyncFactoryException;
import javax.sql.rowset.spi.SyncProviderException;
import javax.transaction.xa.XAException;

import net.sf.hajdbc.ColumnProperties;
import net.sf.hajdbc.Credentials;
import net.sf.hajdbc.Database;
import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.IdentityColumnSupport;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.SequenceProperties;
import net.sf.hajdbc.SequencePropertiesFactory;
import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.codec.Decoder;

import org.junit.Test;

/**
 * @author Paul Ferraro
 *
 */
public class StandardDialectTest
{
	private DialectFactory factory;
	Dialect dialect;
	
	public StandardDialectTest()
	{
		this(new StandardDialectFactory());
	}
	
	protected StandardDialectTest(DialectFactory factory)
	{
		this.factory = factory;
		this.dialect = factory.createDialect();
	}

	@Test
	public void getSequenceSupport()
	{
		assertThat(this.dialect.getSequenceSupport()).isNull();
	}
	
	@Test
	public void getIdentityColumnSupport()
	{
		assertThat(this.dialect.getIdentityColumnSupport()).isNull();
	}
	
	@Test
	public void getAlterSequenceSQL() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		
		if (support != null)
		{
			SequenceProperties sequence = mock(SequenceProperties.class);
			QualifiedName name = mock(QualifiedName.class);
			
			when(sequence.getName()).thenReturn(name);
			when(name.getDDLName()).thenReturn("sequence");
			when(sequence.getIncrement()).thenReturn(1);
			
			String result = support.getAlterSequenceSQL(sequence, 1000L);
			
			assertThat(result).isEqualTo("ALTER SEQUENCE sequence RESTART WITH 1000");
		}
	}

	@Test
	public void getColumnType() throws SQLException
	{
		ColumnProperties column = mock(ColumnProperties.class);
		
		when(column.getType()).thenReturn(Types.INTEGER);
		
		int result = this.dialect.getColumnType(column);
		
		assertThat(result).isEqualTo(Types.INTEGER);
	}

	@Test
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
		
		assertThat(result).isEqualTo("ALTER TABLE table ADD CONSTRAINT name FOREIGN KEY (column1, column2) REFERENCES foreign_table (foreign_column1, foreign_column2) ON DELETE CASCADE ON UPDATE RESTRICT DEFERRABLE INITIALLY DEFERRED");
	}

	@Test
	public void getCreateUniqueConstraintSQL() throws SQLException
	{
		QualifiedName table = mock(QualifiedName.class);
		UniqueConstraint constraint = mock(UniqueConstraint.class);
		
		when(table.getDDLName()).thenReturn("table");
		when(constraint.getName()).thenReturn("name");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("column1", "column2"));
		
		String result = this.dialect.getCreateUniqueConstraintSQL(constraint);
		
		assertThat(result).isEqualTo("ALTER TABLE table ADD CONSTRAINT name UNIQUE (column1, column2)");
	}

	@Test
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

	@Test
	public void getDropUniqueConstraintSQL() throws SQLException
	{
		QualifiedName table = mock(QualifiedName.class);
		UniqueConstraint constraint = mock(UniqueConstraint.class);
		
		when(table.getDDLName()).thenReturn("table");
		when(constraint.getName()).thenReturn("name");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("column1", "column2"));
		
		String result = this.dialect.getDropUniqueConstraintSQL(constraint);
		
		assertThat(result).isEqualTo("ALTER TABLE table DROP CONSTRAINT name");
	}

	@Test
	public void getNextSequenceValueSQL() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		
		if (support != null)
		{
			QualifiedName name = mock(QualifiedName.class);
			SequenceProperties sequence = mock(SequenceProperties.class);
			
			when(sequence.getName()).thenReturn(name);
			when(name.getDMLName()).thenReturn("sequence");
			
			String result = support.getNextSequenceValueSQL(sequence);
			
			assertThat(result).isEqualTo("SELECT NEXT VALUE FOR sequence");
		}
	}

	@Test
	public void getSequences() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		
		if (support != null)
		{
			SequencePropertiesFactory factory = mock(SequencePropertiesFactory.class);
			SequenceProperties sequence1 = mock(SequenceProperties.class);
			SequenceProperties sequence2 = mock(SequenceProperties.class);
			DatabaseMetaData metaData = mock(DatabaseMetaData.class);
			ResultSet resultSet = mock(ResultSet.class);
			
			when(metaData.supportsSchemasInTableDefinitions()).thenReturn(true);
			when(metaData.supportsSchemasInDataManipulation()).thenReturn(true);
			when(metaData.getTables(eq(""), eq((String) null), eq("%"), aryEq(new String[] { "SEQUENCE" }))).thenReturn(resultSet);
			when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
			when(resultSet.getString("TABLE_SCHEM")).thenReturn("schema1").thenReturn("schema2");
			when(resultSet.getString("TABLE_NAME")).thenReturn("sequence1").thenReturn("sequence2");
			when(factory.createSequenceProperties("schema1", "sequence1", 1)).thenReturn(sequence1);
			when(factory.createSequenceProperties("schema2", "sequence2", 1)).thenReturn(sequence2);
			
			Collection<SequenceProperties> results = support.getSequences(metaData, factory);
			
			verify(resultSet).close();
			
			assertThat(results.size()).isEqualTo(2);
			
			Iterator<SequenceProperties> sequences = results.iterator();

			assertThat(sequences.next()).isSameAs(sequence1);
			assertThat(sequences.next()).isSameAs(sequence2);
		}
	}

	@Test
	public void getTruncateTableSQL() throws SQLException
	{
		TableProperties table = mock(TableProperties.class);
		QualifiedName name = mock(QualifiedName.class);
		
		when(table.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("table");
		
		String result = this.dialect.getTruncateTableSQL(table);
		
		assertThat(result).isEqualTo("DELETE FROM table");
	}

	@Test
	public void isSelectForUpdate() throws SQLException
	{
		assertThat(this.dialect.isSelectForUpdate("SELECT * FROM test FOR UPDATE")).isTrue();
		assertThat(this.dialect.isSelectForUpdate("SELECT * FROM test")).isFalse();
	}

	@Test
	public void parseSequence() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		
		if (support != null)
		{
			assertThat(support.parseSequence("SELECT NEXT VALUE FOR test")).isEqualTo("test");
			assertThat(support.parseSequence("SELECT NEXT VALUE FOR test, * FROM table")).isEqualTo("test");
			assertThat(support.parseSequence("INSERT INTO table VALUES (NEXT VALUE FOR test)")).isEqualTo("test");
			assertThat(support.parseSequence("UPDATE table SET id = NEXT VALUE FOR test")).isEqualTo("test");
			assertThat(support.parseSequence("SELECT * FROM table")).isNull();
		}
	}

	@Test
	public void getDefaultSchemas() throws SQLException
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		
		String user = "user";
		
		when(metaData.getUserName()).thenReturn(user);
		
		List<String> result = this.dialect.getDefaultSchemas(metaData);
		
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isSameAs(user);
	}

	@Test
	public void parseInsertTable() throws SQLException
	{
		IdentityColumnSupport support = this.dialect.getIdentityColumnSupport();
		
		if (support != null)
		{
			assertThat(support.parseInsertTable("INSERT INTO table (column1, column2) VALUES (1, 2)")).isEqualTo("table");
			assertThat(support.parseInsertTable("INSERT INTO table VALUES (1, 2)")).isEqualTo("table");
			assertThat(support.parseInsertTable("INSERT table (column1, column2) VALUES (1, 2)")).isEqualTo("table");
			assertThat(support.parseInsertTable("INSERT table VALUES (1, 2)")).isEqualTo("table");
			assertThat(support.parseInsertTable("INSERT INTO table (column1, column2) SELECT column1, column2 FROM dummy")).isEqualTo("table");
			assertThat(support.parseInsertTable("INSERT INTO table SELECT column1, column2 FROM dummy")).isEqualTo("table");
			assertThat(support.parseInsertTable("INSERT table (column1, column2) SELECT column1, column2 FROM dummy")).isEqualTo("table");
			assertThat(support.parseInsertTable("INSERT table SELECT column1, column2 FROM dummy")).isEqualTo("table");
			assertThat(support.parseInsertTable("SELECT * FROM table WHERE 0=1")).isNull();
			assertThat(support.parseInsertTable("UPDATE table SET column = 0")).isNull();
		}
	}

	@Test
	public void evaluateCurrentDate()
	{
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATE FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CCURRENT_DATE FROM test", date)).isEqualTo("SELECT CCURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATES FROM test", date)).isEqualTo("SELECT CURRENT_DATES FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_TIME FROM test", date)).isEqualTo("SELECT CURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_TIMESTAMP FROM test", date)).isEqualTo("SELECT CURRENT_TIMESTAMP FROM test");
	}

	@Test
	public void evaluateCurrentTime()
	{
		java.sql.Time time = new java.sql.Time(System.currentTimeMillis());
		
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME(2) FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME ( 2 ) FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME(2) FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME ( 2 ) FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CCURRENT_TIME FROM test", time)).isEqualTo("SELECT CCURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT LLOCALTIME FROM test", time)).isEqualTo("SELECT LLOCALTIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_DATE FROM test", time)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIMESTAMP FROM test", time)).isEqualTo("SELECT CURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIMESTAMP FROM test", time)).isEqualTo("SELECT LOCALTIMESTAMP FROM test");
	}

	@Test
	public void evaluateCurrentTimestamp()
	{
		java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
		
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP(2) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP ( 2 ) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP(2) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP ( 2 ) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CCURRENT_TIMESTAMP FROM test", timestamp)).isEqualTo("SELECT CCURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LLOCALTIMESTAMP FROM test", timestamp)).isEqualTo("SELECT LLOCALTIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_DATE FROM test", timestamp)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIME FROM test", timestamp)).isEqualTo("SELECT CURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIME FROM test", timestamp)).isEqualTo("SELECT LOCALTIME FROM test");
	}

	@Test
	public void evaluateRand()
	{
		assertThat(Pattern.matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test", this.dialect.evaluateRand("SELECT RAND() FROM test"))).isTrue();
		assertThat(Pattern.matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test", this.dialect.evaluateRand("SELECT RAND ( ) FROM test"))).isTrue();
		assertThat(this.dialect.evaluateRand("SELECT RAND FROM test")).isEqualTo("SELECT RAND FROM test");
		assertThat(this.dialect.evaluateRand("SELECT OPERAND() FROM test")).isEqualTo("SELECT OPERAND() FROM test");
		assertThat(this.dialect.evaluateRand("SELECT 1 FROM test")).isEqualTo("SELECT 1 FROM test");
	}

	@Test
	public void getAlterIdentityColumnSQL() throws SQLException
	{
		IdentityColumnSupport support = this.dialect.getIdentityColumnSupport();
		
		if (support != null)
		{
			TableProperties table = mock(TableProperties.class);
			ColumnProperties column = mock(ColumnProperties.class);
			QualifiedName name = mock(QualifiedName.class);
			
			when(table.getName()).thenReturn(name);
			when(name.getDDLName()).thenReturn("table");
			when(column.getName()).thenReturn("column");
			
			String result = support.getAlterIdentityColumnSQL(table, column, 1000L);
			
			assertThat(result).isEqualTo("ALTER TABLE table ALTER COLUMN column RESTART WITH 1000");
		}
	}
	
	@Test
	public void indicatesFailureSQLException()
	{
		assertThat(this.dialect.indicatesFailure(new SQLNonTransientConnectionException())).isTrue();
		assertThat(this.dialect.indicatesFailure(new SQLTransientConnectionException())).isTrue();
		int i = 0;
		char[] alphabet = new char[36];
		for (char c = '0'; c <= '9'; ++c)
		{
			alphabet[i++] = c;
		}
		for (char c = 'A'; c <= 'Z'; ++c)
		{
			alphabet[i++] = c;
		}
		for (int a = 0; a < alphabet.length; ++a)
		{
			for (int b = 0; b < alphabet.length; ++b)
			{
				this.indicatesFailure(String.format("%s%s000", alphabet[a], alphabet[b]));
			}
		}
		assertThat(this.dialect.indicatesFailure(new SQLException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new BatchUpdateException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new RowSetWarning())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SerialException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLClientInfoException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLNonTransientException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLDataException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLFeatureNotSupportedException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLIntegrityConstraintViolationException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLInvalidAuthorizationSpecException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLSyntaxErrorException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLRecoverableException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLTransientException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLTimeoutException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLTransactionRollbackException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLWarning())).isFalse();
		assertThat(this.dialect.indicatesFailure(new DataTruncation(1, false, false, 1, 1))).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLDataException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SyncFactoryException())).isFalse();
		assertThat(this.dialect.indicatesFailure(new SyncProviderException())).isFalse();
	}
	
	protected void indicatesFailure(String sqlState)
	{
		SQLException exception = new SQLException("reason", String.valueOf(sqlState));
		if (sqlState.startsWith("08"))
		{
			assertThat(this.dialect.indicatesFailure(exception)).as(sqlState).isTrue();
		}
		else
		{
			assertThat(this.dialect.indicatesFailure(exception)).as(sqlState).isFalse();
		}
	}
	
	@Test
	public void indicatesFailureXAException()
	{
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XAER_RMFAIL))).isTrue();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_HEURCOM))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_HEURHAZ))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_HEURMIX))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_HEURRB))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_NOMIGRATE))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBBASE))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBCOMMFAIL))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBDEADLOCK))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBDEADLOCK))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBEND))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBINTEGRITY))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBOTHER))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBPROTO))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBROLLBACK))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBTIMEOUT))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RBTRANSIENT))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RDONLY))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XA_RETRY))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XAER_ASYNC))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XAER_DUPID))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XAER_INVAL))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XAER_NOTA))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XAER_OUTSIDE))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XAER_PROTO))).isFalse();
		assertThat(this.dialect.indicatesFailure(new XAException(XAException.XAER_RMERR))).isFalse();
	}
	
	@Test
	public void isValid() throws SQLException
	{
		Connection connection = mock(Connection.class);
		
		when(connection.isValid(0)).thenReturn(true);
		
		boolean result = this.dialect.isValid(connection);
		
		assertThat(result).isTrue();
	}

	@Test
	public void getConnectionProperties() throws SQLException
	{
		Database<Void> database = mock(Database.class);
		Credentials credentials = mock(Credentials.class);
		Decoder decoder = mock(Decoder.class);
		Connection connection = mock(Connection.class);
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		String host = "myhost";
		String port = "1234";
		String databaseName = "mydb";
		String user = "user";
		String password = "password";
		
		when(database.getCredentials()).thenReturn(credentials);
		when(credentials.getUser()).thenReturn(user);
		when(credentials.decodePassword(decoder)).thenAnswer(invocation -> password.toCharArray());
		when(database.connect(decoder)).thenReturn(connection);
		when(connection.getMetaData()).thenReturn(metaData);
		when(metaData.getURL()).thenReturn(String.format("jdbc:%s://%s:%s/%s?loginTimeout=0&socketTimeout=0&prepareThreshold=5&unknownLength=2147483647&tcpKeepAlive=false&binaryTransfer=true", this.factory.getId(), host, port, databaseName));
		when(metaData.getUserName()).thenReturn(user);
		
		ConnectionProperties properties = this.dialect.getConnectionProperties(database, decoder);
		
		assertThat(properties.getHost()).isEqualTo(host);
		assertThat(properties.getPort()).isEqualTo(port);
		assertThat(properties.getDatabase()).isEqualTo(databaseName);
		assertThat(properties.getUser()).isSameAs(user);
		assertThat(properties.getPassword()).isEqualTo(password);

		when(metaData.getURL()).thenReturn(String.format("jdbc:%s:%s?loginTimeout=0&socketTimeout=0&prepareThreshold=5&unknownLength=2147483647&tcpKeepAlive=false&binaryTransfer=true", this.factory.getId(), databaseName));
		
		properties = this.dialect.getConnectionProperties(database, decoder);
		
		assertThat(properties.getHost()).isNull();
		assertThat(properties.getPort()).isNull();
		assertThat(properties.getDatabase()).isEqualTo(databaseName);
		assertThat(properties.getUser()).isSameAs(user);
		assertThat(properties.getPassword()).isEqualTo(password);
	}
}
