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

import java.sql.SQLException;

import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.UniqueConstraint;
import org.jspecify.annotations.NonNull;

/**
 * Generates database vendor-specific SQL statements for DDL operations.
 * This interface isolates SQL generation concerns from other dialect responsibilities,
 * following the Interface Segregation Principle.
 *
 * @author Paul Ferraro
 * @since 3.2
 */
public interface DialectSQLGenerator
{
	/**
	 * Returns the SQL statement used to truncate a table.
	 * @param properties table metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	@NonNull String getTruncateTableSQL(@NonNull TableProperties properties) throws SQLException;

	/**
	 * Returns the SQL statement used to create a foreign key constraint.
	 * @param constraint foreign key constraint metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	@NonNull String getCreateForeignKeyConstraintSQL(@NonNull ForeignKeyConstraint constraint) throws SQLException;

	/**
	 * Returns the SQL statement used to drop a foreign key constraint.
	 * @param constraint foreign key constraint metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	@NonNull String getDropForeignKeyConstraintSQL(@NonNull ForeignKeyConstraint constraint) throws SQLException;

	/**
	 * Returns the SQL statement used to create a unique constraint.
	 * @param constraint unique constraint metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	@NonNull String getCreateUniqueConstraintSQL(@NonNull UniqueConstraint constraint) throws SQLException;

	/**
	 * Returns the SQL statement used to drop a unique constraint.
	 * @param constraint unique constraint metadata
	 * @return SQL statement
	 * @throws SQLException if an error occurs retrieving metadata
	 */
	@NonNull String getDropUniqueConstraintSQL(@NonNull UniqueConstraint constraint) throws SQLException;

	/**
	 * Returns the SQL statement used to create a schema.
	 * @param schema schema name
	 * @return SQL statement
	 */
	@NonNull String getCreateSchemaSQL(@NonNull String schema);

	/**
	 * Returns the SQL statement used to drop a schema.
	 * @param schema schema name
	 * @return SQL statement
	 */
	@NonNull String getDropSchemaSQL(@NonNull String schema);
}

