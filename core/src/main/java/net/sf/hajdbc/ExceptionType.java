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

import net.sf.hajdbc.sql.SQLExceptionFactory;
import net.sf.hajdbc.sql.io.IOExceptionFactory;
import net.sf.hajdbc.sql.xa.XAExceptionFactory;
import net.sf.hajdbc.util.Matcher;
import org.jspecify.annotations.NonNull;

/**
 * 例外タイプの列挙型
 * @author Paul Ferraro
 */
@SuppressWarnings("rawtypes")
public enum ExceptionType implements Matcher<ExceptionFactory>
{
	/**
	 * SQL例外
	 */
	SQL(new SQLExceptionFactory()),
	/**
	 * XA例外
	 */
	XA(new XAExceptionFactory()),
	/**
	 * IO例外
	 */
	IO(new IOExceptionFactory())
	;
	private final @NonNull ExceptionFactory<? extends Exception> factory;
	
	/**
	 * 例外ファクトリを指定してコンストラクトします
	 * @param factory 例外ファクトリ
	 */
	private ExceptionType(@NonNull ExceptionFactory<? extends Exception> factory)
	{
		this.factory = factory;
	}
	
	/**
	 * 指定されたファクトリがこのタイプと一致するかどうかを判定します
	 * @param factory 例外ファクトリ
	 * @return 一致する場合はtrue
	 */
	@Override
	public boolean matches(@NonNull ExceptionFactory factory)
	{
		return this == factory.getType();
	}

	/**
	 * 例外ファクトリを返します
	 * @param <E> 例外の型
	 * @return 例外ファクトリ
	 */
	@SuppressWarnings("unchecked")
	public <E extends Exception> @NonNull ExceptionFactory<E> getExceptionFactory()
	{
		return (ExceptionFactory<E>) this.factory;
	}
	
	/**
	 * 指定された例外クラスに対応するタイプを返します
	 * @param exceptionClass 例外クラス
	 * @return 例外タイプ
	 * @throws IllegalArgumentException 対応するタイプが見つからない場合
	 */
	public static @NonNull ExceptionType valueOf(@NonNull Class<?> exceptionClass)
	{
		for (ExceptionType type: ExceptionType.values())
		{
			if (type.factory.getTargetClass().equals(exceptionClass))
			{
				return type;
			}
		}
		
		throw new IllegalArgumentException(exceptionClass.getName());
	}
}