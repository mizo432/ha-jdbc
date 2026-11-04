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

import net.sf.hajdbc.configuration.PropertiesBuilder;

/**
 * データベースビルダーのインターフェース
 * @author Paul Ferraro
 * @param <Z> 接続ソース（例：Driver、DataSourceなど）
 * @param <D> データベースの実装
 */
public interface DatabaseBuilder<Z, D extends Database<Z>> extends PropertiesBuilder<D>
{
	/**
	 * データベースIDの最大サイズ
	 */
	int ID_MAX_SIZE = 64;

	/**
	 * 接続ソースを設定します
	 * @param connectionSource 接続ソース
	 * @return このビルダー
	 */
	DatabaseBuilder<Z, D> connectionSource(Z connectionSource);

	/**
	 * データベースの場所を設定します
	 * @param location 場所
	 * @return このビルダー
	 */
	DatabaseBuilder<Z, D> location(String location);

	/**
	 * 認証情報を設定します
	 * @param user ユーザー名
	 * @param password パスワード
	 * @return このビルダー
	 */
	DatabaseBuilder<Z, D> credentials(String user, String password);

	/**
	 * 重みを設定します
	 * @param weight 重み
	 * @return このビルダー
	 */
	DatabaseBuilder<Z, D> weight(int weight);

	/**
	 * 所在地を設定します
	 * @param locality 所在地
	 * @return このビルダー
	 */
	DatabaseBuilder<Z, D> locality(Locality locality);
}
