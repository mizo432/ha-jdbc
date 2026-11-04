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

/**
 * データベースクラスタ設定の変更を監視するリスナーのインターフェース
 * @author Paul Ferraro
 * @param <Z> 接続ソース（例：Driver、DataSourceなど）
 * @param <D> データベースの実装
 */
public interface DatabaseClusterConfigurationListener<Z, D extends Database<Z>>
{
	/**
	 * データベースが追加されたときに呼び出されます
	 * @param database 追加されたデータベース
	 * @param configuration データベースクラスタ設定
	 */
	void added(D database, DatabaseClusterConfiguration<Z, D> configuration);
	
	/**
	 * データベースが削除されたときに呼び出されます
	 * @param database 削除されたデータベース
	 * @param configuration データベースクラスタ設定
	 */
	void removed(D database, DatabaseClusterConfiguration<Z, D> configuration);
}
