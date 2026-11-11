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

import java.sql.DatabaseMetaData;
import java.util.Arrays;

import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.UniqueConstraint;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class DialectConstraintSQLGeneratorTest
{
	private DialectConstraintSQLGenerator createGenerator()
	{
		return new DialectConstraintSQLGenerator(new DialectConstraintSQLGenerator.ConstraintTemplate()
		{
			@Override
			public String createForeignKeyConstraintFormat()
			{
				return "ALTER TABLE {1} ADD CONSTRAINT {0} FOREIGN KEY ({2}) REFERENCES {3} ({4}) ON DELETE {5,choice,0#CASCADE|1#RESTRICT|2#SET NULL|3#NO ACTION|4#SET DEFAULT} ON UPDATE {6,choice,0#CASCADE|1#RESTRICT|2#SET NULL|3#NO ACTION|4#SET DEFAULT} {7,choice,5#DEFERRABLE INITIALLY DEFERRED|6#DEFERRABLE INITIALLY IMMEDIATE}";
			}

			@Override
			public String dropForeignKeyConstraintFormat()
			{
				return "ALTER TABLE {1} DROP CONSTRAINT {0}";
			}

			@Override
			public String createUniqueConstraintFormat()
			{
				return "ALTER TABLE {1} ADD CONSTRAINT {0} UNIQUE ({2})";
			}

			@Override
			public String dropUniqueConstraintFormat()
			{
				return "ALTER TABLE {1} DROP CONSTRAINT {0}";
			}
		});
	}

	@Test
	public void getCreateForeignKeyConstraintSQL()
	{
		QualifiedName table = mock(QualifiedName.class);
		QualifiedName foreignTable = mock(QualifiedName.class);
		ForeignKeyConstraint constraint = mock(ForeignKeyConstraint.class);

		when(table.getDDLName()).thenReturn("orders");
		when(foreignTable.getDDLName()).thenReturn("customers");
		when(constraint.getName()).thenReturn("fk_customer");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("customer_id"));
		when(constraint.getForeignTable()).thenReturn(foreignTable);
		when(constraint.getForeignColumnList()).thenReturn(Arrays.asList("id"));
		when(constraint.getDeleteRule()).thenReturn(DatabaseMetaData.importedKeyCascade);
		when(constraint.getUpdateRule()).thenReturn(DatabaseMetaData.importedKeyRestrict);
		when(constraint.getDeferrability()).thenReturn(DatabaseMetaData.importedKeyInitiallyDeferred);

		String result = createGenerator().getCreateForeignKeyConstraintSQL(constraint);

		assertThat(result).isEqualTo("ALTER TABLE orders ADD CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE ON UPDATE RESTRICT DEFERRABLE INITIALLY DEFERRED");
	}

	@Test
	public void getCreateForeignKeyConstraintSQLWithMultipleColumns()
	{
		QualifiedName table = mock(QualifiedName.class);
		QualifiedName foreignTable = mock(QualifiedName.class);
		ForeignKeyConstraint constraint = mock(ForeignKeyConstraint.class);

		when(table.getDDLName()).thenReturn("order_items");
		when(foreignTable.getDDLName()).thenReturn("orders");
		when(constraint.getName()).thenReturn("fk_order");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("order_id", "version"));
		when(constraint.getForeignTable()).thenReturn(foreignTable);
		when(constraint.getForeignColumnList()).thenReturn(Arrays.asList("id", "version"));
		when(constraint.getDeleteRule()).thenReturn(DatabaseMetaData.importedKeyNoAction);
		when(constraint.getUpdateRule()).thenReturn(DatabaseMetaData.importedKeyNoAction);
		when(constraint.getDeferrability()).thenReturn(DatabaseMetaData.importedKeyInitiallyImmediate);

		String result = createGenerator().getCreateForeignKeyConstraintSQL(constraint);

		assertThat(result).isEqualTo("ALTER TABLE order_items ADD CONSTRAINT fk_order FOREIGN KEY (order_id, version) REFERENCES orders (id, version) ON DELETE NO ACTION ON UPDATE NO ACTION DEFERRABLE INITIALLY IMMEDIATE");
	}

	@Test
	public void getDropForeignKeyConstraintSQL()
	{
		QualifiedName table = mock(QualifiedName.class);
		ForeignKeyConstraint constraint = mock(ForeignKeyConstraint.class);

		when(table.getDDLName()).thenReturn("orders");
		when(constraint.getName()).thenReturn("fk_customer");
		when(constraint.getTable()).thenReturn(table);

		String result = createGenerator().getDropForeignKeyConstraintSQL(constraint);

		assertThat(result).isEqualTo("ALTER TABLE orders DROP CONSTRAINT fk_customer");
	}

	@Test
	public void getCreateUniqueConstraintSQL()
	{
		QualifiedName table = mock(QualifiedName.class);
		UniqueConstraint constraint = mock(UniqueConstraint.class);

		when(table.getDDLName()).thenReturn("users");
		when(constraint.getName()).thenReturn("uk_email");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("email"));

		String result = createGenerator().getCreateUniqueConstraintSQL(constraint);

		assertThat(result).isEqualTo("ALTER TABLE users ADD CONSTRAINT uk_email UNIQUE (email)");
	}

	@Test
	public void getCreateUniqueConstraintSQLWithMultipleColumns()
	{
		QualifiedName table = mock(QualifiedName.class);
		UniqueConstraint constraint = mock(UniqueConstraint.class);

		when(table.getDDLName()).thenReturn("users");
		when(constraint.getName()).thenReturn("uk_user_role");
		when(constraint.getTable()).thenReturn(table);
		when(constraint.getColumnList()).thenReturn(Arrays.asList("user_id", "role_id"));

		String result = createGenerator().getCreateUniqueConstraintSQL(constraint);

		assertThat(result).isEqualTo("ALTER TABLE users ADD CONSTRAINT uk_user_role UNIQUE (user_id, role_id)");
	}

	@Test
	public void getDropUniqueConstraintSQL()
	{
		QualifiedName table = mock(QualifiedName.class);
		UniqueConstraint constraint = mock(UniqueConstraint.class);

		when(table.getDDLName()).thenReturn("users");
		when(constraint.getName()).thenReturn("uk_email");
		when(constraint.getTable()).thenReturn(table);

		String result = createGenerator().getDropUniqueConstraintSQL(constraint);

		assertThat(result).isEqualTo("ALTER TABLE users DROP CONSTRAINT uk_email");
	}
}
