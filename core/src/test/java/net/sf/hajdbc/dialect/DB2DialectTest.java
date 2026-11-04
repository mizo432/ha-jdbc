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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;

import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.SequenceProperties;
import net.sf.hajdbc.SequencePropertiesFactory;
import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.dialect.db2.DB2DialectFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Paul Ferraro
 *
 */
public class DB2DialectTest extends StandardDialectTest
{
	public DB2DialectTest()
	{
		super(new DB2DialectFactory());
	}

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
		when(statement.executeQuery("SELECT SEQSCHEMA, SEQNAME, INCREMENT FROM SYSCAT.SEQUENCES")).thenReturn(resultSet);
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

	@Override
	public void getNextSequenceValueSQL() throws SQLException
	{
		SequenceProperties sequence = mock(SequenceProperties.class);
		QualifiedName name = mock(QualifiedName.class);
		
		when(sequence.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("sequence");
		
		String result = this.dialect.getSequenceSupport().getNextSequenceValueSQL(sequence);
		
		assertThat(result).isEqualTo("VALUES NEXTVAL FOR sequence");
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#parseSequence()
	 */
	@Override
	public void parseSequence() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		assertThat(support.parseSequence("VALUES NEXTVAL FOR test")).isEqualTo("test");
		assertThat(support.parseSequence("INSERT INTO table VALUES (NEXTVAL FOR test, 0)")).isEqualTo("test");
		assertThat(support.parseSequence("INSERT INTO table VALUES (PREVVAL FOR test, 0)")).isEqualTo("test");
		assertThat(support.parseSequence("UPDATE table SET id = NEXTVAL FOR test")).isEqualTo("test");
		assertThat(support.parseSequence("UPDATE table SET id = PREVVAL FOR test")).isEqualTo("test");
		assertThat(support.parseSequence("SELECT * FROM test")).isNull();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#evaluateCurrentDate()
	 */
	@Override
	public void evaluateCurrentDate()
	{
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATE FROM test", date)).isEqualTo(String.format("SELECT '%s' FROM test", date.toString()));
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
		
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME FROM test", time)).isEqualTo(String.format("SELECT '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME(2) FROM test", time)).isEqualTo(String.format("SELECT '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIME ( 2 ) FROM test", time)).isEqualTo(String.format("SELECT '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME FROM test", time)).isEqualTo(String.format("SELECT '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME(2) FROM test", time)).isEqualTo(String.format("SELECT '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIME ( 2 ) FROM test", time)).isEqualTo(String.format("SELECT '%s' FROM test", time.toString()));
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
		
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP(2) FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMP ( 2 ) FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP(2) FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIMESTAMP ( 2 ) FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CCURRENT_TIMESTAMP FROM test", timestamp)).isEqualTo("SELECT CCURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LLOCALTIMESTAMP FROM test", timestamp)).isEqualTo("SELECT LLOCALTIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_DATE FROM test", timestamp)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIME FROM test", timestamp)).isEqualTo("SELECT CURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIME FROM test", timestamp)).isEqualTo("SELECT LOCALTIME FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT 1 FROM test", timestamp)).isEqualTo("SELECT 1 FROM test");
	}
}
