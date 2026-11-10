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
package net.sf.hajdbc.balancer;

import net.sf.hajdbc.MockDatabase;
import net.sf.hajdbc.balancer.random.RandomBalancerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;


/**
 * @author Paul Ferraro
 */
public class RandomBalancerTest extends AbstractBalancerTest
{
	public RandomBalancerTest()
	{
		super(new RandomBalancerFactory());
	}
	
	@Override
	public void next(Balancer<Void, MockDatabase> balancer)
	{
		int count = 1000;
		int[] results = new int[] { 0, 0, 0 };
		
		for (int i = 0; i < count; ++i)
		{
			results[balancer.next().getWeight()] += 1;
		}

		assertThat(results[0]).isEqualTo(0);
		assertThat(results[1]).as(Integer.toString(results[1])).isLessThan(count / 2);
		assertThat(results[2]).as(Integer.toString(results[2])).isGreaterThan(count / 2);
	}
}
