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

import java.util.ResourceBundle;
import java.util.regex.Pattern;

import net.sf.hajdbc.util.Strings;
import org.jspecify.annotations.NonNull;

/**
 * バージョン情報を表すクラス
 * @author Paul Ferraro
 */
public class Version
{
	/**
	 * 現在のバージョン
	 */
	public static final @NonNull Version CURRENT = new Version(ResourceBundle.getBundle(Version.class.getName()).getString("version"));
	
	private final @NonNull String version;
	private final int major;
	private final int minor;
	private final int revision;
	
	/**
	 * バージョン文字列を指定してコンストラクトします
	 * @param version バージョン文字列
	 */
	Version(@NonNull String version)
	{
		this.version = version;
		this.major = parse(0);
		this.minor = parse(1);
		this.revision = parse(2);
	}
	
	private int parse(int index)
	{
		return Integer.parseInt(this.version.split(Pattern.quote(Strings.DASH))[0].split(Pattern.quote(Strings.DOT))[index]);
	}
	
	/**
	 * メジャーバージョンを返します
	 * @return メジャーバージョン
	 */
	public int getMajor()
	{
		return this.major;
	}
	
	/**
	 * マイナーバージョンを返します
	 * @return マイナーバージョン
	 */
	public int getMinor()
	{
		return this.minor;
	}
	
	/**
	 * リビジョン番号を返します
	 * @return リビジョン番号
	 */
	public int getRevision()
	{
		return this.revision;
	}
	
	@Override
	public @NonNull String toString()
	{
		return this.version;
	}
}
