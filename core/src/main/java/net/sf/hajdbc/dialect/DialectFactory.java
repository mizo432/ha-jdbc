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

import java.io.Serializable;

import net.sf.hajdbc.Identifiable;


/**
 * Factory for creating {@link Dialect} instances.
 * <p>
 * This factory is a core component of the HA-JDBC architecture, registered via
 * {@link DatabaseClusterConfiguration#getDialectFactory()}. Each database vendor
 * (e.g., MySQL, PostgreSQL, Oracle) has its own dialect factory that creates a
 * dialect providing vendor-specific SQL generation, metadata access, and identifier
 * normalization. The dialect is the primary mechanism for database vendor abstraction.
 * </p>
 * <p>
 * The created dialect implements several sub-interfaces:
 * </p>
 * <ul>
 * <li>{@link net.sf.hajdbc.dialect.DialectSQLGenerator} — DDL SQL generation</li>
 * <li>{@link net.sf.hajdbc.dialect.DialectSQLParserContract} — SQL parsing</li>
 * <li>{@link net.sf.hajdbc.dialect.DialectMetadataContract} — DatabaseMetaData access</li>
 * <li>{@link net.sf.hajdbc.dialect.DialectFactoryContract} — Factory creation</li>
 * <li>{@link net.sf.hajdbc.dialect.DialectConnectionValidator} — Connection validation</li>
 * <li>{@link net.sf.hajdbc.dialect.DialectFailureContract} — Failure detection</li>
 * </ul>
 *
 * @see Dialect
 * @see DatabaseClusterConfiguration#getDialectFactory()
 */
public interface DialectFactory extends Identifiable, Serializable
{
	/**
	 * Creates a new dialect instance.
	 * @return new dialect
	 */
	Dialect createDialect();
}
