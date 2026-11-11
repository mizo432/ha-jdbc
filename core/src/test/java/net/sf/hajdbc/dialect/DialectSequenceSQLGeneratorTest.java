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
import net.sf.hajdbc.SequenceProperties;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class DialectSequenceSQLGeneratorTest
{
	private DialectSequenceSQLGenerator createGenerator()
	{
		return new DialectSequenceSQLGenerator(new DialectSequenceSQLGenerator.SequenceTemplate()
		{
			@Override
			public String nextSequenceValueFormat()
			{
				return "NEXT VALUE FOR {0}";
			}

			@Override
			public String alterSequenceFormat()
			{
				return "ALTER SEQUENCE {0} RESTART WITH {1}";
			}

			@Override
			public String executeFunctionSQL(String function)
			{
				return "SELECT " + function;
			}
		});
	}

	@Test
	public void getNextSequenceValueSQL()
	{
		QualifiedName name = mock(QualifiedName.class);
		SequenceProperties sequence = mock(SequenceProperties.class);

		when(sequence.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("hibernate_sequence");

		String result = createGenerator().getNextSequenceValueSQL(sequence);

		assertThat(result).isEqualTo("SELECT NEXT VALUE FOR hibernate_sequence");
	}

	@Test
	public void getNextSequenceValueSQLWithSchema()
	{
		QualifiedName name = mock(QualifiedName.class);
		SequenceProperties sequence = mock(SequenceProperties.class);

		when(sequence.getName()).thenReturn(name);
		when(name.getDMLName()).thenReturn("public.hibernate_sequence");

		String result = createGenerator().getNextSequenceValueSQL(sequence);

		assertThat(result).isEqualTo("SELECT NEXT VALUE FOR public.hibernate_sequence");
	}

	@Test
	public void getAlterSequenceSQL()
	{
		QualifiedName name = mock(QualifiedName.class);
		SequenceProperties sequence = mock(SequenceProperties.class);

		when(sequence.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("hibernate_sequence");
		when(sequence.getIncrement()).thenReturn(1);

		String result = createGenerator().getAlterSequenceSQL(sequence, 1000L);

		assertThat(result).isEqualTo("ALTER SEQUENCE hibernate_sequence RESTART WITH 1000");
	}

	@Test
	public void getAlterSequenceSQLWithCustomIncrement()
	{
		QualifiedName name = mock(QualifiedName.class);
		SequenceProperties sequence = mock(SequenceProperties.class);

		when(sequence.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("order_seq");
		when(sequence.getIncrement()).thenReturn(10);

		String result = createGenerator().getAlterSequenceSQL(sequence, 500L);

		assertThat(result).isEqualTo("ALTER SEQUENCE order_seq RESTART WITH 500");
	}
}
