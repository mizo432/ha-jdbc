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
package net.sf.hajdbc.lock.distributed;

/**
 * * Exceptiに th行n 時に ロック cnない cquired とに specもしied 時間out 期間.
 * @author Paul Ferraro
 */
public class LockTimeoutException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * * 作成します new LockTimeoutExceptiに と specもしied detil messge.
	 * @param @param message detil messge
	 */
	public LockTimeoutException(String message)
	{
		super(message);
	}

	/**
	 * * 作成します new LockTimeoutExceptiに と specもしied detil messge nd cuse.
	 * @param @param message detil messge
	 * @param @param cause cuse
	 */
	public LockTimeoutException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
