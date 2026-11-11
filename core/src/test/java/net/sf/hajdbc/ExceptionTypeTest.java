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
package net.sf.hajdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.sql.SQLException;
import javax.transaction.xa.XAException;

import org.junit.jupiter.api.Test;

public class ExceptionTypeTest
{
	@Test
	public void enumValues()
	{
		ExceptionType[] types = ExceptionType.values();
		assertThat(types).hasSize(3);
		assertThat(types).containsExactly(ExceptionType.SQL, ExceptionType.XA, ExceptionType.IO);
	}
	
	@Test
	public void getExceptionFactory()
	{
		assertThat(ExceptionType.SQL.getExceptionFactory().getTargetClass()).isEqualTo(SQLException.class);
		assertThat(ExceptionType.XA.getExceptionFactory().getTargetClass()).isEqualTo(XAException.class);
		assertThat(ExceptionType.IO.getExceptionFactory().getTargetClass()).isEqualTo(IOException.class);
	}
	
	@Test
	public void matches()
	{
		assertThat(ExceptionType.SQL.matches(ExceptionType.SQL.getExceptionFactory())).isTrue();
		assertThat(ExceptionType.XA.matches(ExceptionType.XA.getExceptionFactory())).isTrue();
		assertThat(ExceptionType.IO.matches(ExceptionType.IO.getExceptionFactory())).isTrue();
		
		assertThat(ExceptionType.SQL.matches(ExceptionType.XA.getExceptionFactory())).isFalse();
		assertThat(ExceptionType.XA.matches(ExceptionType.SQL.getExceptionFactory())).isFalse();
	}
	
	@Test
	public void valueOfWithClass()
	{
		assertThat(ExceptionType.valueOf(SQLException.class)).isEqualTo(ExceptionType.SQL);
		assertThat(ExceptionType.valueOf(XAException.class)).isEqualTo(ExceptionType.XA);
		assertThat(ExceptionType.valueOf(IOException.class)).isEqualTo(ExceptionType.IO);
	}
	
	@Test
	public void valueOfWithUnknownClass()
	{
		assertThatThrownBy(() -> ExceptionType.valueOf(RuntimeException.class))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("RuntimeException");
	}
}
