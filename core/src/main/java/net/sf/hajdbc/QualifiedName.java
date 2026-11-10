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

/**
 * 修飾名前のインターフェース
 * @author Paul Ferraro
 */
public interface QualifiedName extends Named<String, QualifiedName>
{
	/**
	 * スキーマ名を返します
	 * @return スキーマ名
	 */
	@NonNull String getSchema();
	
	/**
	 * DDLで使用される名前を返します
	 * @return DDL名前
	 */
	@NonNull String getDDLName();
	
	/**
	 * DMLで使用される名前を返します
	 * @return DML名前
	 */
	@NonNull String getDMLName();
}
