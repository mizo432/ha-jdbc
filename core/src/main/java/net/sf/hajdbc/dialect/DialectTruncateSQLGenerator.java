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

import net.sf.hajdbc.TableProperties;

/**
 * Helper that generates SQL for truncating tables.
 * Delegates to template method hooks on the dialect for database-specific customization.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectTruncateSQLGenerator
{
	private final DialectSQLFormats formats;

	DialectTruncateSQLGenerator(DialectSQLFormats formats)
	{
		this.formats = formats;
	}

	/**
	 * Generate SQL to truncate a table.
	 */
	String getTruncateTableSQL(TableProperties properties)
	{
		return MessageFormat.format(this.formats.truncateTableFormat(), properties.getName().getDMLName());
	}
}
