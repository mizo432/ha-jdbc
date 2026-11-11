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
package net.sf.hajdbc.distributed.pekko;

import java.util.ArrayList;
import java.util.List;

import net.sf.hajdbc.distributed.CommandDispatcher;
import net.sf.hajdbc.distributed.CommandDispatcherFactory;
import net.sf.hajdbc.distributed.MembershipListener;
import net.sf.hajdbc.distributed.Stateful;

/**
 * Apache PekkoベースのCommandDispatcherを作成するファクトリです。
 *
 * @author Paul Ferraro
 */
public class PekkoCommandDispatcherFactory implements CommandDispatcherFactory
{
	private static final long serialVersionUID = 5135621114239237377L;
	
	public static final long DEFAULT_TIMEOUT = 60000;
	public static final String DEFAULT_SYSTEM_NAME = "ha-jdbc";
	public static final int DEFAULT_PORT = 25520;
	
	private String systemName = DEFAULT_SYSTEM_NAME;
	private long timeout = DEFAULT_TIMEOUT;
	private int port = DEFAULT_PORT;
	private List<String> seedNodes = new ArrayList<>();

	@Override
	public String getId()
	{
		return "pekko";
	}

	@Override
	public <C> CommandDispatcher<C> createCommandDispatcher(String id, C context, Stateful stateful, MembershipListener membershipListener) throws Exception
	{
		return new PekkoCommandDispatcher<>(
			id, this.systemName, this.port, this.timeout, this.seedNodes, context, stateful, membershipListener);
	}

	public String getSystemName()
	{
		return this.systemName;
	}

	public void setSystemName(String systemName)
	{
		this.systemName = systemName;
	}

	public long getTimeout()
	{
		return this.timeout;
	}

	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	public int getPort()
	{
		return this.port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public List<String> getSeedNodes()
	{
		return this.seedNodes;
	}

	public void setSeedNodes(List<String> seedNodes)
	{
		this.seedNodes = seedNodes;
	}
}
