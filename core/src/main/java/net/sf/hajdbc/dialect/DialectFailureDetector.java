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

import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;
import java.sql.SQLException;
import java.util.Set;

import javax.transaction.xa.XAException;

/**
 * Helper that determines whether a SQL exception indicates a database failure
 * that should trigger database deactivation.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectFailureDetector
{
	private final FailureDetector detector;

	DialectFailureDetector(FailureDetector detector)
	{
		this.detector = detector;
	}

	/**
	 * Determine whether the specified exception indicates a failure.
	 */
	boolean indicatesFailure(SQLException e)
	{
		if ((e instanceof SQLNonTransientConnectionException) || (e instanceof SQLTransientConnectionException))
		{
			return true;
		}

		String sqlState = e.getSQLState();
		if ((sqlState != null) && indicatesFailure(sqlState))
		{
			return true;
		}

		return indicatesFailure(e.getErrorCode());
	}

	/**
	 * Determine whether the specified SQLState indicates a failure.
	 * Default: 08 class SQLStates indicate connection failures.
	 */
	boolean indicatesFailure(String sqlState)
	{
		return detector != null ? detector.indicatesFailureSQLState(sqlState) : sqlState.startsWith("08");
	}

	/**
	 * Determine whether the specified vendor-specific error code indicates a failure.
	 * Default: no vendor-specific codes indicate failure.
	 */
	boolean indicatesFailure(int code)
	{
		return detector != null ? detector.indicatesFailureErrorCode(code) : false;
	}

	/**
	 * Determine whether the specified XA exception indicates a failure.
	 */
	boolean indicatesFailure(XAException e)
	{
		Set<Integer> failureCodes = detector != null ? detector.failureXAErrorCodes() : null;
		return failureCodes != null && failureCodes.contains(e.errorCode);
	}

	/**
	 * Optional custom failure detector for vendor-specific logic.
	 */
	interface FailureDetector
	{
		boolean indicatesFailureSQLState(String sqlState);

		boolean indicatesFailureErrorCode(int code);

		Set<Integer> failureXAErrorCodes();
	}
}
