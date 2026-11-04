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

import java.sql.SQLException;
import java.util.Collection;

/**
 * テーブルプロパティのインターフェース
 * @author Paul Ferraro
 */
public interface TableProperties extends Named<QualifiedName, TableProperties>
{
	/**
	 * 列名のコレクションを返します
	 * @return 列名のコレクション
	 * @throws SQLException エラーが発生した場合
	 */
	Collection<String> getColumns() throws SQLException;
	
	/**
	 * 指定された列のプロパティを返します
	 * @param column 列名
	 * @return 列プロパティ
	 * @throws SQLException エラーが発生した場合
	 */
	ColumnProperties getColumnProperties(String column) throws SQLException;
	
	/**
	 * 主キー制約を返します
	 * @return 主キー制約
	 * @throws SQLException エラーが発生した場合
	 */
	UniqueConstraint getPrimaryKey() throws SQLException;
	
	/**
	 * 外部キー制約のコレクションを返します
	 * @return 外部キー制約のコレクション
	 * @throws SQLException エラーが発生した場合
	 */
	Collection<ForeignKeyConstraint> getForeignKeyConstraints() throws SQLException;
	
	/**
	 * 一意制約のコレクションを返します
	 * @return 一意制約のコレクション
	 * @throws SQLException エラーが発生した場合
	 */
	Collection<UniqueConstraint> getUniqueConstraints() throws SQLException;
	
	/**
	 * アイデンティティ列名のコレクションを返します
	 * @return アイデンティティ列名のコレクション
	 * @throws SQLException エラーが発生した場合
	 */
	Collection<String> getIdentityColumns() throws SQLException;
}
