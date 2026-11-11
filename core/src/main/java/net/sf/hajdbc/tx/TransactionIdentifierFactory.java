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
package net.sf.hajdbc.tx;

/**
 * Factory for generating and managing transaction identifiers.
 * <p>
 * This factory is used by the XA transaction support to generate unique transaction
 * identifiers that can be serialized and deserialized for distributed transaction
 * coordination. Each transaction identifier is globally unique within the cluster.
 * </p>
 *
 * @param <T> transaction identifier type
 */
public interface TransactionIdentifierFactory<T>
{
	/**
	 * Creates a new unique transaction identifier.
	 * @return new transaction identifier
	 */
	T createTransactionIdentifier();

	/**
	 * Serializes a transaction identifier to bytes for storage or transmission.
	 * @param transactionId the transaction identifier
	 * @return serialized bytes
	 */
	byte[] serialize(T transactionId);

	/**
	 * Deserializes a transaction identifier from bytes.
	 * @param bytes serialized bytes
	 * @return deserialized transaction identifier
	 */
	T deserialize(byte[] bytes);

	/**
	 * Returns the size in bytes of a serialized transaction identifier.
	 * @return size in bytes
	 */
	int size();
}
