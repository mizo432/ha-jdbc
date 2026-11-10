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
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.sf.hajdbc.SequenceProperties;
import net.sf.hajdbc.SequencePropertiesFactory;
import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.dialect.ingres.IngresDialectFactory;

/**
 * @author Paul Ferraro
 */
@SuppressWarnings("nls")
public class IngresDialectTest extends StandardDialectTest
{
	@Override
	protected DialectFactory createDialectFactory()
	{
		return new IngresDialectFactory();
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
		when(statement.executeQuery("SELECT seq_name FROM iisequence")).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
		when(resultSet.getString(1)).thenReturn("sequence1").thenReturn("sequence2");
		when(factory.createSequenceProperties(null, "sequence1", 1)).thenReturn(sequence1);
		when(factory.createSequenceProperties(null, "sequence2", 1)).thenReturn(sequence2);

		Collection<SequenceProperties> results = this.dialect.getSequenceSupport().getSequences(metaData, factory);
		
		verify(statement).close();
		
		assertThat(results.size()).isEqualTo(2);
		
		Iterator<SequenceProperties> sequences = results.iterator();

		assertThat(sequences.next()).isSameAs(sequence1);
		assertThat(sequences.next()).isSameAs(sequence2);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#parseSequence()
	 */
	@Override
	public void parseSequence() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		assertThat(support.parseSequence("SELECT NEXT VALUE FOR sequence")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT CURRENT VALUE FOR sequence")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT NEXT VALUE FOR sequence, * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT CURRENT VALUE FOR sequence, * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("INSERT INTO table VALUES (NEXT VALUE FOR sequence, 0)")).isEqualTo("sequence");
		assertThat(support.parseSequence("INSERT INTO table VALUES (CURRENT VALUE FOR sequence, 0)")).isEqualTo("sequence");
		assertThat(support.parseSequence("UPDATE table SET id = NEXT VALUE FOR sequence")).isEqualTo("sequence");
		assertThat(support.parseSequence("UPDATE table SET id = CURRENT VALUE FOR sequence")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT sequence.nextval")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT sequence.currval")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT sequence.nextval, * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT sequence.currval, * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("INSERT INTO table VALUES (sequence.nextval, 0)")).isEqualTo("sequence");
		assertThat(support.parseSequence("INSERT INTO table VALUES (sequence.currval, 0)")).isEqualTo("sequence");
		assertThat(support.parseSequence("UPDATE table SET id = sequence.nextval")).isEqualTo("sequence");
		assertThat(support.parseSequence("UPDATE table SET id = sequence.currval")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT * FROM table")).isNull();
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
		assertThat(this.dialect.evaluateCurrentDate("SELECT DATE('TODAY') FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT DATE ( 'TODAY' ) FROM test", date)).isEqualTo(String.format("SELECT DATE '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATES FROM test", date)).isEqualTo("SELECT CURRENT_DATES FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CCURRENT_DATE FROM test", date)).isEqualTo("SELECT CCURRENT_DATE FROM test");
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
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCAL_TIME FROM test", time)).isEqualTo(String.format("SELECT TIME '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIMES FROM test", time)).isEqualTo("SELECT CURRENT_TIMES FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CCURRENT_TIME FROM test", time)).isEqualTo("SELECT CCURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_DATE FROM test", time)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIMESTAMP FROM test", time)).isEqualTo("SELECT CURRENT_TIMESTAMP FROM test");
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
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCAL_TIMESTAMP FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT DATE('NOW') FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT DATE ( 'NOW' ) FROM test", timestamp)).isEqualTo(String.format("SELECT TIMESTAMP '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIMESTAMPS FROM test", timestamp)).isEqualTo("SELECT CURRENT_TIMESTAMPS FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CCURRENT_TIMESTAMP FROM test", timestamp)).isEqualTo("SELECT CCURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_DATE FROM test", timestamp)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIME FROM test", timestamp)).isEqualTo("SELECT CURRENT_TIME FROM test");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#evaluateRand()
	 */
	@Override
	public void evaluateRand()
	{
		assertThat(Pattern.matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test", this.dialect.evaluateRand("SELECT RANDOMF() FROM test"))).isTrue();
		assertThat(Pattern.matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test", this.dialect.evaluateRand("SELECT RANDOMF ( ) FROM test"))).isTrue();
		assertThat(this.dialect.evaluateRand("SELECT RANDOMF FROM test")).isEqualTo("SELECT RANDOMF FROM test");
		assertThat(this.dialect.evaluateRand("SELECT OPERANDOMF() FROM test")).isEqualTo("SELECT OPERANDOMF() FROM test");
		assertThat(this.dialect.evaluateRand("SELECT RAND() FROM test")).isEqualTo("SELECT RAND() FROM test");
	}
}
