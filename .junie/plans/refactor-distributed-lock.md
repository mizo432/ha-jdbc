# Requirements

### Overview & Goals
Refactor the `DistributedLock` inner class in `DistributedLockManager` (lines 272-644, ~370 lines) by extracting the common lock acquisition pattern shared across 4 nearly identical lock methods.

### Current Situation
The `DistributedLock` inner class implements 4 lock methods with nearly identical structure:
1. `lock()` — blocks until acquired, uses `this.lock.lock()`, swallows InterruptedException
2. `lockInterruptibly()` — blocks until acquired, uses `this.lock.lockInterruptibly()`, throws InterruptedException
3. `tryLock()` — single attempt with backoff, uses `this.lock.tryLock()`, swallows InterruptedException
4. `tryLock(time, unit)` — timed attempt, uses `this.lock.tryLock(timeout, MILLISECONDS)`, swallows InterruptedException

Each method follows the same pattern:
1. Loop with sleep(retry)
2. Get coordinator
3. If coordinator: acquire local lock, lock members, unlock on failure
4. Else: lockFromNonCoordinator with remaining timeout
5. Increment retry

The differences are:
- Loop condition (timeout-based vs retry-limit-based)
- Local lock acquisition method (lock, lockInterruptibly, tryLock, tryLock with timeout)
- Exception handling (swallow vs throw InterruptedException)

### Proposed Changes
Extract a `doLock()` helper method that encapsulates the common coordinator vs non-coordinator logic, and a `doLockLoop()` helper that handles the retry loop structure.

### Scope
**In Scope:**
- Extract `doLock()` helper for the coordinator vs non-coordinator branch
- Extract `doLockLoop()` helper for the retry loop with sleep
- Refactor all 4 lock methods to use the extracted helpers

**Out of Scope:**
- Modifying the outer DistributedLockManager class
- Changing the lock algorithm or behavior

# Technical Design

### Current Implementation

All 4 lock methods share this inner body:
```java
Member coordinator = this.dispatcher.getCoordinator();

if (this.dispatcher.getLocal().equals(coordinator))
{
    // Acquire local lock (method varies)
    this.lock.lock();  // or lockInterruptibly(), tryLock(), tryLock(timeout)
    
    try
    {
        locked = this.lockMembers(coordinator);
    }
    finally
    {
        if (!locked)
        {
            this.lock.unlock();
        }
    }
}
else
{
    locked = this.lockFromNonCoordinator(coordinator, timeout);
}
```

### Proposed Changes

**Extract `LockAttempt` functional interface** — represents a single lock attempt:
```java
@FunctionalInterface
interface LockAttempt {
    LockResult attempt();
}
enum LockResult { ACQUIRED, NOT_ACQUIRED, RETRY }
```

**Extract `doLockLoop()` helper** — handles the retry loop structure:
```java
private boolean doLockLoop(LockCondition condition, LockAttempt attempt)
```

**Extract `acquireAsCoordinator()` helper** — handles the coordinator path:
```java
private boolean acquireAsCoordinator(Member coordinator, LocalLockAcquirer acquirer)
```

Where `LocalLockAcquirer` is a functional interface for the varying local lock acquisition:
```java
@FunctionalInterface
interface LocalLockAcquirer {
    void acquire() throws InterruptedException;  // for blocking methods
    boolean tryAcquire();  // for tryLock methods
}
```

Actually, the simplest approach is to extract the inner body as a method that takes a callback for the local lock acquisition:

**Extract `acquireLock()` helper** (~30 lines):
```java
private boolean acquireLock(Member coordinator, long timeout, LocalLockAction action)
```
Where `LocalLockAction` defines how to acquire the local lock.

### Key Decisions
- Use functional interfaces (LocalLockAction) to parameterize the varying local lock acquisition
- Keep the 4 public lock methods as thin wrappers that set up the loop condition and call the helper
- Preserve exact behavior including exception handling

# Testing

### Validation Approach
Compile and run the core test suite to verify no regressions.

### Key Scenarios
- `:core:compileJava` — DistributedLockManager compiles with extracted helpers
- `:core:test` — all core tests pass, especially lock-related tests

### Edge Cases
- InterruptedException handling must be preserved exactly
- Timeout calculation must remain accurate
- The coordinator check must use the same logic

# Delivery Steps

### ✓ Step 1: Create plan and analyze lock methods
Analyze the 4 lock methods to identify the exact common pattern and differences.

### ✓ Step 2: Extract helper methods
Extract the common lock acquisition pattern into helper methods in the DistributedLock inner class.
- Define `LocalLockAcquirer` functional interface for varying local lock acquisition
- Extract `acquireAsCoordinator()` helper for the coordinator path
- Refactor all 4 lock methods to use the extracted helpers

### ✓ Step 3: Verify compilation and tests
- Verify `:core:compileJava` succeeds
- Run `:core:test` to validate no regressions
