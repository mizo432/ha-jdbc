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

import java.util.concurrent.ThreadFactory;

import net.sf.hajdbc.io.InputSinkProvider;
import net.sf.hajdbc.util.concurrent.cron.CronExpression;
import org.jspecify.annotations.NonNull;

/**
 * Configures scheduling, threading, and miscellaneous cluster settings.
 * This interface isolates scheduling and operational settings from other configuration concerns,
 * following the Interface Segregation Principle.
 *
 * @author Paul Ferraro
 * @since 3.2
 */
public interface DatabaseClusterSchedulingConfiguration
{
	/**
	 * Returns the cron expression for failure detection scheduling.
	 * @return failure detection cron expression
	 */
	@NonNull CronExpression getFailureDetectionExpression();

	/**
	 * Returns the cron expression for auto-activation scheduling.
	 * @return auto-activation cron expression
	 */
	@NonNull CronExpression getAutoActivationExpression();

	/**
	 * Returns the thread factory for scheduled tasks.
	 * @return thread factory
	 */
	@NonNull ThreadFactory getThreadFactory();

	/**
	 * Returns the input sink provider for SQL logging.
	 * @return input sink provider
	 */
	@NonNull InputSinkProvider getInputSinkProvider();

	/**
	 * Returns the distributed lock acquisition timeout in milliseconds.
	 * @return lock timeout in milliseconds
	 */
	long getLockTimeoutMs();

	/**
	 * Indicates whether an empty cluster (no active databases) is allowed.
	 * @return true if empty cluster is allowed
	 */
	boolean isEmptyClusterAllowed();
}

