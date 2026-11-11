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
package net.sf.hajdbc.dialect.mssql;

import java.sql.Connection;
import java.sql.SQLException;

import net.sf.hajdbc.IdentityColumnSupport;
import net.sf.hajdbc.dialect.StandardDialect;

/**
 * Microsoft SQL Server用のDialectです。
 * @author Paul Ferraro
 */
@SuppressWarnings("nls")
public class MSSQLDialect extends StandardDialect
{
	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialect#getIdentityColumnSupport()
	 */
	@Override
	public IdentityColumnSupport getIdentityColumnSupport()
	{
		return this;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialect#vendorPattern()
	 */
	@Override
	protected String vendorPattern()
	{
		return "sqlserver";
	}

	/**
	 * @see net.sf.hajdbc.dialect.StandardDialect#truncateTableFormat()
	 */
	@Override
	protected String truncateTableFormat()
	{
		return "TRUNCATE TABLE {0}";
	}

	/**
	 * Deferrability clause is not supported.
	 * @see net.sf.hajdbc.dialect.StandardDialect#createForeignKeyConstraintFormat()
	 */
	@Override
	protected String createForeignKeyConstraintFormat()
	{
		return "ALTER TABLE {1} WITH NOCHECK ADD CONSTRAINT {0} FOREIGN KEY ({2}) REFERENCES {3} ({4}) ON DELETE {5,choice,0#CASCADE|1#NO ACTION|2#SET NULL|3#NO ACTION|4#SET DEFAULT} ON UPDATE {6,choice,0#CASCADE|1#NO ACTION|2#SET NULL|3#NO ACTION|4#SET DEFAULT}";
	}

	/**
	 * @see net.sf.hajdbc.dialect.StandardDialect#dropConstraintFormat()
	 */
	@Override
	protected String dropConstraintFormat()
	{
		return "ALTER TABLE {1} DROP CONSTRAINT {0}";
	}

	/**
	 * @see net.sf.hajdbc.dialect.StandardDialect#currentDatePattern()
	 */
	@Override
	protected String currentDatePattern()
	{
		return "(?<=\\W)CURRENT_DATE(?=\\W)|(?<=\\W)GETDATE\\s*\\(\\s*\\)";
	}

	/**
	 * @see net.sf.hajdbc.dialect.StandardDialect#currentTimePattern()
	 */
	@Override
	protected String currentTimePattern()
	{
		return "(?<=\\W)CURRENT_TIME(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)|(?<=\\W)GETDATE\\s*\\(\\s*\\)";
	}

	/**
	 * @see net.sf.hajdbc.dialect.StandardDialect#currentTimestampPattern()
	 */
	@Override
	protected String currentTimestampPattern()
	{
		return "(?<=\\W)CURRENT_TIMESTAMP(?:\\s*\\(\\s*\\d+\\s*\\))?(?=\\W)|(?<=\\W)GETDATE\\s*\\(\\s*\\)|(?<=\\W)SYSDATETIME\\s*\\(\\s*\\)";
	}

	/**
	 * @see net.sf.hajdbc.dialect.StandardDialect#randomPattern()
	 */
	@Override
	protected String randomPattern()
	{
		return "(?<=\\W)RAND\\s*\\(\\s*\\d*\\s*\\)";
	}

	/**
	 * @see net.sf.hajdbc.dialect.StandardDialect#alterIdentityColumnFormat()
	 */
	@Override
	protected String alterIdentityColumnFormat()
	{
		return "DBCC CHECKIDENT ({0}, RESEED, {2})";
	}

	/**
	 * MS SQL Server does not support sequences in older versions, but supports them since SQL Server 2012.
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialect#getSequenceSupport()
	 */
	@Override
	public net.sf.hajdbc.SequenceSupport getSequenceSupport()
	{
		return this;
	}

	/**
	 * @see net.sf.hajdbc.dialect.StandardDialect#nextSequenceValueFormat()
	 */
	@Override
	protected String nextSequenceValueFormat()
	{
		return "NEXT VALUE FOR {0}";
	}

	/**
	 * @see net.sf.hajdbc.dialect.StandardDialect#alterSequenceFormat()
	 */
	@Override
	protected String alterSequenceFormat()
	{
		return "ALTER SEQUENCE {0} RESTART WITH {1}";
	}

}
