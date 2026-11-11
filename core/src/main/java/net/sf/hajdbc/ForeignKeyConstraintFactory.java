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
package net.sf.hajdbc;

import org.jspecify.annotations.NonNull;

/**
 * Factory for creating {@link ForeignKeyConstraint} instances.
 * <p>
 * This factory is part of the {@link net.sf.hajdbc.dialect.DialectFactoryProvider} hierarchy,
 * providing database vendor-specific foreign key constraint metadata. Each dialect supplies
 * its own implementation to handle vendor-specific constraint naming and rules.
 * </p>
 *
 * @see net.sf.hajdbc.dialect.DialectFactoryContract#createForeignKeyConstraintFactory()
 * @see ForeignKeyConstraint
 */
public interface ForeignKeyConstraintFactory
{
	/**
	 * Creates a foreign key constraint.
	 * @param name constraint name
	 * @param table qualified name of the referencing table
	 * @param foreignTable qualified name of the referenced table
	 * @param deleteRule referential action on delete (java.sql.ForeignKey constant)
	 * @param updateRule referential action on update (java.sql.ForeignKey constant)
	 * @param deferrability constraint deferrability (java.sql.ForeignKey constant)
	 * @return new foreign key constraint
	 */
	@NonNull ForeignKeyConstraint createForeignKeyConstraint(@NonNull String name, @NonNull QualifiedName table, @NonNull QualifiedName foreignTable, int deleteRule, int updateRule, int deferrability);

	/**
	 * Returns the qualified name factory used to create table names within constraints.
	 * @return qualified name factory
	 */
	@NonNull QualifiedNameFactory getQualifiedNameFactory();
}
