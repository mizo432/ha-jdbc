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
 * Factory for creating {@link UniqueConstraint} instances.
 * <p>
 * This factory is part of the {@link net.sf.hajdbc.dialect.DialectFactoryProvider} hierarchy,
 * providing database vendor-specific unique constraint metadata. Each dialect supplies
 * its own implementation to handle vendor-specific constraint naming and column handling.
 * </p>
 *
 * @see net.sf.hajdbc.dialect.DialectFactoryContract#createUniqueConstraintFactory(IdentifierNormalizer)
 * @see UniqueConstraint
 */
public interface UniqueConstraintFactory
{
	/**
	 * Creates a unique constraint.
	 * @param name constraint name
	 * @param table qualified name of the constrained table
	 * @return new unique constraint
	 */
	@NonNull UniqueConstraint createUniqueConstraint(@NonNull String name, @NonNull QualifiedName table);
}
