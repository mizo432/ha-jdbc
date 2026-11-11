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
package net.sf.hajdbc.sync;

import static org.assertj.core.api.Assertions.assertThat;

import net.sf.hajdbc.DatabaseCluster;

import org.junit.jupiter.api.Test;

/**
 * Tests for PassiveSynchronizationStrategy
 */
public class PassiveSynchronizationStrategyTest {

	@Test
	public void getId() {
		PassiveSynchronizationStrategy strategy = new PassiveSynchronizationStrategy();
		assertThat(strategy.getId()).isEqualTo("passive");
	}

	@Test
	public void synchronizeDoesNothing() {
		PassiveSynchronizationStrategy strategy = new PassiveSynchronizationStrategy();
		// synchronize should not throw any exception
		strategy.synchronize(null);
	}

	@Test
	public void initDoesNothing() {
		PassiveSynchronizationStrategy strategy = new PassiveSynchronizationStrategy();
		// init should not throw any exception
		strategy.init(null);
	}

	@Test
	public void destroyDoesNothing() {
		PassiveSynchronizationStrategy strategy = new PassiveSynchronizationStrategy();
		// destroy should not throw any exception
		strategy.destroy(null);
	}

	@Test
	public void serialization() {
		PassiveSynchronizationStrategy strategy = new PassiveSynchronizationStrategy();
		// Verify it implements Serializable
		assertThat(strategy).isInstanceOf(java.io.Serializable.class);
	}
}
