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
import net.sf.hajdbc.dialect.firebird.FirebirdDialectFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Paul Ferraro
 *
 */
@SuppressWarnings("nls")
public class FirebirdDialectTest extends StandardDialectTest
{
	public FirebirdDialectTest()
	{
		super(new FirebirdDialectFactory());
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

		assertThat(result).isEqualTo("SET GENERATOR sequence TO 1000");
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
		when(statement.executeQuery("SELECT RDB$GENERATOR_NAME FROM RDB$GENERATORS")).thenReturn(resultSet);
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
		
		assertThat(result).isEqualTo("SELECT GEN_ID(sequence, 1) FROM RDB$DATABASE");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#isSelectForUpdate()
	 */
	@Override
	public void isSelectForUpdate() throws SQLException
	{
		assertThat(this.dialect.isSelectForUpdate("SELECT * FROM test WITH LOCK")).isTrue();
		assertThat(this.dialect.isSelectForUpdate("SELECT * FROM test")).isFalse();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#parseSequence()
	 */
	@Override
	public void parseSequence() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		assertThat(support.parseSequence("SELECT GEN_ID(sequence, 1) FROM RDB$DATABASE")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT GEN_ID(sequence, 1), * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("INSERT INTO table VALUES (GEN_ID(sequence, 1), 0)")).isEqualTo("sequence");
		assertThat(support.parseSequence("UPDATE table SET id = GEN_ID(sequence, 1)")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT NEXT VALUE FOR test")).isNull();
		assertThat(support.parseSequence("SELECT NEXT VALUE FOR test, * FROM table")).isNull();
		assertThat(support.parseSequence("INSERT INTO table VALUES (NEXT VALUE FOR test)")).isNull();
		assertThat(support.parseSequence("UPDATE table SET id = NEXT VALUE FOR test")).isNull();
		assertThat(support.parseSequence("SELECT * FROM table")).isNull();
	}
}
