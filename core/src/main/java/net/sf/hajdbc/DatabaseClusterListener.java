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

import java.util.EventListener;

import net.sf.hajdbc.state.DatabaseEvent;
import org.jspecify.annotations.NonNull;

/**
 * Listener interface for cluster events.
 * @author Paul Ferraro
 */
public interface DatabaseClusterListener extends EventListener
{
	/**
	 * Called after a database is successfully activated.
	 * @param event indicating the activated database
	 */
	void activated(@NonNull DatabaseEvent event);
	
	/**
	 * Called after a database is successfully deactivated.
	 * @param event indicating the deactivated database
	 */
	void deactivated(@NonNull DatabaseEvent event);
}
