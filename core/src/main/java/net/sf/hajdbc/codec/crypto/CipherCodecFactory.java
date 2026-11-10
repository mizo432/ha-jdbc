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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.sql.SQLException;
import java.text.MessageFormat;

import net.sf.hajdbc.codec.Codec;
import net.sf.hajdbc.codec.CodecFactory;
import net.sf.hajdbc.util.Strings;
import net.sf.hajdbc.util.SystemProperties;

/**
 * キーストアに保存された共通鍵を使用して設定ファイルのパスワードを復号化するために使用します。
 * 設定ファイルで使用するためのBase64エンコードされた暗号化パスワードを生成するには、次のコマンドを使用します:<br/>
 * <p><code>java -classpath ha-jdbc.jar net.sf.hajdbc.codec.crypto.CipherCodecFactory [password]</code></p>
 * 次のシステムプロパティを使用して、鍵および/またはキーストアのプロパティをカスタマイズできます:
 * <table>
 * 	<tr>
 * 		<th>プロパティ</th>
 * 		<th>デフォルト</th>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.keystore.file</td>
 * 		<td>~/.keystore</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.keystore.type</td>
 * 		<td>jceks</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.keystore.password</td>
 * 		<td><em>なし</em></td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.keystore.password.file</td>
 * 		<td><em>なし</em></td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.key.alias</td>
 * 		<td>ha-jdbc</td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.key.password</td>
 * 		<td><em>必須</em><td>
 * 	</tr>
 * 	<tr>
 * 		<td>ha-jdbc.key.password.file</td>
 * 		<td><em>なし</em></td>
 * 	</tr>
 * </table>
 * パスワードプロパティ（<code>ha-jdbc.keystore.password</code>および<code>ha-jdbc.key.password</code>）は、
 * 以下の優先順位で解決されます（高い順）:
 * <ol>
 * 	<li>ファイルベース: <code>ha-jdbc.keystore.password.file</code>システムプロパティで指定されたファイルの内容</li>
 * 	<li>環境変数: <code>HA_JDBC_KEYSTORE_PASSWORD</code>環境変数</li>
 * 	<li>システムプロパティ: <code>ha-jdbc.keystore.password</code></li>
 * </ol>
 * ファイルベースと環境変数の方法は、コマンドライン履歴やプロセスリストにパスワードが露出するリスクを回避するために推奨されます。
 * @author Paul Ferraro
 */
public class CipherCodecFactory implements CodecFactory
{
	private static final long serialVersionUID = -4409167180573651279L;
	
	public static final String DEFAULT_KEYSTORE_FILE = String.format("%s/.keystore", SystemProperties.getSystemProperty("user.home"));
	public static final String DEFAULT_KEY_ALIAS = "ha-jdbc";

	enum Property
	{
		KEYSTORE_FILE("keystore.file", DEFAULT_KEYSTORE_FILE),
		KEYSTORE_TYPE("keystore.type", "jceks"),
		KEYSTORE_PASSWORD("keystore.password", null),
		KEYSTORE_PASSWORD_FILE("keystore.password.file", null),
		KEY_ALIAS("key.alias", DEFAULT_KEY_ALIAS),
		KEY_PASSWORD("key.password", null),
		KEY_PASSWORD_FILE("key.password.file", null);
		
		final String nameFormat;
		final String name;
		final String defaultValue;
		
		private Property(String name, String defaultValue)
		{
			this.nameFormat = "ha-jdbc.{0}." + name;
			this.name = "ha-jdbc." + name;
			this.defaultValue = defaultValue;
		}
	}
	
	/**
	 * パスワード関連のプロパティに対応する環境変数名を返します。
	 * 例: keystore.password → HA_JDBC_KEYSTORE_PASSWORD
	 */
	private static String getEnvironmentVariableName(Property property)
	{
		// "keystore.password" → "HA_JDBC_KEYSTORE_PASSWORD" に変換
		String base = property.name.replace("ha-jdbc.", "").replace(".", "_").toUpperCase();
		return "HA_JDBC_" + base;
	}
	
	private static String getProperty(String id, Property property)
	{
		String value = SystemProperties.getSystemProperty(MessageFormat.format(property.nameFormat, id));
		
		if (value != null) return value;
		
		String pattern = SystemProperties.getSystemProperty(property.name, property.defaultValue);
		
		if (pattern == null) return null;
		
		return MessageFormat.format(pattern, id);
	}
	
	/**
	 * センシティブなプロパティ（パスワードなど）を取得します。
	 * 優先順位は以下の通りです（高い順）:
	 * <ol>
	 * 	<li>ファイルベース: .file サフィックス付きのプロパティで指定されたファイルの内容</li>
	 * 	<li>環境変数: HA_JDBC_ 接頭辞付きの環境変数</li>
	 * 	<li>システムプロパティ: 通常のプロパティ名</li>
	 * </ol>
	 */
	private static String getPropertySecure(String id, Property passwordProperty, Property fileProperty)
	{
		// 1. ファイルベースのパスワードを試す
		String filePath = getProperty(id, fileProperty);
		if (filePath != null && !filePath.isEmpty())
		{
			String filePassword = readPasswordFromFile(filePath);
			if (filePassword != null)
			{
				return filePassword;
			}
		}
		
		// 2. 環境変数を試す
		String envName = getEnvironmentVariableName(passwordProperty);
		String envValue = SystemProperties.getEnvironmentVariable(envName);
		if (envValue != null && !envValue.isEmpty())
		{
			return envValue;
		}
		
		// 3. システムプロパティを試す（既存の動作）
		return getProperty(id, passwordProperty);
	}
	
	/**
	 * ファイルからパスワードを読み取ります。
	 * ファイルの内容をトリムして返します。
	 * 空または空白のみのコンテンツの場合は null を返します。
	 */
	private static String readPasswordFromFile(String filePath)
	{
		try
		{
			File file = new File(filePath);
			if (!file.exists() || !file.isFile())
			{
				return null;
			}
			String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
			return content.isEmpty() ? null : content;
		}
		catch (IOException e)
		{
			return null;
		}
	}
	
	@Override
	public String getId()
	{
		return "?";
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.codec.CodecFactory#createCodec(java.lang.String)
	 */
	@Override
	public Codec createCodec(String clusterId) throws SQLException
	{
		String type = getProperty(clusterId, Property.KEYSTORE_TYPE);
		File file = new File(getProperty(clusterId, Property.KEYSTORE_FILE));
		String password = getPropertySecure(clusterId, Property.KEYSTORE_PASSWORD, Property.KEYSTORE_PASSWORD_FILE);

		String keyAlias = getProperty(clusterId, Property.KEY_ALIAS);
		String keyPassword = getPropertySecure(clusterId, Property.KEY_PASSWORD, Property.KEY_PASSWORD_FILE);
		
		try
		{
			KeyStore store = KeyStore.getInstance(type);
			
			try (InputStream input = new FileInputStream(file))
			{
				store.load(input, (password != null) ? password.toCharArray() : null);
			}
			
			return new CipherCodec(store.getKey(keyAlias, (keyPassword != null) ? keyPassword.toCharArray() : null));
		}
		catch (GeneralSecurityException e)
		{
			throw new SQLException(e);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
	}
	
	public static void main(String... args)
	{
		if (args.length != 2)
		{
			System.err.println(String.format("Usage:%s\tjava %s <cluster-id> <password-to-encrypt>", Strings.NEW_LINE, CipherCodecFactory.class.getName()));
			System.exit(1);
			return;
		}
		
		String clusterId = args[0];
		String value = args[1];
		
		try
		{
			Codec codec = new CipherCodecFactory().createCodec(clusterId);

			System.out.println(codec.encode(value));
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
		}
	}
}
