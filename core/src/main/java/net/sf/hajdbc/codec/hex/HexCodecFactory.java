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
package net.sf.hajdbc.codec.hex;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;

import net.sf.hajdbc.codec.AbstractCodec;
import net.sf.hajdbc.codec.Codec;
import net.sf.hajdbc.util.Strings;

/**
 * 16進数エンコーディング/デコーディングを使用するコーデックです。
 * @author Paul Ferraro
 */
public class HexCodecFactory extends AbstractCodec
{
	private static final long serialVersionUID = 5273729775503057299L;

	@Override
	public String getId()
	{
		return "16";
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.codec.Codec#decode(java.lang.String)
	 */
	@Override
	public String decode(String value)
	{
		byte[] decodedBytes = new byte[value.length() / 2];
		for (int i = 0; i < value.length(); i += 2) {
			decodedBytes[i / 2] = (byte) Integer.parseInt(
				value.substring(i, i + 2), 16);
		}
		return new String(decodedBytes, StandardCharsets.UTF_8);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.codec.Codec#encode(java.lang.String)
	 */
	@Override
	public String encode(String value)
	{
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		StringBuilder builder = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			builder.append(String.format(Locale.ROOT, "%02X", b));
		}
		return builder.toString();
	}
	
	public static void main(String... args)
	{
		if (args.length != 2)
		{
			System.err.println(String.format("Usage:%s\tjava %s <cluster-id> <password-to-encrypt>", Strings.NEW_LINE, HexCodecFactory.class.getName()));
			System.exit(1);
			return;
		}
		
		String clusterId = args[0];
		String value = args[1];
		
		try
		{
			Codec codec = new HexCodecFactory().createCodec(clusterId);

			System.out.println(codec.encode(value));
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
		}
	}
}
