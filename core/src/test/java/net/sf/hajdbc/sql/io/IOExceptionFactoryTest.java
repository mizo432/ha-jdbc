package net.sf.hajdbc.sql.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import net.sf.hajdbc.ExceptionFactory;
import net.sf.hajdbc.ExceptionType;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.durability.Durability.Phase;

import org.junit.Test;
import org.mockito.Mockito;

public class IOExceptionFactoryTest
{
	private final ExceptionFactory<IOException> factory = new IOExceptionFactory();
	
	@Test
	public void getTargetClass()
	{
		assertThat(this.factory.getTargetClass()).isSameAs(IOException.class);
	}
	
	@Test
	public void createExceptionFromMessage()
	{
		String message = "message";
		
		IOException result = this.factory.createException(message);
		
		assertThat(result.getMessage()).isSameAs(message);
		assertThat(result.getCause()).isNull();
	}
	
	@Test
	public void createExceptionFromException()
	{
		String message = "message";
		Exception exception = new Exception(message);
		
		IOException result = this.factory.createException(exception);
		
		assertThat(result.getMessage()).isNotNull();
		assertThat(result.getMessage()).isSameAs(exception.getMessage());
		assertThat(result.getCause()).isSameAs(exception);
	}
	
	@Test
	public void createExceptionFromSQLException()
	{
		IOException exception = new IOException();
		
		IOException result = this.factory.createException(exception);
		
		assertThat(result).isSameAs(exception);
	}
	
	@Test
	public void getType()
	{
		ExceptionType result = this.factory.getType();
		
		assertThat(result).isSameAs(ExceptionType.IO);
	}
	
	@Test
	public void equals()
	{
		assertThat(this.factory.equals(new IOException(), new IOException())).isTrue();
		assertThat(this.factory.equals(new IOException("message"), new IOException())).isFalse();
		assertThat(this.factory.equals(new IOException("message"), new IOException("message"))).isTrue();
		assertThat(this.factory.equals(new IOException("message1"), new IOException("message2"))).isFalse();
	}
	
	@Test
	public void correctHeuristic()
	{
		assertThat(this.factory.correctHeuristic(new IOException(), Phase.COMMIT)).isFalse();
		assertThat(this.factory.correctHeuristic(new IOException(), Phase.FORGET)).isFalse();
		assertThat(this.factory.correctHeuristic(new IOException(), Phase.PREPARE)).isFalse();
		assertThat(this.factory.correctHeuristic(new IOException(), Phase.ROLLBACK)).isFalse();
	}
	
	@Test
	public void indicatesFailure()
	{
		Dialect dialect = mock(Dialect.class);
		
		assertThat(this.factory.indicatesFailure(new IOException(), dialect)).isFalse();

		Mockito.verifyNoMoreInteractions(dialect);
	}
}
