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

import net.sf.hajdbc.DumpRestoreSupport;
import net.sf.hajdbc.IdentityColumnSupport;
import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.TriggerSupport;
import org.jspecify.annotations.Nullable;

/**
 * Encapsulates database vendor-specific SQL syntax.
 * This interface extends six sub-interfaces that isolate distinct responsibilities:
 * <ul>
 *   <li>{@link DialectSQLGenerator} — DDL SQL generation (truncate, constraints, schemas)</li>
 *   <li>{@link DialectSQLParserContract} — SQL parsing and temporal evaluation</li>
 *   <li>{@link DialectMetadataContract} — DatabaseMetaData access</li>
 *   <li>{@link DialectFactoryContract} — Factory creation for identifiers, names, and constraints</li>
 *   <li>{@link DialectConnectionValidator} — Connection validation and property resolution</li>
 *   <li>{@link DialectFailureContract} — Fatal error detection from exceptions</li>
 * </ul>
 * Components should depend on the smallest sub-interface they need rather than this aggregate interface.
 *
 * @author  Paul Ferraro
 * @since   1.1
 */
public interface Dialect extends
    DialectSQLGenerator,
    DialectSQLParserContract,
    DialectMetadataContract,
    DialectFactoryContract,
    DialectConnectionValidator,
    DialectFailureContract
{
	/**
	 * Returns the sequence support for this dialect, or null if not supported.
	 * @return sequence support, or null if not supported
	 */
	@Nullable SequenceSupport getSequenceSupport();
	
	/**
	 * Returns the identity column support for this dialect, or null if not supported.
	 * @return identity column support, or null if not supported
	 */
	@Nullable IdentityColumnSupport getIdentityColumnSupport();
	
	/**
	 * Returns the dump/restore support for this dialect, or null if not supported.
	 * @return dump/restore support, or null if not supported
	 */
	@Nullable DumpRestoreSupport getDumpRestoreSupport();
	
	/**
	 * Returns the trigger support for this dialect, or null if not supported.
	 * @return trigger support, or null if not supported
	 */
	@Nullable TriggerSupport getTriggerSupport();
}
