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

import java.sql.SQLException;

import javax.transaction.xa.XAException;

import org.jspecify.annotations.NonNull;

/**
 * Detects fatal database failures from exceptions.
 * This interface isolates failure detection concerns from other dialect responsibilities,
 * following the Interface Segregation Principle.
 *
 * @author Paul Ferraro
 * @since 3.2
 */
public interface DialectFailureContract
{
	/**
	 * Determines if the given SQL exception indicates a fatal error.
	 * @param e the exception
	 * @return true if the exception indicates a fatal error, false otherwise
	 */
	boolean indicatesFailure(@NonNull SQLException e);

	/**
	 * Determines if the given XA exception indicates a fatal error.
	 * @param e the exception
	 * @return true if the exception indicates a fatal error, false otherwise
	 */
	boolean indicatesFailure(@NonNull XAException e);
}

