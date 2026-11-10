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

import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.durability.Durability;
import org.jspecify.annotations.NonNull;


/**
 * 例外ファクトリのインターフェース
 * @author Paul Ferraro
 * @param <E> 例外の型
 */
public interface ExceptionFactory<E extends Exception> extends Serializable
{
	/**
	 * 対象の例外クラスを返します
	 * @return 例外クラス
	 */
	@NonNull Class<E> getTargetClass();
	
	/**
	 * 指定されたスロー可能オブジェクトから例外を作成します
	 * @param e スロー可能オブジェクト
	 * @return 作成された例外
	 */
	@NonNull E createException(@NonNull Throwable e);
	
	/**
	 * 指定されたメッセージから例外を作成します
	 * @param message メッセージ
	 * @return 作成された例外
	 */
	@NonNull E createException(@NonNull String message);
	
	/**
	 * 2つの例外が等しいかどうかを比較します
	 * @param exception1 最初の例外
	 * @param exception2 2番目の例外
	 * @return 等しい場合はtrue
	 */
	boolean equals(@NonNull E exception1, @NonNull E exception2);
	
	/**
	 * 例外が失敗を示しているかどうかを判定します
	 * @param exception 例外
	 * @param dialect ダイアレクト
	 * @return 失敗を示す場合はtrue
	 */
	boolean indicatesFailure(@NonNull E exception, @NonNull Dialect dialect);
	
	/**
	 * 例外のタイプを返します
	 * @return 例外タイプ
	 */
	@NonNull ExceptionType getType();
	
	/**
	 * 例外がヒューリスティックに正しいフェーズに属しているかどうかを判定します
	 * @param exception 例外
	 * @param phase 永続性フェーズ
	 * @return 正しい場合はtrue
	 */
	boolean correctHeuristic(@NonNull E exception, Durability.@NonNull Phase phase);
}
