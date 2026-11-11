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
import org.jspecify.annotations.Nullable;

/**
 * Factory for creating {@link QualifiedName} instances.
 * <p>
 * This factory is part of the {@link net.sf.hajdbc.dialect.DialectFactoryProvider} hierarchy,
 * providing database vendor-specific qualified name (schema.table) handling. Each dialect
 * supplies its own implementation to handle vendor-specific naming conventions, such as
 * case sensitivity and identifier quoting rules.
 * </p>
 *
 * @see net.sf.hajdbc.dialect.DialectFactoryContract#createQualifiedNameFactory()
 * @see QualifiedName
 */
public interface QualifiedNameFactory
{
	/**
	 * Creates a qualified name from schema and table components.
	 * @param schema schema name, or null for default schema
	 * @param table table name
	 * @return new qualified name
	 */
	@NonNull QualifiedName createQualifiedName(@Nullable String schema, @NonNull String table);

	/**
	 * Returns the identifier normalizer used by this factory.
	 * @return identifier normalizer
	 */
	@NonNull IdentifierNormalizer getIdentifierNormalizer();

	/**
	 * Parses a raw qualified name string into a {@link QualifiedName} instance.
	 * @param raw raw qualified name string (e.g., "schema.table")
	 * @return parsed qualified name
	 */
	@NonNull QualifiedName parse(@NonNull String raw);
}
