---
sessionId: session-261111-115758-11zj
---

# Requirements

### Overview & Goals
Extract the durability listener delegation pattern from SQLStateManager and SQLiteStateManager into a helper delegate class, following the same pattern used in the StandardDialect refactoring.

### Current Situation
Both SQLStateManager (653 lines) and SQLiteStateManager (665 lines) implement `SerializedDurabilityListener` with 4 methods that persist durability events to their respective databases. They also both use `DurabilityListenerAdapter` which bridges `DurabilityListener` (object-based API) to `SerializedDurabilityListener` (byte-array-based API).

The `DurabilityListener` methods (beforeInvocation, afterInvocation, beforeInvoker, afterInvoker) in both StateManagers simply delegate to the `listener` field (DurabilityListenerAdapter). This delegation pattern is identical in both classes.

### Proposed Changes
Extract a `StateManagerDurabilityHelper` class that:
- Holds the `DurabilityListenerAdapter` instance
- Implements the `DurabilityListener` interface methods by delegating to the adapter
- Both SQLStateManager and SQLiteStateManager will delegate their `DurabilityListener` methods to this helper instead of holding the adapter directly

This reduces each StateManager by ~40 lines (the 4 DurabilityListener delegation methods) and centralizes the adapter management.

### Scope
**In Scope:**
- Create StateManagerDurabilityHelper in `net.sf.hajdbc.state` package
- Refactor SQLStateManager to use the helper
- Refactor SQLiteStateManager to use the helper

**Out of Scope:**
- Refactoring the Transaction/Query patterns (database-specific)
- Extracting abstract base class for both StateManagers
- Modifying DistributedStateManager or SimpleStateManager

# Technical Design

### Current Implementation

**SQLStateManager** (653 lines) and **SQLiteStateManager** (665 lines) both:
- Hold a `DurabilityListenerAdapter listener` field
- Implement `DurabilityListener` interface with 4 methods that delegate to `this.listener`
- Implement `SerializedDurabilityListener` with 4 methods that persist to their database

The delegation methods in both classes are identical:
```java
@Override
public void beforeInvocation(InvocationEvent event) { this.listener.beforeInvocation(event); }
@Override
public void afterInvocation(InvocationEvent event) { this.listener.afterInvocation(event); }
@Override
public void beforeInvoker(InvokerEvent event) { this.listener.beforeInvoker(event); }
@Override
public void afterInvoker(InvokerEvent event) { this.listener.afterInvoker(event); }
```

### Proposed Changes

**New class: `StateManagerDurabilityHelper`** (~30 lines)
- Package: `net.sf.hajdbc.state`
- Holds `DurabilityListenerAdapter` reference
- Implements `DurabilityListener` interface
- Delegates all 4 methods to the adapter
- Provides `createInvocationEvent()` and `createEvent()` from `SerializedDurabilityEventFactory`

**Modified: `SQLStateManager`**
- Replace `final DurabilityListenerAdapter listener` with `final StateManagerDurabilityHelper durability`
- Delegate `beforeInvocation(InvocationEvent)`, `afterInvocation(InvocationEvent)`, `beforeInvoker(InvokerEvent)`, `afterInvoker(InvokerEvent)` to `this.durability`
- The `recover()` method still needs `listener.createInvocationEvent()` — access via helper

**Modified: `SQLiteStateManager`**
- Same changes as SQLStateManager

### Key Decisions
- The helper only extracts the `DurabilityListener` delegation, not the `SerializedDurabilityListener` implementation, because the serialized methods are database-specific (JDBC vs sqljet APIs)
- The helper provides access to `SerializedDurabilityEventFactory` methods needed by `recover()` in both classes

# Testing

### Validation Approach
Compile and run the core test suite to verify no regressions.

### Key Scenarios
- `:core:compileJava` — both StateManagers compile with the new helper
- `:core:test` — all core tests pass
- `:state:sqlite:test` — SQLite state manager tests pass (if available)

### Edge Cases
- The `recover()` method in both StateManagers calls `listener.createInvocationEvent()` and `listener.createEvent()` — these must still work via the helper
- DurabilityListenerAdapter initialization order must be preserved

# Delivery Steps

### * Step 1: Create SynchronizationSupportHelper
Create the helper class that encapsulates DurabilityListenerAdapter delegation.
- Create `StateManagerDurabilityHelper.java` in `net.sf.hajdbc.state` package (~30 lines)
- Hold `DurabilityListenerAdapter` reference
- Implement `DurabilityListener` interface (4 methods delegating to adapter)
- Expose `createInvocationEvent()` and `createEvent()` from `SerializedDurabilityEventFactory` for use by `recover()` methods

### ✓ Step 2: Refactor SQLStateManager and SQLiteStateManager
Update both StateManagers to use the new helper instead of direct DurabilityListenerAdapter.
- In `SQLStateManager`: replace `final DurabilityListenerAdapter listener` with `final StateManagerDurabilityHelper durability`
- In `SQLStateManager`: delegate 4 `DurabilityListener` methods to `this.durability`
- In `SQLStateManager`: update `recover()` to use `this.durability.createInvocationEvent()` and `this.durability.createEvent()`
- Apply same changes to `SQLiteStateManager`
- Verify `:core:compileJava` succeeds
- Run `:core:test` to validate no regressions