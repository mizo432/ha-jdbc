/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2014  Paul Ferraro
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

import net.sf.hajdbc.codec.Decoder;

/**
 * 認証情報のインターフェース
 * @author Paul Ferraro
 */
public interface Credentials
{
	/**
	 * ユーザー名を返します
	 * @return ユーザー名
	 */
	String getUser();
	
	/**
	 * エンコードされたパスワードを返します
	 * @return エンコードされたパスワード
	 */
	String getEncodedPassword();
	
	/**
	 * パスワードをデコードします
	 * @param decoder デコーダー
	 * @return デコードされたパスワード
	 * @throws SQLException パスワードのデコードに失敗した場合
	 */
	String decodePassword(Decoder decoder) throws SQLException;
}
