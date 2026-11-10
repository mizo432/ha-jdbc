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
package net.sf.hajdbc.codec.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import net.sf.hajdbc.codec.Codec;

/**
 * 一般的な暗号化コーデックです。
 * AES/CBC/PKCS5Paddingモードを使用し、各暗号化でランダムなIV（初期化ベクトル）を生成します。
 * 暗号文は Base64エンコードされ、IVが先頭に付加されます。
 * @author Paul Ferraro
 */
public class CipherCodec implements Codec
{
	/** AESブロックサイズ (128ビット = 16バイト) */
	private static final int AES_BLOCK_SIZE = 16;

	private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

	private final Key key;

	public CipherCodec(Key key)
	{
		this.key = key;
	}

	public Key getKey()
	{
		return this.key;
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.codec.Codec#decode(java.lang.String)
	 */
	@Override
	public String decode(String value) throws SQLException
	{
		try
		{
			byte[] decoded = Base64.getDecoder().decode(value);

			// IVは暗号文の先頭16バイトに格納されている
			if (decoded.length < AES_BLOCK_SIZE)
			{
				throw new SQLException("Invalid encrypted data: too short");
			}

			byte[] iv = new byte[AES_BLOCK_SIZE];
			System.arraycopy(decoded, 0, iv, 0, AES_BLOCK_SIZE);

			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(iv));

			byte[] plaintext = cipher.doFinal(decoded, AES_BLOCK_SIZE, decoded.length - AES_BLOCK_SIZE);

			return new String(plaintext, StandardCharsets.UTF_8);
		}
		catch (GeneralSecurityException e)
		{
			throw new SQLException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.codec.Codec#encode(java.lang.String)
	 */
	@Override
	public String encode(String value) throws SQLException
	{
		try
		{
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);

			// ランダムなIVを生成
			byte[] iv = new byte[AES_BLOCK_SIZE];
			new SecureRandom().nextBytes(iv);

			cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(iv));

			byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

			// IVと暗号文を結合してBase64エンコード
			byte[] combined = new byte[iv.length + ciphertext.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

			return Base64.getEncoder().encodeToString(combined);
		}
		catch (GeneralSecurityException e)
		{
			throw new SQLException(e);
		}
	}
}
