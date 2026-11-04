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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import net.sf.hajdbc.balancer.Balancer;
import net.sf.hajdbc.cache.DatabaseMetaDataCache;
import net.sf.hajdbc.codec.Decoder;
import net.sf.hajdbc.dialect.Dialect;
import net.sf.hajdbc.durability.Durability;
import net.sf.hajdbc.io.InputSinkStrategy;
import net.sf.hajdbc.lock.LockManager;
import net.sf.hajdbc.state.StateManager;
import net.sf.hajdbc.tx.TransactionIdentifierFactory;

/**
 * @author Paul Ferraro
 * @param <Z> either java.sql.Driver or javax.sql.DataSource
 * @param <D> database implementation
 */
public interface DatabaseCluster<Z, D extends Database<Z>> extends Lifecycle
{
	/**
	 * このクラスタの識別子を返します。
	 * @return 識別子
	 */
	String getId();
	
	/**
	 * Activates the specified database
	 * @param database a database descriptor
	 * @param manager a state manager
	 * @return true, if the database was activated, false it was already active
	 */
	boolean activate(D database, StateManager manager);
	
	/**
	 * Deactivates the specified database
	 * @param database a database descriptor
	 * @param manager a state manager
	 * @return true, if the database was deactivated, false it was already inactive
	 */
	boolean deactivate(D database, StateManager manager);
	
	/**
	 * 指定されたIDで識別されるデータベースを返します
	 * @param id データベース識別子
	 * @return データベース記述子
	 * @throws IllegalArgumentException 指定された識別子を持つデータベースが存在しない場合
	 */
	D getDatabase(String id);
	
	/**
	 * このデータベースクラスタで使用されるBalancerの実装を返します。
	 * @return <code>Balancer</code>の実装
	 */
	Balancer<Z, D> getBalancer();
	
	TransactionMode getTransactionMode();
	
	ExecutorService getExecutor();
	
	/**
	 * データベースベンダー固有の値を返すことができるDialectを返します。
	 * @return <code>Dialect</code>の実装
	 */
	Dialect getDialect();
	
	/**
	 * このデータベースクラスタ内の特定のオブジェクトに対して名前付きの読み取り/書き込みロックを取得できるLockManagerを返します。
	 * @return LockManagerの実装
	 */
	LockManager getLockManager();
	
	/**
	 * データベースクラスタの状態を永続化するためのStateManagerを返します。
	 * @return StateManagerの実装
	 */
	StateManager getStateManager();
	
	/**
	 * DatabaseMetaDataキャッシュを返します。
	 * @return <code>DatabaseMetaDataCache</code>の実装
	 */
	DatabaseMetaDataCache<Z, D> getDatabaseMetaDataCache();
	
	/**
	 * Indicates whether or not sequence detection is enabled for this cluster.
	 * @return true, if sequence detection is enabled, false otherwise.
	 */
	boolean isSequenceDetectionEnabled();
	
	/**
	 * Indicates whether or not identity column detection is enabled for this cluster.
	 * @return true, if identity column detection is enabled, false otherwise.
	 */
	boolean isIdentityColumnDetectionEnabled();
	
	/**
	 * Indicates whether or not non-deterministic CURRENT_DATE SQL functions will be evaluated to deterministic static values.
	 * @return true, if temporal SQL replacement is enabled, false otherwise.
	 */
	boolean isCurrentDateEvaluationEnabled();
	
	/**
	 * Indicates whether or not non-deterministic CURRENT_TIME functions will be evaluated to deterministic static values.
	 * @return true, if temporal SQL replacement is enabled, false otherwise.
	 */
	boolean isCurrentTimeEvaluationEnabled();
	
	/**
	 * Indicates whether or not non-deterministic CURRENT_TIMESTAMP functions will be evaluated to deterministic static values.
	 * @return true, if temporal SQL replacement is enabled, false otherwise.
	 */
	boolean isCurrentTimestampEvaluationEnabled();
	
	/**
	 * Indicates whether or not non-deterministic RAND() functions will be replaced by evaluated to static values.
	 * @return true, if temporal SQL replacement is enabled, false otherwise.
	 */
	boolean isRandEvaluationEnabled();
	
	/**
	 * Indicates whether or not this cluster is active, i.e. started, but not yet stopped.
	 * @return true, if this cluster is active, false otherwise.
	 */
	boolean isActive();

	void addListener(DatabaseClusterListener listener);
	
	void removeListener(DatabaseClusterListener listener);
	
	void addSynchronizationListener(SynchronizationListener listener);
	
	void removeSynchronizationListener(SynchronizationListener listener);
	
	void addConfigurationListener(DatabaseClusterConfigurationListener<Z, D> listener);
	
	void removeConfigurationListener(DatabaseClusterConfigurationListener<Z, D> listener);
	
	Durability<Z, D> getDurability();
	
	ThreadFactory getThreadFactory();
	
	Decoder getDecoder();
	
	TransactionIdentifierFactory<? extends Object> getTransactionIdentifierFactory();

	InputSinkStrategy<? extends Object> getInputSinkStrategy();
}
