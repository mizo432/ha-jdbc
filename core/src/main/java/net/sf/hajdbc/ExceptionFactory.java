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

import java.io.Serializable;

import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.durability.Durability;
import org.jspecify.annotations.NonNull;


/**
 * Exception factory interface.
 * @author Paul Ferraro
 * @param <E> exception type
 */
public interface ExceptionFactory<E extends Exception> extends Serializable
{
	/**
	 * Returns the target exception class.
	 * @return exception class
	 */
	@NonNull Class<E> getTargetClass();
	
	/**
	 * Creates an exception from the given throwable.
	 * @param e throwable
	 * @return the created exception
	 */
	@NonNull E createException(@NonNull Throwable e);
	
	/**
	 * Creates an exception from the given message.
	 * @param message message
	 * @return the created exception
	 */
	@NonNull E createException(@NonNull String message);
	
	/**
	 * Compares two exceptions for equality.
	 * @param exception1 first exception
	 * @param exception2 second exception
	 * @return true if equal
	 */
	boolean equals(@NonNull E exception1, @NonNull E exception2);
	
	/**
	 * Determines if the exception indicates failure.
	 * @param exception exception
	 * @param dialect dialect
	 * @return true if it indicates failure
	 */
	boolean indicatesFailure(@NonNull E exception, @NonNull Dialect dialect);
	
	/**
	 * Returns the exception type.
	 * @return exception type
	 */
	@NonNull ExceptionType getType();
	
	/**
	 * Determines if the exception heuristically belongs to the correct phase.
	 * @param exception exception
	 * @param phase durability phase
	 * @return true if correct
	 */
	boolean correctHeuristic(@NonNull E exception, Durability.@NonNull Phase phase);
}
