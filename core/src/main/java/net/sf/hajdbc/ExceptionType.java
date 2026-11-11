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

import net.sf.hajdbc.sql.SQLExceptionFactory;
import net.sf.hajdbc.sql.io.IOExceptionFactory;
import net.sf.hajdbc.sql.xa.XAExceptionFactory;
import net.sf.hajdbc.util.Matcher;
import org.jspecify.annotations.NonNull;

/**
 * Exception type enumeration.
 * @author Paul Ferraro
 */
@SuppressWarnings("rawtypes")
public enum ExceptionType implements Matcher<ExceptionFactory>
{
	/**
	 * SQL exception
	 */
	SQL(new SQLExceptionFactory()),
	/**
	 * XA exception
	 */
	XA(new XAExceptionFactory()),
	/**
	 * IO exception
	 */
	IO(new IOExceptionFactory())
	;
	private final @NonNull ExceptionFactory<? extends Exception> factory;
	
	/**
	 * Constructs with the given exception factory.
	 * @param factory exception factory
	 */
	private ExceptionType(@NonNull ExceptionFactory<? extends Exception> factory)
	{
		this.factory = factory;
	}
	
	/**
	 * Determines if the given factory matches this type.
	 * @param factory exception factory
	 * @return true if it matches
	 */
	@Override
	public boolean matches(@NonNull ExceptionFactory factory)
	{
		return this == factory.getType();
	}

	/**
	 * Returns the exception factory.
	 * @param <E> exception type
	 * @return exception factory
	 */
	@SuppressWarnings("unchecked")
	public <E extends Exception> @NonNull ExceptionFactory<E> getExceptionFactory()
	{
		return (ExceptionFactory<E>) this.factory;
	}
	
	/**
	 * Returns the type corresponding to the given exception class.
	 * @param exceptionClass exception class
	 * @return exception type
	 * @throws IllegalArgumentException if no matching type is found
	 */
	public static @NonNull ExceptionType valueOf(@NonNull Class<?> exceptionClass)
	{
		for (ExceptionType type: ExceptionType.values())
		{
			if (type.factory.getTargetClass().equals(exceptionClass))
			{
				return type;
			}
		}
		
		throw new IllegalArgumentException(exceptionClass.getName());
	}
}