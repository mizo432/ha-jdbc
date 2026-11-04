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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for SemaphoreLockManager
 */
public class SemaphoreLockManagerTest {

	@Test
	public void readLockWithNullObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.readLock(null);
		Assertions.assertNotNull(lock);
		
		// Should be able to acquire read lock
		Assertions.assertTrue(lock.tryLock());
		lock.unlock();
	}

	@Test
	public void writeLockWithNullObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.writeLock(null);
		Assertions.assertNotNull(lock);
		
		// Should be able to acquire write lock
		Assertions.assertTrue(lock.tryLock());
		lock.unlock();
	}

	@Test
	public void readLockWithObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.readLock("table1");
		Assertions.assertNotNull(lock);
		
		// Should be able to acquire read lock
		Assertions.assertTrue(lock.tryLock());
		lock.unlock();
	}

	@Test
	public void writeLockWithObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.writeLock("table1");
		Assertions.assertNotNull(lock);
		
		// Should be able to acquire write lock
		Assertions.assertTrue(lock.tryLock());
		lock.unlock();
	}

	@Test
	public void multipleReadLocksOnSameObject() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock1 = lockManager.readLock("table1");
		Lock lock2 = lockManager.readLock("table1");
		
		// Multiple readers should be able to acquire locks
		Assertions.assertTrue(lock1.tryLock());
		Assertions.assertTrue(lock2.tryLock());
		
		lock1.unlock();
		lock2.unlock();
	}

	@Test
	public void writeLockBlocksReadLock() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock writeLock = lockManager.writeLock("table1");
		Lock readLock = lockManager.readLock("table1");
		
		// Acquire write lock
		Assertions.assertTrue(writeLock.tryLock());
		
		// Read lock should not be able to acquire while write lock is held
		Assertions.assertFalse(readLock.tryLock());
		
		writeLock.unlock();
		
		// Now read lock should be able to acquire
		Assertions.assertTrue(readLock.tryLock());
		readLock.unlock();
	}

	@Test
	public void readLockBlocksWriteLock() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock readLock = lockManager.readLock("table1");
		Lock writeLock = lockManager.writeLock("table1");
		
		// Acquire read lock
		Assertions.assertTrue(readLock.tryLock());
		
		// Write lock should not be able to acquire while read lock is held
		Assertions.assertFalse(writeLock.tryLock());
		
		readLock.unlock();
		
		// Now write lock should be able to acquire
		Assertions.assertTrue(writeLock.tryLock());
		writeLock.unlock();
	}

	@Test
	public void differentObjectsHaveIndependentLocks() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock1 = lockManager.writeLock("table1");
		Lock lock2 = lockManager.writeLock("table2");
		
		// Different objects should have independent locks
		Assertions.assertTrue(lock1.tryLock());
		Assertions.assertTrue(lock2.tryLock());
		
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
		Assertions.assertNotNull(lock);
		
		Assertions.assertTrue(lock.tryLock());
		lock.unlock();
	}

	@Test
	public void globalLockConditionNotSupported() {
		SemaphoreLockManager lockManager = new SemaphoreLockManager(false);
		
		Lock lock = lockManager.readLock("table1");
		
		// GlobalLock should not support conditions
		try {
			lock.newCondition();
			Assertions.fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			// Expected
		}
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
		
		Assertions.assertTrue(lock1.tryLock());
		Assertions.assertTrue(lock2.tryLock());
		Assertions.assertTrue(lock3.tryLock());
		
		// Write lock should not be able to acquire
		Lock writeLock = lockManager.writeLock("table1");
		Assertions.assertFalse(writeLock.tryLock());
		
		lock1.unlock();
		lock2.unlock();
		lock3.unlock();
		
		// Now write lock should be able to acquire
		Assertions.assertTrue(writeLock.tryLock());
		writeLock.unlock();
	}
}
