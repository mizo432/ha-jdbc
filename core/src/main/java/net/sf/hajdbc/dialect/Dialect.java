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
package net.sf.hajdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAException;

import net.sf.hajdbc.ColumnProperties;
import net.sf.hajdbc.ColumnPropertiesFactory;
import net.sf.hajdbc.Database;
import net.sf.hajdbc.DumpRestoreSupport;
import net.sf.hajdbc.ForeignKeyConstraint;
import net.sf.hajdbc.ForeignKeyConstraintFactory;
import net.sf.hajdbc.IdentifierNormalizer;
import net.sf.hajdbc.IdentityColumnSupport;
import net.sf.hajdbc.QualifiedName;
import net.sf.hajdbc.QualifiedNameFactory;
import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.TableProperties;
import net.sf.hajdbc.TriggerSupport;
import net.sf.hajdbc.UniqueConstraint;
import net.sf.hajdbc.UniqueConstraintFactory;
import net.sf.hajdbc.codec.Decoder;


/**
 * データベースベンダー固有のSQL構文をカプセル化します。
 * 
 * @author  Paul Ferraro
 * @since   1.1
 */
public interface Dialect
{
	/**
	 * テーブルを切り捨てるために使用されるSQL文を返します。
	 * @param properties テーブルのメタデータ
	 * @return SQL文
	 * @throws SQLException メタデータの取得にエラーが発生した場合
	 */
	String getTruncateTableSQL(TableProperties properties) throws SQLException;
	
	/**
	 * 外部キー制約を作成するために使用されるSQL文を返します。
	 * @param constraint 外部キー制約のメタデータ
	 * @return SQL文
	 * @throws SQLException メタデータの取得にエラーが発生した場合
	 */
	String getCreateForeignKeyConstraintSQL(ForeignKeyConstraint constraint) throws SQLException;

	/**
	 * 外部キー制約を削除するために使用されるSQL文を返します。
	 * @param constraint 外部キー制約のメタデータ
	 * @return SQL文
	 * @throws SQLException メタデータの取得にエラーが発生した場合
	 */
	String getDropForeignKeyConstraintSQL(ForeignKeyConstraint constraint) throws SQLException;

	/**
	 * 一意制約を作成するために使用されるSQL文を返します。
	 * @param constraint 一意制約のメタデータ
	 * @return SQL文
	 * @throws SQLException メタデータの取得にエラーが発生した場合
	 */
	String getCreateUniqueConstraintSQL(UniqueConstraint constraint) throws SQLException;

	/**
	 * 一意制約を削除するために使用されるSQL文を返します。
	 * @param constraint 一意制約のメタデータ
	 * @return SQL文
	 * @throws SQLException メタデータの取得にエラーが発生した場合
	 */
	String getDropUniqueConstraintSQL(UniqueConstraint constraint) throws SQLException;
	
	/**
	 * 指定されたSQLがSELECT ... FOR UPDATE文かどうかを判定します
	 * @param sql SQL文
	 * @return SELECT ... FOR UPDATE文の場合はtrue、そうでない場合はfalse
	 * @throws SQLException メタデータの取得にエラーが発生した場合
	 */
	boolean isSelectForUpdate(String sql) throws SQLException;
	
	/**
	 * 指定されたスキーマとテーブルの指定された列のデータ型を返します。
	 * このメソッドはJDBCドライバーの型マッピングの癖を修正するために使用されます。
	 * @param properties テーブル列のメタデータ
	 * @return この列のJDBCデータ型
	 * @throws SQLException 
	 */
	int getColumnType(ColumnProperties properties) throws SQLException;
	
	/**
	 * スキーマの検索パスを返します
	 * @param metaData データベースメタデータ
	 * @return スキーマ名のリスト
	 * @throws SQLException 
	 * @since 2.0
	 */
	List<String> getDefaultSchemas(DatabaseMetaData metaData) throws SQLException;
	
	/**
	 * 非決定論的なCURRENT_DATE関数を決定論的な静的値に置き換えます。
	 * @param sql SQL文
	 * @param date 置き換えの日付
	 * @return 同等の決定論的なSQL文
	 * @throws SQLException 
	 * @since 2.0.2
	 */
	String evaluateCurrentDate(String sql, java.sql.Date date);
	
	/**
	 * 非決定論的なCURRENT_TIME関数を決定論的な静的値に置き換えます。
	 * @param sql SQL文
	 * @param time 置き換えの時刻
	 * @return 同等の決定論的なSQL文
	 * @throws SQLException 
	 * @since 2.0.2
	 */
	String evaluateCurrentTime(String sql, java.sql.Time time);
	
	/**
	 * 非決定論的なCURRENT_TIMESTAMP関数を決定論的な静的値に置き換えます。
	 * @param sql SQL文
	 * @param timestamp 置き換えのタイムスタンプ
	 * @return 同等の決定論的なSQL文
	 * @throws SQLException 
	 * @since 2.0.2
	 */
	String evaluateCurrentTimestamp(String sql, java.sql.Timestamp timestamp);
	
	/**
	 * 非決定論的なRAND()関数を決定論的な静的値に置き換えます。
	 * @param sql SQL文
	 * @return 同等の決定論的なSQL文
	 * @throws SQLException 
	 * @since 2.0.2
	 */
	String evaluateRand(String sql);
	
	/**
	 * 指定された例外が致命的なエラーを示しているかどうかを判定します。
	 * @param e 例外
	 * @return 例外が致命的なエラーを示す場合はtrue、そうでない場合はfalse
	 */
	boolean indicatesFailure(SQLException e);
	
	/**
	 * 指定された例外が致命的なエラーを示しているかどうかを判定します。
	 * @param e 例外
	 * @return 例外が致命的なエラーを示す場合はtrue、そうでない場合はfalse
	 */
	boolean indicatesFailure(XAException e);

	SequenceSupport getSequenceSupport();
	
	IdentityColumnSupport getIdentityColumnSupport();
	
	DumpRestoreSupport getDumpRestoreSupport();
	
	TriggerSupport getTriggerSupport();
	
	String getCreateSchemaSQL(String schema);
	
	String getDropSchemaSQL(String schema);
	
	/**
	 * このデータベースのすべてのテーブルをスキーマ別にマッピングして返します。
	 * @param metaData DatabaseMetaDataの実装
	 * @return スキーマ名からテーブル名のコレクションへのマップ
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	Collection<QualifiedName> getTables(DatabaseMetaData metaData, QualifiedNameFactory factory) throws SQLException;

	/**
	 * 指定されたテーブルの列を返します。
	 * @param metaData DatabaseMetaDataの実装
	 * @param table スキーマ修飾されたテーブル名
	 * @return 列名から列プロパティへのマップ
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	Map<String, ColumnProperties> getColumns(DatabaseMetaData metaData, QualifiedName table, ColumnPropertiesFactory factory) throws SQLException;

	/**
	 * 指定されたテーブルの主キーを返します。
	 * @param metaData DatabaseMetaDataの実装
	 * @param table スキーマ修飾されたテーブル名
	 * @return 一意制約
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	UniqueConstraint getPrimaryKey(DatabaseMetaData metaData, QualifiedName table, UniqueConstraintFactory factory) throws SQLException;

	/**
	 * 指定されたテーブルの外部キー制約を返します。
	 * @param metaData DatabaseMetaDataの実装
	 * @param table スキーマ修飾されたテーブル名
	 * @return 外部キー制約のコレクション
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	Collection<ForeignKeyConstraint> getForeignKeyConstraints(DatabaseMetaData metaData, QualifiedName table, ForeignKeyConstraintFactory factory) throws SQLException;

	/**
	 * 指定されたテーブルの一意制約を返します - テーブルの主キーは除外されます。
	 * @param metaData DatabaseMetaDataの実装
	 * @param table 修飾されたテーブル名
	 * @param primaryKey このテーブルの主キー
	 * @return 一意制約のコレクション
	 * @throws SQLException DatabaseMetaDataへのアクセスでエラーが発生した場合
	 */
	Collection<UniqueConstraint> getUniqueConstraints(DatabaseMetaData metaData, QualifiedName table, UniqueConstraint primaryKey, UniqueConstraintFactory factory) throws SQLException;
	
	/**
	 * 指定された列のコレクションからID列を特定します
	 * @param columns テーブルの列
	 * @return 列名のコレクション
	 * @throws SQLException
	 */
	Collection<String> getIdentityColumns(Collection<ColumnProperties> columns) throws SQLException;
	
	/**
	 * 標準JDBC型からネイティブ型へのマッピングを返します
	 * @param metaData データベースメタデータ
	 * @return JDBC型のマップ
	 * @throws SQLException
	 */
	Map<Integer, Map.Entry<String, Integer>> getTypes(DatabaseMetaData metaData) throws SQLException;

	IdentifierNormalizer createIdentifierNormalizer(DatabaseMetaData metaData) throws SQLException;
	
	QualifiedNameFactory createQualifiedNameFactory(DatabaseMetaData metaData, IdentifierNormalizer normalizer) throws SQLException;
	
	ColumnPropertiesFactory createColumnPropertiesFactory(IdentifierNormalizer normalizer);
	
	ForeignKeyConstraintFactory createForeignKeyConstraintFactory(QualifiedNameFactory factory);
	
	UniqueConstraintFactory createUniqueConstraintFactory(IdentifierNormalizer normalizer);
	
	boolean isValid(Connection connection) throws SQLException;
	
	<Z, D extends Database<Z>> ConnectionProperties getConnectionProperties(D database, Decoder decoder) throws SQLException;
}
