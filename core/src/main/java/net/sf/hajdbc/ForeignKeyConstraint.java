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
 * テーブルの外部キー制約を表します
 * 
 * @author  Paul Ferraro
 * @since   1.1
 */
public interface ForeignKeyConstraint extends Constraint<ForeignKeyConstraint>
{
	/**
	 * この外部キーの外部テーブルを返します
	 * @return 外部テーブル名
	 */
	@NonNull QualifiedName getForeignTable();
	
	/**
	 * この外部キーの外部列を返します
	 * @return 外部列名のリスト
	 */
	@NonNull List<String> getForeignColumnList();
	
	/**
	 * 削除ルールを返します
	 * @return 削除ルール
	 */
	int getDeleteRule();

	/**
	 * 更新ルールを返します
	 * @return 更新ルール
	 */
	int getUpdateRule();

	/**
	 * 遅延可能性を返します
	 * @return 遅延可能性
	 */
	int getDeferrability();
}
