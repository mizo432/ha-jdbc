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

import org.jspecify.annotations.NonNull;

/**
 * Parses and evaluates SQL statements for database vendor-specific syntax.
 * This interface isolates SQL parsing concerns from other dialect responsibilities,
 * following the Interface Segregation Principle.
 *
 * @author Paul Ferraro
 * @since 3.2
 */
public interface DialectSQLParserContract
{
	/**
	 * Determines if the given SQL is a SELECT ... FOR UPDATE statement.
	 * @param sql SQL statement
	 * @return true if SELECT ... FOR UPDATE, false otherwise
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	boolean isSelectForUpdate(@NonNull String sql) throws SQLException;

	/**
	 * Replaces the non-deterministic CURRENT_DATE function with a deterministic static value.
	 * @param sql SQL statement
	 * @param date the date to replace with
	 * @return an equivalent deterministic SQL statement
	 * @throws SQLException
	 * @since 2.0.2
	 */
	@NonNull String evaluateCurrentDate(@NonNull String sql, java.sql.Date date);

	/**
	 * Replaces the non-deterministic CURRENT_TIME function with a deterministic static value.
	 * @param sql SQL statement
	 * @param time the time to replace with
	 * @return an equivalent deterministic SQL statement
	 * @throws SQLException
	 * @since 2.0.2
	 */
	@NonNull String evaluateCurrentTime(@NonNull String sql, java.sql.Time time);

	/**
	 * Replaces the non-deterministic CURRENT_TIMESTAMP function with a deterministic static value.
	 * @param sql SQL statement
	 * @param timestamp the timestamp to replace with
	 * @return an equivalent deterministic SQL statement
	 * @throws SQLException
	 * @since 2.0.2
	 */
	@NonNull String evaluateCurrentTimestamp(@NonNull String sql, java.sql.Timestamp timestamp);

	/**
	 * Replaces the non-deterministic RAND() function with a deterministic static value.
	 * @param sql SQL statement
	 * @return an equivalent deterministic SQL statement
	 * @throws SQLException
	 * @since 2.0.2
	 */
	@NonNull String evaluateRand(@NonNull String sql);
}

