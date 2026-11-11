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

import net.sf.hajdbc.durability.Durability.Phase;

import org.jspecify.annotations.NonNull;


/**
 * Abstract base class for exception factories.
 * @author paul
 * @param <E> exception type
 */
public abstract class AbstractExceptionFactory<E extends Exception> implements ExceptionFactory<E>
{
	private static final long serialVersionUID = 6715631233424287636L;

	private @NonNull Class<E> targetClass;
	
	/**
	 * Constructs with the target exception class.
	 * @param targetClass target exception class
	 */
	protected AbstractExceptionFactory(@NonNull Class<E> targetClass)
	{
		this.targetClass = targetClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NonNull Class<E> getTargetClass()
	{
		return this.targetClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NonNull ExceptionType getType()
	{
		return ExceptionType.valueOf(this.targetClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Safe to cast after isInstance check")
	public @NonNull E createException(@NonNull Throwable e)
	{
		if (e == null)
		{
			return this.createException("Unknown error");
		}
		
		if (this.targetClass.isInstance(e))
		{
			return this.targetClass.cast(e);
		}
		
		E exception = this.createException(e.getMessage());
		
		exception.initCause(e);
		
		return exception;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean correctHeuristic(@NonNull E exception, @NonNull Phase phase)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(@NonNull E exception1, @NonNull E exception2)
	{
		String message1 = exception1.getMessage();
		String message2 = exception2.getMessage();
		
		if (message1 == null) return message2 == null;
		return message1.equals(message2);
	}
}
