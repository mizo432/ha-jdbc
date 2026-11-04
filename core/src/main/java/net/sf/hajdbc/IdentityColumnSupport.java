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


/**
 * アイデンティティ列のサポートインターフェース
 * @author Paul Ferraro
 */
public interface IdentityColumnSupport
{
	/**
	 * アイデンティティ列を含む可能性のあるINSERT SQLステートメントからテーブル名を解析します
	 * @param sql SQLステートメント
	 * @return テーブル名、またはSQLステートメントがINSERTでない場合またはこのダイアレクトがアイデンティティ列をサポートしない場合はnull
	 * @throws SQLException エラーが発生した場合
	 * @since 2.0
	 */
	String parseInsertTable(String sql) throws SQLException;
	
	/**
	 * アイデンティティ列の現在値をリセットするために使用するSQLステートメントを返します
	 * @param table テーブルプロパティ
	 * @param column 列プロパティ
	 * @param value 値
	 * @return SQLステートメント
	 * @throws SQLException エラーが発生した場合
	 * @since 2.0.2
	 */
	String getAlterIdentityColumnSQL(TableProperties table, ColumnProperties column, long value) throws SQLException;
}
