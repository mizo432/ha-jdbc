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
import net.sf.hajdbc.TriggerEvent;
import net.sf.hajdbc.TriggerTime;

/**
 * Helper that generates SQL for creating and dropping triggers.
 * Delegates to template method hooks on the dialect for database-specific customization.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectTriggerSQLGenerator
{
	private final DialectSQLFormats formats;

	DialectTriggerSQLGenerator(DialectSQLFormats formats)
	{
		this.formats = formats;
	}

	/**
	 * Generate SQL to create a trigger.
	 */
	String getCreateTriggerSQL(String name, TableProperties table, TriggerEvent event, String action)
	{
		return MessageFormat.format(this.formats.createTriggerFormat(),
				name, event.getTime().toString(), event.toString(), table.getName().getDDLName(), action);
	}

	/**
	 * Generate SQL to drop a trigger.
	 */
	String getDropTriggerSQL(String name, TableProperties table)
	{
 	return MessageFormat.format(this.formats.dropTriggerFormat(), name, table.getName().getDDLName());
	}

	/**
	 * Return the row alias for the specified trigger time.
	 */
	String getTriggerRowAlias(TriggerTime time)
	{
 	return this.formats.triggerRowAlias(time);
	}

}
