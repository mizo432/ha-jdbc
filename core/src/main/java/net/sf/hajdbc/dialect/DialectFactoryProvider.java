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
import java.util.Set;
import java.util.regex.Pattern;

import net.sf.hajdbc.ColumnPropertiesFactory;
import net.sf.hajdbc.ForeignKeyConstraintFactory;
import net.sf.hajdbc.IdentifierNormalizer;
import net.sf.hajdbc.QualifiedNameFactory;
import net.sf.hajdbc.SequencePropertiesFactory;
import net.sf.hajdbc.UniqueConstraintFactory;

/**
 * Helper that creates various factory instances for a dialect.
 * Delegates to template method hooks on the dialect for database-specific customization.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectFactoryProvider
{
	private final DialectSQLFormats formats;

	DialectFactoryProvider(DialectSQLFormats formats)
	{
		this.formats = formats;
	}

	/**
	 * Create an identifier normalizer for the specified metadata.
	 */
	IdentifierNormalizer createIdentifierNormalizer(DatabaseMetaData metaData) throws SQLException
	{
 	return new StandardIdentifierNormalizer(metaData,
				this.formats.identifierPattern(metaData),
				this.formats.reservedIdentifiers(metaData));
	}

	/**
	 * Create a qualified name factory for the specified metadata and normalizer.
	 */
	QualifiedNameFactory createQualifiedNameFactory(DatabaseMetaData metaData, IdentifierNormalizer normalizer) throws SQLException
	{
		return new StandardQualifiedNameFactory(metaData, normalizer);
	}

	/**
	 * Create a column properties factory for the specified normalizer.
	 */
	ColumnPropertiesFactory createColumnPropertiesFactory(IdentifierNormalizer normalizer)
	{
		return new StandardColumnPropertiesFactory(normalizer);
	}

	/**
	 * Create a sequence properties factory for the specified qualified name factory.
	 */
	SequencePropertiesFactory createSequencePropertiesFactory(QualifiedNameFactory factory)
	{
		return new StandardSequencePropertiesFactory(factory);
	}

	/**
	 * Create a foreign key constraint factory for the specified qualified name factory.
	 */
	ForeignKeyConstraintFactory createForeignKeyConstraintFactory(QualifiedNameFactory factory)
	{
		return new StandardForeignKeyConstraintFactory(factory);
	}

	/**
	 * Create a unique constraint factory for the specified normalizer.
	 */
	UniqueConstraintFactory createUniqueConstraintFactory(IdentifierNormalizer normalizer)
	{
		return new StandardUniqueConstraintFactory(normalizer);
	}

}
