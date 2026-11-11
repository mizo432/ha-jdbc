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

import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.util.Strings;

/**
 * Helper that generates SQL for creating and dropping foreign key and unique constraints.
 * Delegates to template method hooks on the dialect for database-specific customization.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectConstraintSQLGenerator
{
	private final ConstraintTemplate template;

	DialectConstraintSQLGenerator(ConstraintTemplate template)
	{
		this.template = template;
	}

	/**
	 * Generate SQL to create a foreign key constraint.
	 */
	String getCreateForeignKeyConstraintSQL(ForeignKeyConstraint key)
	{
		return MessageFormat.format(this.template.createForeignKeyConstraintFormat(),
				key.getName(),
				key.getTable().getDDLName(),
				Strings.join(key.getColumnList(), Strings.PADDED_COMMA),
				key.getForeignTable().getDDLName(),
				Strings.join(key.getForeignColumnList(), Strings.PADDED_COMMA),
				key.getDeleteRule(),
				key.getUpdateRule(),
				key.getDeferrability());
	}

	/**
	 * Generate SQL to drop a foreign key constraint.
	 */
	String getDropForeignKeyConstraintSQL(ForeignKeyConstraint key)
	{
		return MessageFormat.format(this.template.dropForeignKeyConstraintFormat(), key.getName(), key.getTable().getDDLName());
	}

	/**
	 * Generate SQL to create a unique constraint.
	 */
	String getCreateUniqueConstraintSQL(UniqueConstraint constraint)
	{
		return MessageFormat.format(this.template.createUniqueConstraintFormat(),
				constraint.getName(),
				constraint.getTable().getDDLName(),
				Strings.join(constraint.getColumnList(), Strings.PADDED_COMMA));
	}

	/**
	 * Generate SQL to drop a unique constraint.
	 */
	String getDropUniqueConstraintSQL(UniqueConstraint constraint)
	{
		return MessageFormat.format(this.template.dropUniqueConstraintFormat(), constraint.getName(), constraint.getTable().getDDLName());
	}

	/**
	 * Template method hooks that the constraint generator delegates to the dialect for.
	 */
	interface ConstraintTemplate
	{
		String createForeignKeyConstraintFormat();

		String dropForeignKeyConstraintFormat();

		String createUniqueConstraintFormat();

		String dropUniqueConstraintFormat();
	}
}
