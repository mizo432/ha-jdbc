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
package net.sf.hajdbc.sql;

import java.util.concurrent.ThreadFactory;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseClusterConfiguration;
import net.sf.hajdbc.util.concurrent.cron.CronExpression;
import net.sf.hajdbc.util.concurrent.cron.CronThreadPoolExecutor;

/**
 * Manages scheduled tasks (failure detection and auto-activation) for a database cluster.
 * @param <Z> the locator type
 * @param <D> the database type
 */
class DatabaseClusterScheduler<Z, D extends Database<Z>>
{
	private CronThreadPoolExecutor cronExecutor;

	private final DatabaseClusterImpl<Z, D> cluster;
	private final DatabaseClusterConfiguration<Z, D> configuration;

	DatabaseClusterScheduler(DatabaseClusterImpl<Z, D> cluster, DatabaseClusterConfiguration<Z, D> configuration)
	{
		this.cluster = cluster;
		this.configuration = configuration;
	}

	void initialize()
	{
		CronExpression failureDetectionExpression = this.configuration.getFailureDetectionExpression();
		CronExpression autoActivationExpression = this.configuration.getAutoActivationExpression();
		int threads = requiredThreads(failureDetectionExpression) + requiredThreads(autoActivationExpression);

		if (threads > 0)
		{
			ThreadFactory threadFactory = this.configuration.getThreadFactory();
			this.cronExecutor = new CronThreadPoolExecutor(threads, threadFactory);

			if (failureDetectionExpression != null)
			{
				this.cronExecutor.schedule(new DatabaseClusterFailureDetectionTask<>(this.cluster), failureDetectionExpression);
			}

			if (autoActivationExpression != null)
			{
				this.cronExecutor.schedule(new DatabaseClusterAutoActivationTask<>(this.cluster), autoActivationExpression);
			}
		}
	}

	void shutdown()
	{
		if (this.cronExecutor != null)
		{
			this.cronExecutor.shutdown();
			try
			{
				if (!this.cronExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS))
				{
					this.cronExecutor.shutdownNow();
				}
			}
			catch (InterruptedException e)
			{
				this.cronExecutor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	private static int requiredThreads(CronExpression expression)
	{
		return (expression != null) ? 1 : 0;
	}
}
