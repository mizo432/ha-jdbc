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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Sequence support interface.
 * @author Paul Ferraro
 */
public interface SequenceSupport
{
	/**
	 * Parses a sequence name from the given SQL statement.
	 * @param sql SQL statement
	 * @return sequence name, or null if the SQL statement does not reference a sequence or this dialect does not support sequences
	 * @throws SQLException if an error occurs
	 * @since 2.0
	 */
	@Nullable String parseSequence(@NonNull String sql) throws SQLException;
	
	/**
	 * Returns a collection of sequences using dialect-specific logic.
	 * @param metaData database metadata
	 * @param factory sequence properties factory
	 * @return collection of sequences
	 * @throws SQLException if an error occurs
	 */
	@NonNull Collection<SequenceProperties> getSequences(@NonNull DatabaseMetaData metaData, @NonNull SequencePropertiesFactory factory) throws SQLException;
	
	/**
	 * Returns the SQL statement to get the next value of the given sequence.
	 * @param sequence sequence properties
	 * @return SQL statement
	 * @throws SQLException if an error occurs
	 * @since 2.0
	 */
	@NonNull String getNextSequenceValueSQL(@NonNull SequenceProperties sequence) throws SQLException;

	/**
	 * Returns the SQL statement to reset the current value of a sequence.
	 * @param sequence sequence properties
	 * @param value sequence value
	 * @return SQL statement
	 * @throws SQLException if an error occurs
	 * @since 2.0
	 */
	@NonNull String getAlterSequenceSQL(@NonNull SequenceProperties sequence, long value) throws SQLException;

	/**
	 * Creates a sequence properties factory.
	 * @param factory qualified name factory
	 * @return sequence properties factory
	 */
	@NonNull SequencePropertiesFactory createSequencePropertiesFactory(@NonNull QualifiedNameFactory factory);
}
