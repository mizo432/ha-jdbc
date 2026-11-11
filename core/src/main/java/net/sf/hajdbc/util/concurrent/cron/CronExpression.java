/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */
package net.sf.hajdbc.util.concurrent.cron;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import net.sf.hajdbc.logging.Level;
import net.sf.hajdbc.logging.Logger;
import net.sf.hajdbc.logging.LoggerFactory;

/**
 * Provides a parser and evaluator for unix-like cron expressions. Cron 
 * expressions provide the ability to specify complex time combinations such as
 * &quot;At 8:00am every Monday through Friday&quot; or &quot;At 1:30am every 
 * last Friday of the month&quot;. 
 * <P>
 * Cron expressions are comprised of 6 required fields and one optional field
 * separated by white space. The fields respectively are described as follows:
 * 
 * <table cellspacing="8">
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Special Characters</th>
 * </tr>
 * <tr>
 * <td align="left"><code>Seconds</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Minutes</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Hours</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-23</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-31</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L W</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-11 or JAN-DEC</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-Week</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L #</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Year (Optional)</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>empty, 1970-2199</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * </table>
 * <P>
 * The '*' character is used to specify all values. For example, &quot;*&quot; 
 * in the minute field means &quot;every minute&quot;.
 * <P>
 * The '?' character is allowed for the day-of-month and day-of-week fields. It
 * is used to specify 'no specific value'. This is useful when you need to
 * specify something in one of the two fields, but not the other.
 * <P>
 * The '-' character is used to specify ranges For example &quot;10-12&quot; in
 * the hour field means &quot;the hours 10, 11 and 12&quot;.
 * <P>
 * The ',' character is used to specify additional values. For example
 * &quot;MON,WED,FRI&quot; in the day-of-week field means &quot;the days Monday,
 * Wednesday, and Friday&quot;.
 * <P>
 * The '/' character is used to specify increments. For example &quot;0/15&quot;
 * in the seconds field means &quot;the seconds 0, 15, 30, and 45&quot;. And 
 * &quot;5/15&quot; in the seconds field means &quot;the seconds 5, 20, 35, and
 * 50&quot;.  Specifying '*' before the  '/' is equivalent to specifying 0 is
 * the value to start with. Essentially, for each field in the expression, there
 * is a set of numbers that can be turned on or off. For seconds and minutes, 
 * the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to
 * 31, and for months 0 to 11 (JAN to DEC). The &quot;/&quot; character simply helps you turn
 * on every &quot;nth&quot; value in the given set. Thus &quot;7/6&quot; in the
 * month field only turns on month &quot;7&quot;, it does NOT mean every 6th 
 * month, please note that subtlety.  
 * <P>
 * The 'L' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for &quot;last&quot;, but it has different 
 * meaning in each of the two fields. For example, the value &quot;L&quot; in 
 * the day-of-month field means &quot;the last day of the month&quot; - day 31 
 * for January, day 28 for February on non-leap years. If used in the 
 * day-of-week field by itself, it simply means &quot;7&quot; or 
 * &quot;SAT&quot;. But if used in the day-of-week field after another value, it
 * means &quot;the last xxx day of the month&quot; - for example &quot;6L&quot;
 * means &quot;the last friday of the month&quot;. You can also specify an offset 
 * from the last day of the month, such as "L-3" which would mean the third-to-last 
 * day of the calendar month. <i>When using the 'L' option, it is important not to 
 * specify lists, or ranges of values, as you'll get confusing/unexpected results.</i>
 * <P>
 * The 'W' character is allowed for the day-of-month field.  This character 
 * is used to specify the weekday (Monday-Friday) nearest the given day.  As an 
 * example, if you were to specify &quot;15W&quot; as the value for the 
 * day-of-month field, the meaning is: &quot;the nearest weekday to the 15th of
 * the month&quot;. So if the 15th is a Saturday, the trigger will fire on 
 * Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the
 * 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th. 
 * However if you specify &quot;1W&quot; as the value for day-of-month, and the
 * 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not 
 * 'jump' over the boundary of a month's days.  The 'W' character can only be 
 * specified when the day-of-month is a single day, not a range or list of days.
 * <P>
 * The 'L' and 'W' characters can also be combined for the day-of-month 
 * expression to yield 'LW', which translates to &quot;last weekday of the 
 * month&quot;.
 * <P>
 * The '#' character is allowed for the day-of-week field. This character is
 * used to specify &quot;the nth&quot; XXX day of the month. For example, the 
 * value of &quot;6#3&quot; in the day-of-week field means the third Friday of 
 * the month (day 6 = Friday and &quot;#3&quot; = the 3rd one in the month). 
 * Other examples: &quot;2#1&quot; = the first Monday of the month and 
 * &quot;4#5&quot; = the fifth Wednesday of the month. Note that if you specify
 * &quot;#5&quot; and there is not 5 of the given day-of-week in the month, then
 * no firing will occur that month.  If the '#' character is used, there can
 * only be one expression in the day-of-week field (&quot;3#1,6#3&quot; is 
 * not valid, since there are two expressions).
 * <P>
 * <!--The 'C' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for "calendar". This means values are
 * calculated against the associated calendar, if any. If no calendar is
 * associated, then it is equivalent to having an all-inclusive calendar. A
 * value of "5C" in the day-of-month field means "the first day included by the
 * calendar on or after the 5th". A value of "1C" in the day-of-week field
 * means "the first day included by the calendar on or after Sunday".-->
 * <P>
 * The legal characters and the names of months and days of the week are not
 * case sensitive.
 * 
 * <p>
 * <b>NOTES:</b>
 * <ul>
 * <li>Support for specifying both a day-of-week and a day-of-month value is
 * not complete (you'll need to use the '?' character in one of these fields).
 * </li>
 * <li>Overflowing ranges is supported - that is, having a larger number on 
 * the left hand side than the right. You might do 22-2 to catch 10 o'clock 
 * at night until 2 o'clock in the morning, or you might have NOV-FEB. It is 
 * very important to note that overuse of overflowing ranges creates ranges 
 * that don't make sense and no effort has been made to determine which 
 * interpretation CronExpression chooses. An example would be 
 * "0 0 14-6 ? * FRI-MON". </li>
 * </ul>
 * </p>
 * 
 * 
 * @author Sharada Jambula, James House
 * @author Contributions from Mads Henderson
 * @author Refactoring from CronTrigger to CronExpression by Aaron Craven
 */
@SuppressWarnings({ "static-method", "unqualified-field-access", "unused" })
public final class CronExpression implements Serializable, Cloneable {

    private static final long serialVersionUID = 12423409423L;
    
    // Constants for field types - used by CronExpressionParser
    protected static final int SECOND = 0;
    protected static final int MINUTE = 1;
    protected static final int HOUR = 2;
    protected static final int DAY_OF_MONTH = 3;
    protected static final int MONTH = 4;
    protected static final int DAY_OF_WEEK = 5;
    protected static final int YEAR = 6;
    protected static final int ALL_SPEC_INT = 99; // '*'
    protected static final int NO_SPEC_INT = 98; // '?'
    protected static final Integer ALL_SPEC = ALL_SPEC_INT;
    protected static final Integer NO_SPEC = NO_SPEC_INT;

    private final String cronExpression;
    private TimeZone timeZone = null;
    protected transient TreeSet<Integer> seconds;
    protected transient TreeSet<Integer> minutes;
    protected transient TreeSet<Integer> hours;
    protected transient TreeSet<Integer> daysOfMonth;
    protected transient TreeSet<Integer> months;
    protected transient TreeSet<Integer> daysOfWeek;
    protected transient TreeSet<Integer> years;

    protected transient boolean lastdayOfWeek = false;
    protected transient int nthdayOfWeek = 0;
    protected transient boolean lastdayOfMonth = false;
    protected transient boolean nearestWeekday = false;
    protected transient int lastdayOffset = 0;
    protected transient boolean expressionParsed = false;
    
    public static final int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 100;

    /**
     * Constructs a new <CODE>CronExpression</CODE> based on the specified 
     * parameter.
     * 
     * @param cronExpression String representation of the cron expression the
     *                       new object should represent
     * @throws java.text.ParseException
     *         if the string expression cannot be parsed into a valid 
     *         <CODE>CronExpression</CODE>
     */
    public CronExpression(String cronExpression) throws ParseException {
        if (cronExpression == null) {
            throw new IllegalArgumentException("cronExpression cannot be null");
        }
        
        this.cronExpression = cronExpression.toUpperCase(Locale.US);
        
        buildExpression(this.cronExpression);
    }
    
    /**
     * Constructs a new {@code CronExpression} as a copy of an existing
     * instance.
     * 
     * @param expression
     *            The existing cron expression to be copied
     */
	public CronExpression(CronExpression expression) {
        /*
         * We don't call the other constructor here since we need to swallow the
         * ParseException. We also elide some of the sanity checking as it is
         * not logically trippable.
         */
        this.cronExpression = expression.getCronExpression();
        try {
            buildExpression(cronExpression);
        } catch (ParseException ex) {
            throw new AssertionError();
        }
        if (expression.getTimeZone() != null) {
            setTimeZone((TimeZone) expression.getTimeZone().clone());
        }
    }

    /**
     * Indicates whether the given date satisfies the cron expression. Note that
     * milliseconds are ignored, so two Dates falling on different milliseconds
     * of the same second will always have the same result here.
     * 
     * @param date the date to evaluate
     * @return a boolean indicating whether the given date satisfies the cron
     *         expression
     */
    public boolean isSatisfiedBy(Date date) {
        Calendar testDateCal = Calendar.getInstance(getTimeZone());
        testDateCal.setTime(date);
        testDateCal.set(Calendar.MILLISECOND, 0);
        Date originalDate = testDateCal.getTime();
        
        testDateCal.add(Calendar.SECOND, -1);
        
        Date timeAfter = getTimeAfter(testDateCal.getTime());

        return ((timeAfter != null) && (timeAfter.equals(originalDate)));
    }
    
    /**
     * Returns the next date/time <I>after</I> the given date/time which
     * satisfies the cron expression.
     * 
     * @param date the date/time at which to begin the search for the next valid
     *             date/time
     * @return the next valid date/time
     */
    public Date getNextValidTimeAfter(Date date) {
        return getTimeAfter(date);
    }
    
    /**
     * Returns the next date/time <I>after</I> the given date/time which does
     * <I>not</I> satisfy the expression
     * 
     * @param date the date/time at which to begin the search for the next 
     *             invalid date/time
     * @return the next valid date/time
     */
    public Date getNextInvalidTimeAfter(Date date) {
        //move back to the nearest second so differences will be accurate
        Calendar adjustCal = Calendar.getInstance(getTimeZone());
        adjustCal.setTime(date);
        adjustCal.set(Calendar.MILLISECOND, 0);
        Date lastDate = adjustCal.getTime();
        
        Date newDate;
        
        // Get the next fire time after the given date
        newDate = getTimeAfter(lastDate);
        if (newDate == null) {
            return new Date(lastDate.getTime() + 1000);
        }
        
        // Use exponential backoff to find consecutive seconds efficiently
        // First, check if the next fire time is exactly 1 second after
        long difference = newDate.getTime() - lastDate.getTime();
        
        if (difference != 1000) {
            // Not consecutive, so lastDate is already the last valid fire time
            return new Date(lastDate.getTime() + 1000);
        }
        
        // Check for a sequence of consecutive seconds using exponential backoff
        // to avoid iterating one second at a time for long sequences
        Date probeDate = newDate;
        int maxIterations = 1000000; // Safety limit to prevent infinite loops
        int iterations = 0;
        
        while (difference == 1000 && iterations < maxIterations) {
            iterations++;
            lastDate = probeDate;
            probeDate = getTimeAfter(probeDate);
            
            if (probeDate == null) {
                break;
            }
            
            difference = probeDate.getTime() - lastDate.getTime();
        }
        
        return new Date(lastDate.getTime() + 1000);
    }
    
    private CronExpressionEvaluator getEvaluator() {
        return new CronExpressionEvaluator(this);
    }
    
    /**
     * Returns the time zone for which this <code>CronExpression</code> 
     * will be resolved.
     */
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        return timeZone;
    }

    /**
     * Sets the time zone for which  this <code>CronExpression</code> 
     * will be resolved.
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    /**
     * Returns the string representation of the <CODE>CronExpression</CODE>
     * 
     * @return a string representation of the <CODE>CronExpression</CODE>
     */
    @Override
    public String toString() {
        return cronExpression;
    }

    /**
     * Indicates whether the specified cron expression can be parsed into a 
     * valid cron expression
     * 
     * @param cronExpression the expression to evaluate
     * @return a boolean indicating whether the given expression is a valid cron
     *         expression
     */
    public static boolean isValidExpression(String cronExpression) {
        
        try {
            new CronExpression(cronExpression);
        } catch (ParseException pe) {
            return false;
        }
        
        return true;
    }

    public static void validateExpression(String cronExpression) throws ParseException {
        
        new CronExpression(cronExpression);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    //
    // Expression Parsing Functions
    //
    ////////////////////////////////////////////////////////////////////////////

    protected void buildExpression(String expression) throws ParseException {
        expressionParsed = true;
        CronExpressionParser parser = new CronExpressionParser(this);
        parser.parse(expression);
    }

    public String getCronExpression() {
        return cronExpression;
    }
    
    public String getExpressionSummary() {
        StringBuilder buf = new StringBuilder();

        buf.append("seconds: ");
        buf.append(getExpressionSetSummary(seconds));
        buf.append("\n");
        buf.append("minutes: ");
        buf.append(getExpressionSetSummary(minutes));
        buf.append("\n");
        buf.append("hours: ");
        buf.append(getExpressionSetSummary(hours));
        buf.append("\n");
        buf.append("daysOfMonth: ");
        buf.append(getExpressionSetSummary(daysOfMonth));
        buf.append("\n");
        buf.append("months: ");
        buf.append(getExpressionSetSummary(months));
        buf.append("\n");
        buf.append("daysOfWeek: ");
        buf.append(getExpressionSetSummary(daysOfWeek));
        buf.append("\n");
        buf.append("lastdayOfWeek: ");
        buf.append(lastdayOfWeek);
        buf.append("\n");
        buf.append("nearestWeekday: ");
        buf.append(nearestWeekday);
        buf.append("\n");
        buf.append("NthDayOfWeek: ");
        buf.append(nthdayOfWeek);
        buf.append("\n");
        buf.append("lastdayOfMonth: ");
        buf.append(lastdayOfMonth);
        buf.append("\n");
        buf.append("years: ");
        buf.append(getExpressionSetSummary(years));
        buf.append("\n");

        return buf.toString();
    }

    protected String getExpressionSetSummary(java.util.Set<Integer> set) {

        if (set.contains(NO_SPEC)) {
            return "?";
        }
        if (set.contains(ALL_SPEC)) {
            return "*";
        }

        StringBuilder buf = new StringBuilder();

        Iterator<Integer> itr = set.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = itr.next();
            String val = iVal.toString();
            if (!first) {
                buf.append(",");
            }
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    protected String getExpressionSetSummary(java.util.ArrayList<Integer> list) {

        if (list.contains(NO_SPEC)) {
            return "?";
        }
        if (list.contains(ALL_SPEC)) {
            return "*";
        }

        StringBuilder buf = new StringBuilder();

        Iterator<Integer> itr = list.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = itr.next();
            String val = iVal.toString();
            if (!first) {
                buf.append(",");
            }
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    TreeSet<Integer> getSet(int type) {
        switch (type) {
            case SECOND:
                return seconds;
            case MINUTE:
                return minutes;
            case HOUR:
                return hours;
            case DAY_OF_MONTH:
                return daysOfMonth;
            case MONTH:
                return months;
            case DAY_OF_WEEK:
                return daysOfWeek;
            case YEAR:
                return years;
            default:
                return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Computation Functions
    //
    ////////////////////////////////////////////////////////////////////////////

    public Date getTimeAfter(Date afterTime) {
        return getEvaluator().getTimeAfter(afterTime);
    }

    /**
     * Returns the time before the given time
     * that the <code>CronExpression</code> matches.
     * 
     * @param endTime the date/time to look before
     * @return the next date/time before the given date/time
     */
    public Date getTimeBefore(Date endTime) {
        if (endTime == null) {
            throw new IllegalArgumentException("endTime cannot be null");
        }
        
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.setTime(endTime);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.SECOND, -1);
        
        Date previousRoundSecondDate = cal.getTime();
        Date result = getTimeAfter(previousRoundSecondDate);
        
        if (result != null && result.after(endTime)) {
            return null;
        }
        
        return result;
    }

    /**
     * Returns the final time that the 
     * <code>CronExpression</code> will match.
     * 
     * @return the final fire date/time for this expression, or null if the expression has no upper bound
     */
    public Date getFinalFireTime() {
        TreeSet<Integer> yearSet = getSet(YEAR);
        
        if (yearSet == null || yearSet.isEmpty() || yearSet.contains(ALL_SPEC)) {
            return null;
        }
        
        int maxYear = yearSet.last();
        
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.YEAR, maxYear);
        
        Date startOfYear = cal.getTime();
        Date lastFire = getTimeAfter(startOfYear);
        
        if (lastFire == null) {
            return null;
        }
        
        Date nextFire;
        while ((nextFire = getTimeAfter(lastFire)) != null) {
            cal.setTime(nextFire);
            if (cal.get(Calendar.YEAR) > maxYear) {
                break;
            }
            lastFire = nextFire;
        }
        
        return lastFire;
    }
    

    private static final Logger LOGGER = LoggerFactory.getLogger(CronExpression.class);

    private void readObject(java.io.ObjectInputStream stream)
        throws java.io.IOException, ClassNotFoundException {
        
        stream.defaultReadObject();
        try {
            buildExpression(cronExpression);
        } catch (Exception e) {
            LOGGER.log(Level.WARN, e, "Failed to rebuild cron expression during deserialization");
        }
    }    
    
    @Override
    @Deprecated
    public Object clone() {
        return new CronExpression(this);
    }
}
