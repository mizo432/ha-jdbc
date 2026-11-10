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

import java.security.Key;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import net.sf.hajdbc.codec.Codec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for CipherCodec AES encryption/decryption.
 * @author Paul Ferraro
 */
public class CipherCodecTest
{
	private static final String ALGORITHM = "AES";
	private static final int KEY_SIZE = 128;
	private static final String PASSWORD = "password";
	
	private Codec codec;
	private SecretKey key;
	
	@BeforeEach
	public void before() throws Exception
	{
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		keyGenerator.init(KEY_SIZE, new SecureRandom(new byte[]{42}));
		this.key = keyGenerator.generateKey();
		
		this.codec = new CipherCodec(key);
	}
	
	@Test
	public void encode() throws SQLException
	{
		String encoded = this.codec.encode(PASSWORD);
		Assertions.assertNotNull(encoded);
		Assertions.assertNotEquals(PASSWORD, encoded);
		// Verify it's valid Base64
		Base64.getDecoder().decode(encoded);
	}
	
	@Test
	public void decode() throws SQLException
	{
		String encoded = this.codec.encode(PASSWORD);
		Assertions.assertEquals(PASSWORD, this.codec.decode(encoded));
	}
	
	@Test
	public void encodeDecodeRoundTrip() throws SQLException
	{
		String original = "my-secret-password";
		String encoded = this.codec.encode(original);
		String decoded = this.codec.decode(encoded);
		Assertions.assertEquals(original, decoded);
	}
	
	@Test
	public void differentCiphertextForSamePlaintext() throws SQLException
	{
		// Each encryption should produce different ciphertext due to random IV
		String encoded1 = this.codec.encode(PASSWORD);
		String encoded2 = this.codec.encode(PASSWORD);
		Assertions.assertNotEquals(encoded1, encoded2);
		// But both should decode to the same value
		Assertions.assertEquals(PASSWORD, this.codec.decode(encoded1));
		Assertions.assertEquals(PASSWORD, this.codec.decode(encoded2));
	}
	
	@Test
	public void decodeShortInputThrowsException() throws SQLException
	{
		// Base64 of 3 bytes — shorter than the 16-byte IV
		String shortInput = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3});
		try
		{
			this.codec.decode(shortInput);
			Assertions.fail("Expected SQLException for short input");
		}
		catch (SQLException e)
		{
			Assertions.assertTrue(e.getMessage().contains("too short"));
		}
	}
}
