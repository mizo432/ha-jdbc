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
package net.sf.hajdbc.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class SimpleBuilderTest
{
	@Test
	public void buildReturnsInitialConfiguration()
	{
		String config = "test-config";
		SimpleBuilder<String> builder = new SimpleBuilder<>(config);
		
		assertThat(builder.build()).isEqualTo(config);
	}
	
	@Test
	public void readUpdatesConfiguration()
	{
		SimpleBuilder<String> builder = new SimpleBuilder<>("initial");
		builder.read("updated");
		
		assertThat(builder.build()).isEqualTo("updated");
	}
	
	@Test
	public void readReturnsThis()
	{
		SimpleBuilder<String> builder = new SimpleBuilder<>("initial");
		
		assertThat(builder.read("updated")).isSameAs(builder);
	}
	
	@Test
	public void buildReturnsNullWhenConfigurationIsNull()
	{
		SimpleBuilder<String> builder = new SimpleBuilder<>(null);
		
		assertThat(builder.build()).isNull();
	}
	
	@Test
	public void multipleReadsKeepLastValue()
	{
		SimpleBuilder<Integer> builder = new SimpleBuilder<>(1);
		builder.read(2);
		builder.read(3);
		
		assertThat(builder.build()).isEqualTo(3);
	}
}
