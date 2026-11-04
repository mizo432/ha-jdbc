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
package net.sf.hajdbc.balancer;

import java.util.Set;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.invocation.Invoker;

/**
 * アクティブなデータベースのセットと、次のバランシングターゲットを決定する戦略です。
 * 
 * @author Paul Ferraro
 *
 * @param <Z> データベース接続ソース
 * @param <D> データベース記述子
 */
public interface Balancer<Z, D extends Database<Z>> extends Set<D>
{
	/**
	 * プライマリデータベースを返します。
	 * このメソッドが返すデータベースは、すべてのノード間で一貫している必要があります。
	 * @return プライマリデータベース
	 */
	D primary();
	
	/**
	 * プライマリ以外のデータベースです。
	 * @return イテレート可能なデータベースのコレクション
	 */
	Iterable<D> backups();
	
	/**
	 * このバランサーから次のデータベースを返します
	 * @return 次のデータベース
	 */
	D next();

	/**
	 * 指定されたデータベースに対して指定されたオブジェクトで指定されたインボーカーを実行し、バランサーに通知する可能性があります。
	 * @param <T> インボーカーのターゲットオブジェクト型
	 * @param <R> インボーカーの戻り値型
	 * @param <E> インボーカーの例外型
	 * @param invoker インボーカー
	 * @param database データベース
	 * @param object ターゲットオブジェクト
	 * @return 実行結果
	 * @throws E 例外
	 */
	<T, R, E extends Exception> R invoke(Invoker<Z, D, T, R, E> invoker, D database, T object) throws E;
}
