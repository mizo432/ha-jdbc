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
import java.util.List;

import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.dialect.mysql.MySQLDialectFactory;

/**
 * @author Paul Ferraro
 *
 */
@SuppressWarnings("nls")
public class MySQLDialectTest extends StandardDialectTest
{
	@Override
	protected DialectFactory createDialectFactory()
	{
		return new MySQLDialectFactory();
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
		
		assertThat(result).isEqualTo("ALTER TABLE table DROP FOREIGN KEY name");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getCreateUniqueConstraintSQL()
	 */
	@Override
	public void getCreateUniqueConstraintSQL() throws SQLException
	{
		QualifiedName table = mock(QualifiedName.class);
		UniqueConstraint constraint = mock(UniqueConstraint.class);
		
		when(table.getDDLName()).thenReturn("table");
		when(constraint.getName()).thenReturn("name");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("column1", "column2"));
		
		String result = this.dialect.getCreateUniqueConstraintSQL(constraint);
		
		assertThat(result).isEqualTo("ALTER TABLE table ADD UNIQUE name (column1, column2)");
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getDropUniqueConstraintSQL()
	 */
	@Override
	public void getDropUniqueConstraintSQL() throws SQLException
	{
		QualifiedName table = mock(QualifiedName.class);
		UniqueConstraint constraint = mock(UniqueConstraint.class);
		
		when(table.getDDLName()).thenReturn("table");
		when(constraint.getName()).thenReturn("name");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("column1", "column2"));
		
		String result = this.dialect.getDropUniqueConstraintSQL(constraint);
		
		assertThat(result).isEqualTo("ALTER TABLE table DROP INDEX name");
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
		when(statement.executeQuery("SELECT DATABASE()")).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(false);
		when(resultSet.getString(1)).thenReturn("database");

		List<String> result = this.dialect.getDefaultSchemas(metaData);

		verify(statement).close();
		
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isEqualTo("database");
	}
	
	@Override
	public void evaluateCurrentDate()
	{
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATE FROM test", date)).isEqualTo(String.format("SELECT '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURDATE() FROM test", date)).isEqualTo(String.format("SELECT '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURDATE ( ) FROM test", date)).isEqualTo(String.format("SELECT '%s' FROM test", date.toString()));
		assertThat(this.dialect.evaluateCurrentDate("SELECT CCURRENT_DATE FROM test", date)).isEqualTo("SELECT CCURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_DATES FROM test", date)).isEqualTo("SELECT CURRENT_DATES FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CCURDATE() FROM test", date)).isEqualTo("SELECT CCURDATE() FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_TIME FROM test", date)).isEqualTo("SELECT CURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT CURRENT_TIMESTAMP FROM test", date)).isEqualTo("SELECT CURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentDate("SELECT 1 FROM test", date)).isEqualTo("SELECT 1 FROM test");
	}
	
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
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURTIME() FROM test", time)).isEqualTo(String.format("SELECT '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURTIME ( ) FROM test", time)).isEqualTo(String.format("SELECT '%s' FROM test", time.toString()));
		assertThat(this.dialect.evaluateCurrentTime("SELECT CCURRENT_TIME FROM test", time)).isEqualTo("SELECT CCURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT LLOCALTIME FROM test", time)).isEqualTo("SELECT LLOCALTIME FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_DATE FROM test", time)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT CURRENT_TIMESTAMP FROM test", time)).isEqualTo("SELECT CURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT LOCALTIMESTAMP FROM test", time)).isEqualTo("SELECT LOCALTIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTime("SELECT 1 FROM test", time)).isEqualTo("SELECT 1 FROM test");
	}
	
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
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT NOW() FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT NOW ( ) FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT SYSDATE() FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT SYSDATE ( ) FROM test", timestamp)).isEqualTo(String.format("SELECT '%s' FROM test", timestamp.toString()));
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CCURRENT_TIMESTAMP FROM test", timestamp)).isEqualTo("SELECT CCURRENT_TIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LLOCALTIMESTAMP FROM test", timestamp)).isEqualTo("SELECT LLOCALTIMESTAMP FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_DATE FROM test", timestamp)).isEqualTo("SELECT CURRENT_DATE FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT CURRENT_TIME FROM test", timestamp)).isEqualTo("SELECT CURRENT_TIME FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT LOCALTIME FROM test", timestamp)).isEqualTo("SELECT LOCALTIME FROM test");
		assertThat(this.dialect.evaluateCurrentTimestamp("SELECT 1 FROM test", timestamp)).isEqualTo("SELECT 1 FROM test");
	}
}
