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

import java.sql.SQLException;

/**
 * Factory for creating {@link Decoder} instances.
 * <p>
 * This factory is a core component of the HA-JDBC architecture, registered via
 * {@link net.sf.hajdbc.DatabaseClusterConfiguration#getDecoderFactory()}. The decoder
 * is responsible for deserializing state data stored by the state manager. It must be
 * compatible with the {@link Codec} used for encoding. Common implementations include
 * base64, hex, and cipher (encrypted) codecs.
 * </p>
 *
 * @see Decoder
 * @see Codec
 * @see net.sf.hajdbc.DatabaseClusterConfiguration#getDecoderFactory()
 * @see net.sf.hajdbc.codec.base64.Base64CodecFactory
 * @see net.sf.hajdbc.codec.hex.HexCodecFactory
 * @see net.sf.hajdbc.codec.crypto.CipherCodecFactory
 */
public interface DecoderFactory
{
	/**
	 * Creates a new decoder for the specified cluster.
	 * @param clusterId unique cluster identifier
	 * @return new decoder
	 * @throws SQLException if decoder creation fails
	 */
	Decoder createDecoder(String clusterId) throws SQLException;
}
