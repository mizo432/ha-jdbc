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
 * Factory for creating {@link SequenceProperties} instances.
 * <p>
 * This factory is part of the {@link net.sf.hajdbc.dialect.DialectFactoryProvider} hierarchy,
 * providing database vendor-specific sequence metadata. Each dialect supplies its own
 * implementation to handle vendor-specific sequence naming and increment rules.
 * </p>
 *
 * @see net.sf.hajdbc.SequenceSupport#createSequencePropertiesFactory(QualifiedNameFactory)
 * @see SequenceProperties
 */
public interface SequencePropertiesFactory
{
	/**
	 * Creates sequence properties.
	 * @param schema schema name, or null for default schema
	 * @param name sequence name
	 * @param increment sequence increment value
	 * @return new sequence properties
	 */
	@NonNull SequenceProperties createSequenceProperties(@Nullable String schema, @NonNull String name, int increment);
}
