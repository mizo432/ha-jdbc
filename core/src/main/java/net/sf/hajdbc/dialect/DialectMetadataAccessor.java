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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.hajdbc.ColumnProperties;
import net.sf.hajdbc.ColumnPropertiesFactory;
import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.ForeignKeyConstraintFactory;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.QualifiedNameFactory;
import net.sf.hajdbc.SequenceProperties;
import net.sf.hajdbc.SequencePropertiesFactory;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.UniqueConstraintFactory;
import net.sf.hajdbc.util.Strings;

/**
 * Helper that encapsulates database metadata query logic for a dialect.
 * Delegates to template method hooks on the dialect for database-specific customization.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectMetadataAccessor
{
	private final DialectSQLFormats formats;

	DialectMetadataAccessor(DialectSQLFormats formats)
	{
		this.formats = formats;
	}

	/**
	 * Return all tables in this database, mapped by schema.
	 */
	Collection<QualifiedName> getTables(DatabaseMetaData metaData, QualifiedNameFactory factory) throws SQLException
	{
 	try (ResultSet resultSet = metaData.getTables(getCatalog(metaData), formats.schemaPattern(metaData), Strings.ANY, new String[] { "TABLE" }))
		{
			List<QualifiedName> list = new LinkedList<>();

			while (resultSet.next())
			{
				list.add(factory.createQualifiedName(resultSet.getString("TABLE_SCHEM"), resultSet.getString("TABLE_NAME")));
			}

			return list;
		}
	}

	/**
	 * Return columns for the specified table.
	 */
	Map<String, ColumnProperties> getColumns(DatabaseMetaData metaData, QualifiedName table, ColumnPropertiesFactory factory) throws SQLException
	{
		try (Statement statement = metaData.getConnection().createStatement())
		{
			Map<String, ColumnProperties> map = new HashMap<>();

			ResultSetMetaData resultSet = statement.executeQuery(String.format("SELECT * FROM %s WHERE 0=1", table.getDMLName())).getMetaData();

			for (int i = 1; i <= resultSet.getColumnCount(); ++i)
			{
				String column = resultSet.getColumnName(i);
				int type = resultSet.getColumnType(i);
				String nativeType = resultSet.getColumnTypeName(i);
				boolean autoIncrement = resultSet.isAutoIncrement(i);

				ColumnProperties properties = factory.createColumnProperties(column, type, nativeType, null, null, autoIncrement);
				map.put(properties.getName(), properties);
			}

			return map;
		}
	}

	/**
	 * Return the primary key for the specified table.
	 */
	UniqueConstraint getPrimaryKey(DatabaseMetaData metaData, QualifiedName table, UniqueConstraintFactory factory) throws SQLException
	{
		try (ResultSet resultSet = metaData.getPrimaryKeys(getCatalog(metaData), table.getSchema(), table.getName()))
		{
			UniqueConstraint constraint = null;

			while (resultSet.next())
			{
				if (constraint == null)
				{
					constraint = factory.createUniqueConstraint(resultSet.getString("PK_NAME"), table);
				}

				constraint.getColumnList().add(resultSet.getString("COLUMN_NAME"));
			}

			return constraint;
		}
	}

	/**
	 * Return foreign key constraints for the specified table.
	 */
	Collection<ForeignKeyConstraint> getForeignKeyConstraints(DatabaseMetaData metaData, QualifiedName table, ForeignKeyConstraintFactory factory) throws SQLException
	{
		try (ResultSet resultSet = metaData.getImportedKeys(getCatalog(metaData), table.getSchema(), table.getName()))
		{
			Map<String, ForeignKeyConstraint> foreignKeyMap = new HashMap<>();

			while (resultSet.next())
			{
				String name = resultSet.getString("FK_NAME");

				ForeignKeyConstraint foreignKey = foreignKeyMap.get(name);

				if (foreignKey == null)
				{
					foreignKey = factory.createForeignKeyConstraint(name, table, factory.getQualifiedNameFactory().createQualifiedName(resultSet.getString("PKTABLE_SCHEM"), resultSet.getString("PKTABLE_NAME")), resultSet.getInt("DELETE_RULE"), resultSet.getInt("UPDATE_RULE"), resultSet.getInt("DEFERRABILITY"));

					foreignKeyMap.put(name, foreignKey);
				}

				foreignKey.getColumnList().add(resultSet.getString("FKCOLUMN_NAME"));
				foreignKey.getForeignColumnList().add(resultSet.getString("PKCOLUMN_NAME"));
			}

			return foreignKeyMap.values();
		}
	}

	/**
	 * Return unique constraints for the specified table — excluding the primary key.
	 */
	Collection<UniqueConstraint> getUniqueConstraints(DatabaseMetaData metaData, QualifiedName table, UniqueConstraint primaryKey, UniqueConstraintFactory factory) throws SQLException
	{
		try (ResultSet resultSet = metaData.getIndexInfo(getCatalog(metaData), table.getSchema(), table.getName(), true, false))
		{
			Map<String, UniqueConstraint> keyMap = new HashMap<>();

			while (resultSet.next())
			{
				if (resultSet.getShort("TYPE") == DatabaseMetaData.tableIndexHashed)
				{
					String name = resultSet.getString("INDEX_NAME");

					UniqueConstraint key = keyMap.get(name);

					if (key == null)
					{
						key = factory.createUniqueConstraint(name, table);

						// Don't include the primary key
						if (key.equals(primaryKey)) continue;

						keyMap.put(name, key);
					}

					key.getColumnList().add(resultSet.getString("COLUMN_NAME"));
				}
			}
			return keyMap.values();
		}
	}

	/**
	 * Identify ID columns from the specified collection of columns.
	 */
	Collection<String> getIdentityColumns(Collection<ColumnProperties> columns) throws SQLException
	{
		List<String> columnList = new LinkedList<>();

		for (ColumnProperties column: columns)
		{
			if (column.isAutoIncrement())
			{
				columnList.add(column.getName());
			}
		}

		return columnList;
	}

	/**
	 * Return the mapping from standard JDBC types to native types.
	 */
	Map<Integer, Entry<String, Integer>> getTypes(DatabaseMetaData metaData) throws SQLException
	{
		try (ResultSet resultSet = metaData.getTypeInfo())
		{
			Map<Integer, Map.Entry<String, Integer>> types = new HashMap<>();

			while (resultSet.next())
			{
				int type = resultSet.getInt("DATA_TYPE");
				if (!types.containsKey(type))
				{
					String name = resultSet.getString("TYPE_NAME");
					String params = resultSet.getString("CREATE_PARAMS");
					types.put(type, new AbstractMap.SimpleImmutableEntry<>(name, (params != null) ? resultSet.getInt("PRECISION") : null));
				}
			}

			return types;
		}
	}

	/**
	 * Return sequences using dialect-specific logic.
	 */
	Collection<SequenceProperties> getSequences(DatabaseMetaData metaData, SequencePropertiesFactory factory) throws SQLException
	{
 	try (ResultSet resultSet = metaData.getTables(Strings.EMPTY, null, Strings.ANY, new String[] { formats.sequenceTableType() }))
		{
			List<SequenceProperties> sequences = new LinkedList<>();

			while (resultSet.next())
			{
				sequences.add(factory.createSequenceProperties(resultSet.getString("TABLE_SCHEM"), resultSet.getString("TABLE_NAME"), 1));
			}

			return sequences;
		}
	}

	private static String getCatalog(DatabaseMetaData metaData) throws SQLException
	{
		String catalog = metaData.getConnection().getCatalog();

		return (catalog != null) ? catalog : Strings.EMPTY;
	}

}
