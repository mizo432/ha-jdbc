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
import java.sql.Types;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.hajdbc.ColumnProperties;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.SequenceProperties;
import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.dialect.postgresql.PostgreSQLDialectFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Paul Ferraro
 *
 */
public class PostgreSQLDialectTest extends StandardDialectTest
{
	@Override
	protected DialectFactory createDialectFactory()
	{
		return new PostgreSQLDialectFactory();
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
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getColumnType()
	 */
	@Override
	public void getColumnType() throws SQLException
	{
		ColumnProperties column = mock(ColumnProperties.class);
		
		when(column.getNativeType()).thenReturn("oid");
		
		int result = this.dialect.getColumnType(column);
		
		assertThat(result).isEqualTo(Types.BLOB);
		
		when(column.getNativeType()).thenReturn("int");		
		when(column.getType()).thenReturn(Types.INTEGER);
		
		result = this.dialect.getColumnType(column);
		
		assertThat(result).isEqualTo(Types.INTEGER);
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
		
		assertThat(result).isEqualTo("SELECT NEXTVAL('sequence')");
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#parseSequence()
	 */
	@Override
	public void parseSequence() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		
		assertThat(support.parseSequence("SELECT CURRVAL('sequence')")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT nextval('sequence'), * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("INSERT INTO table VALUES (NEXTVAL('sequence'), 0)")).isEqualTo("sequence");
		assertThat(support.parseSequence("UPDATE table SET id = NEXTVAL('sequence')")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT NEXT VALUE FOR sequence")).isNull();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getDefaultSchemas()
	 */
	@Override
	public void getDefaultSchemas() throws SQLException
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		Connection connection = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		
		when(metaData.getConnection()).thenReturn(connection);
		when(connection.createStatement()).thenReturn(statement);
		
		when(statement.executeQuery("SHOW search_path")).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(false);
		when(resultSet.getString(1)).thenReturn("$user,public");
		
		when(metaData.getUserName()).thenReturn("user");
		
		List<String> result = this.dialect.getDefaultSchemas(metaData);

		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0)).isEqualTo("user");
		assertThat(result.get(1)).isEqualTo("public");
		
		verify(resultSet).close();
		verify(statement).close();
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

		assertThat(result).isEqualTo("ALTER SEQUENCE table_column_seq RESTART WITH 1000");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#evaluateRand()
	 */
	@Override
	public void evaluateRand()
	{
		assertThat(Pattern.matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test", this.dialect.evaluateRand("SELECT RANDOM() FROM test"))).isTrue();
		assertThat(Pattern.matches("SELECT ((0\\.\\d+)|([1-9]\\.\\d+E\\-\\d+)) FROM test", this.dialect.evaluateRand("SELECT RANDOM ( ) FROM test"))).isTrue();
		assertThat(this.dialect.evaluateRand("SELECT RAND() FROM test")).isEqualTo("SELECT RAND() FROM test");
		assertThat(this.dialect.evaluateRand("SELECT OPERANDOM() FROM test")).isEqualTo("SELECT OPERANDOM() FROM test");
	}
}
