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
 * Factory for creating {@link ColumnProperties} instances.
 * <p>
 * This factory is part of the {@link net.sf.hajdbc.dialect.DialectFactoryProvider} hierarchy,
 * providing database vendor-specific column metadata. Each dialect (e.g., MySQL, PostgreSQL)
 * supplies its own implementation to handle vendor-specific column type mappings.
 * </p>
 * 
 * @see net.sf.hajdbc.dialect.DialectFactoryContract#createColumnPropertiesFactory()
 * @see ColumnProperties
 */
public interface ColumnPropertiesFactory
{
	/**
	 * Creates column properties for the specified column.
	 * @param name column name
	 * @param type SQL type (java.sql.Types constant)
	 * @param nativeType database-specific type name
	 * @param defaultValue default value, or null if none
	 * @param remarks description, or null if none
	 * @param autoIncrement true if the column is auto-increment, false or null otherwise
	 * @return new column properties
	 */
	@NonNull ColumnProperties createColumnProperties(@NonNull String name, int type, @NonNull String nativeType, @Nullable String defaultValue, @Nullable String remarks, @Nullable Boolean autoIncrement);
}
