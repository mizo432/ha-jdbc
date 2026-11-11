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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.xa.XAException;

import net.sf.hajdbc.ColumnProperties;
import net.sf.hajdbc.ColumnPropertiesFactory;
import net.sf.hajdbc.Database;
import net.sf.hajdbc.DumpRestoreSupport;
import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.ForeignKeyConstraintFactory;
import net.sf.hajdbc.IdentifierNormalizer;
import net.sf.hajdbc.IdentityColumnSupport;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.QualifiedNameFactory;
import net.sf.hajdbc.SequenceProperties;
import net.sf.hajdbc.SequencePropertiesFactory;
import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.TriggerEvent;
import net.sf.hajdbc.TriggerSupport;
import net.sf.hajdbc.TriggerTime;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.UniqueConstraintFactory;
import net.sf.hajdbc.codec.Decoder;
import net.sf.hajdbc.util.Strings;

/**
 * @author  Paul Ferraro
 * @since   1.1
 */
public class StandardDialect implements Dialect, SequenceSupport, IdentityColumnSupport, TriggerSupport,
		DialectMetadataAccessor.DialectTemplate
{
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

	private DialectSQLParser sqlParser;
	private DialectTemporalEvaluator temporalEvaluator;
	private DialectMetadataAccessor metadataAccessor;
	private DialectFailureDetector failureDetector;
	private final Pattern urlPattern;

	/**
	 * Default constructor. Subclasses may override template methods to customize
	 * database-specific behavior. Helpers are initialized lazily via {@link #initHelpers()}.
	 */
	protected StandardDialect()
	{
		this.urlPattern = Pattern.compile(String.format("jdbc\\:%s\\:%s", this.vendorPattern(), this.locatorPattern()));
		this.initHelpers();
	}
	
	protected String vendorPattern()
	{
		return "[^\\:]+";
	}

	protected String locatorPattern()
	{
		return "(?://(?<host>[^\\:/]+)(?:\\:(?<port>\\d+))?/)?(?<database>[^\\?]+)";
	}

	protected String selectForUpdatePattern()
	{
		return "SELECT\\s+.+\\s+FOR\\s+UPDATE";
	}

	protected String insertIntoTablePattern()
	{
		return "INSERT\\s+(?:INTO\\s+)?'?([^'\\s\\(]+)";
	}

	protected String sequencePattern()
	{
		return "NEXT\\s+VALUE\\s+FOR\\s+'?([^',\\s\\(\\)]+)";
	}

	protected String currentDatePattern()
	{
		return "(?<=\\W)CURRENT_DATE(?=\\W)";
	}

	protected String currentTimePattern()
	{
		return "(?<=\\W)CURRENT_TIME(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)|(?<=\\W)LOCALTIME(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)";
	}

	protected String currentTimestampPattern()
	{
		return "(?<=\\W)CURRENT_TIMESTAMP(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)|(?<=\\W)LOCALTIMESTAMP(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)";
	}

	protected String randomPattern()
	{
		return "(?<=\\W)RAND\\s*\\(\\s*\\)";
	}

	@SuppressWarnings("unused")
	@Override
	public String schemaPattern(DatabaseMetaData metaData) throws SQLException
	{
		return null;
	}

	/* ------------------------------------------------------------------- *
	 *  Helper initialization — called once after all template methods     *
	 *  are resolved (i.e. in the constructor / after subclass init).      *
	 * ------------------------------------------------------------------- */
	private void initHelpers()
	{
		this.sqlParser = new DialectSQLParser(
				this.selectForUpdatePattern(),
				this.insertIntoTablePattern(),
				this.sequencePattern());
		this.temporalEvaluator = new DialectTemporalEvaluator(
				this.currentDatePattern(),
				this.currentTimePattern(),
				this.currentTimestampPattern(),
				this.randomPattern(),
				this.dateLiteralFormat(),
				this.timeLiteralFormat(),
				this.timestampLiteralFormat());
		this.metadataAccessor = new DialectMetadataAccessor(this);
		this.failureDetector = new DialectFailureDetector(new DialectFailureDetector.FailureDetector()
		{
			@Override
			public boolean indicatesFailureSQLState(String sqlState)
			{
				return StandardDialect.this.indicatesFailureSQLState(sqlState);
			}

			@Override
			public boolean indicatesFailureErrorCode(int code)
			{
				return StandardDialect.this.indicatesFailureErrorCode(code);
			}

			@Override
			public Set<Integer> failureXAErrorCodes()
			{
				return StandardDialect.this.failureXAErrorCodes();
			}
		});
	}

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
	
	protected String executeFunctionSQL(String function)
	{
		return MessageFormat.format(this.executeFunctionFormat(), function);
	}

	protected String dummyTable()
	{
		return null;
	}

	/**
	 * @see net.sf.hajdbc.dialect.Dialect#getTruncateTableSQL(net.sf.hajdbc.TableProperties)
	 */
	@Override
	public String getTruncateTableSQL(TableProperties properties)
	{
		return MessageFormat.format(this.truncateTableFormat(), properties.getName().getDMLName());
	}
	
	protected String truncateTableFormat()
	{
		return "DELETE FROM {0}";
	}

	/**
	 * @see net.sf.hajdbc.dialect.Dialect#getCreateForeignKeyConstraintSQL(net.sf.hajdbc.ForeignKeyConstraint)
	 */
	@Override
	public String getCreateForeignKeyConstraintSQL(ForeignKeyConstraint key)
	{
		return MessageFormat.format(this.createForeignKeyConstraintFormat(), key.getName(), key.getTable().getDDLName(), Strings.join(key.getColumnList(), Strings.PADDED_COMMA), key.getForeignTable().getDDLName(), Strings.join(key.getForeignColumnList(), Strings.PADDED_COMMA), key.getDeleteRule(), key.getUpdateRule(), key.getDeferrability());
	}
	
	protected String createForeignKeyConstraintFormat()
	{
		return "ALTER TABLE {1} ADD CONSTRAINT {0} FOREIGN KEY ({2}) REFERENCES {3} ({4}) ON DELETE {5,choice,0#CASCADE|1#RESTRICT|2#SET NULL|3#NO ACTION|4#SET DEFAULT} ON UPDATE {6,choice,0#CASCADE|1#RESTRICT|2#SET NULL|3#NO ACTION|4#SET DEFAULT} {7,choice,5#DEFERRABLE INITIALLY DEFERRED|6#DEFERRABLE INITIALLY IMMEDIATE|7#NOT DEFERRABLE}";
	}
	
	/**
	 * @see net.sf.hajdbc.dialect.Dialect#getDropForeignKeyConstraintSQL(net.sf.hajdbc.ForeignKeyConstraint)
	 */
	@Override
	public String getDropForeignKeyConstraintSQL(ForeignKeyConstraint key)
	{
		return MessageFormat.format(this.dropForeignKeyConstraintFormat(), key.getName(), key.getTable().getDDLName());
	}
	
	protected String dropForeignKeyConstraintFormat()
	{
		return this.dropConstraintFormat();
	}
	
	protected String dropConstraintFormat()
	{
		return "ALTER TABLE {1} DROP CONSTRAINT {0}";
	}

	/**
	 * @see net.sf.hajdbc.dialect.Dialect#getCreateUniqueConstraintSQL(net.sf.hajdbc.UniqueConstraint)
	 */
	@Override
	public String getCreateUniqueConstraintSQL(UniqueConstraint constraint)
	{
		return MessageFormat.format(this.createUniqueConstraintFormat(), constraint.getName(), constraint.getTable().getDDLName(), Strings.join(constraint.getColumnList(), Strings.PADDED_COMMA));
	}
	
	protected String createUniqueConstraintFormat()
	{
		return "ALTER TABLE {1} ADD CONSTRAINT {0} UNIQUE ({2})";
	}

	/**
	 * @see net.sf.hajdbc.dialect.Dialect#getDropUniqueConstraintSQL(net.sf.hajdbc.UniqueConstraint)
	 */
	@Override
	public String getDropUniqueConstraintSQL(UniqueConstraint constraint)
	{
		return MessageFormat.format(this.dropUniqueConstraintFormat(), constraint.getName(), constraint.getTable().getDDLName());
	}
	
	protected String dropUniqueConstraintFormat()
	{
		return this.dropConstraintFormat();
	}

	/**
	 * @see net.sf.hajdbc.dialect.Dialect#isSelectForUpdate(java.lang.String)
	 */
	@Override
	public boolean isSelectForUpdate(String sql)
	{
		return this.sqlParser.isSelectForUpdate(sql);
	}

	/**
	 * @see net.sf.hajdbc.dialect.Dialect#getDefaultSchemas(java.sql.DatabaseMetaData)
	 */
	@Override
	public List<String> getDefaultSchemas(DatabaseMetaData metaData) throws SQLException
	{
		return Collections.singletonList(metaData.getUserName());
	}


	protected String executeFunction(Connection connection, String function) throws SQLException
	{
		try (java.sql.Statement statement = connection.createStatement())
		{
			try (java.sql.ResultSet resultSet = statement.executeQuery(this.executeFunctionSQL(function)))
			{
				resultSet.next();

				return resultSet.getString(1);
			}
		}
	}

	protected String parse(Pattern pattern, String string)
	{
		Matcher matcher = pattern.matcher(string);

		return matcher.find() ? matcher.group(1) : null;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#getSequenceSupport()
	 */
	@Override
	public SequenceSupport getSequenceSupport()
	{
		return null;
	}

	/**
	 * @see net.sf.hajdbc.SequenceSupport#parseSequence(java.lang.String)
	 */
	@Override
	public String parseSequence(String sql)
	{
		return this.sqlParser.parseSequence(sql);
	}

	/**
	 * @see net.sf.hajdbc.dialect.Dialect#getColumnType(net.sf.hajdbc.ColumnProperties)
	 */
	@Override
	public int getColumnType(ColumnProperties properties)
	{
		return properties.getType();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.SequenceSupport#getSequences(java.sql.DatabaseMetaData, net.sf.hajdbc.SequencePropertiesFactory)
	 */
	@Override
	public Collection<SequenceProperties> getSequences(DatabaseMetaData metaData, SequencePropertiesFactory factory) throws SQLException
	{
		return this.metadataAccessor.getSequences(metaData, factory);
	}

	@Override
	public String sequenceTableType()
	{
		return "SEQUENCE";
	}

	/**
	 * @see net.sf.hajdbc.SequenceSupport#getNextSequenceValueSQL(net.sf.hajdbc.SequenceProperties)
	 */
	@Override
	public String getNextSequenceValueSQL(SequenceProperties sequence)
	{
		return this.executeFunctionSQL(MessageFormat.format(this.nextSequenceValueFormat(), sequence.getName().getDMLName()));
	}
	
	protected String nextSequenceValueFormat()
	{
		return "NEXT VALUE FOR {0}";
	}
	
	/**
	 * @see net.sf.hajdbc.SequenceSupport#getAlterSequenceSQL(net.sf.hajdbc.SequenceProperties, long)
	 */
	@Override
	public String getAlterSequenceSQL(SequenceProperties sequence, long value)
	{
		return MessageFormat.format(this.alterSequenceFormat(), sequence.getName().getDDLName(), String.valueOf(value), String.valueOf(sequence.getIncrement()));
	}
	
	protected String alterSequenceFormat()
	{
		return "ALTER SEQUENCE {0} RESTART WITH {1}";
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#getIdentityColumnSupport()
	 */
	@Override
	public IdentityColumnSupport getIdentityColumnSupport()
	{
		return null;
	}
	
	@Override
	public String parseInsertTable(String sql)
	{
		return this.sqlParser.parseInsertTable(sql);
	}

	@Override
	public String getAlterIdentityColumnSQL(TableProperties table, ColumnProperties column, long value)
	{
		return MessageFormat.format(this.alterIdentityColumnFormat(), table.getName().getDDLName(), column.getName(), String.valueOf(value));
	}

	protected String alterIdentityColumnFormat()
	{
		return "ALTER TABLE {0} ALTER COLUMN {1} RESTART WITH {2}";
	}


	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#evaluateCurrentDate(java.lang.String, java.sql.Date)
	 */
	@Override
	public String evaluateCurrentDate(String sql, java.sql.Date date)
	{
		return this.temporalEvaluator.evaluateCurrentDate(sql, date);
	}
	
	protected String dateLiteralFormat()
	{
		return "DATE ''{0}''";
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#evaluateCurrentTime(java.lang.String, java.sql.Time)
	 */
	@Override
	public String evaluateCurrentTime(String sql, java.sql.Time time)
	{
		return this.temporalEvaluator.evaluateCurrentTime(sql, time);
	}
	
	protected String timeLiteralFormat()
	{
		return "TIME ''{0}''";
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#evaluateCurrentTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	@Override
	public String evaluateCurrentTimestamp(String sql, java.sql.Timestamp timestamp)
	{
		return this.temporalEvaluator.evaluateCurrentTimestamp(sql, timestamp);
	}
	
	protected String timestampLiteralFormat()
	{
		return "TIMESTAMP ''{0}''";
	}


	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#evaluateRand(java.lang.String)
	 */
	@Override
	public String evaluateRand(String sql)
	{
		return this.temporalEvaluator.evaluateRand(sql);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#indicatesFailure(java.sql.SQLException)
	 */
	@Override
	public boolean indicatesFailure(SQLException e)
	{
		return this.failureDetector.indicatesFailure(e);
	}

	/**
	 * 指定されたSQLStateがデータベースの非活性化をもたらすべきデータベース障害を示しているかどうかを示します。
	 * Subclasses may override for vendor-specific SQLState handling.
	 * @param sqlState SQL:2003またはX/Open SQLState
	 * @return SQLStateが障害を示す場合はtrue、そうでない場合はfalse
	 */
	protected boolean indicatesFailureSQLState(String sqlState)
	{
		// 08 class SQLStates indicate connection failures
		return sqlState.startsWith("08");
	}

	/**
	 * 指定されたベンダー固有のエラーコードがデータベースの非活性化をもたらすべきデータベース障害を示しているかどうかを示します。
	 * Subclasses may override for vendor-specific error code handling.
	 * @param code ベンダー固有のエラーコード
	 * @return エラーコードが障害を示す場合はtrue、そうでない場合はfalse
	 */
	protected boolean indicatesFailureErrorCode(int code)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#indicatesFailure(javax.transaction.xa.XAException)
	 */
	@Override
	public boolean indicatesFailure(XAException e)
	{
		return this.failureDetector.indicatesFailure(e);
	}

	protected Set<Integer> failureXAErrorCodes()
	{
		return Collections.singleton(XAException.XAER_RMFAIL);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#getDumpRestoreSupport()
	 */
	@Override
	public DumpRestoreSupport getDumpRestoreSupport()
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#getTriggerSupport()
	 */
	@Override
	public TriggerSupport getTriggerSupport()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.TriggerSupport#getCreateTriggerSQL(java.lang.String, net.sf.hajdbc.TableProperties, net.sf.hajdbc.TriggerEvent, java.lang.String)
	 */
	@Override
	public String getCreateTriggerSQL(String name, TableProperties table, TriggerEvent event, String action)
	{
		return MessageFormat.format(this.createTriggerFormat(), name, event.getTime().toString(), event.toString(), table.getName().getDDLName(), action);
	}

	protected String createTriggerFormat()
	{
		return "CREATE TRIGGER {0} {1} {2} ON {3} FOR EACH ROW BEGIN {4} END";
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.TriggerSupport#getDropTriggerSQL(String, net.sf.hajdbc.TableProperties)
	 */
	@Override
	public String getDropTriggerSQL(String name, TableProperties table)
	{
		return MessageFormat.format(this.dropTriggerFormat(), name, table.getName().getDDLName());
	}

	protected String dropTriggerFormat()
	{
		return "DROP TRIGGER {1} ON {2}";
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.TriggerSupport#getTriggerRowAlias(net.sf.hajdbc.TriggerTime)
	 */
	@Override
	public String getTriggerRowAlias(TriggerTime time)
	{
		return time.getAlias();
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#getCreateSchemaSQL(java.lang.String)
	 */
	@Override
	public String getCreateSchemaSQL(String schema)
	{
		return MessageFormat.format(this.createSchemaFormat(), schema);
	}

	protected String createSchemaFormat()
	{
		return "CREATE SCHEMA {0}";
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#getDropSchemaSQL(java.lang.String)
	 */
	@Override
	public String getDropSchemaSQL(String schema)
	{
		return MessageFormat.format(this.dropSchemaFormat(), schema);
	}

	protected String dropSchemaFormat()
	{
		return "DROP SCHEMA {0}";
	}

	protected boolean meetsRequirement(int minMajor, int minMinor)
	{
		Driver driver = this.findDriver();

		if (driver != null)
		{
			int major = driver.getMajorVersion();
			int minor = driver.getMinorVersion();
			return (major > minMajor) || ((major == minMajor) && (minor >= minMinor));
		}
		
		return false;
	}
	
	private Driver findDriver()
	{
		String url = String.format("jdbc:%s:test", this.vendorPattern());
		
		List<Driver> drivers = Collections.list(DriverManager.getDrivers());
		for (Driver driver: drivers)
		{
			try
			{
				if (driver.acceptsURL(url))
				{
					return driver;
				}
			}
			catch (SQLException e)
			{
				// Skip
			}
		}
		return null;
	}
	
	/**
	 * このデータベースのすべてのテーブルをスキーマ別にマッピングして返します。
	 * @param metaData DatabaseMetaDataの実装
	 * @return スキーマ名からテーブル名のコレクションへのマップ
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	@Override
	public Collection<QualifiedName> getTables(DatabaseMetaData metaData, QualifiedNameFactory factory) throws SQLException
	{
		return this.metadataAccessor.getTables(metaData, factory);
	}

	/**
	 * 指定されたテーブルの列を返します。
	 * @param metaData DatabaseMetaDataの実装
	 * @param table スキーマ修飾されたテーブル名
	 * @return 列名から列プロパティへのマップ
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	@Override
	public Map<String, ColumnProperties> getColumns(DatabaseMetaData metaData, QualifiedName table, ColumnPropertiesFactory factory) throws SQLException
	{
		return this.metadataAccessor.getColumns(metaData, table, factory);
	}

	/**
	 * 指定されたテーブルの主キーを返します。
	 * @param metaData DatabaseMetaDataの実装
	 * @param table スキーマ修飾されたテーブル名
	 * @return 一意制約
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	@Override
	public UniqueConstraint getPrimaryKey(DatabaseMetaData metaData, QualifiedName table, UniqueConstraintFactory factory) throws SQLException
	{
		return this.metadataAccessor.getPrimaryKey(metaData, table, factory);
	}

	/**
	 * 指定されたテーブルの外部キー制約を返します。
	 * @param metaData DatabaseMetaDataの実装
	 * @param table スキーマ修飾されたテーブル名
	 * @return 外部キー制約のコレクション
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	@Override
	public Collection<ForeignKeyConstraint> getForeignKeyConstraints(DatabaseMetaData metaData, QualifiedName table, ForeignKeyConstraintFactory factory) throws SQLException
	{
		return this.metadataAccessor.getForeignKeyConstraints(metaData, table, factory);
	}

	/**
	 * 指定されたテーブルの一意制約を返します - テーブルの主キーは除外されます。
	 * @param metaData DatabaseMetaDataの実装
	 * @param table 修飾されたテーブル名
	 * @param primaryKey このテーブルの主キー
	 * @return 一意制約のコレクション
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	@Override
	public Collection<UniqueConstraint> getUniqueConstraints(DatabaseMetaData metaData, QualifiedName table, UniqueConstraint primaryKey, UniqueConstraintFactory factory) throws SQLException
	{
		return this.metadataAccessor.getUniqueConstraints(metaData, table, primaryKey, factory);
	}

	/**
	 * 指定された列のコレクションからID列を特定します
	 * @param columns テーブルの列
	 * @return 列名のコレクション
	 * @throws SQLException
	 */
	@Override
	public Collection<String> getIdentityColumns(Collection<ColumnProperties> columns) throws SQLException
	{
		return this.metadataAccessor.getIdentityColumns(columns);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.Dialect#getTypes(java.sql.DatabaseMetaData)
	 */
	@Override
	public Map<Integer, Entry<String, Integer>> getTypes(DatabaseMetaData metaData) throws SQLException
	{
		return this.metadataAccessor.getTypes(metaData);
	}

	@Override
	public IdentifierNormalizer createIdentifierNormalizer(DatabaseMetaData metaData) throws SQLException
	{
		return new StandardIdentifierNormalizer(metaData, this.identifierPattern(metaData), this.reservedIdentifiers(metaData));
	}

	protected Pattern identifierPattern(DatabaseMetaData metaData) throws SQLException
	{
		return Pattern.compile(MessageFormat.format("[a-zA-Z][\\w{0}]*", Pattern.quote(metaData.getExtraNameCharacters())));
	}
	
	protected Set<String> reservedIdentifiers(DatabaseMetaData metaData) throws SQLException
	{
		Set<String> set = new HashSet<>(Arrays.asList(SQL_2003_RESERVED_KEY_WORDS));
		
		for (String word: metaData.getSQLKeywords().split(Strings.COMMA))
		{
			set.add(word.toUpperCase());
		}
		
		return set;
	}

	@Override
	public QualifiedNameFactory createQualifiedNameFactory(DatabaseMetaData metaData, IdentifierNormalizer normalizer) throws SQLException
	{
		return new StandardQualifiedNameFactory(metaData, normalizer);
	}

	@Override
	public ColumnPropertiesFactory createColumnPropertiesFactory(IdentifierNormalizer normalizer)
	{
		return new StandardColumnPropertiesFactory(normalizer);
	}

	@Override
	public SequencePropertiesFactory createSequencePropertiesFactory(QualifiedNameFactory factory)
	{
		return new StandardSequencePropertiesFactory(factory);
	}

	@Override
	public ForeignKeyConstraintFactory createForeignKeyConstraintFactory(QualifiedNameFactory factory)
	{
		return new StandardForeignKeyConstraintFactory(factory);
	}

	@Override
	public UniqueConstraintFactory createUniqueConstraintFactory(IdentifierNormalizer normalizer)
	{
		return new StandardUniqueConstraintFactory(normalizer);
	}

	@Override
	public boolean isValid(Connection connection) throws SQLException
	{
		return connection.isValid(0);
	}

	@Override
	public <Z, D extends Database<Z>> ConnectionProperties getConnectionProperties(D database, Decoder decoder) throws SQLException
	{
		char[] passwordChars = null;
		try
		{
			passwordChars = database.getCredentials().decodePassword(decoder);
			final String password = new String(passwordChars);
			try (Connection connection = database.connect(decoder))
			{
				DatabaseMetaData metaData = connection.getMetaData();
				String url = metaData.getURL();
				
				if (url == null)
				{
					throw new UnsupportedOperationException();
				}
					
				Matcher matcher = this.urlPattern.matcher(url);
				
				if (!matcher.find())
				{
					throw new UnsupportedOperationException(url);
				}
				
				final String host = matcher.group("host");
				final String port = matcher.group("port");
				final String databaseName = matcher.group("database");
				final String user = metaData.getUserName();
				
				return new ConnectionProperties()
				{
					@Override
					public String getHost()
					{
						return host;
					}

					@Override
					public String getPort()
					{
						return port;
					}

					@Override
					public String getDatabase()
					{
						return databaseName;
					}

					@Override
					public String getUser()
					{
						return user;
					}

					@Override
					public String getPassword()
					{
						return password;
					}
				};
			}
		}
		finally
		{
			if (passwordChars != null)
			{
				Arrays.fill(passwordChars, (char) 0);
			}
		}
	}
}
