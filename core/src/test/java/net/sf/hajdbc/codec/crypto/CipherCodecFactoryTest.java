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

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.SQLException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import net.sf.hajdbc.codec.Codec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Paul Ferraro
 */
public class CipherCodecFactoryTest
{
	private static final String ALGORITHM = "AES";
	private static final int KEY_SIZE = 128;
	private static final String STORE_PASSWORD = "password_store";
	private static final String KEY_PASSWORD = "password_key";
	
	private SecretKey key;
	
	@Before
	public void before() throws Exception
	{
		File file = File.createTempFile("ha-jdbc", "keystore");
		
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		keyGenerator.init(KEY_SIZE, new SecureRandom(new byte[]{42}));
		this.key = keyGenerator.generateKey();
		KeyStore store = KeyStore.getInstance(CipherCodecFactory.Property.KEYSTORE_TYPE.defaultValue);
		store.load(null, null);
		store.setKeyEntry(CipherCodecFactory.Property.KEY_ALIAS.defaultValue, this.key, KEY_PASSWORD.toCharArray(), null);
		
		try (FileOutputStream out = new FileOutputStream(file))
		{
			store.store(out, STORE_PASSWORD.toCharArray());
		}
		
		System.setProperty(CipherCodecFactory.Property.KEYSTORE_FILE.name, file.getPath());
		System.setProperty(CipherCodecFactory.Property.KEYSTORE_PASSWORD.name, STORE_PASSWORD);
		System.setProperty(CipherCodecFactory.Property.KEY_PASSWORD.name, KEY_PASSWORD);
	}
	
	@Test
	public void test() throws SQLException
	{
		Codec codec = new CipherCodecFactory().createCodec("cluster");
		
		Assert.assertTrue(codec instanceof CipherCodec);
		Assert.assertEquals(this.key, ((CipherCodec) codec).getKey());
	}
}
