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
package net.sf.hajdbc.dialect;

import java.sql.SQLException;

import net.sf.hajdbc.SequenceSupport;
import net.sf.hajdbc.dialect.mckoi.MckoiDialectFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Paul Ferraro
 */
@SuppressWarnings("nls")
public class MckoiDialectTest extends StandardDialectTest
{
	public MckoiDialectTest()
	{
		super(new MckoiDialectFactory());
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#getSequenceSupport()
	 */
	@Override
	public void getSequenceSupport()
	{
		assertThat(this.dialect.getSequenceSupport()).isSameAs(this.dialect);
	}

	/**
	 * {@inheritDoc}
	 * @see net.sf.hajdbc.dialect.StandardDialectTest#parseSequence()
	 */
	@Override
	public void parseSequence() throws SQLException
	{
		SequenceSupport support = this.dialect.getSequenceSupport();
		assertThat(support.parseSequence("SELECT NEXTVAL('sequence'), * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT NEXTVAL ( 'sequence' ), * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT CURRVAL('sequence'), * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT CURRVAL ( 'sequence' ), * FROM table")).isEqualTo("sequence");
		assertThat(support.parseSequence("SELECT NEXT VALUE FOR sequence")).isNull();
		assertThat(support.parseSequence("SELECT * FROM table")).isNull();
	}
}
