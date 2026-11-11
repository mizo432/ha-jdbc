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
package net.sf.hajdbc.pool.generic;

import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


/**
 * Expose getters/setters to {@link GenericObjectPoolConfig} properties.
 * @author Paul Ferraro
 */
public class GenericObjectPoolConfiguration
{
	private final GenericObjectPoolConfig config = new GenericObjectPoolConfig<>();

	public GenericObjectPoolConfig toConfig()
	{
		return this.config;
	}
	public int getMaxIdle()
	{
		return this.config.getMaxIdle();
	}
	public void setMaxIdle(int maxIdle)
	{
		this.config.setMaxIdle(maxIdle);
	}
	public int getMinIdle()
	{
		return this.config.getMinIdle();
	}
	public void setMinIdle(int minIdle)
	{
		this.config.setMinIdle(minIdle);
	}
	public int getMaxTotal()
	{
		return this.config.getMaxTotal();
	}
	public void setMaxTotal(int maxTotal)
	{
		this.config.setMaxTotal(maxTotal);
	}
	public long getMaxWaitMillis()
	{
		return this.config.getMaxWaitMillis();
	}
	public void setMaxWaitMillis(long maxWaitMillis)
	{
		this.config.setMaxWaitMillis(maxWaitMillis);
	}
	public boolean isTestOnBorrow()
	{
		return this.config.getTestOnBorrow();
	}
	public void setTestOnBorrow(boolean testOnBorrow)
	{
		this.config.setTestOnBorrow(testOnBorrow);
	}
	public boolean isTestOnReturn()
	{
		return this.config.getTestOnReturn();
	}
	public void setTestOnReturn(boolean testOnReturn)
	{
		this.config.setTestOnReturn(testOnReturn);
	}
	public boolean isTestWhileIdle()
	{
		return this.config.getTestWhileIdle();
	}
	public void setTestWhileIdle(boolean testWhileIdle)
	{
		this.config.setTestWhileIdle(testWhileIdle);
	}
	public long getTimeBetweenEvictionRunsMillis()
	{
		Duration duration = this.config.getTimeBetweenEvictionRuns();
		return (duration != null) ? duration.toMillis() : -1L;
	}
	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis)
	{
		this.config.setTimeBetweenEvictionRuns(Duration.ofMillis(timeBetweenEvictionRunsMillis));
	}
	public int getNumTestsPerEvictionRun()
	{
		return this.config.getNumTestsPerEvictionRun();
	}
	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun)
	{
		this.config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
	}
	public long getMinEvictableIdleTimeMillis()
	{
		Duration duration = this.config.getMinEvictableIdleDuration();
		return (duration != null) ? duration.toMillis() : -1L;
	}
	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis)
	{
		this.config.setMinEvictableIdleDuration(Duration.ofMillis(minEvictableIdleTimeMillis));
	}
	public long getSoftMinEvictableIdleTimeMillis()
	{
		Duration duration = this.config.getSoftMinEvictableIdleDuration();
		return (duration != null) ? duration.toMillis() : -1L;
	}
	public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis)
	{
		this.config.setSoftMinEvictableIdleDuration(Duration.ofMillis(softMinEvictableIdleTimeMillis));
	}
	public boolean isLifo()
	{
		return this.config.getLifo();
	}
	public void setLifo(boolean lifo)
	{
		this.config.setLifo(lifo);
	}
}
