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

import org.junit.jupiter.api.Test;

/**
 * Tests for FullSynchronizationStrategy
 */
public class FullSynchronizationStrategyTest {

	@Test
	public void getId() {
		FullSynchronizationStrategy strategy = new FullSynchronizationStrategy();
		assertThat(strategy.getId()).isEqualTo("full");
	}

	@Test
	public void defaultFetchSize() {
		FullSynchronizationStrategy strategy = new FullSynchronizationStrategy();
		assertThat(strategy.getFetchSize()).isEqualTo(0);
	}

	@Test
	public void defaultMaxBatchSize() {
		FullSynchronizationStrategy strategy = new FullSynchronizationStrategy();
		assertThat(strategy.getMaxBatchSize()).isEqualTo(100);
	}

	@Test
	public void setFetchSize() {
		FullSynchronizationStrategy strategy = new FullSynchronizationStrategy();
		strategy.setFetchSize(50);
		assertThat(strategy.getFetchSize()).isEqualTo(50);
	}

	@Test
	public void setMaxBatchSize() {
		FullSynchronizationStrategy strategy = new FullSynchronizationStrategy();
		strategy.setMaxBatchSize(200);
		assertThat(strategy.getMaxBatchSize()).isEqualTo(200);
	}

	@Test
	public void serialization() {
		FullSynchronizationStrategy strategy = new FullSynchronizationStrategy();
		assertThat(strategy).isInstanceOf(java.io.Serializable.class);
	}

	@Test
	public void implementsTableSynchronizationStrategy() {
		FullSynchronizationStrategy strategy = new FullSynchronizationStrategy();
		assertThat(strategy).isInstanceOf(TableSynchronizationStrategy.class);
	}
}
