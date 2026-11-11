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
import net.sf.hajdbc.TriggerSupport;
import net.sf.hajdbc.TriggerTime;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class DialectTriggerSQLGeneratorTest
{
	private DialectTriggerSQLGenerator createGenerator()
	{
		return new DialectTriggerSQLGenerator(new DialectTriggerSQLGenerator.TriggerTemplate()
		{
			@Override
			public String createTriggerFormat()
			{
				return "CREATE TRIGGER {0} {1} {2} ON {3} FOR EACH ROW {4}";
			}

			@Override
			public String dropTriggerFormat()
			{
				return "DROP TRIGGER {0} ON {1}";
			}

			@Override
			public String triggerRowAlias(TriggerTime time)
			{
				return time.getAlias();
			}
		});
	}

	@Test
	public void getCreateTriggerSQL()
	{
		QualifiedName name = mock(QualifiedName.class);
		TableProperties table = mock(TableProperties.class);

		when(table.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("orders");

		String result = createGenerator().getCreateTriggerSQL(
				"audit_trigger", table, TriggerSupport.TriggerEventEnum.INSERT, "INSERT INTO audit VALUES (NEW.id)");

		assertThat(result).isEqualTo("CREATE TRIGGER audit_trigger AFTER INSERT ON orders FOR EACH ROW INSERT INTO audit VALUES (NEW.id)");
	}

	@Test
	public void getCreateTriggerSQLForUpdate()
	{
		QualifiedName name = mock(QualifiedName.class);
		TableProperties table = mock(TableProperties.class);

		when(table.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("users");

		String result = createGenerator().getCreateTriggerSQL(
				"update_trigger", table, TriggerSupport.TriggerEventEnum.UPDATE, "SET NEW.updated = CURRENT_TIMESTAMP");

		assertThat(result).isEqualTo("CREATE TRIGGER update_trigger AFTER UPDATE ON users FOR EACH ROW SET NEW.updated = CURRENT_TIMESTAMP");
	}

	@Test
	public void getCreateTriggerSQLForDelete()
	{
		QualifiedName name = mock(QualifiedName.class);
		TableProperties table = mock(TableProperties.class);

		when(table.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("products");

		String result = createGenerator().getCreateTriggerSQL(
				"delete_trigger", table, TriggerSupport.TriggerEventEnum.DELETE, "INSERT INTO archive VALUES (OLD.id)");

		assertThat(result).isEqualTo("CREATE TRIGGER delete_trigger BEFORE DELETE ON products FOR EACH ROW INSERT INTO archive VALUES (OLD.id)");
	}

	@Test
	public void getDropTriggerSQL()
	{
		QualifiedName name = mock(QualifiedName.class);
		TableProperties table = mock(TableProperties.class);

		when(table.getName()).thenReturn(name);
		when(name.getDDLName()).thenReturn("orders");

		String result = createGenerator().getDropTriggerSQL("audit_trigger", table);

		assertThat(result).isEqualTo("DROP TRIGGER audit_trigger ON orders");
	}

	@Test
	public void getTriggerRowAlias()
	{
		DialectTriggerSQLGenerator generator = createGenerator();

		assertThat(generator.getTriggerRowAlias(TriggerSupport.TriggerTimeEnum.BEFORE)).isEqualTo("OLD");
		assertThat(generator.getTriggerRowAlias(TriggerSupport.TriggerTimeEnum.AFTER)).isEqualTo("NEW");
	}
}
