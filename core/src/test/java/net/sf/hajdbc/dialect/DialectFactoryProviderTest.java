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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.sf.hajdbc.ColumnPropertiesFactory;
import net.sf.hajdbc.ForeignKeyConstraintFactory;
import net.sf.hajdbc.IdentifierNormalizer;
import net.sf.hajdbc.QualifiedNameFactory;
import net.sf.hajdbc.SequencePropertiesFactory;
import net.sf.hajdbc.UniqueConstraintFactory;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class DialectFactoryProviderTest
{
	private DialectFactoryProvider createProvider()
	{
		return new DialectFactoryProvider(new DialectFactoryProvider.FactoryTemplate()
		{
			@Override
			public Pattern identifierPattern(DatabaseMetaData metaData)
			{
				return Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
			}

			@Override
			public Set<String> reservedIdentifiers(DatabaseMetaData metaData)
			{
				Set<String> reserved = new HashSet<>();
				reserved.add("SELECT");
				reserved.add("FROM");
				reserved.add("WHERE");
				return reserved;
			}
		});
	}

	@Test
	public void createIdentifierNormalizer() throws Exception
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		when(metaData.storesUpperCaseIdentifiers()).thenReturn(false);
		when(metaData.storesLowerCaseIdentifiers()).thenReturn(false);
		when(metaData.storesMixedCaseIdentifiers()).thenReturn(true);

		IdentifierNormalizer normalizer = createProvider().createIdentifierNormalizer(metaData);

		assertThat(normalizer).isNotNull();
		assertThat(normalizer).isInstanceOf(StandardIdentifierNormalizer.class);
	}

	@Test
	public void createQualifiedNameFactory() throws Exception
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		when(metaData.storesUpperCaseIdentifiers()).thenReturn(false);
		when(metaData.storesLowerCaseIdentifiers()).thenReturn(false);
		when(metaData.storesMixedCaseIdentifiers()).thenReturn(true);
		when(metaData.supportsSchemasInTableDefinitions()).thenReturn(true);
		when(metaData.supportsSchemasInDataManipulation()).thenReturn(true);

		IdentifierNormalizer normalizer = createProvider().createIdentifierNormalizer(metaData);
		QualifiedNameFactory factory = createProvider().createQualifiedNameFactory(metaData, normalizer);

		assertThat(factory).isNotNull();
		assertThat(factory).isInstanceOf(StandardQualifiedNameFactory.class);
	}

	@Test
	public void createColumnPropertiesFactory() throws Exception
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		when(metaData.storesUpperCaseIdentifiers()).thenReturn(false);
		when(metaData.storesLowerCaseIdentifiers()).thenReturn(false);
		when(metaData.storesMixedCaseIdentifiers()).thenReturn(true);

		IdentifierNormalizer normalizer = createProvider().createIdentifierNormalizer(metaData);
		ColumnPropertiesFactory factory = createProvider().createColumnPropertiesFactory(normalizer);

		assertThat(factory).isNotNull();
		assertThat(factory).isInstanceOf(StandardColumnPropertiesFactory.class);
	}

	@Test
	public void createSequencePropertiesFactory() throws Exception
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		when(metaData.storesUpperCaseIdentifiers()).thenReturn(false);
		when(metaData.storesLowerCaseIdentifiers()).thenReturn(false);
		when(metaData.storesMixedCaseIdentifiers()).thenReturn(true);
		when(metaData.supportsSchemasInTableDefinitions()).thenReturn(true);
		when(metaData.supportsSchemasInDataManipulation()).thenReturn(true);

		IdentifierNormalizer normalizer = createProvider().createIdentifierNormalizer(metaData);
		QualifiedNameFactory nameFactory = createProvider().createQualifiedNameFactory(metaData, normalizer);
		SequencePropertiesFactory factory = createProvider().createSequencePropertiesFactory(nameFactory);

		assertThat(factory).isNotNull();
		assertThat(factory).isInstanceOf(StandardSequencePropertiesFactory.class);
	}

	@Test
	public void createForeignKeyConstraintFactory() throws Exception
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		when(metaData.storesUpperCaseIdentifiers()).thenReturn(false);
		when(metaData.storesLowerCaseIdentifiers()).thenReturn(false);
		when(metaData.storesMixedCaseIdentifiers()).thenReturn(true);
		when(metaData.supportsSchemasInTableDefinitions()).thenReturn(true);
		when(metaData.supportsSchemasInDataManipulation()).thenReturn(true);

		IdentifierNormalizer normalizer = createProvider().createIdentifierNormalizer(metaData);
		QualifiedNameFactory nameFactory = createProvider().createQualifiedNameFactory(metaData, normalizer);
		ForeignKeyConstraintFactory factory = createProvider().createForeignKeyConstraintFactory(nameFactory);

		assertThat(factory).isNotNull();
		assertThat(factory).isInstanceOf(StandardForeignKeyConstraintFactory.class);
	}

	@Test
	public void createUniqueConstraintFactory() throws Exception
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		when(metaData.storesUpperCaseIdentifiers()).thenReturn(false);
		when(metaData.storesLowerCaseIdentifiers()).thenReturn(false);
		when(metaData.storesMixedCaseIdentifiers()).thenReturn(true);

		IdentifierNormalizer normalizer = createProvider().createIdentifierNormalizer(metaData);
		UniqueConstraintFactory factory = createProvider().createUniqueConstraintFactory(normalizer);

		assertThat(factory).isNotNull();
		assertThat(factory).isInstanceOf(StandardUniqueConstraintFactory.class);
	}

	@Test
	public void reservedIdentifiersCauseQuoting() throws Exception
	{
		DatabaseMetaData metaData = mock(DatabaseMetaData.class);
		when(metaData.storesUpperCaseIdentifiers()).thenReturn(false);
		when(metaData.storesLowerCaseIdentifiers()).thenReturn(false);
		when(metaData.storesMixedCaseIdentifiers()).thenReturn(true);
		when(metaData.getIdentifierQuoteString()).thenReturn("\"");
		when(metaData.supportsMixedCaseQuotedIdentifiers()).thenReturn(true);
		when(metaData.storesLowerCaseQuotedIdentifiers()).thenReturn(false);
		when(metaData.storesUpperCaseQuotedIdentifiers()).thenReturn(false);

		IdentifierNormalizer normalizer = createProvider().createIdentifierNormalizer(metaData);

		/* Reserved identifiers should be quoted */
		assertThat(normalizer.normalize("SELECT")).isEqualTo("\"SELECT\"");
		assertThat(normalizer.normalize("FROM")).isEqualTo("\"FROM\"");
		assertThat(normalizer.normalize("WHERE")).isEqualTo("\"WHERE\"");
		/* Non-reserved identifiers should not be quoted */
		assertThat(normalizer.normalize("TABLE")).isEqualTo("TABLE");
	}
}
