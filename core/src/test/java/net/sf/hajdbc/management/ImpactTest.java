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
package net.sf.hajdbc.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.management.MBeanOperationInfo;

import org.junit.jupiter.api.Test;

public class ImpactTest
{
	@Test
	public void enumValues()
	{
		Impact[] impacts = Impact.values();
		assertThat(impacts).hasSize(4);
		assertThat(impacts).containsExactly(Impact.ACTION, Impact.ACTION_INFO, Impact.INFO, Impact.UNKNOWN);
	}
	
	@Test
	public void getCode()
	{
		assertThat(Impact.ACTION.getCode()).isEqualTo(MBeanOperationInfo.ACTION);
		assertThat(Impact.ACTION_INFO.getCode()).isEqualTo(MBeanOperationInfo.ACTION_INFO);
		assertThat(Impact.INFO.getCode()).isEqualTo(MBeanOperationInfo.INFO);
		assertThat(Impact.UNKNOWN.getCode()).isEqualTo(MBeanOperationInfo.UNKNOWN);
	}
	
	@Test
	public void forCode()
	{
		assertThat(Impact.forCode(MBeanOperationInfo.ACTION)).isEqualTo(Impact.ACTION);
		assertThat(Impact.forCode(MBeanOperationInfo.ACTION_INFO)).isEqualTo(Impact.ACTION_INFO);
		assertThat(Impact.forCode(MBeanOperationInfo.INFO)).isEqualTo(Impact.INFO);
		assertThat(Impact.forCode(MBeanOperationInfo.UNKNOWN)).isEqualTo(Impact.UNKNOWN);
	}
	
	@Test
	public void forCodeWithInvalidCode()
	{
		assertThatThrownBy(() -> Impact.forCode(999))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
