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
 * データベースクラスタファクトリのインターフェース
 * @author Paul Ferraro
 * @param <Z> 接続ソース（例：Driver、DataSourceなど）
 * @param <D> データベースの実装
 */
public interface DatabaseClusterFactory<Z, D extends Database<Z>>
{
	/**
	 * データベースクラスタを作成します
	 * @param id クラスタの識別子
	 * @param factory 設定ファクトリ
	 * @param builder 設定ビルダー
	 * @return データベースクラスタ
	 * @throws SQLException クラスタの作成に失敗した場合
	 */
	<B extends DatabaseBuilder<Z, D>> DatabaseCluster<Z, D> createDatabaseCluster(String id, DatabaseClusterConfigurationFactory<Z, D> factory, DatabaseClusterConfigurationBuilder<Z, D, B> builder) throws SQLException;
}
