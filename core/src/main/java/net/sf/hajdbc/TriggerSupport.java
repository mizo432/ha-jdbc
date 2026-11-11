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

import org.jspecify.annotations.NonNull;

/**
 * @author Paul Ferraro
 */
public interface TriggerSupport
{
	enum TriggerEventEnum implements TriggerEvent
	{
		INSERT(TriggerTimeEnum.AFTER, true), UPDATE(TriggerTimeEnum.AFTER, false), DELETE(TriggerTimeEnum.BEFORE, null);
		
		private final @NonNull TriggerTime time;
		private final Boolean value;
		
		private TriggerEventEnum(@NonNull TriggerTime time, Boolean value)
		{
			this.time = time;
			this.value = value;
		}

		@Override
		public @NonNull TriggerTime getTime()
		{
			return this.time;
		}
		
		@Override
		public Boolean getValue()
		{
			return this.value;
		}
		
		static @NonNull TriggerEvent valueOf(Boolean value)
		{
			for (TriggerEventEnum event: TriggerEventEnum.values())
			{
				if (value == null) {
					if (event.value == null) {
						return event;
					}
				} else if (value.equals(event.value)) {
					return event;
				}
			}
			
			throw new IllegalArgumentException();
		}
	}
	
	enum TriggerTimeEnum implements TriggerTime
	{
		BEFORE("OLD"), AFTER("NEW");
		
		private final @NonNull String alias;
		
		private TriggerTimeEnum(@NonNull String alias)
		{
			this.alias = alias;
		}
		
		@Override
		public @NonNull String getAlias()
		{
			return this.alias;
		}
	}
	
	@NonNull String getTriggerRowAlias(@NonNull TriggerTime time);
	
	@NonNull String getCreateTriggerSQL(@NonNull String name, @NonNull TableProperties table, @NonNull TriggerEvent event, @NonNull String action);
	
	@NonNull String getDropTriggerSQL(@NonNull String name, @NonNull TableProperties table);
}
