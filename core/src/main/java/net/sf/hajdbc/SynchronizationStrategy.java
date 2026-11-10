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

import java.io.Serializable;
import java.sql.SQLException;

import net.sf.hajdbc.sync.SynchronizationContext;
import org.jspecify.annotations.NonNull;


/**
 * 同期戦略のインターフェース
 * @author  Paul Ferraro
 */
public interface SynchronizationStrategy extends Identifiable, Serializable
{
	/**
	 * 同期戦略を初期化します
	 * @param cluster データベースクラスタ
	 */
	<Z, D extends Database<Z>> void init(@NonNull DatabaseCluster<Z, D> cluster);
	
	/**
	 * 同期コンテキストで定義されたソースデータベースとターゲットデータベースを同期します
	 * @param context 同期コンテキスト
	 * @throws SQLException 同期に失敗した場合
	 */
	<Z, D extends Database<Z>> void synchronize(@NonNull SynchronizationContext<Z, D> context) throws SQLException;
	
	/**
	 * 同期戦略を破棄します
	 * @param cluster データベースクラスタ
	 */
	<Z, D extends Database<Z>> void destroy(@NonNull DatabaseCluster<Z, D> cluster);	
}
