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
package net.sf.hajdbc.codec;

import java.io.Serializable;
import java.sql.SQLException;

import net.sf.hajdbc.Identifiable;

/**
 * Factory for creating {@link Codec} instances.
 * <p>
 * A codec provides both encoding and decoding capabilities. This factory extends
 * {@link DecoderFactory} and is used to create codecs for serializing state data.
 * Common implementations include base64, hex, and cipher (encrypted) codecs.
 * </p>
 *
 * @see Codec
 * @see DecoderFactory
 * @see net.sf.hajdbc.codec.base64.Base64CodecFactory
 * @see net.sf.hajdbc.codec.hex.HexCodecFactory
 * @see net.sf.hajdbc.codec.crypto.CipherCodecFactory
 */
public interface CodecFactory extends Identifiable, Serializable
{
	/**
	 * Creates a new codec for the specified cluster.
	 * @param clusterId unique cluster identifier
	 * @return new codec
	 * @throws SQLException if codec creation fails
	 */
	Codec createCodec(String clusterId) throws SQLException;
}
