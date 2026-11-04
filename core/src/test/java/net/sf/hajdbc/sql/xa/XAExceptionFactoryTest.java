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
package net.sf.hajdbc.sql.xa;

import javax.transaction.xa.XAException;

import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.ExceptionType;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.durability.Durability.Phase;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author Paul Ferraro
 */
public class XAExceptionFactoryTest
{
	private final ExceptionFactory<XAException> factory = new XAExceptionFactory();
	
	@Test
	public void getTargetClass()
	{
		assertThat(this.factory.getTargetClass()).isSameAs(XAException.class);
	}
	
	@Test
	public void createExceptionFromMessage()
	{
		String message = "message";
		
		XAException result = this.factory.createException(message);
		
		assertThat(result.getMessage()).isSameAs(message);
		assertThat(result.getCause()).isNull();
	}
	
	@Test
	public void createExceptionFromException()
	{
		String message = "message";
		Exception exception = new Exception("message");
		
		XAException result = this.factory.createException(exception);
		
		assertThat(result.getMessage()).isNotNull();
		assertThat(result.getMessage()).isSameAs(message);
		assertThat(result.getCause()).isSameAs(exception);
	}
	
	@Test
	public void createExceptionFromXAException()
	{
		XAException exception = new XAException();
		
		XAException result = this.factory.createException(exception);
		
		assertThat(result).isSameAs(exception);
	}
	
	@Test
	public void getType()
	{
		ExceptionType result = this.factory.getType();
		
		assertThat(result).isSameAs(ExceptionType.XA);
	}
	
	@Test
	public void equals()
	{
		assertThat(this.factory.equals(new XAException(), new XAException())).isTrue();
		
		assertThat(this.factory.equals(new XAException(XAException.XA_HEURCOM), new XAException(XAException.XA_HEURCOM))).isTrue();
		assertThat(this.factory.equals(new XAException(XAException.XA_HEURCOM), new XAException(XAException.XA_HEURHAZ))).isFalse();
		
		assertThat(this.factory.equals(new XAException("reason"), new XAException("reason"))).isTrue();
		assertThat(this.factory.equals(new XAException("reason"), new XAException())).isFalse();
		assertThat(this.factory.equals(new XAException("reason1"), new XAException("reason2"))).isFalse();
	}
	
	@Test
	public void correctHeuristic()
	{
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBBASE), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBCOMMFAIL), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBDEADLOCK), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBEND), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBINTEGRITY), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBOTHER), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBPROTO), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBROLLBACK), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBTIMEOUT), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_RBTRANSIENT), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_INVAL), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_NOTA), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_PROTO), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_RMERR), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_RMFAIL), Phase.PREPARE)).isFalse();
		
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_HEURCOM), Phase.COMMIT)).isTrue();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_HEURHAZ), Phase.COMMIT)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_HEURMIX), Phase.COMMIT)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_HEURRB), Phase.COMMIT)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_INVAL), Phase.COMMIT)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_NOTA), Phase.COMMIT)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_PROTO), Phase.COMMIT)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_RMERR), Phase.COMMIT)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_RMFAIL), Phase.COMMIT)).isFalse();
		
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_HEURCOM), Phase.ROLLBACK)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_HEURHAZ), Phase.ROLLBACK)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_HEURMIX), Phase.ROLLBACK)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XA_HEURRB), Phase.ROLLBACK)).isTrue();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_INVAL), Phase.ROLLBACK)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_NOTA), Phase.ROLLBACK)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_PROTO), Phase.ROLLBACK)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_RMERR), Phase.ROLLBACK)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_RMFAIL), Phase.ROLLBACK)).isFalse();
		
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_INVAL), Phase.FORGET)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_NOTA), Phase.FORGET)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_PROTO), Phase.FORGET)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_RMERR), Phase.FORGET)).isFalse();
		assertThat(this.factory.correctHeuristic(new XAException(XAException.XAER_RMFAIL), Phase.FORGET)).isFalse();
	}
	
	@Test
	public void indicatesFailure()
	{
		Dialect dialect = mock(Dialect.class);
		XAException exception = new XAException();
		
		when(dialect.indicatesFailure(exception)).thenReturn(true);
		
		boolean result = this.factory.indicatesFailure(exception, dialect);
		
		assertThat(result).isTrue();
	}
}
