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
		// Allow net.sf.hajdbc classes (internal classes)
		if (name.startsWith("net.sf.hajdbc.")) return true;
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