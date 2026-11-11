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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class DialectSchemaSQLGeneratorTest
{
	private DialectSchemaSQLGenerator createGenerator()
	{
		return new DialectSchemaSQLGenerator(new DialectSchemaSQLGenerator.SchemaTemplate()
		{
			@Override
			public String createSchemaFormat()
			{
				return "CREATE SCHEMA {0}";
			}

			@Override
			public String dropSchemaFormat()
			{
				return "DROP SCHEMA {0}";
			}
		});
	}

	@Test
	public void getCreateSchemaSQL()
	{
		String result = createGenerator().getCreateSchemaSQL("public");

		assertThat(result).isEqualTo("CREATE SCHEMA public");
	}

	@Test
	public void getCreateSchemaSQLWithQuotedName()
	{
		String result = createGenerator().getCreateSchemaSQL("my-schema");

		assertThat(result).isEqualTo("CREATE SCHEMA my-schema");
	}

	@Test
	public void getDropSchemaSQL()
	{
		String result = createGenerator().getDropSchemaSQL("public");

		assertThat(result).isEqualTo("DROP SCHEMA public");
	}
}
