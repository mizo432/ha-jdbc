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
package net.sf.hajdbc.distributed.zookeeper;

import net.sf.hajdbc.distributed.CommandDispatcher;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.distributed.MembershipListener;
import net.sf.hajdbc.distributed.Stateful;

/**
 * ZookeeperベースのCommandDispatcherを作成するファクトリです。
 *
 * @author Paul Ferraro
 */
public class ZookeeperCommandDispatcherFactory implements CommandDispatcherFactory
{
	private static final long serialVersionUID = 5135621114239237377L;
	
	public static final long DEFAULT_TIMEOUT = 60000;
	public static final String DEFAULT_CONNECTION_STRING = "localhost:2181";
	
	private String connectionString = DEFAULT_CONNECTION_STRING;
	private long timeout = DEFAULT_TIMEOUT;
	private String name;
	private int baseSleepTime = 1000;
	private int maxRetries = 5;

	@Override
	public String getId()
	{
		return "zookeeper";
	}

	@Override
	public <C> CommandDispatcher<C> createCommandDispatcher(String id, C context, Stateful stateful, MembershipListener membershipListener) throws Exception
	{
		ZookeeperCommandDispatcher<C> dispatcher = new ZookeeperCommandDispatcher<>(
			id, this.connectionString, this.timeout, context, stateful, membershipListener);
		dispatcher.setBaseSleepTime(this.baseSleepTime);
		dispatcher.setMaxRetries(this.maxRetries);
		return dispatcher;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getConnectionString()
	{
		return this.connectionString;
	}

	public void setConnectionString(String connectionString)
	{
		this.connectionString = connectionString;
	}

	public long getTimeout()
	{
		return this.timeout;
	}

	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	public int getBaseSleepTime()
	{
		return this.baseSleepTime;
	}

	public void setBaseSleepTime(int baseSleepTime)
	{
		this.baseSleepTime = baseSleepTime;
	}

	public int getMaxRetries()
	{
		return this.maxRetries;
	}

	public void setMaxRetries(int maxRetries)
	{
		this.maxRetries = maxRetries;
	}
}
