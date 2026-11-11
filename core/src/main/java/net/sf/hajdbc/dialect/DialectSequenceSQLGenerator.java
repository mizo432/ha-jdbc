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

import java.text.MessageFormat;

import net.sf.hajdbc.SequenceProperties;

/**
 * Helper that generates SQL for sequence operations (next value, alter).
 * Delegates to template method hooks on the dialect for database-specific customization.
 *
 * @author Paul Ferraro
 * @since 3.0
 */
final class DialectSequenceSQLGenerator
{
	private final SequenceTemplate template;

	DialectSequenceSQLGenerator(SequenceTemplate template)
	{
		this.template = template;
	}

	/**
	 * Generate SQL to get the next value from a sequence.
	 */
	String getNextSequenceValueSQL(SequenceProperties sequence)
	{
		return this.template.executeFunctionSQL(
				MessageFormat.format(this.template.nextSequenceValueFormat(), sequence.getName().getDMLName()));
	}

	/**
	 * Generate SQL to alter a sequence to a specific value.
	 */
	String getAlterSequenceSQL(SequenceProperties sequence, long value)
	{
		return MessageFormat.format(this.template.alterSequenceFormat(),
				sequence.getName().getDDLName(), String.valueOf(value), String.valueOf(sequence.getIncrement()));
	}

	/**
	 * Template method hooks that the sequence generator delegates to the dialect for.
	 */
	interface SequenceTemplate
	{
		String nextSequenceValueFormat();

		String alterSequenceFormat();

		String executeFunctionSQL(String function);
	}
}
