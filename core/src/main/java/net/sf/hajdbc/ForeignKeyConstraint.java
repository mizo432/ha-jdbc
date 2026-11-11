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

import java.util.List;

import org.jspecify.annotations.NonNull;

/**
 * Represents a foreign key constraint on a table.
 * 
 * @author  Paul Ferraro
 * @since   1.1
 */
public interface ForeignKeyConstraint extends Constraint<ForeignKeyConstraint>
{
	/**
	 * Returns the foreign table of this foreign key.
	 * @return foreign table name
	 */
	@NonNull QualifiedName getForeignTable();
	
	/**
	 * Returns the foreign columns of this foreign key.
	 * @return list of foreign column names
	 */
	@NonNull List<String> getForeignColumnList();
	
	/**
	 * Returns the delete rule.
	 * @return delete rule
	 */
	int getDeleteRule();

	/**
	 * Returns the update rule.
	 * @return update rule
	 */
	int getUpdateRule();

	/**
	 * Returns the deferrability.
	 * @return deferrability
	 */
	int getDeferrability();
}
