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

/**
 * Configures temporal SQL function evaluation for deterministic behavior.
 * This interface isolates temporal evaluation settings from other configuration concerns,
 * following the Interface Segregation Principle.
 *
 * @author Paul Ferraro
 * @since 3.2
 */
public interface DatabaseClusterTemporalEvaluationConfiguration
{
	/**
	 * Indicates whether non-deterministic CURRENT_DATE SQL functions will be evaluated to deterministic static values.
	 * @return true if CURRENT_DATE evaluation is enabled
	 */
	boolean isCurrentDateEvaluationEnabled();

	/**
	 * Indicates whether non-deterministic CURRENT_TIME SQL functions will be evaluated to deterministic static values.
	 * @return true if CURRENT_TIME evaluation is enabled
	 */
	boolean isCurrentTimeEvaluationEnabled();

	/**
	 * Indicates whether non-deterministic CURRENT_TIMESTAMP SQL functions will be evaluated to deterministic static values.
	 * @return true if CURRENT_TIMESTAMP evaluation is enabled
	 */
	boolean isCurrentTimestampEvaluationEnabled();

	/**
	 * Indicates whether non-deterministic RAND() functions will be replaced by static values.
	 * @return true if RAND() evaluation is enabled
	 */
	boolean isRandEvaluationEnabled();
}

