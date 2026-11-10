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

import java.util.Locale;

import org.jspecify.annotations.NonNull;

/**
 * データベースの所在地を示す列挙型
 * @author Paul Ferraro
 */
public enum Locality
{
	/**
	 * リモート
	 */
	REMOTE(false),
	/**
	 * ローカル
	 */
	LOCAL(true),
	/**
	 * エンベデッド
	 */
	EMBEDDED(true),
	;

	private final boolean local;

	/**
	 * ローカルかどうかを指定してコンストラクトします
	 * @param local ローカルの場合はtrue
	 */
	private Locality(boolean local)
	{
		this.local = local;
	}

	/**
	 * ローカルかどうかを返します
	 * @return ローカルの場合はtrue
	 */
	public boolean isLocal()
	{
		return this.local;
	}

	@Override
	public @NonNull String toString()
	{
		return this.name().toLowerCase(Locale.US);
	}
}
