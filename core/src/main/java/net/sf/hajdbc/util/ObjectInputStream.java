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
package net.sf.hajdbc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

/**
 * 特定のクラスローダーに対してクラスを解決するカスタム{@link java.io.ObjectInputStream}です。
 * セキュリティフィルタを設定して、不正なオブジェクトの逆直列化を防ぎます。
 * @author Paul Ferraro
 */
public class ObjectInputStream extends java.io.ObjectInputStream
{
	private final ClassLoader loader;

	public ObjectInputStream(InputStream in, ClassLoader loader) throws IOException
	{
		super(in);
		this.loader = loader;
		// Set up a security filter to prevent deserialization of untrusted classes
		this.setObjectInputFilter(createFilter());
	}

	/**
	 * Create an ObjectInputFilter that only allows safe classes to be deserialized.
	 * This prevents remote code execution via malicious serialized objects.
	 * @return the configured filter
	 */
	private static ObjectInputFilter createFilter()
	{
		return filterInfo ->
		{
			Class<?> clazz = filterInfo.serialClass();
			if (clazz == null)
			{
				// No class to check yet, just metrics
				return ObjectInputFilter.Status.UNDECIDED;
			}
			// Allow primitive types and their arrays
			if (clazz.isPrimitive()) return ObjectInputFilter.Status.ALLOWED;
			// Allow arrays of allowed types
			if (clazz.isArray()) return ObjectInputFilter.Status.ALLOWED;
			// Allow String, numbers, booleans, dates, and collections
			if (isAllowedClass(clazz)) return ObjectInputFilter.Status.ALLOWED;
			// Block everything else
			return ObjectInputFilter.Status.REJECTED;
		};
	}

	/**
	 * Check if a class is allowed for deserialization.
	 * Uses a whitelist approach to prevent deserialization of untrusted classes.
	 * @param clazz the class to check
	 * @return true if the class is allowed
	 */
	private static boolean isAllowedClass(Class<?> clazz)
	{
		String name = clazz.getName();
		// Allow basic Java types
		if (clazz == String.class) return true;
		if (Number.class.isAssignableFrom(clazz)) return true;
		if (Boolean.class.isAssignableFrom(clazz)) return true;
		if (java.util.Date.class.isAssignableFrom(clazz)) return true;
		if (clazz.isEnum()) return true;
		if (clazz == java.lang.Enum.class) return true;
		// Allow specific java.lang classes needed as base types
		if (isAllowedLangClass(clazz)) return true;
		// Allow specific standard collections and maps (whitelist of safe implementations)
		if (isAllowedCollection(clazz)) return true;
		// Allow specific java.sql classes needed for database operations
		if (isAllowedSqlClass(clazz)) return true;
		// Allow specific java.math classes
		if (isAllowedMathClass(clazz)) return true;
		// Allow specific java.time classes
		if (isAllowedTimeClass(clazz)) return true;
		// Allow specific java.util.Collections inner classes (using name matching for private classes)
		if (isAllowedCollectionsInnerClass(name)) return true;
		// Allow specific net.sf.hajdbc classes (internal classes that need deserialization)
		if (isAllowedHajdbcClass(name)) return true;
		return false;
	}

	/**
	 * Check if a net.sf.hajdbc class is allowed for deserialization.
	 * Uses a whitelist approach to prevent deserialization of untrusted classes.
	 * Only allows classes that are actually used for serialization in the application.
	 */
	private static boolean isAllowedHajdbcClass(String name)
	{
		// Event classes
		if (name.equals("net.sf.hajdbc.util.Event")) return true;
		if (name.equals("net.sf.hajdbc.durability.DurabilityEventImpl")) return true;
		if (name.equals("net.sf.hajdbc.durability.InvocationEventAdapter")) return true;
		if (name.equals("net.sf.hajdbc.durability.InvocationEventImpl")) return true;
		if (name.equals("net.sf.hajdbc.durability.InvokerEventImpl")) return true;
		if (name.equals("net.sf.hajdbc.state.DatabaseEvent")) return true;
		
		// Codec classes
		if (name.equals("net.sf.hajdbc.codec.AbstractCodec")) return true;
		if (name.equals("net.sf.hajdbc.codec.MultiplexingDecoderFactory")) return true;
		if (name.equals("net.sf.hajdbc.codec.SimpleCodecFactory")) return true;
		if (name.equals("net.sf.hajdbc.codec.base64.Base64CodecFactory")) return true;
		if (name.equals("net.sf.hajdbc.codec.hex.HexCodecFactory")) return true;
		if (name.equals("net.sf.hajdbc.codec.crypto.CipherCodecFactory")) return true;
		
		// Dialect factories
		if (name.equals("net.sf.hajdbc.dialect.StandardDialectFactory")) return true;
		if (name.equals("net.sf.hajdbc.dialect.db2.DB2DialectFactory")) return true;
		if (name.equals("net.sf.hajdbc.dialect.h2.H2DialectFactory")) return true;
		if (name.equals("net.sf.hajdbc.dialect.hsqldb.HSQLDBDialectFactory")) return true;
		if (name.equals("net.sf.hajdbc.dialect.mssql.MSSQLDialectFactory")) return true;
		if (name.equals("net.sf.hajdbc.dialect.mysql.MySQLDialectFactory")) return true;
		if (name.equals("net.sf.hajdbc.dialect.oracle.OracleDialectFactory")) return true;
		if (name.equals("net.sf.hajdbc.dialect.postgresql.PostgreSQLDialectFactory")) return true;
		
		// Balancer factories
		if (name.equals("net.sf.hajdbc.balancer.load.LoadBalancerFactory")) return true;
		if (name.equals("net.sf.hajdbc.balancer.random.RandomBalancerFactory")) return true;
		if (name.equals("net.sf.hajdbc.balancer.roundrobin.RoundRobinBalancerFactory")) return true;
		if (name.equals("net.sf.hajdbc.balancer.simple.SimpleBalancerFactory")) return true;
		
		// Lock manager factories
		if (name.equals("net.sf.hajdbc.lock.semaphore.SemaphoreLockManagerFactory")) return true;
		
		// State manager factories
		if (name.equals("net.sf.hajdbc.state.simple.SimpleStateManagerFactory")) return true;
		
		// Cache factories
		if (name.equals("net.sf.hajdbc.cache.eager.EagerDatabaseMetaDataCacheFactory")) return true;
		if (name.equals("net.sf.hajdbc.cache.eager.SharedEagerDatabaseMetaDataCacheFactory")) return true;
		if (name.equals("net.sf.hajdbc.cache.lazy.LazyDatabaseMetaDataCacheFactory")) return true;
		if (name.equals("net.sf.hajdbc.cache.lazy.SharedLazyDatabaseMetaDataCacheFactory")) return true;
		if (name.equals("net.sf.hajdbc.cache.simple.SimpleDatabaseMetaDataCacheFactory")) return true;
		
		// Durability factories
		if (name.equals("net.sf.hajdbc.durability.coarse.CoarseDurabilityFactory")) return true;
		if (name.equals("net.sf.hajdbc.durability.fine.FineDurabilityFactory")) return true;
		if (name.equals("net.sf.hajdbc.durability.none.NoDurabilityFactory")) return true;
		
		// Exception factories
		if (name.equals("net.sf.hajdbc.sql.SQLExceptionFactory")) return true;
		if (name.equals("net.sf.hajdbc.sql.io.IOExceptionFactory")) return true;
		if (name.equals("net.sf.hajdbc.sql.xa.XAExceptionFactory")) return true;
		
		// XML stream factories
		if (name.equals("net.sf.hajdbc.xml.FileXMLStreamFactory")) return true;
		if (name.equals("net.sf.hajdbc.xml.URLXMLStreamFactory")) return true;
		
		// Configuration factories
		if (name.equals("net.sf.hajdbc.SimpleDatabaseClusterConfigurationFactory")) return true;
		if (name.equals("net.sf.hajdbc.xml.XMLDatabaseClusterConfigurationFactory")) return true;
		
		// Synchronization strategies
		if (name.equals("net.sf.hajdbc.sync.DifferentialSynchronizationStrategy")) return true;
		if (name.equals("net.sf.hajdbc.sync.DumpRestoreSynchronizationStrategy")) return true;
		if (name.equals("net.sf.hajdbc.sync.FastDifferentialSynchronizationStrategy")) return true;
		if (name.equals("net.sf.hajdbc.sync.FullSynchronizationStrategy")) return true;
		if (name.equals("net.sf.hajdbc.sync.PassiveSynchronizationStrategy")) return true;
		if (name.equals("net.sf.hajdbc.sync.PerTableSynchronizationStrategy")) return true;
		
		// Durability classes
		if (name.equals("net.sf.hajdbc.durability.InvokerResultImpl")) return true;
		
		// Executor service provider
		if (name.equals("net.sf.hajdbc.sql.DefaultExecutorServiceProvider")) return true;
		
		// Distributed state manager inner classes
		if (name.equals("net.sf.hajdbc.state.distributed.DistributedStateManager$RemoteDescriptor")) return true;
		if (name.equals("net.sf.hajdbc.state.distributed.DistributedStateManager$RemoteInvocationDescriptorImpl")) return true;
		if (name.equals("net.sf.hajdbc.state.distributed.DistributedStateManager$RemoteInvokerDescriptorImpl")) return true;
		
		// Distributed lock manager inner classes
		if (name.equals("net.sf.hajdbc.lock.distributed.DistributedLockManager$RemoteLockDescriptorImpl")) return true;
		
		// Command classes
		if (name.equals("net.sf.hajdbc.lock.distributed.AcquireLockCommand")) return true;
		if (name.equals("net.sf.hajdbc.lock.distributed.ReleaseLockCommand")) return true;
		if (name.equals("net.sf.hajdbc.lock.distributed.LockInterruptedException")) return true;
		if (name.equals("net.sf.hajdbc.lock.distributed.LockTimeoutException")) return true;
		
		// State command classes
		if (name.equals("net.sf.hajdbc.state.distributed.ActivationCommand")) return true;
		if (name.equals("net.sf.hajdbc.state.distributed.DeactivationCommand")) return true;
		if (name.equals("net.sf.hajdbc.state.distributed.InvocationCommand")) return true;
		if (name.equals("net.sf.hajdbc.state.distributed.InvokerCommand")) return true;
		if (name.equals("net.sf.hajdbc.state.distributed.PostInvocationCommand")) return true;
		if (name.equals("net.sf.hajdbc.state.distributed.PreInvocationCommand")) return true;
		if (name.equals("net.sf.hajdbc.state.distributed.StateCommand")) return true;
		if (name.equals("net.sf.hajdbc.state.distributed.StopCommand")) return true;
		
		// State manager factory
		if (name.equals("net.sf.hajdbc.state.sql.SQLStateManagerFactory")) return true;
		
		// Identifier list
		if (name.equals("net.sf.hajdbc.dialect.IdentifierList")) return true;
		
		// Pekko distributed classes
		if (name.equals("net.sf.hajdbc.distributed.pekko.PekkoMember")) return true;
		if (name.equals("net.sf.hajdbc.distributed.pekko.PekkoCommandDispatcherFactory")) return true;
		
		// Cron expression
		if (name.equals("net.sf.hajdbc.util.concurrent.cron.CronExpression")) return true;
		
		// Collections inner classes
		if (name.equals("net.sf.hajdbc.util.Collections$NaturalComparator")) return true;
		if (name.equals("net.sf.hajdbc.util.Collections$EmptySortedSet")) return true;
		if (name.equals("net.sf.hajdbc.util.Collections$EmptySortedMap")) return true;
		if (name.equals("net.sf.hajdbc.util.Collections$NullComparator")) return true;
		if (name.equals("net.sf.hajdbc.util.Collections$SingletonSortedSet")) return true;
		if (name.equals("net.sf.hajdbc.util.Collections$SingletonSortedMap")) return true;
		
		// Test classes (used in unit tests for serialization)
		if (name.equals("net.sf.hajdbc.util.ObjectInputStreamTest$TestSerializable")) return true;
		if (name.equals("net.sf.hajdbc.util.ObjectsTest$TestObject")) return true;
		
		return false;
	}

	/**
	 * Check if a java.util.Collections inner class is allowed.
	 * Uses name-based matching since some inner classes are private.
	 */
	private static boolean isAllowedCollectionsInnerClass(String name)
	{
		return name.equals("java.util.Collections$EmptyList") ||
		       name.equals("java.util.Collections$EmptySet") ||
		       name.equals("java.util.Collections$EmptyMap") ||
		       name.equals("java.util.Collections$SingletonList") ||
		       name.equals("java.util.Collections$SingletonSet") ||
		       name.equals("java.util.Collections$SingletonMap") ||
		       name.equals("java.util.Collections$UnmodifiableList") ||
		       name.equals("java.util.Collections$UnmodifiableSet") ||
		       name.equals("java.util.Collections$UnmodifiableMap") ||
		       name.equals("java.util.Collections$UnmodifiableCollection") ||
		       name.equals("java.util.Collections$SynchronizedList") ||
		       name.equals("java.util.Collections$SynchronizedSet") ||
		       name.equals("java.util.Collections$SynchronizedMap") ||
		       name.equals("java.util.Collections$SynchronizedCollection");
	}

	/**
	 * Check if a java.lang class is allowed.
	 * Only allows specific safe classes, not the entire package.
	 */
	private static boolean isAllowedLangClass(Class<?> clazz)
	{
		return clazz == java.lang.Throwable.class ||
		       clazz == java.lang.Exception.class ||
		       clazz == java.lang.RuntimeException.class ||
		       clazz == java.lang.IllegalStateException.class ||
		       clazz == java.lang.IllegalArgumentException.class ||
		       clazz == java.lang.NullPointerException.class ||
		       clazz == java.lang.ClassNotFoundException.class ||
		       clazz == java.lang.NoSuchMethodError.class ||
		       clazz == java.lang.NoSuchFieldError.class ||
		       clazz == java.lang.NoSuchFieldException.class ||
		       clazz == java.lang.NoSuchMethodException.class ||
		       clazz == java.lang.reflect.UndeclaredThrowableException.class ||
		       clazz == java.lang.invoke.SerializedLambda.class ||
		       clazz == java.lang.StackTraceElement.class;
	}

	/**
	 * Check if a java.util class is allowed.
	 * Only allows specific, well-known collection implementations and utility classes.
	 */
	private static boolean isAllowedCollection(Class<?> clazz)
	{
		return clazz == java.util.ArrayList.class ||
		       clazz == java.util.LinkedList.class ||
		       clazz == java.util.HashMap.class ||
		       clazz == java.util.LinkedHashMap.class ||
		       clazz == java.util.TreeMap.class ||
		       clazz == java.util.HashSet.class ||
		       clazz == java.util.LinkedHashSet.class ||
		       clazz == java.util.TreeSet.class ||
		       clazz == java.util.Vector.class ||
		       clazz == java.util.Hashtable.class ||
		       clazz == java.util.Properties.class ||
		       clazz == java.util.EnumMap.class ||
		       clazz == java.util.EnumSet.class ||
		       clazz == java.util.concurrent.ConcurrentHashMap.class ||
		       clazz == java.util.concurrent.CopyOnWriteArrayList.class ||
		       clazz == java.util.AbstractCollection.class ||
		       clazz == java.util.AbstractList.class ||
		       clazz == java.util.AbstractSet.class ||
		       clazz == java.util.AbstractMap.class ||
		       clazz == java.util.AbstractSequentialList.class ||
		       clazz == java.util.EventObject.class ||
		       clazz == java.util.UUID.class ||
		       clazz == java.util.BitSet.class ||
		       clazz == java.util.Arrays.class ||
		       clazz == java.util.Collections.class ||
		       clazz == java.util.Comparator.class ||
		       clazz == java.util.concurrent.atomic.AtomicBoolean.class ||
		       clazz == java.util.concurrent.atomic.AtomicInteger.class ||
		       clazz == java.util.concurrent.atomic.AtomicLong.class ||
		       clazz == java.util.concurrent.atomic.AtomicReference.class;
	}

	/**
	 * Check if a java.sql class is allowed.
	 * Only allows specific SQL-related classes needed for database operations.
	 */
	private static boolean isAllowedSqlClass(Class<?> clazz)
	{
		return clazz == java.sql.Date.class ||
		       clazz == java.sql.Time.class ||
		       clazz == java.sql.Timestamp.class ||
		       clazz == java.sql.SQLException.class ||
		       clazz == java.sql.SQLWarning.class ||
		       clazz == java.sql.SQLDataException.class ||
		       clazz == java.sql.SQLFeatureNotSupportedException.class ||
		       clazz == java.sql.SQLIntegrityConstraintViolationException.class ||
		       clazz == java.sql.SQLInvalidAuthorizationSpecException.class ||
		       clazz == java.sql.SQLNonTransientConnectionException.class ||
		       clazz == java.sql.SQLNonTransientException.class ||
		       clazz == java.sql.SQLRecoverableException.class ||
		       clazz == java.sql.SQLSyntaxErrorException.class ||
		       clazz == java.sql.SQLTransactionRollbackException.class ||
		       clazz == java.sql.SQLTransientConnectionException.class ||
		       clazz == java.sql.SQLTransientException.class ||
		       clazz == java.sql.SQLTimeoutException.class;
	}

	/**
	 * Check if a java.math class is allowed.
	 */
	private static boolean isAllowedMathClass(Class<?> clazz)
	{
		return clazz == java.math.BigDecimal.class ||
		       clazz == java.math.BigInteger.class ||
		       clazz == java.math.MathContext.class ||
		       clazz == java.math.RoundingMode.class;
	}

	/**
	 * Check if a java.time class is allowed.
	 */
	private static boolean isAllowedTimeClass(Class<?> clazz)
	{
		return clazz == java.time.LocalDate.class ||
		       clazz == java.time.LocalDateTime.class ||
		       clazz == java.time.LocalTime.class ||
		       clazz == java.time.Instant.class ||
		       clazz == java.time.ZonedDateTime.class ||
		       clazz == java.time.OffsetDateTime.class ||
		       clazz == java.time.OffsetTime.class ||
		       clazz == java.time.Duration.class ||
		       clazz == java.time.Period.class ||
		       clazz == java.time.Year.class ||
		       clazz == java.time.YearMonth.class ||
		       clazz == java.time.MonthDay.class ||
		       clazz == java.time.Month.class ||
		       clazz == java.time.DayOfWeek.class ||
		       clazz == java.time.ZoneId.class ||
		       clazz == java.time.ZoneOffset.class ||
		       clazz == java.time.chrono.IsoChronology.class ||
		       clazz == java.time.chrono.IsoEra.class ||
		       clazz == java.time.format.DateTimeFormatter.class ||
		       clazz == java.time.format.DateTimeFormatterBuilder.class ||
		       clazz == java.time.temporal.ChronoField.class ||
		       clazz == java.time.temporal.ChronoUnit.class ||
		       clazz == java.time.temporal.IsoFields.class ||
		       clazz == java.time.temporal.WeekFields.class;
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
	{
		return Class.forName(desc.getName(), false, this.loader);
	}

	@Override
	protected Class<?> resolveProxyClass(String[] names) throws IOException, ClassNotFoundException
	{
		Class<?>[] interfaces = new Class[names.length];
		for (int i = 0; i < names.length; ++i)
		{
			interfaces[i] = Class.forName(names[i], false, this.loader);
		}
		return Proxy.getProxyClass(this.loader, interfaces);
	}
}