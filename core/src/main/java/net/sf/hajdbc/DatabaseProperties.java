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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * データベースプロパティのインターフェース
 * @author Paul Ferraro
 */
public interface DatabaseProperties
{
	/**
	 * テーブルプロパティのコレクションを返します
	 * @return テーブルプロパティのコレクション
	 * @throws SQLException エラーが発生した場合
	 */
	@NonNull Collection<TableProperties> getTables() throws SQLException;

	/**
	 * SELECT FOR UPDATEをサポートしているかどうかを返します
	 * @return サポートしている場合はtrue
	 * @throws SQLException エラーが発生した場合
	 */
	boolean supportsSelectForUpdate() throws SQLException;
	
	/**
	 * ロケータがコピーを更新するかどうかを返します
	 * @return 更新する場合はtrue
	 * @throws SQLException エラーが発生した場合
	 */
	boolean locatorsUpdateCopy() throws SQLException;
	
	/**
	 * 指定されたテーブル名のプロパティを検索します
	 * @param table テーブル名
	 * @return テーブルプロパティ
	 * @throws SQLException エラーが発生した場合
	 */
	@Nullable TableProperties findTable(@NonNull String table) throws SQLException;
	
	/**
	 * シーケンスプロパティのコレクションを返します
	 * @return シーケンスプロパティのコレクション
	 * @throws SQLException エラーが発生した場合
	 */
	@NonNull Collection<SequenceProperties> getSequences() throws SQLException;
	
	/**
	 * 指定されたシーケンス名のプロパティを検索します
	 * @param sequence シーケンス名
	 * @return シーケンスプロパティ
	 * @throws SQLException エラーが発生した場合
	 */
	@Nullable SequenceProperties findSequence(@NonNull String sequence) throws SQLException;
	
	/**
	 * 指定された精度と型に一致する型名を検索します
	 * @param precision 精度
	 * @param types 型のリスト
	 * @return 型名
	 * @throws SQLException エラーが発生した場合
	 */
	@Nullable String findType(int precision, int... types) throws SQLException;
}
