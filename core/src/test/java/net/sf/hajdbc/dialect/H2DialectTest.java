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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.SequenceProperties;
import net.sf.hajdbc.SequencePropertiesFactory;
import net.sf.hajdbc.dialect.h2.H2DialectFactory;

import org.h2.api.ErrorCode;

/**
 * @author Paul Ferraro
 */
@SuppressWarnings("nls")
public class H2DialectTest extends StandardDialectTest
{
	public H2DialectTest()
	{
		super(new H2DialectFactory());
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
		
		assertThat(result).isEqualTo("ALTER TABLE table ADD CONSTRAINT name FOREIGN KEY (column1, column2) REFERENCES foreign_table (foreign_column1, foreign_column2) ON DELETE CASCADE ON UPDATE RESTRICT");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getSequences()
	 */
	@Override
	public void getSequences() throws SQLException
	{
		SequencePropertiesFactory factory = mock(SequencePropertiesFactory.class);
		SequenceProperties sequence1 = mock(SequenceProperties.class);
		SequenceProperties sequence2 = mock(SequenceProperties.class);
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		Connection connection = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		
		when(metaData.getConnection()).thenReturn(connection);
		when(connection.createStatement()).thenReturn(statement);
		when(statement.executeQuery("SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME, INCREMENT FROM INFORMATION_SCHEMA.SEQUENCES")).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
		when(resultSet.getString(1)).thenReturn("schema1").thenReturn("schema2");
		when(resultSet.getString(2)).thenReturn("sequence1").thenReturn("sequence2");
		when(resultSet.getInt(3)).thenReturn(1).thenReturn(2);
		when(factory.createSequenceProperties("schema1", "sequence1", 1)).thenReturn(sequence1);
		when(factory.createSequenceProperties("schema2", "sequence2", 2)).thenReturn(sequence2);

		Collection<SequenceProperties> results = this.dialect.getSequenceSupport().getSequences(metaData, factory);
		
		verify(statement).close();
		
		assertThat(results.size()).isEqualTo(2);
		
		Iterator<SequenceProperties> sequences = results.iterator();

		assertThat(sequences.next()).isSameAs(sequence1);
		assertThat(sequences.next()).isSameAs(sequence2);
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getNextSequenceValueSQL()
	 */
	@Override
	public void getNextSequenceValueSQL() throws SQLException
	{
		SequenceProperties sequence = mock(SequenceProperties.class);
		QualifiedName name = mock(QualifiedName.class);
		
		when(sequence.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("sequence");
		
		String result = this.dialect.getSequenceSupport().getNextSequenceValueSQL(sequence);
		
		assertThat(result).isEqualTo("CALL NEXT VALUE FOR sequence");
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getDefaultSchemas()
	 */
	@Override
	public void getDefaultSchemas() throws SQLException
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		List<String> result = this.dialect.getDefaultSchemas(metaData);
		
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isEqualTo("PUBLIC");
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
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATE() FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATE ( ) FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURDATE FROM test", date)).isEqualTo("SELECT CURDATE FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURDATE() FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURDATE ( ) FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CCURRENT_DATE FROM test", date)).isEqualTo("SELECT CCURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATES FROM test", date)).isEqualTo("SELECT CURRENT_DATES FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_TIME FROM test", date)).isEqualTo("SELECT CURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_TIMESTAMP FROM test", date)).isEqualTo("SELECT CURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT 1 FROM test", date)).isEqualTo("SELECT 1 FROM test");
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
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME() FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME ( ) FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURTIME FROM test", time)).isEqualTo("SELECT CURTIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURTIME() FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURTIME ( ) FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME FROM test", time)).isEqualTo("SELECT LOCALTIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME(2) FROM test", time)).isEqualTo("SELECT LOCALTIME(2) FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME ( 2 ) FROM test", time)).isEqualTo("SELECT LOCALTIME ( 2 ) FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CCURRENT_TIME FROM test", time)).isEqualTo("SELECT CCURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT LLOCALTIME FROM test", time)).isEqualTo("SELECT LLOCALTIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_DATE FROM test", time)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIMESTAMP FROM test", time)).isEqualTo("SELECT CURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIMESTAMP FROM test", time)).isEqualTo("SELECT LOCALTIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT 1 FROM test", time)).isEqualTo("SELECT 1 FROM test");
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
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP() FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP ( ) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT NOW FROM test", timestamp)).isEqualTo("SELECT NOW FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT NOW() FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT NOW ( ) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP FROM test", timestamp)).isEqualTo("SELECT LOCALTIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP(2) FROM test", timestamp)).isEqualTo("SELECT LOCALTIMESTAMP(2) FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP ( 2 ) FROM test", timestamp)).isEqualTo("SELECT LOCALTIMESTAMP ( 2 ) FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CCURRENT_TIMESTAMP FROM test", timestamp)).isEqualTo("SELECT CCURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LLOCALTIMESTAMP FROM test", timestamp)).isEqualTo("SELECT LLOCALTIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_DATE FROM test", timestamp)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIME FROM test", timestamp)).isEqualTo("SELECT CURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIME FROM test", timestamp)).isEqualTo("SELECT LOCALTIME FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT 1 FROM test", timestamp)).isEqualTo("SELECT 1 FROM test");
	}

	@Override
	public void indicatesFailureSQLException()
	{
		assertThat(this.dialect.indicatesFailure(new SQLException("", "", ErrorCode.UNKNOWN_MODE_1))).isFalse();
		assertThat(this.dialect.indicatesFailure(new SQLException("", "", ErrorCode.FILE_CORRUPTED_1))).isTrue();
		assertThat(this.dialect.indicatesFailure(new SQLException("", "", ErrorCode.CONNECTION_BROKEN_1))).isTrue();
		assertThat(this.dialect.indicatesFailure(new SQLException("", "", ErrorCode.DATABASE_NOT_FOUND_1))).isTrue();
		assertThat(this.dialect.indicatesFailure(new SQLException("", "", ErrorCode.OUT_OF_MEMORY))).isTrue();
		assertThat(this.dialect.indicatesFailure(new SQLException("", "", ErrorCode.REMOTE_CONNECTION_NOT_ALLOWED))).isTrue();
		assertThat(this.dialect.indicatesFailure(new SQLException("", "", ErrorCode.DATABASE_CALLED_AT_SHUTDOWN))).isTrue();
		assertThat(this.dialect.indicatesFailure(new SQLException("", "", ErrorCode.URL_FORMAT_ERROR_2))).isTrue();
	}
}
