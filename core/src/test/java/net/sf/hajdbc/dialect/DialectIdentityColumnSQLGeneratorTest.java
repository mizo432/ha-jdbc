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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.sf.hajdbc.ColumnProperties;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.TableProperties;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class DialectIdentityColumnSQLGeneratorTest
{
	private DialectIdentityColumnSQLGenerator createGenerator()
	{
		return new DialectIdentityColumnSQLGenerator(new DialectIdentityColumnSQLGenerator.IdentityColumnTemplate()
		{
			@Override
			public String alterIdentityColumnFormat()
			{
				return "ALTER TABLE {0} ALTER COLUMN {1} RESTART WITH {2}";
			}
		});
	}

	@Test
	public void getAlterIdentityColumnSQL()
	{
		QualifiedName name = mock(QualifiedName.class);
		TableProperties table = mock(TableProperties.class);
		ColumnProperties column = mock(ColumnProperties.class);

		when(table.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("orders");
		when(column.getName()).thenReturn("id");

		String result = createGenerator().getAlterIdentityColumnSQL(table, column, 1000L);

		assertThat(result).isEqualTo("ALTER TABLE orders ALTER COLUMN id RESTART WITH 1000");
	}

	@Test
	public void getAlterIdentityColumnSQLWithDifferentValue()
	{
		QualifiedName name = mock(QualifiedName.class);
		TableProperties table = mock(TableProperties.class);
		ColumnProperties column = mock(ColumnProperties.class);

		when(table.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("users");
		when(column.getName()).thenReturn("user_id");

		String result = createGenerator().getAlterIdentityColumnSQL(table, column, 50000L);

		assertThat(result).isEqualTo("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 50000");
	}
}
