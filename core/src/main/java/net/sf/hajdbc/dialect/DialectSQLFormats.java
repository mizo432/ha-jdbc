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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.transaction.xa.XAException;

import net.sf.hajdbc.TriggerTime;
import net.sf.hajdbc.util.Strings;

/**
 * Consolidated configuration class that holds all SQL format strings and patterns
 * for a dialect. Subclasses may override individual methods to customize database-specific
 * behavior, eliminating the need for anonymous inner classes.
 *
 * @author Paul Ferraro
 * @since 3.1
 */
public class DialectSQLFormats
{
	/* ------------------------------------------------------------------- *
	 *  SQL:2003 reserved keywords — base set for standard SQL compliance  *
	 * ------------------------------------------------------------------- */

	// Taken from SQL:2003 column of: http://www.postgresql.org/docs/9.1/static/sql-keywords-appendix.html
	// Only includes reserved keywords, since we only care about those keywords that require quoting to be used as an identifier
	protected static final String[] SQL_2003_RESERVED_KEY_WORDS = new String[] {
		"ABS", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS", "ASENSITIVE", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION", "AVG",
		"BEGIN", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOOLEAN", "BOTH", "BY",
		"CALL", "CALLED", "CARDINALITY", "CASCADED", "CASE", "CAST", "CEIL", "CEILING", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOB", "CLOSE", "COALESCE", "COLLATE", "COLLECT", "COLUMN", "COMMIT", "CONDITION", "CONNECT", "CONSTRAINT", "CONVERT", "CORR", "CORRESPONDING", "COUNT", "COVAR_POP", "COVAR_SAMP", "CREATE", "CROSS", "CUBE", "CUME_DIST", "CURRENT", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CYCLE",
		"DATALINK", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELETE", "DENSE_RANK", "DEREF", "DESCRIBE", "DETERMINISTIC", "DISCONNECT", "DISTINCT", "DLNEWCOPY", "DLPREVIOUSCOPY", "DLURLCOMPLETE", "DLURLCOMPLETEONLY", "DLURLCOMPLETEWRITE", "DLURLPATH", "DLURLPATHONLY", "DLURLPATHWRITE", "DLURLSCHEME", "DLURLSERVER", "DLVALUE", "DOUBLE", "DROP", "DYNAMIC",
		"EACH", "ELEMENT", "ELSE", "END", "END-EXEC", "ESCAPE", "EVERY", "EXCEPT", "EXEC", "EXECUTE", "EXISTS", "EXP", "EXTERNAL", "EXTRACT",
		"FALSE", "FETCH", "FILTER", "FLOAT", "FLOOR", "FOR", "FOREIGN", "FREE", "FROM", "FULL", "FUNCTION", "FUSION",
		"GET", "GLOBAL", "GRANT", "GROUP", "GROUPING",
		"HAVING", "HOLD", "HOUR",
		"IDENTITY", "IMPORT", "IN", "INDICATOR", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERSECTION", "INTERVAL", "INTO", "IS",
		"JOIN",
		"LANGUAGE", "LARGE", "LATERAL", "LEADING", "LEFT", "LIKE", "LN", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOWER",
		"MATCH", "MAX", "MEMBER", "MERGE", "METHOD", "MIN", "MINUTE", "MOD", "MODIFIES", "MODULE", "MONTH", "MULTISET",
		"NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NO", "NONE", "NORMALIZE", "NOT", "NULL", "NULLIF", "NUMERIC",
		"OCTET_LENGTH", "OF", "OLD", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUT", "OUTER", "OVER", "OVERLAPS", "OVERLAY",
		"PARAMETER", "PARTITION", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERCENT_RANK", "POSITION", "POWER", "PRECISION", "PREPARE", "PRIMARY", "PROCEDURE",
		"RANGE", "RANK", "READS", "REAL", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "RELEASE", "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "ROW_NUMBER",
		"SAVEPOINT", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SELECT", "SENSITIVE", "SESSION_USER", "SET", "SIMILAR", "SMALLINT", "SOME", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQRT", "START", "STATIC", "STDDEV_POP", "STDDEV_SAMP", "SUBMULTISET", "SUBSTRING", "SUM", "SYMMETRIC", "SYSTEM", "SYSTEM_USER",
		"TABLE", "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSLATE", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE",
		"UESCAPE", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE", "UPPER", "USER", "USING",
		"VALUE", "VALUES", "VARCHAR", "VARYING", "VAR_POP", "VAR_SAMP",
		"WHEN", "WHENEVER", "WHERE", "WIDTH_BUCKET", "WINDOW", "WITH", "WITHIN", "WITHOUT",
		"XML", "XMLAGG", "XMLATTRIBUTES", "XMLBINARY", "XMLCOMMENT", "XMLCONCAT", "XMLELEMENT", "XMLFOREST", "XMLNAMESPACES", "XMLPARSE", "XMLPI", "XMLROOT", "XMLSERIALIZE",
		"YEAR",
	};

	/* ------------------------------------------------------------------- *
	 *  URL patterns                                                       *
	 * ------------------------------------------------------------------- */

	/**
	 * JDBC URL vendor pattern (e.g., "postgresql", "mysql").
	 */
	protected String vendorPattern()
	{
		return "[^\\:]+";
	}

	/**
	 * JDBC URL locator pattern for extracting host, port, and database name.
	 */
	protected String locatorPattern()
	{
		return "(?://(?<host>[^\\:/]+)(?:\\:(?<port>\\d+))?/)?(?<database>[^\\?]+)";
	}

	/* ------------------------------------------------------------------- *
	 *  SQL parsing patterns                                               *
	 * ------------------------------------------------------------------- */

	/**
	 * Pattern to detect SELECT ... FOR UPDATE statements.
	 */
	protected String selectForUpdatePattern()
	{
		return "SELECT\\s+.+\\s+FOR\\s+UPDATE";
	}

	/**
	 * Pattern to extract table name from INSERT statements.
	 */
	protected String insertIntoTablePattern()
	{
		return "INSERT\\s+(?:INTO\\s+)?'?([^'\\s\\(]+)";
	}

	/**
	 * Pattern to extract sequence name from SQL.
	 */
	protected String sequencePattern()
	{
		return "NEXT\\s+VALUE\\s+FOR\\s+'?([^',\\s\\(\\)]+)";
	}

	/* ------------------------------------------------------------------- *
	 *  Temporal function patterns                                         *
	 * ------------------------------------------------------------------- */

	/**
	 * Pattern to match CURRENT_DATE in SQL.
	 */
	protected String currentDatePattern()
	{
		return "(?<=\\W)CURRENT_DATE(?=\\W)";
	}

	/**
	 * Pattern to match CURRENT_TIME and LOCALTIME in SQL.
	 */
	protected String currentTimePattern()
	{
		return "(?<=\\W)CURRENT_TIME(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)|(?<=\\W)LOCALTIME(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)";
	}

	/**
	 * Pattern to match CURRENT_TIMESTAMP and LOCALTIMESTAMP in SQL.
	 */
	protected String currentTimestampPattern()
	{
		return "(?<=\\W)CURRENT_TIMESTAMP(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)|(?<=\\W)LOCALTIMESTAMP(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)";
	}

	/**
	 * Pattern to match RAND() function in SQL.
	 */
	protected String randomPattern()
	{
		return "(?<=\\W)RAND\\s*\\(\\s*\\)";
	}

	/**
	 * Format string for date literals.
	 */
	protected String dateLiteralFormat()
	{
		return "DATE ''{0}''";
	}

	/**
	 * Format string for time literals.
	 */
	protected String timeLiteralFormat()
	{
		return "TIME ''{0}''";
	}

	/**
	 * Format string for timestamp literals.
	 */
	protected String timestampLiteralFormat()
	{
		return "TIMESTAMP ''{0}''";
	}

	/* ------------------------------------------------------------------- *
	 *  Function execution                                                 *
	 * ------------------------------------------------------------------- */

	/**
	 * Dummy table name for function execution queries (e.g., "DUAL" for Oracle).
	 */
	protected String dummyTable()
	{
		return null;
	}

	/**
	 * Format string for executing a function.
	 */
	protected String executeFunctionFormat()
	{
		StringBuilder builder = new StringBuilder("SELECT {0}");

		String dummyTable = this.dummyTable();

		if (dummyTable != null)
		{
			builder.append(" FROM ").append(dummyTable);
		}

		return builder.toString();
	}

	/**
	 * Build the full SQL to execute a function.
	 */
	protected String executeFunctionSQL(String function)
	{
		return java.text.MessageFormat.format(this.executeFunctionFormat(), function);
	}

	/* ------------------------------------------------------------------- *
	 *  Truncate table                                                     *
	 * ------------------------------------------------------------------- */

	/**
	 * Format string for truncating a table. Default uses DELETE FROM.
	 */
	protected String truncateTableFormat()
	{
		return "DELETE FROM {0}";
	}

	/* ------------------------------------------------------------------- *
	 *  Constraint SQL formats                                             *
	 * ------------------------------------------------------------------- */

	/**
	 * Format string for creating a foreign key constraint.
	 */
	protected String createForeignKeyConstraintFormat()
	{
		return "ALTER TABLE {1} ADD CONSTRAINT {0} FOREIGN KEY ({2}) REFERENCES {3} ({4}) ON DELETE {5,choice,0#CASCADE|1#RESTRICT|2#SET NULL|3#NO ACTION|4#SET DEFAULT} ON UPDATE {6,choice,0#CASCADE|1#RESTRICT|2#SET NULL|3#NO ACTION|4#SET DEFAULT} {7,choice,5#DEFERRABLE INITIALLY DEFERRED|6#DEFERRABLE INITIALLY IMMEDIATE|7#NOT DEFERRABLE}";
	}

	/**
	 * Format string for dropping a foreign key constraint.
	 */
	protected String dropForeignKeyConstraintFormat()
	{
		return this.dropConstraintFormat();
	}

	/**
	 * Generic format string for dropping a constraint.
	 */
	protected String dropConstraintFormat()
	{
		return "ALTER TABLE {1} DROP CONSTRAINT {0}";
	}

	/**
	 * Format string for creating a unique constraint.
	 */
	protected String createUniqueConstraintFormat()
	{
		return "ALTER TABLE {1} ADD CONSTRAINT {0} UNIQUE ({2})";
	}

	/**
	 * Format string for dropping a unique constraint.
	 */
	protected String dropUniqueConstraintFormat()
	{
		return this.dropConstraintFormat();
	}

	/* ------------------------------------------------------------------- *
	 *  Trigger SQL formats                                                *
	 * ------------------------------------------------------------------- */

	/**
	 * Format string for creating a trigger.
	 */
	protected String createTriggerFormat()
	{
		return "CREATE TRIGGER {0} {1} {2} ON {3} FOR EACH ROW BEGIN {4} END";
	}

	/**
	 * Format string for dropping a trigger.
	 */
	protected String dropTriggerFormat()
	{
		return "DROP TRIGGER {1} ON {2}";
	}

	/**
	 * Return the row alias for the specified trigger time.
	 */
	protected String triggerRowAlias(TriggerTime time)
	{
		return time.getAlias();
	}

	/* ------------------------------------------------------------------- *
	 *  Schema SQL formats                                                 *
	 * ------------------------------------------------------------------- */

	/**
	 * Format string for creating a schema.
	 */
	protected String createSchemaFormat()
	{
		return "CREATE SCHEMA {0}";
	}

	/**
	 * Format string for dropping a schema.
	 */
	protected String dropSchemaFormat()
	{
		return "DROP SCHEMA {0}";
	}

	/* ------------------------------------------------------------------- *
	 *  Sequence SQL formats                                               *
	 * ------------------------------------------------------------------- */

	/**
	 * Format string for getting the next value from a sequence.
	 */
	protected String nextSequenceValueFormat()
	{
		return "NEXT VALUE FOR {0}";
	}

	/**
	 * Format string for altering a sequence to a specific value.
	 */
	protected String alterSequenceFormat()
	{
		return "ALTER SEQUENCE {0} RESTART WITH {1}";
	}

	/* ------------------------------------------------------------------- *
	 *  Identity column SQL formats                                        *
	 * ------------------------------------------------------------------- */

	/**
	 * Format string for altering an identity column to a specific value.
	 */
	protected String alterIdentityColumnFormat()
	{
		return "ALTER TABLE {0} ALTER COLUMN {1} RESTART WITH {2}";
	}

	/* ------------------------------------------------------------------- *
	 *  Failure detection                                                  *
	 * ------------------------------------------------------------------- */

	/**
	 * Determine whether the specified SQLState indicates a failure.
	 * Default: 08 class SQLStates indicate connection failures.
	 */
	protected boolean indicatesFailureSQLState(String sqlState)
	{
		return sqlState.startsWith("08");
	}

	/**
	 * Determine whether the specified vendor-specific error code indicates a failure.
	 * Default: no vendor-specific codes indicate failure.
	 */
	protected boolean indicatesFailureErrorCode(int code)
	{
		return false;
	}

	/**
	 * Return the set of XA error codes that indicate failure.
	 */
	protected Set<Integer> failureXAErrorCodes()
	{
		return Collections.singleton(XAException.XAER_RMFAIL);
	}

	/* ------------------------------------------------------------------- *
	 *  Identifier patterns                                                *
	 * ------------------------------------------------------------------- */

	/**
	 * Return a regex pattern that matches valid identifiers for this dialect.
	 */
	protected Pattern identifierPattern(DatabaseMetaData metaData) throws SQLException
	{
		return Pattern.compile(java.text.MessageFormat.format("[a-zA-Z][\\w{0}]*", Pattern.quote(metaData.getExtraNameCharacters())));
	}

	/**
	 * Return the set of reserved identifiers for this dialect.
	 * Subclasses may override to provide vendor-specific reserved keywords.
	 */
	protected Set<String> reservedIdentifiers(DatabaseMetaData metaData) throws SQLException
	{
		Set<String> set = new HashSet<>(Arrays.asList(SQL_2003_RESERVED_KEY_WORDS));

		for (String word : metaData.getSQLKeywords().split(Strings.COMMA))
		{
			set.add(word.toUpperCase());
		}

		return set;
	}

	/* ------------------------------------------------------------------- *
	 *  Metadata accessor                                                  *
	 * ------------------------------------------------------------------- */

	/**
	 * Return the schema pattern for metadata queries.
	 */
	protected String schemaPattern(DatabaseMetaData metaData) throws SQLException
	{
		return null;
	}

	/**
	 * Return the table type name for sequences.
	 */
	protected String sequenceTableType()
	{
		return "SEQUENCE";
	}
}
