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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import net.sf.hajdbc.ColumnPropertiesFactory;
import net.sf.hajdbc.ForeignKeyConstraintFactory;
import net.sf.hajdbc.IdentifierNormalizer;
import net.sf.hajdbc.QualifiedNameFactory;
import net.sf.hajdbc.UniqueConstraintFactory;
import org.jspecify.annotations.NonNull;

/**
 * Creates factory instances for database vendor-specific object construction.
 * This interface isolates factory creation concerns from other dialect responsibilities,
 * following the Interface Segregation Principle.
 *
 * @author Paul Ferraro
 * @since 3.2
 */
public interface DialectFactoryContract
{
	/**
	 * Creates an identifier normalizer for this dialect.
	 * @param metaData database metadata
	 * @return identifier normalizer
	 * @throws SQLException if an error occurs accessing DatabaseMetaData
	 */
	@NonNull IdentifierNormalizer createIdentifierNormalizer(@NonNull DatabaseMetaData metaData) throws SQLException;

	/**
	 * Creates a qualified name factory for this dialect.
	 * @param metaData database metadata
	 * @param normalizer identifier normalizer
	 * @return qualified name factory
	 * @throws SQLException if an error occurs accessing DatabaseMetaData
	 */
	@NonNull QualifiedNameFactory createQualifiedNameFactory(@NonNull DatabaseMetaData metaData, @NonNull IdentifierNormalizer normalizer) throws SQLException;

	/**
	 * Creates a column properties factory for this dialect.
	 * @param normalizer identifier normalizer
	 * @return column properties factory
	 */
	@NonNull ColumnPropertiesFactory createColumnPropertiesFactory(@NonNull IdentifierNormalizer normalizer);

	/**
	 * Creates a foreign key constraint factory for this dialect.
	 * @param factory qualified name factory
	 * @return foreign key constraint factory
	 */
	@NonNull ForeignKeyConstraintFactory createForeignKeyConstraintFactory(@NonNull QualifiedNameFactory factory);

	/**
	 * Creates a unique constraint factory for this dialect.
	 * @param normalizer identifier normalizer
	 * @return unique constraint factory
	 */
	@NonNull UniqueConstraintFactory createUniqueConstraintFactory(@NonNull IdentifierNormalizer normalizer);
}

