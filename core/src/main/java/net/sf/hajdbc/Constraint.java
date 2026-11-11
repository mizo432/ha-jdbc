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

public interface Constraint<T extends Constraint<T>> extends Named<String, T>
{
	/**
	 * Returns the table of this constraint.
	 * @return table name
	 */
	@NonNull QualifiedName getTable();
	
	/**
	 * Returns the list of columns included in this unique constraint.
	 * @return list of column names
	 */
	@NonNull List<String> getColumnList();
}
