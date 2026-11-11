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

/**
 * Helper that generates SQL for creating and dropping schemas.
 * Delegates to template method hooks on the dialect for database-specific customization.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectSchemaSQLGenerator
{
	private final SchemaTemplate template;

	DialectSchemaSQLGenerator(SchemaTemplate template)
	{
		this.template = template;
	}

	/**
	 * Generate SQL to create a schema.
	 */
	String getCreateSchemaSQL(String schema)
	{
		return MessageFormat.format(this.template.createSchemaFormat(), schema);
	}

	/**
	 * Generate SQL to drop a schema.
	 */
	String getDropSchemaSQL(String schema)
	{
		return MessageFormat.format(this.template.dropSchemaFormat(), schema);
	}

	/**
	 * Template method hooks that the schema generator delegates to the dialect for.
	 */
	interface SchemaTemplate
	{
		String createSchemaFormat();

		String dropSchemaFormat();
	}
}
