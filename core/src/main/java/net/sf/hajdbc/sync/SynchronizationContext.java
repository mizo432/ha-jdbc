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
package net.sf.hajdbc.sync;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseProperties;
import net.sf.hajdbc.codec.Decoder;
import net.sf.hajdbc.dialect.Dialect;


/**
 * @author Paul Ferraro
 * @param <D> Driver or DataSource
 * @since 2.0
 */
public interface SynchronizationContext<Z, D extends Database<Z>> extends AutoCloseable
{
	/**
	 * 指定されたデータベースへの接続を返します。
	 * @param database 接続するデータベース
	 * @return データベース接続
	 * @throws SQLException 接続を取得できなかった場合
	 */
	Connection getConnection(D database) throws SQLException;
	
	/**
	 * 同期するソースデータベースを返します。
	 * @return データベース
	 */
	D getSourceDatabase();
	
	/**
	 * 同期するターゲットデータベースを返します。
	 * @return データベース
	 */
	D getTargetDatabase();
	
	/**
	 * 同期が開始された時点でのクラスタのアクティブなデータベースのスナップショットを返します。
	 * @return データベースのコレクション
	 */
	Set<D> getActiveDatabaseSet();
	
	/**
	 * ソースデータベースのデータベースメタデータのキャッシュを返します。
	 * @return データベースメタデータのキャッシュ
	 */
	DatabaseProperties getSourceDatabaseProperties();
	
	/**
	 * ターゲットデータベースのデータベースメタデータのキャッシュを返します。
	 * @return データベースメタデータのキャッシュ
	 */
	DatabaseProperties getTargetDatabaseProperties();
	
	/**
	 * クラスタのDialectを返します。
	 * @return Dialect
	 */
	Dialect getDialect();
	
	/**
	 * タスクを非同期に実行するためのエグゼキュータサービスです。
	 * @return エグゼキュータサービス
	 */
	ExecutorService getExecutor();
	
	SynchronizationSupport getSynchronizationSupport();
	
	Decoder getDecoder();
	
	/**
	 * Closes any open database connections and shuts down the executor service. 
	 */
	@Override
	void close();
}
