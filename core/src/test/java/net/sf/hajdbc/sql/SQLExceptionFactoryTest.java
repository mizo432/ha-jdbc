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
package net.sf.hajdbc.sql;

import java.sql.ClientInfoStatus;
import java.sql.SQLException;
import java.util.Collections;

import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.ExceptionType;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.durability.Durability.Phase;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Paul Ferraro
 */
public class SQLExceptionFactoryTest
{
	private final ExceptionFactory<SQLException> factory = new SQLExceptionFactory();
	
	@Test
	public void getTargetClass()
	{
		assertThat(this.factory.getTargetClass()).isSameAs(SQLException.class);
	}
	
	@Test
	public void createExceptionFromMessage()
	{
		String message = "message";
		
		SQLException result = this.factory.createException(message);
		
		assertThat(result.getMessage()).isSameAs(message);
		assertThat(result.getCause()).isNull();
	}
	
	@Test
	public void createExceptionFromException()
	{
		String message = "message";
		Exception exception = new Exception(message);
		
		SQLException result = this.factory.createException(exception);
		
		assertThat(result.getMessage()).isNotNull();
		assertThat(result.getMessage()).isSameAs(exception.getMessage());
		assertThat(result.getCause()).isSameAs(exception);
	}
	
	@Test
	public void createExceptionFromSQLException()
	{
		SQLException exception = new SQLException();
		
		SQLException result = this.factory.createException(exception);
		
		org.assertj.core.api.Assertions.assertThat((Throwable)result).isSameAs(exception);
	}
	
	@Test
	public void getType()
	{
		ExceptionType result = this.factory.getType();
		
		assertThat(result).isSameAs(ExceptionType.SQL);
	}
	
	@Test
	public void equals()
	{
		assertThat(this.factory.equals(null, null)).isTrue();
		assertThat(this.factory.equals(new SQLException(), null)).isFalse();
		assertThat(this.factory.equals(new SQLException(), new SQLException())).isTrue();

		assertThat(this.factory.equals(new java.sql.SQLDataException(), new java.sql.SQLDataException())).isTrue();
		assertThat(this.factory.equals(new java.sql.SQLClientInfoException(), new java.sql.SQLDataException())).isFalse();

		assertThat(this.factory.equals(new java.sql.BatchUpdateException(new int[] { 1, 2 }), new java.sql.BatchUpdateException(new int[] { 1, 2 }))).isTrue();
		assertThat(this.factory.equals(new java.sql.BatchUpdateException(new int[] { 1, 2 }), new java.sql.BatchUpdateException(new int[] { 1, 3 }))).isFalse();

		assertThat(this.factory.equals(new java.sql.DataTruncation(1, true, true, 1, 1), new java.sql.DataTruncation(1, true, true, 1, 1))).isTrue();
		assertThat(this.factory.equals(new java.sql.DataTruncation(1, true, true, 1, 1), new java.sql.DataTruncation(2, true, true, 1, 1))).isFalse();
		assertThat(this.factory.equals(new java.sql.DataTruncation(1, true, true, 1, 1), new java.sql.DataTruncation(1, false, true, 1, 1))).isFalse();
		assertThat(this.factory.equals(new java.sql.DataTruncation(1, true, true, 1, 1), new java.sql.DataTruncation(1, true, false, 1, 1))).isFalse();
		assertThat(this.factory.equals(new java.sql.DataTruncation(1, true, true, 1, 1), new java.sql.DataTruncation(1, true, true, 2, 1))).isFalse();
		assertThat(this.factory.equals(new java.sql.DataTruncation(1, true, true, 1, 1), new java.sql.DataTruncation(1, true, true, 1, 2))).isFalse();
		
		assertThat(this.factory.equals(new java.sql.SQLClientInfoException(Collections.singletonMap("test", ClientInfoStatus.REASON_UNKNOWN)), new java.sql.SQLClientInfoException(Collections.singletonMap("test", ClientInfoStatus.REASON_UNKNOWN)))).isTrue();
		assertThat(this.factory.equals(new java.sql.SQLClientInfoException(Collections.singletonMap("test", ClientInfoStatus.REASON_UNKNOWN)), new java.sql.SQLClientInfoException(Collections.singletonMap("test", ClientInfoStatus.REASON_UNKNOWN_PROPERTY)))).isFalse();
		
		assertThat(this.factory.equals(new SQLException("reason1", "sql.state1", 1), new SQLException("reason2", "sql.state2", 1))).isTrue();
		assertThat(this.factory.equals(new SQLException("reason", "sql.state", 1), new SQLException("reason", "sql.state", 2))).isFalse();
		assertThat(this.factory.equals(new SQLException("reason", "sql.state", 1), new SQLException("reason", "sql.state", 0))).isFalse();
		
		assertThat(this.factory.equals(new SQLException("reason1", "sql.state", 0), new SQLException("reason2", "sql.state", 0))).isTrue();
		assertThat(this.factory.equals(new SQLException("reason", "sql.state1", 0), new SQLException("reason", "sql.state2", 0))).isFalse();
		assertThat(this.factory.equals(new SQLException("reason", "sql.state1", 0), new SQLException("reason", null, 0))).isFalse();
		
		assertThat(this.factory.equals(new SQLException("reason", null, 0), new SQLException("reason", null, 0))).isTrue();
		assertThat(this.factory.equals(new SQLException("reason1", null, 0), new SQLException("reason2", null, 0))).isFalse();
		
		SQLException exception1 = new SQLException();
		SQLException exception2 = new SQLException();
		
		exception1.setNextException(new SQLException());
		exception2.setNextException(new SQLException());
		
		assertThat(this.factory.equals(exception1, exception2)).isTrue();
		
		exception1.setNextException(new SQLException("reason1"));
		exception2.setNextException(new SQLException("reason2"));
		
		assertThat(this.factory.equals(exception1, exception2)).isFalse();
	}
	
	@Test
	public void correctHeuristic()
	{
		assertThat(this.factory.correctHeuristic(new SQLException(), Phase.COMMIT)).isFalse();
		assertThat(this.factory.correctHeuristic(new SQLException(), Phase.FORGET)).isFalse();
		assertThat(this.factory.correctHeuristic(new SQLException(), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new SQLException(), Phase.ROLLBACK)).isFalse();
	}
	
	@Test
	public void indicatesFailure()
	{
		Dialect dialect = mock(Dialect.class);
		SQLException exception = new SQLException();
		SQLException nextException = new SQLException();
		
		when(dialect.indicatesFailure(exception)).thenReturn(true);
		
		boolean result = this.factory.indicatesFailure(exception, dialect);
		
		assertThat(result).isTrue();

		reset(dialect);
		
		exception.setNextException(nextException);
		
		when(dialect.indicatesFailure(exception)).thenReturn(false);
		when(dialect.indicatesFailure(nextException)).thenReturn(true);
		
		result = this.factory.indicatesFailure(exception, dialect);
		
		assertThat(result).isTrue();
	}
}
