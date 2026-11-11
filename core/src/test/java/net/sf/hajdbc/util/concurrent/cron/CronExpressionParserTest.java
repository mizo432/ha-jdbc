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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Tests for CronExpression class (which uses CronExpressionParser internally)
 */
public class CronExpressionParserTest {

    @Test
    public void parseEveryMinute() throws ParseException {
        CronExpression expr = new CronExpression("0 * * * * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseEveryHour() throws ParseException {
        CronExpression expr = new CronExpression("0 0 * * * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseDailyAtMidnight() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 * * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWeeklyOnMonday() throws ParseException {
        CronExpression expr = new CronExpression("0 0 9 ? * MON");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseMonthlyOnFirstDay() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 1 * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithMonthNames() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 1 JAN ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithDayNames() throws ParseException {
        CronExpression expr = new CronExpression("0 0 9 ? * MON,FRI");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithRange() throws ParseException {
        CronExpression expr = new CronExpression("0 0 9-17 * * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithIncrement() throws ParseException {
        CronExpression expr = new CronExpression("0 0/15 * * * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithWildcard() throws ParseException {
        CronExpression expr = new CronExpression("* * * * * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithLastDayOfMonth() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 L * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithNthDayOfWeek() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 ? * 5#2");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithQuestionMark() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 * * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithYear() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 1 1 ? 2099");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void isSatisfiedBy() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 * * ?");
        // A date at midnight should be satisfied
        assertThat(expr.isSatisfiedBy(new Date())).isNotNull();
    }

    @Test
    public void parseNullExpressionThrowsException() {
        assertThatThrownBy(() -> new CronExpression((String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    public void parseInvalidExpressionThrowsException() {
        assertThatThrownBy(() -> new CronExpression("invalid"))
                .isInstanceOf(ParseException.class);
    }

    @Test
    public void parseIncompleteExpressionThrowsException() throws ParseException {
        assertThatThrownBy(() -> new CronExpression("0 0"))
                .isInstanceOf(ParseException.class);
    }

    @Test
    public void copyConstructor() throws ParseException {
        CronExpression original = new CronExpression("0 0 0 * * ?");
        CronExpression copy = new CronExpression(original);
        Date now = new Date();
        assertThat(copy.getNextValidTimeAfter(now)).isEqualTo(original.getNextValidTimeAfter(now));
    }

    @Test
    public void parseWithLastDayOfWeek() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 ? * 5L");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }

    @Test
    public void parseWithWeekdayNearest() throws ParseException {
        CronExpression expr = new CronExpression("0 0 0 15W * ?");
        Date now = new Date();
        Date next = expr.getNextValidTimeAfter(now);
        assertThat(next).isAfter(now);
    }
}
