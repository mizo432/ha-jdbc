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

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper that evaluates non-deterministic SQL temporal functions, replacing them
 * with deterministic static values for synchronization purposes.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectTemporalEvaluator
{
	// Maximum SQL length to prevent ReDoS attacks
	private static final int MAX_SQL_LENGTH = 1024 * 1024; // 1MB

	private final Pattern currentDatePattern;
	private final Pattern currentTimePattern;
	private final Pattern currentTimestampPattern;
	private final Pattern randomPattern;
	private final String dateLiteralFormat;
	private final String timeLiteralFormat;
	private final String timestampLiteralFormat;

	DialectTemporalEvaluator(
			String currentDatePattern,
			String currentTimePattern,
			String currentTimestampPattern,
			String randomPattern,
			String dateLiteralFormat,
			String timeLiteralFormat,
			String timestampLiteralFormat)
	{
		this.currentDatePattern = Pattern.compile(currentDatePattern, Pattern.CASE_INSENSITIVE);
		this.currentTimePattern = Pattern.compile(currentTimePattern, Pattern.CASE_INSENSITIVE);
		this.currentTimestampPattern = Pattern.compile(currentTimestampPattern, Pattern.CASE_INSENSITIVE);
		this.randomPattern = Pattern.compile(randomPattern, Pattern.CASE_INSENSITIVE);
		this.dateLiteralFormat = dateLiteralFormat;
		this.timeLiteralFormat = timeLiteralFormat;
		this.timestampLiteralFormat = timestampLiteralFormat;
	}

	/**
	 * Replace non-deterministic CURRENT_DATE with a deterministic static value.
	 */
	String evaluateCurrentDate(String sql, java.sql.Date date)
	{
		return evaluateTemporal(sql, this.currentDatePattern, date, this.dateLiteralFormat);
	}

	/**
	 * Replace non-deterministic CURRENT_TIME with a deterministic static value.
	 */
	String evaluateCurrentTime(String sql, java.sql.Time time)
	{
		return evaluateTemporal(sql, this.currentTimePattern, time, this.timeLiteralFormat);
	}

	/**
	 * Replace non-deterministic CURRENT_TIMESTAMP with a deterministic static value.
	 */
	String evaluateCurrentTimestamp(String sql, java.sql.Timestamp timestamp)
	{
		return evaluateTemporal(sql, this.currentTimestampPattern, timestamp, this.timestampLiteralFormat);
	}

	/**
	 * Replace non-deterministic RAND() with a deterministic static value.
	 */
	String evaluateRand(String sql)
	{
		// Guard against excessively long SQL strings to prevent ReDoS
		if (sql.length() > MAX_SQL_LENGTH) {
			return sql;
		}
		
		StringBuffer buffer = new StringBuffer();
		Matcher matcher = this.randomPattern.matcher(sql);

		while (matcher.find())
		{
			matcher.appendReplacement(buffer, Double.toString(Math.random()));
		}

		return matcher.appendTail(buffer).toString();
	}

	private static String evaluateTemporal(String sql, Pattern pattern, java.util.Date date, String format)
	{
		// Guard against excessively long SQL strings to prevent ReDoS
		if (sql.length() > MAX_SQL_LENGTH) {
			return sql;
		}
		
		return pattern.matcher(sql).replaceAll(MessageFormat.format(format, date.toString()));
	}
}
