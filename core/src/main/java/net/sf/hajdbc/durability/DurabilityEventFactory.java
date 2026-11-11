package net.sf.hajdbc.durability;

import net.sf.hajdbc.ExceptionType;

/**
 * Factory for creating durability events.
 * <p>
 * This factory is used internally by the durability manager to create events that
 * track the lifecycle of database invocations. Events are persisted to enable
 * recovery after failures, ensuring that no invocation is lost.
 * </p>
 *
 * @see DurabilityEvent
 * @see InvocationEvent
 * @see InvokerEvent
 */
public interface DurabilityEventFactory
{
	/**
	 * Creates a new durability event.
	 * @param transactionId transaction identifier
	 * @param phase durability phase
	 * @return new durability event
	 */
	DurabilityEvent createEvent(Object transactionId, Durability.Phase phase);

	/**
	 * Creates a new invocation event for tracking SQL execution.
	 * @param transactionId transaction identifier
	 * @param phase durability phase
	 * @param exceptionType type of exception, if any
	 * @return new invocation event
	 */
	InvocationEvent createInvocationEvent(Object transactionId, Durability.Phase phase, ExceptionType exceptionType);

	/**
	 * Creates a new invoker event for tracking database selection.
	 * @param transactionId transaction identifier
	 * @param phase durability phase
	 * @param databaseId identifier of the selected database
	 * @return new invoker event
	 */
	InvokerEvent createInvokerEvent(Object transactionId, Durability.Phase phase, String databaseId);
}
