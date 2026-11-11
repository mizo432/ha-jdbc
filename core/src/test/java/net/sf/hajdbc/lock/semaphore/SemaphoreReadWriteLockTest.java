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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.junit.jupiter.api.Test;

public class SemaphoreReadWriteLockTest
{
	@Test
	public void readLockReturnsLock()
	{
		SemaphoreReadWriteLock rwLock = new SemaphoreReadWriteLock(new Semaphore(2));
		assertThat(rwLock.readLock()).isNotNull();
	}
	
	@Test
	public void writeLockReturnsLock()
	{
		SemaphoreReadWriteLock rwLock = new SemaphoreReadWriteLock(new Semaphore(2));
		assertThat(rwLock.writeLock()).isNotNull();
	}
	
	@Test
	public void readLockAcquireAndRelease()
	{
		SemaphoreReadWriteLock rwLock = new SemaphoreReadWriteLock(new Semaphore(2));
		Lock readLock = rwLock.readLock();
		
		readLock.lock();
		readLock.unlock();
	}
	
	@Test
	public void writeLockAcquireAndRelease()
	{
		SemaphoreReadWriteLock rwLock = new SemaphoreReadWriteLock(new Semaphore(2));
		Lock writeLock = rwLock.writeLock();
		
		writeLock.lock();
		writeLock.unlock();
	}
	
	@Test
	public void writeLockTryLock()
	{
		SemaphoreReadWriteLock rwLock = new SemaphoreReadWriteLock(new Semaphore(2));
		Lock writeLock = rwLock.writeLock();
		
		assertThat(writeLock.tryLock()).isTrue();
		writeLock.unlock();
	}
	
	@Test
	public void writeLockTryLockWithTimeout() throws InterruptedException
	{
		SemaphoreReadWriteLock rwLock = new SemaphoreReadWriteLock(new Semaphore(2));
		Lock writeLock = rwLock.writeLock();
		
		assertThat(writeLock.tryLock(1, TimeUnit.SECONDS)).isTrue();
		writeLock.unlock();
	}
	
	@Test
	public void writeLockNewConditionThrowsUnsupportedOperationException()
	{
		SemaphoreReadWriteLock rwLock = new SemaphoreReadWriteLock(new Semaphore(2));
		Lock writeLock = rwLock.writeLock();
		
		assertThatThrownBy(writeLock::newCondition)
			.isInstanceOf(UnsupportedOperationException.class);
	}
	
	@Test
	public void multipleReadLocks()
	{
		SemaphoreReadWriteLock rwLock = new SemaphoreReadWriteLock(new Semaphore(2));
		Lock readLock1 = rwLock.readLock();
		Lock readLock2 = rwLock.readLock();
		
		readLock1.lock();
		readLock2.lock();
		
		readLock1.unlock();
		readLock2.unlock();
	}
	
	@Test
	public void writeLockExcludesReadLocks() throws InterruptedException
	{
		Semaphore semaphore = new Semaphore(1);
		SemaphoreReadWriteLock rwLock = new SemaphoreReadWriteLock(semaphore);
		
		Lock readLock = rwLock.readLock();
		Lock writeLock = rwLock.writeLock();
		
		readLock.lock();
		
		// Write lock should not be able to acquire while read lock is held
		assertThat(writeLock.tryLock(0, TimeUnit.SECONDS)).isFalse();
		
		readLock.unlock();
		
		// Now write lock should be able to acquire
		assertThat(writeLock.tryLock(1, TimeUnit.SECONDS)).isTrue();
		writeLock.unlock();
	}
}
