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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

/**
 * Tests for TransactionModeEnum
 */
public class TransactionModeEnumTest {

	@Test
	public void enumValues() {
		TransactionModeEnum[] modes = TransactionModeEnum.values();
		assertThat(modes).hasSize(2);
		assertThat(modes).containsExactly(TransactionModeEnum.PARALLEL, TransactionModeEnum.SERIAL);
	}

	@Test
	public void parallelToString() {
		assertThat(TransactionModeEnum.PARALLEL.toString()).isEqualTo("parallel");
	}

	@Test
	public void serialToString() {
		assertThat(TransactionModeEnum.SERIAL.toString()).isEqualTo("serial");
	}

	@Test
	public void parallelGetTransactionExecutor() {
		ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		});
		try {
			ExecutorService result = TransactionModeEnum.PARALLEL.getTransactionExecutor(executor, false);
			// PARALLEL mode returns the same executor
			assertThat(result).isSameAs(executor);
		} finally {
			executor.shutdown();
		}
	}

	@Test
	public void serialGetTransactionExecutor() {
		ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		});
		try {
			ExecutorService result = TransactionModeEnum.SERIAL.getTransactionExecutor(executor, false);
			// SERIAL mode wraps the executor in SynchronousExecutor
			assertThat(result).isNotSameAs(executor);
		} finally {
			executor.shutdown();
		}
	}

	@Test
	public void valueOf() {
		assertThat(TransactionModeEnum.valueOf("PARALLEL")).isEqualTo(TransactionModeEnum.PARALLEL);
		assertThat(TransactionModeEnum.valueOf("SERIAL")).isEqualTo(TransactionModeEnum.SERIAL);
	}
}
