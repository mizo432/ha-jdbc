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
package net.sf.hajdbc.durability;

import java.io.Serializable;

import net.sf.hajdbc.Database;
import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.Identifiable;


/**
 * Factory for creating {@link Durability} instances.
 * <p>
 * This factory is a core component of the HA-JDBC architecture, registered via
 * {@link net.sf.hajdbc.DatabaseClusterConfiguration#getDurabilityFactory()}. The durability
 * manager ensures that invocation events are persisted, enabling recovery after failures.
 * Three implementations are available: fine-grained (persists each invocation),
 * coarse-grained (persists batches), and no durability (disabled).
 * </p>
 *
 * @see Durability
 * @see net.sf.hajdbc.DatabaseClusterConfiguration#getDurabilityFactory()
 * @see net.sf.hajdbc.durability.fine.FineDurabilityFactory
 * @see net.sf.hajdbc.durability.coarse.CoarseDurabilityFactory
 * @see net.sf.hajdbc.durability.none.NoDurabilityFactory
 */
public interface DurabilityFactory extends Identifiable, Serializable
{
	/**
	 * Creates a new durability manager for the specified database cluster.
	 * @param cluster the database cluster
	 * @return new durability manager
	 */
	<Z, D extends Database<Z>> Durability<Z, D> createDurability(DatabaseCluster<Z, D> cluster);
}
