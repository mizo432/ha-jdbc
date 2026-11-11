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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper that parses SQL statements to extract table names, sequence names,
 * and detect SELECT ... FOR UPDATE clauses.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectSQLParser
{
	private final Pattern selectForUpdatePattern;
	private final Pattern insertIntoTablePattern;
	private final Pattern sequencePattern;

	DialectSQLParser(String selectForUpdatePattern, String insertIntoTablePattern, String sequencePattern)
	{
		this.selectForUpdatePattern = Pattern.compile(selectForUpdatePattern, Pattern.CASE_INSENSITIVE);
		this.insertIntoTablePattern = Pattern.compile(insertIntoTablePattern, Pattern.CASE_INSENSITIVE);
		this.sequencePattern = Pattern.compile(sequencePattern, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Determine whether the specified SQL is a SELECT ... FOR UPDATE statement.
	 */
	boolean isSelectForUpdate(String sql)
	{
		return this.selectForUpdatePattern.matcher(sql).find();
	}

	/**
	 * Parse the table name from an INSERT statement that may contain an identity column.
	 */
	String parseInsertTable(String sql)
	{
		return parse(this.insertIntoTablePattern, sql);
	}

	/**
	 * Parse the sequence name from SQL that references a sequence.
	 */
	String parseSequence(String sql)
	{
		return parse(this.sequencePattern, sql);
	}

	private static String parse(Pattern pattern, String string)
	{
		Matcher matcher = pattern.matcher(string);

		return matcher.find() ? matcher.group(1) : null;
	}
}
