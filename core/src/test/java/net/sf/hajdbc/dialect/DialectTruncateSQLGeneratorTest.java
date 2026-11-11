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

import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.TableProperties;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class DialectTruncateSQLGeneratorTest
{
	private DialectTruncateSQLGenerator createGenerator()
	{
		return new DialectTruncateSQLGenerator(new DialectTruncateSQLGenerator.TruncateTemplate()
		{
			@Override
			public String truncateTableFormat()
			{
				return "DELETE FROM {0}";
			}
		});
	}

	@Test
	public void getTruncateTableSQL()
	{
		QualifiedName name = mock(QualifiedName.class);
		TableProperties table = mock(TableProperties.class);

		when(table.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("orders");

		String result = createGenerator().getTruncateTableSQL(table);

		assertThat(result).isEqualTo("DELETE FROM orders");
	}

	@Test
	public void getTruncateTableSQLWithSchema()
	{
		QualifiedName name = mock(QualifiedName.class);
		TableProperties table = mock(TableProperties.class);

		when(table.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("public.orders");

		String result = createGenerator().getTruncateTableSQL(table);

		assertThat(result).isEqualTo("DELETE FROM public.orders");
	}

	@Test
	public void getTruncateTableSQLWithTruncateSyntax()
	{
		DialectTruncateSQLGenerator generator = new DialectTruncateSQLGenerator(new DialectTruncateSQLGenerator.TruncateTemplate()
		{
			@Override
			public String truncateTableFormat()
			{
				return "TRUNCATE TABLE {0}";
			}
		});

		QualifiedName name = mock(QualifiedName.class);
		TableProperties table = mock(TableProperties.class);

		when(table.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("logs");

		String result = generator.getTruncateTableSQL(table);

		assertThat(result).isEqualTo("TRUNCATE TABLE logs");
	}
}
