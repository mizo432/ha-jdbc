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
package net.sf.hajdbc.state;

import net.sf.hajdbc.durability.DurabilityEvent;
import net.sf.hajdbc.durability.DurabilityListener;
import net.sf.hajdbc.durability.InvocationEvent;
import net.sf.hajdbc.durability.InvokerEvent;

/**
 * Helper that encapsulates {@link DurabilityListenerAdapter} delegation for StateManager classes.
 * Both SQLStateManager and SQLiteStateManager use this helper to delegate their
 * {@link DurabilityListener} methods, eliminating duplicated delegation code.
 * @author Paul Ferraro
 */
public class StateManagerDurabilityHelper implements DurabilityListener, SerializedDurabilityEventFactory
{
	private final DurabilityListenerAdapter listener;

	public StateManagerDurabilityHelper(DurabilityListenerAdapter listener)
	{
		this.listener = listener;
	}

	@Override
	public void beforeInvocation(InvocationEvent event)
	{
		this.listener.beforeInvocation(event);
	}

	@Override
	public void afterInvocation(InvocationEvent event)
	{
		this.listener.afterInvocation(event);
	}

	@Override
	public void beforeInvoker(InvokerEvent event)
	{
		this.listener.beforeInvoker(event);
	}

	@Override
	public void afterInvoker(InvokerEvent event)
	{
		this.listener.afterInvoker(event);
	}

	@Override
	public InvocationEvent createInvocationEvent(byte[] transactionId, byte phase, byte exceptionType)
	{
		return this.listener.createInvocationEvent(transactionId, phase, exceptionType);
	}

	@Override
	public DurabilityEvent createEvent(byte[] transactionId, byte phase)
	{
		return this.listener.createEvent(transactionId, phase);
	}
}
