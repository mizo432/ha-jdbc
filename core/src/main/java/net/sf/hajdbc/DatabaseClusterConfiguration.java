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

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;

import net.sf.hajdbc.balancer.BalancerFactory;
import net.sf.hajdbc.cache.DatabaseMetaDataCacheFactory;
import net.sf.hajdbc.codec.DecoderFactory;
import net.sf.hajdbc.dialect.DialectFactory;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.durability.DurabilityFactory;
import net.sf.hajdbc.io.InputSinkProvider;
import net.sf.hajdbc.lock.LockManagerFactory;
import net.sf.hajdbc.management.MBeanRegistrarFactory;
import net.sf.hajdbc.state.StateManagerFactory;
import net.sf.hajdbc.util.concurrent.cron.CronExpression;
import org.jspecify.annotations.NonNull;

/**
 * @author Paul Ferraro
 */
public interface DatabaseClusterConfiguration<Z, D extends Database<Z>>
{
	@NonNull CommandDispatcherFactory getDispatcherFactory();
	
	/**
	 * このクラスタのデータベースを識別子でインデックス付けて返します
	 * @return データベースのマップ
	 * @throws IllegalArgumentException 指定された識別子を持つデータベースが存在しない場合
	 */
	@NonNull ConcurrentMap<String, D> getDatabaseMap();
	
	@NonNull Map<String, SynchronizationStrategy> getSynchronizationStrategyMap();
	
	@NonNull String getDefaultSynchronizationStrategy();
	
	/**
	 * このデータベースクラスタで使用されるBalancerの実装を返します。
	 * @return <code>Balancer</code>の実装
	 */
	@NonNull BalancerFactory getBalancerFactory();

	@NonNull TransactionMode getTransactionMode();
	
	@NonNull ExecutorServiceProvider getExecutorProvider();
	
	/**
	 * データベースベンダー固有の値を返すことができるDialectを返します。
	 * @return <code>Dialect</code>の実装
	 */
	@NonNull DialectFactory getDialectFactory();
	
	/**
	 * データベースクラスタの状態を永続化するためのStateManagerを返します。
	 * @return StateManagerの実装
	 */
	@NonNull StateManagerFactory getStateManagerFactory();
	
	/**
	 * DatabaseMetaDataキャッシュを返します。
	 * @return <code>DatabaseMetaDataCache</code>の実装
	 */
	@NonNull DatabaseMetaDataCacheFactory getDatabaseMetaDataCacheFactory();

	@NonNull DurabilityFactory getDurabilityFactory();

	@NonNull LockManagerFactory getLockManagerFactory();
	
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

	@NonNull CronExpression getFailureDetectionExpression();
	
	@NonNull CronExpression getAutoActivationExpression();
	
	@NonNull ThreadFactory getThreadFactory();
	
	@NonNull DecoderFactory getDecoderFactory();
	
	@NonNull MBeanRegistrarFactory getMBeanRegistrarFactory();
	
	boolean isEmptyClusterAllowed();

	@NonNull InputSinkProvider getInputSinkProvider();

	/**
	 * 分散ロックの取得タイムアウトをミリ秒単位で返します。
	 * @return ロックタイムアウト（ミリ秒）
	 */
	long getLockTimeoutMs();
}
