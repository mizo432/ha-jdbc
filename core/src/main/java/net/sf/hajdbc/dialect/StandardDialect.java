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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

/**
 * Base dialect implementation that delegates to specialized helper classes.
 * Subclasses may override template methods to customize database-specific behavior.
 *
 * @author  Paul Ferraro
 * @since   1.1
 */
public class StandardDialect extends DialectSQLFormats implements Dialect, SequenceSupport, IdentityColumnSupport, TriggerSupport
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
		this.failureDetector = new DialectFailureDetector(this);
		this.constraintSQLGenerator = new DialectConstraintSQLGenerator(this);
		this.triggerSQLGenerator = new DialectTriggerSQLGenerator(this);
		this.schemaSQLGenerator = new DialectSchemaSQLGenerator(this);
		this.sequenceSQLGenerator = new DialectSequenceSQLGenerator(this);
		this.identityColumnSQLGenerator = new DialectIdentityColumnSQLGenerator(this);
		this.truncateSQLGenerator = new DialectTruncateSQLGenerator(this);
		this.factoryProvider = new DialectFactoryProvider(this);
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

	/**
	 * Helper to parse a regex pattern against a string.
	 * Subclasses may use this for custom SQL parsing.
	 */
	protected String parse(Pattern pattern, String string)
	{
		Matcher matcher = pattern.matcher(string);

		return matcher.find() ? matcher.group(1) : null;
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
