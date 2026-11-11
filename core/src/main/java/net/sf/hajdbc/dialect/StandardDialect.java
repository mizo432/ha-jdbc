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
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.hajdbc.util.Strings;

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

/**
 * Base dialect implementation that delegates to specialized helper classes.
 * Subclasses may override template methods to customize database-specific behavior.
 *
 * @author  Paul Ferraro
 * @since   1.1
 */
public class StandardDialect implements Dialect, SequenceSupport, IdentityColumnSupport, TriggerSupport,
		DialectMetadataAccessor.DialectTemplate
{
	private DialectSQLParser sqlParser;
	private DialectTemporalEvaluator temporalEvaluator;
	private DialectMetadataAccessor metadataAccessor;
	private DialectFailureDetector failureDetector;
	private DialectConstraintSQLGenerator constraintSQLGenerator;
	private DialectTriggerSQLGenerator triggerSQLGenerator;
	private DialectSchemaSQLGenerator schemaSQLGenerator;
	private DialectSequenceSQLGenerator sequenceSQLGenerator;
	private DialectIdentityColumnSQLGenerator identityColumnSQLGenerator;
	private DialectTruncateSQLGenerator truncateSQLGenerator;
	private DialectFactoryProvider factoryProvider;
	private DialectConnectionResolver connectionResolver;

	/**
	 * Default constructor. Subclasses may override template methods to customize
	 * database-specific behavior. Helpers are initialized lazily via {@link #initHelpers()}.
	 */
	protected StandardDialect()
	{
		this.initHelpers();
	}

	/* ------------------------------------------------------------------- *
	 *  Template method hooks — subclasses override these to customize     *
	 *  database-specific behavior.                                        *
	 * ------------------------------------------------------------------- */

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

	protected String dateLiteralFormat()
	{
		return "DATE ''{0}''";
	}

	protected String timeLiteralFormat()
	{
		return "TIME ''{0}''";
	}

	protected String timestampLiteralFormat()
	{
		return "TIMESTAMP ''{0}''";
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

	protected String truncateTableFormat()
	{
		return "DELETE FROM {0}";
	}

	protected String createForeignKeyConstraintFormat()
	{
		return "ALTER TABLE {1} ADD CONSTRAINT {0} FOREIGN KEY ({2}) REFERENCES {3} ({4}) ON DELETE {5,choice,0#CASCADE|1#RESTRICT|2#SET NULL|3#NO ACTION|4#SET DEFAULT} ON UPDATE {6,choice,0#CASCADE|1#RESTRICT|2#SET NULL|3#NO ACTION|4#SET DEFAULT} {7,choice,5#DEFERRABLE INITIALLY DEFERRED|6#DEFERRABLE INITIALLY IMMEDIATE|7#NOT DEFERRABLE}";
	}

	protected String dropForeignKeyConstraintFormat()
	{
		return this.dropConstraintFormat();
	}

	protected String dropConstraintFormat()
	{
		return "ALTER TABLE {1} DROP CONSTRAINT {0}";
	}

	protected String createUniqueConstraintFormat()
	{
		return "ALTER TABLE {1} ADD CONSTRAINT {0} UNIQUE ({2})";
	}

	protected String dropUniqueConstraintFormat()
	{
		return this.dropConstraintFormat();
	}

	protected String createTriggerFormat()
	{
		return "CREATE TRIGGER {0} {1} {2} ON {3} FOR EACH ROW BEGIN {4} END";
	}

	protected String dropTriggerFormat()
	{
		return "DROP TRIGGER {1} ON {2}";
	}

	protected String triggerRowAlias(TriggerTime time)
	{
		return time.getAlias();
	}

	protected String createSchemaFormat()
	{
		return "CREATE SCHEMA {0}";
	}

	protected String dropSchemaFormat()
	{
		return "DROP SCHEMA {0}";
	}

	protected String nextSequenceValueFormat()
	{
		return "NEXT VALUE FOR {0}";
	}

	protected String alterSequenceFormat()
	{
		return "ALTER SEQUENCE {0} RESTART WITH {1}";
	}

	protected String alterIdentityColumnFormat()
	{
		return "ALTER TABLE {0} ALTER COLUMN {1} RESTART WITH {2}";
	}

	protected boolean indicatesFailureSQLState(String sqlState)
	{
		// 08 class SQLStates indicate connection failures
		return sqlState.startsWith("08");
	}

	protected boolean indicatesFailureErrorCode(int code)
	{
		return false;
	}

	protected Set<Integer> failureXAErrorCodes()
	{
		return Collections.singleton(XAException.XAER_RMFAIL);
	}

	protected Pattern identifierPattern(DatabaseMetaData metaData) throws SQLException
	{
		return Pattern.compile(MessageFormat.format("[a-zA-Z][\\w{0}]*", Pattern.quote(metaData.getExtraNameCharacters())));
	}

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

	/**
	 * Helper to parse a regex pattern against a string.
	 * Subclasses may use this for custom SQL parsing.
	 */
	protected String parse(Pattern pattern, String string)
	{
		Matcher matcher = pattern.matcher(string);

		return matcher.find() ? matcher.group(1) : null;
	}

	/* ------------------------------------------------------------------- *
	 *  Helper initialization                                              *
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
		this.constraintSQLGenerator = new DialectConstraintSQLGenerator(new DialectConstraintSQLGenerator.ConstraintTemplate()
		{
			@Override
			public String createForeignKeyConstraintFormat()
			{
				return StandardDialect.this.createForeignKeyConstraintFormat();
			}

			@Override
			public String dropForeignKeyConstraintFormat()
			{
				return StandardDialect.this.dropForeignKeyConstraintFormat();
			}

			@Override
			public String createUniqueConstraintFormat()
			{
				return StandardDialect.this.createUniqueConstraintFormat();
			}

			@Override
			public String dropUniqueConstraintFormat()
			{
				return StandardDialect.this.dropUniqueConstraintFormat();
			}
		});
		this.triggerSQLGenerator = new DialectTriggerSQLGenerator(new DialectTriggerSQLGenerator.TriggerTemplate()
		{
			@Override
			public String createTriggerFormat()
			{
				return StandardDialect.this.createTriggerFormat();
			}

			@Override
			public String dropTriggerFormat()
			{
				return StandardDialect.this.dropTriggerFormat();
			}

			@Override
			public String triggerRowAlias(TriggerTime time)
			{
				return StandardDialect.this.triggerRowAlias(time);
			}
		});
		this.schemaSQLGenerator = new DialectSchemaSQLGenerator(new DialectSchemaSQLGenerator.SchemaTemplate()
		{
			@Override
			public String createSchemaFormat()
			{
				return StandardDialect.this.createSchemaFormat();
			}

			@Override
			public String dropSchemaFormat()
			{
				return StandardDialect.this.dropSchemaFormat();
			}
		});
		this.sequenceSQLGenerator = new DialectSequenceSQLGenerator(new DialectSequenceSQLGenerator.SequenceTemplate()
		{
			@Override
			public String nextSequenceValueFormat()
			{
				return StandardDialect.this.nextSequenceValueFormat();
			}

			@Override
			public String alterSequenceFormat()
			{
				return StandardDialect.this.alterSequenceFormat();
			}

			@Override
			public String executeFunctionSQL(String function)
			{
				return StandardDialect.this.executeFunctionSQL(function);
			}
		});
		this.identityColumnSQLGenerator = new DialectIdentityColumnSQLGenerator(new DialectIdentityColumnSQLGenerator.IdentityColumnTemplate()
		{
			@Override
			public String alterIdentityColumnFormat()
			{
				return StandardDialect.this.alterIdentityColumnFormat();
			}
		});
		this.truncateSQLGenerator = new DialectTruncateSQLGenerator(new DialectTruncateSQLGenerator.TruncateTemplate()
		{
			@Override
			public String truncateTableFormat()
			{
				return StandardDialect.this.truncateTableFormat();
			}
		});
		this.factoryProvider = new DialectFactoryProvider(new DialectFactoryProvider.FactoryTemplate()
		{
			@Override
			public Pattern identifierPattern(DatabaseMetaData metaData) throws SQLException
			{
				return StandardDialect.this.identifierPattern(metaData);
			}

			@Override
			public Set<String> reservedIdentifiers(DatabaseMetaData metaData) throws SQLException
			{
				return StandardDialect.this.reservedIdentifiers(metaData);
			}
		});
		this.connectionResolver = new DialectConnectionResolver(this.vendorPattern(), this.locatorPattern());
	}

	/* ------------------------------------------------------------------- *
	 *  Dialect interface — delegates to helpers                           *
	 * ------------------------------------------------------------------- */

	@SuppressWarnings("unused")
	@Override
	public String schemaPattern(DatabaseMetaData metaData) throws SQLException
	{
		return null;
	}

	@Override
	public String getTruncateTableSQL(TableProperties properties)
	{
		return this.truncateSQLGenerator.getTruncateTableSQL(properties);
	}

	@Override
	public String getCreateForeignKeyConstraintSQL(ForeignKeyConstraint key)
	{
		return this.constraintSQLGenerator.getCreateForeignKeyConstraintSQL(key);
	}

	@Override
	public String getDropForeignKeyConstraintSQL(ForeignKeyConstraint key)
	{
		return this.constraintSQLGenerator.getDropForeignKeyConstraintSQL(key);
	}

	@Override
	public String getCreateUniqueConstraintSQL(UniqueConstraint constraint)
	{
		return this.constraintSQLGenerator.getCreateUniqueConstraintSQL(constraint);
	}

	@Override
	public String getDropUniqueConstraintSQL(UniqueConstraint constraint)
	{
		return this.constraintSQLGenerator.getDropUniqueConstraintSQL(constraint);
	}

	@Override
	public boolean isSelectForUpdate(String sql)
	{
		return this.sqlParser.isSelectForUpdate(sql);
	}

	@Override
	public int getColumnType(ColumnProperties properties)
	{
		return properties.getType();
	}

	@Override
	public List<String> getDefaultSchemas(DatabaseMetaData metaData) throws SQLException
	{
		return Collections.singletonList(metaData.getUserName());
	}

	@Override
	public String evaluateCurrentDate(String sql, java.sql.Date date)
	{
		return this.temporalEvaluator.evaluateCurrentDate(sql, date);
	}

	@Override
	public String evaluateCurrentTime(String sql, java.sql.Time time)
	{
		return this.temporalEvaluator.evaluateCurrentTime(sql, time);
	}

	@Override
	public String evaluateCurrentTimestamp(String sql, java.sql.Timestamp timestamp)
	{
		return this.temporalEvaluator.evaluateCurrentTimestamp(sql, timestamp);
	}

	@Override
	public String evaluateRand(String sql)
	{
		return this.temporalEvaluator.evaluateRand(sql);
	}

	@Override
	public boolean indicatesFailure(SQLException e)
	{
		return this.failureDetector.indicatesFailure(e);
	}

	@Override
	public boolean indicatesFailure(XAException e)
	{
		return this.failureDetector.indicatesFailure(e);
	}

	@Override
	public SequenceSupport getSequenceSupport()
	{
		return null;
	}

	@Override
	public IdentityColumnSupport getIdentityColumnSupport()
	{
		return null;
	}

	@Override
	public DumpRestoreSupport getDumpRestoreSupport()
	{
		return null;
	}

	@Override
	public TriggerSupport getTriggerSupport()
	{
		return null;
	}

	@Override
	public String getCreateSchemaSQL(String schema)
	{
		return this.schemaSQLGenerator.getCreateSchemaSQL(schema);
	}

	@Override
	public String getDropSchemaSQL(String schema)
	{
		return this.schemaSQLGenerator.getDropSchemaSQL(schema);
	}

	@Override
	public Collection<QualifiedName> getTables(DatabaseMetaData metaData, QualifiedNameFactory factory) throws SQLException
	{
		return this.metadataAccessor.getTables(metaData, factory);
	}

	@Override
	public Map<String, ColumnProperties> getColumns(DatabaseMetaData metaData, QualifiedName table, ColumnPropertiesFactory factory) throws SQLException
	{
		return this.metadataAccessor.getColumns(metaData, table, factory);
	}

	@Override
	public UniqueConstraint getPrimaryKey(DatabaseMetaData metaData, QualifiedName table, UniqueConstraintFactory factory) throws SQLException
	{
		return this.metadataAccessor.getPrimaryKey(metaData, table, factory);
	}

	@Override
	public Collection<ForeignKeyConstraint> getForeignKeyConstraints(DatabaseMetaData metaData, QualifiedName table, ForeignKeyConstraintFactory factory) throws SQLException
	{
		return this.metadataAccessor.getForeignKeyConstraints(metaData, table, factory);
	}

	@Override
	public Collection<UniqueConstraint> getUniqueConstraints(DatabaseMetaData metaData, QualifiedName table, UniqueConstraint primaryKey, UniqueConstraintFactory factory) throws SQLException
	{
		return this.metadataAccessor.getUniqueConstraints(metaData, table, primaryKey, factory);
	}

	@Override
	public Collection<String> getIdentityColumns(Collection<ColumnProperties> columns) throws SQLException
	{
		return this.metadataAccessor.getIdentityColumns(columns);
	}

	@Override
	public Map<Integer, Entry<String, Integer>> getTypes(DatabaseMetaData metaData) throws SQLException
	{
		return this.metadataAccessor.getTypes(metaData);
	}

	@Override
	public IdentifierNormalizer createIdentifierNormalizer(DatabaseMetaData metaData) throws SQLException
	{
		return this.factoryProvider.createIdentifierNormalizer(metaData);
	}

	@Override
	public QualifiedNameFactory createQualifiedNameFactory(DatabaseMetaData metaData, IdentifierNormalizer normalizer) throws SQLException
	{
		return this.factoryProvider.createQualifiedNameFactory(metaData, normalizer);
	}

	@Override
	public ColumnPropertiesFactory createColumnPropertiesFactory(IdentifierNormalizer normalizer)
	{
		return this.factoryProvider.createColumnPropertiesFactory(normalizer);
	}

	@Override
	public ForeignKeyConstraintFactory createForeignKeyConstraintFactory(QualifiedNameFactory factory)
	{
		return this.factoryProvider.createForeignKeyConstraintFactory(factory);
	}

	@Override
	public UniqueConstraintFactory createUniqueConstraintFactory(IdentifierNormalizer normalizer)
	{
		return this.factoryProvider.createUniqueConstraintFactory(normalizer);
	}

	@Override
	public boolean isValid(Connection connection) throws SQLException
	{
		return connection.isValid(0);
	}

	@Override
	public <Z, D extends Database<Z>> ConnectionProperties getConnectionProperties(D database, Decoder decoder) throws SQLException
	{
		return this.connectionResolver.getConnectionProperties(database, decoder);
	}

	/* ------------------------------------------------------------------- *
	 *  SequenceSupport interface                                          *
	 * ------------------------------------------------------------------- */

	@Override
	public String parseSequence(String sql)
	{
		return this.sqlParser.parseSequence(sql);
	}

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

	@Override
	public String getNextSequenceValueSQL(SequenceProperties sequence)
	{
		return this.sequenceSQLGenerator.getNextSequenceValueSQL(sequence);
	}

	@Override
	public String getAlterSequenceSQL(SequenceProperties sequence, long value)
	{
		return this.sequenceSQLGenerator.getAlterSequenceSQL(sequence, value);
	}

	@Override
	public SequencePropertiesFactory createSequencePropertiesFactory(QualifiedNameFactory factory)
	{
		return this.factoryProvider.createSequencePropertiesFactory(factory);
	}

	/* ------------------------------------------------------------------- *
	 *  IdentityColumnSupport interface                                    *
	 * ------------------------------------------------------------------- */

	@Override
	public String parseInsertTable(String sql)
	{
		return this.sqlParser.parseInsertTable(sql);
	}

	@Override
	public String getAlterIdentityColumnSQL(TableProperties table, ColumnProperties column, long value)
	{
		return this.identityColumnSQLGenerator.getAlterIdentityColumnSQL(table, column, value);
	}

	/* ------------------------------------------------------------------- *
	 *  TriggerSupport interface                                           *
	 * ------------------------------------------------------------------- */

	@Override
	public String getCreateTriggerSQL(String name, TableProperties table, TriggerEvent event, String action)
	{
		return this.triggerSQLGenerator.getCreateTriggerSQL(name, table, event, action);
	}

	@Override
	public String getDropTriggerSQL(String name, TableProperties table)
	{
		return this.triggerSQLGenerator.getDropTriggerSQL(name, table);
	}

	@Override
	public String getTriggerRowAlias(TriggerTime time)
	{
		return this.triggerSQLGenerator.getTriggerRowAlias(time);
	}

	/* ------------------------------------------------------------------- *
	 *  Utility methods — kept for subclasses                              *
	 * ------------------------------------------------------------------- */

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
		for (Driver driver : drivers)
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
}
