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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;

/**
 * シーケンスのサポートインターフェース
 * @author Paul Ferraro
 */
public interface SequenceSupport
{
	/**
	 * 指定されたSQLステートメントからシーケンス名を解析します
	 * @param sql SQLステートメント
	 * @return シーケンス名、またはSQLステートメントがシーケンスを参照しない場合またはこのダイアレクトがシーケンスをサポートしない場合はnull
	 * @throws SQLException エラーが発生した場合
	 * @since 2.0
	 */
	String parseSequence(String sql) throws SQLException;
	
	/**
	 * ダイアレクト固有のロジックを使用してシーケンスのコレクションを返します
	 * @param metaData データベースメタデータ
	 * @param factory シーケンスプロパティファクトリ
	 * @return シーケンスのコレクション
	 * @throws SQLException エラーが発生した場合
	 */
	Collection<SequenceProperties> getSequences(DatabaseMetaData metaData, SequencePropertiesFactory factory) throws SQLException;
	
	/**
	 * 指定されたシーケンスの次の値を取得するためのSQLステートメントを返します
	 * @param sequence シーケンスプロパティ
	 * @return SQLステートメント
	 * @throws SQLException エラーが発生した場合
	 * @since 2.0
	 */
	String getNextSequenceValueSQL(SequenceProperties sequence) throws SQLException;

	/**
	 * シーケンスの現在値をリセットするために使用するSQLステートメントを返します
	 * @param sequence シーケンスプロパティ
	 * @param value シーケンス値
	 * @return SQLステートメント
	 * @throws SQLException エラーが発生した場合
	 * @since 2.0
	 */
	String getAlterSequenceSQL(SequenceProperties sequence, long value) throws SQLException;

	/**
	 * シーケンスプロパティファクトリを作成します
	 * @param factory 修飾名前ファクトリ
	 * @return シーケンスプロパティファクトリ
	 */
	SequencePropertiesFactory createSequencePropertiesFactory(QualifiedNameFactory factory);
}
