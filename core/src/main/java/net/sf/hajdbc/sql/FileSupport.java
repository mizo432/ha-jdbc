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
package net.sf.hajdbc.sql;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;

/**
 * ストリームのデータをシリアライズするための一時ファイルサポートを提供します。
 * このオブジェクトによって作成されたファイルは{@link #close()}が呼び出されたときに削除されます。
 * 
 * @author  Paul Ferraro
 * @version $Revision: 1612 $
 * @since   1.0
 */
public interface FileSupport<E extends Throwable> extends Closeable
{
	/**
	 * 指定されたバイナリ入力ストリームからファイルを作成します。
	 * @param inputStream データのバイナリストリーム
	 * @return 一時ファイル
	 * @throws SQLException IOエラーが発生した場合
	 */
	File createFile(InputStream inputStream) throws E;
	
	/**
	 * 指定された文字入力ストリームからファイルを作成します。
	 * @param reader データの文字ストリーム
	 * @return 一時ファイル
	 * @throws SQLException IOエラーが発生した場合
	 */
	File createFile(Reader reader) throws E;

	/**
	 * 指定されたファイルのリーダーを返します。
	 * @param file 一時ファイル
	 * @return リーダー
	 * @throws SQLException IOエラーが発生した場合
	 */
	Reader getReader(File file) throws E;
	
	/**
	 * 指定されたファイルの入力ストリームを返します。
	 * @param file 一時ファイル
	 * @return 入力ストリーム
	 * @throws SQLException IOエラーが発生した場合
	 */
	InputStream getInputStream(File file) throws E;
}
