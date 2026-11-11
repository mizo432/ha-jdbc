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
package net.sf.hajdbc.util.concurrent.cron;

import java.text.ParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CronExpressionBuilderTest
{
	@Test
	public void buildWithValidExpression()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		CronExpression expression = builder.expression("0 0 9 * * ?").build();
		
		Assertions.assertNotNull(expression);
		Assertions.assertEquals("0 0 9 * * ?", expression.toString());
	}
	
	@Test
	public void buildWithNullExpressionReturnsNull()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		CronExpression expression = builder.build();
		
		Assertions.assertNull(expression);
	}
	
	@Test
	public void buildWithInvalidExpressionThrows()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		builder.expression("invalid");
		
		Assertions.assertThrows(IllegalArgumentException.class, () -> builder.build());
	}
	
	@Test
	public void buildWithEmptyExpressionThrows()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		builder.expression("");
		
		Assertions.assertThrows(IllegalArgumentException.class, () -> builder.build());
	}
	
	@Test
	public void readFromExistingExpression() throws ParseException
	{
		CronExpression existing = new CronExpression("0 */5 * * * ?");
		
		CronExpressionBuilder builder = new CronExpressionBuilder();
		builder.read(existing);
		CronExpression built = builder.build();
		
		Assertions.assertNotNull(built);
		Assertions.assertEquals(existing.toString(), built.toString());
	}
	
	@Test
	public void chainedExpressionCall()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		CronExpression expression = builder
			.expression("0 0 9 * * ?")
			.expression("0 0 10 * * ?")
			.build();
		
		Assertions.assertNotNull(expression);
		Assertions.assertEquals("0 0 10 * * ?", expression.toString());
	}
	
	@Test
	public void buildEveryMinute()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		CronExpression expression = builder.expression("* * * * * ?").build();
		
		Assertions.assertNotNull(expression);
	}
	
	@Test
	public void buildWithMonthName()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		CronExpression expression = builder.expression("0 0 0 1 JAN ?").build();
		
		Assertions.assertNotNull(expression);
		Assertions.assertEquals("0 0 0 1 JAN ?", expression.toString());
	}
	
	@Test
	public void buildWithDayOfWeekName()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		CronExpression expression = builder.expression("0 0 9 ? * MON").build();
		
		Assertions.assertNotNull(expression);
		Assertions.assertEquals("0 0 9 ? * MON", expression.toString());
	}
	
	@Test
	public void buildWithRange()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		CronExpression expression = builder.expression("0 0 9-17 * * ?").build();
		
		Assertions.assertNotNull(expression);
		Assertions.assertEquals("0 0 9-17 * * ?", expression.toString());
	}
	
	@Test
	public void buildWithLastDayOfMonth()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		CronExpression expression = builder.expression("0 0 0 L * ?").build();
		
		Assertions.assertNotNull(expression);
	}
	
	@Test
	public void buildWithNthDayOfWeek()
	{
		CronExpressionBuilder builder = new CronExpressionBuilder();
		CronExpression expression = builder.expression("0 0 9 ? * 2#2").build();
		
		Assertions.assertNotNull(expression);
	}
}
