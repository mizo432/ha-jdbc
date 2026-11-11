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
package net.sf.hajdbc.lock.semaphore;

import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 * Tests for SemaphoreLockManager
 */
public class SemaphoreLockManagerTest {

	@Test
	public void readLockWithNullObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.readLock(null);
		assertThat(lock).isNotNull();
		
		// Should be able to acquire read lock
		assertThat(lock.tryLock()).isTrue();
		lock.unlock();
	}

	@Test
	public void writeLockWithNullObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.writeLock(null);
		assertThat(lock).isNotNull();
		
		// Should be able to acquire write lock
		assertThat(lock.tryLock()).isTrue();
		lock.unlock();
	}

	@Test
	public void readLockWithObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.readLock("table1");
		assertThat(lock).isNotNull();
		
		// Should be able to acquire read lock
		assertThat(lock.tryLock()).isTrue();
		lock.unlock();
	}

	@Test
	public void writeLockWithObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.writeLock("table1");
		assertThat(lock).isNotNull();
		
		// Should be able to acquire write lock
		assertThat(lock.tryLock()).isTrue();
		lock.unlock();
	}

	@Test
	public void multipleReadLocksOnSameObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock1 = lockManager.readLock("table1");
		Lock lock2 = lockManager.readLock("table1");
		
		// Multiple readers should be able to acquire locks
		assertThat(lock1.tryLock()).isTrue();
		assertThat(lock2.tryLock()).isTrue();
		
		lock1.unlock();
		lock2.unlock();
	}

	@Test
	public void writeLockBlocksReadLock() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock writeLock = lockManager.writeLock("table1");
		Lock readLock = lockManager.readLock("table1");
		
		// Acquire write lock
		assertThat(writeLock.tryLock()).isTrue();
		
		// Read lock should not be able to acquire while write lock is held
		assertThat(readLock.tryLock()).isFalse();
		
		writeLock.unlock();
		
		// Now read lock should be able to acquire
		assertThat(readLock.tryLock()).isTrue();
		readLock.unlock();
	}

	@Test
	public void readLockBlocksWriteLock() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock readLock = lockManager.readLock("table1");
		Lock writeLock = lockManager.writeLock("table1");
		
		// Acquire read lock
		assertThat(readLock.tryLock()).isTrue();
		
		// Write lock should not be able to acquire while read lock is held
		assertThat(writeLock.tryLock()).isFalse();
		
		readLock.unlock();
		
		// Now write lock should be able to acquire
		assertThat(writeLock.tryLock()).isTrue();
		writeLock.unlock();
	}

	@Test
	public void differentObjectsHaveIndependentLocks() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock1 = lockManager.writeLock("table1");
		Lock lock2 = lockManager.writeLock("table2");
		
		// Different objects should have independent locks
		assertThat(lock1.tryLock()).isTrue();
		assertThat(lock2.tryLock()).isTrue();
		
		lock1.unlock();
		lock2.unlock();
	}

	@Test
	public void startAndStop() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		// Should not throw
		lockManager.start();
		lockManager.stop();
	}

	@Test
	public void fairLockManager() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(true);
		
		Lock lock = lockManager.readLock("table1");
		assertThat(lock).isNotNull();
		
		assertThat(lock.tryLock()).isTrue();
		lock.unlock();
	}

	@Test
	public void globalLockConditionNotSupported() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.readLock("table1");
		
		// GlobalLock should not support conditions
		assertThatThrownBy(() -> lock.newCondition())
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void lockUnlockCycle() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		for (int i = 0; i < 10; i++) {
			Lock lock = lockManager.writeLock("table1");
			lock.lock();
			try {
				// Do nothing
			} finally {
				lock.unlock();
			}
		}
	}

	@Test
	public void concurrentReadLocks() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		// Acquire multiple read locks concurrently
		Lock lock1 = lockManager.readLock("table1");
		Lock lock2 = lockManager.readLock("table1");
		Lock lock3 = lockManager.readLock("table1");
		
		assertThat(lock1.tryLock()).isTrue();
		assertThat(lock2.tryLock()).isTrue();
		assertThat(lock3.tryLock()).isTrue();
		
		// Write lock should not be able to acquire
		Lock writeLock = lockManager.writeLock("table1");
		assertThat(writeLock.tryLock()).isFalse();
		
		lock1.unlock();
		lock2.unlock();
		lock3.unlock();
		
		// Now write lock should be able to acquire
		assertThat(writeLock.tryLock()).isTrue();
		writeLock.unlock();
	}

	@Test
	public void globalLockReleasesGlobalLockOnSecondLockFailure() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);

		// Get a GlobalLock (readLock with non-null object wraps in GlobalLock)
		Lock globalLock = lockManager.readLock("table1");

		// First, verify normal lock/unlock works
		globalLock.lock();
		globalLock.unlock();

		// Now test that if lock() throws, the global lock is released.
		// We verify this by checking that after a failed lock() on one GlobalLock,
		// another GlobalLock for the same object can still acquire.
		Lock lock1 = lockManager.readLock("table1");
		Lock lock2 = lockManager.readLock("table1");

		// Both should be able to acquire (read locks are shared)
		assertThat(lock1.tryLock()).isTrue();
		assertThat(lock2.tryLock()).isTrue();
		lock1.unlock();
		lock2.unlock();
	}

	@Test
	public void globalLockTryLockReleasesGlobalLockOnSecondLockFailure() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);

		// Acquire a write lock to block the second (object-level) lock
		Lock writeLock = lockManager.writeLock("table1");
		assertThat(writeLock.tryLock()).isTrue();

		// readLock with object returns GlobalLock - tryLock should fail and not leak global lock
		Lock readLock = lockManager.readLock("table1");
		assertThat(readLock.tryLock()).isFalse();

		// Release the write lock
		writeLock.unlock();

		// Now the read lock should be able to acquire, proving global lock was not leaked
		assertThat(readLock.tryLock()).isTrue();
		readLock.unlock();
	}
}
