/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2014  Paul Ferraro
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
package net.sf.hajdbc;

import java.sql.SQLException;

import net.sf.hajdbc.codec.Decoder;
import org.jspecify.annotations.NonNull;

/**
 * Credentials interface.
 * @author Paul Ferraro
 */
public interface Credentials
{
	/**
	 * Returns the username.
	 * @return username
	 */
	@NonNull String getUser();
	
	/**
	 * Returns the encoded password.
	 * @return encoded password
	 */
	@NonNull String getEncodedPassword();
	
	/**
	 * Decodes the password.
	 * Returns char[] so the password can be cleared from memory after use.
	 * @param decoder decoder
	 * @return decoded password (zero out with Arrays.fill() after use)
	 * @throws SQLException if password decoding fails
	 */
	@NonNull char[] decodePassword(@NonNull Decoder decoder) throws SQLException;
}
