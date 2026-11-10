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
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class CronExpressionTest
{
	@Test
	public void parseEveryMinute() throws ParseException
	{
		CronExpression expression = new CronExpression("0 * * * * ?");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseEveryHour() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 * * * ?");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseDailyAtMidnight() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 0 * * ?");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseWeeklyOnMonday() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 9 ? * MON");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseMonthly() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 0 1 * ?");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseWithRange() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 9-17 * * ?");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseWithStep() throws ParseException
	{
		CronExpression expression = new CronExpression("0 */5 * * * ?");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseWithMultipleValues() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 9,12,15 * * ?");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseInvalidExpression()
	{
		Assert.assertThrows(ParseException.class, () -> new CronExpression("invalid"));
	}
	
	@Test
	public void parseEmptyExpression()
	{
		Assert.assertThrows(ParseException.class, () -> new CronExpression(""));
	}
	
	@Test
	public void getNextValidTimeAfter() throws ParseException
	{
		CronExpression expression = new CronExpression("0 * * * * ?");
		Date now = new Date();
		Date next = expression.getNextValidTimeAfter(now);
		
		Assert.assertNotNull(next);
		Assert.assertTrue(next.after(now));
	}
	
	
	@Test
	public void parseWithMonthName() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 0 1 JAN ?");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseWithDayName() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 9 ? * MON-FRI");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseWithLastDayOfMonth() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 0 L * ?");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseWithNthDayOfWeek() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 9 ? * 2#2");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void expressionToString() throws ParseException
	{
		String expressionString = "0 0 9 * * ?";
		CronExpression expression = new CronExpression(expressionString);
		
		Assert.assertEquals(expressionString, expression.toString());
	}
	
	@Test
	public void parseWithYear() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 0 1 1 ? 2025");
		Assert.assertNotNull(expression);
	}
	
	@Test
	public void parseWithWildcardYear() throws ParseException
	{
		CronExpression expression = new CronExpression("0 0 0 1 1 ? *");
		Assert.assertNotNull(expression);
	}
}
