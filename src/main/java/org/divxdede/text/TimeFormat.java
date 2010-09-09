/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * TimeFormat.java is a part of this Commons library
 * ====================================================================
 *
 * Commons library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or any later version.
 *
 * This is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.divxdede.text;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.concurrent.TimeUnit;

/**
 * <code>TimeFormat</code> is a concrete class for formatting and
 * parsing delay in a specified {@link TimeUnit} like MILLISECONDS, SECONDS, ...
 * <p>
 * A delay is formated like this scheme <strong><code>15d 21h 45m 32s 475ms 258?s 779ns</code></strong><br>
 * It's canonized in order to use greatest unit as possible.

 * @author André Sébastien (divxdede)
 * @since 0.2
 */
public class TimeFormat extends Format {

    private static final NumberFormat DAY_FORMAT          = new DecimalFormat("#,##0");
    private static final NumberFormat HOUR_FORMAT         = new DecimalFormat("00");
    private static final NumberFormat MINUTE_FORMAT       = new DecimalFormat("00");
    private static final NumberFormat SECOND_FORMAT       = new DecimalFormat("00");
    private static final NumberFormat MILLISECOND_FORMAT  = new DecimalFormat("###,000");
    private static final NumberFormat MICROSECOND_FORMAT  = new DecimalFormat("###,000");
    private static final NumberFormat NANOSECOND_FORMAT   = new DecimalFormat("###,000");

    private TimeUnit unit = null;


    /** Default Constructor with a <strong>MILLISECONDS</strong> timeformat
     */
    public TimeFormat() {
        this( TimeUnit.MILLISECONDS );
    }

    /** TimeFormat constructor for a specified unit.
     *  <p> 
     *  The {@link #format(Object)} will format a delay as ({@link Number} expressed in the specified {@link TimeUnit}.<br>
     *  The {@link #parseObject(String)} parse a string and result a delay as ({@link Number} expressed in the specified {@link TimeUnit}.
     * 
     *  @param unit Time Unit that will be used by this <code>TimeFormat</code> for configure format & parse behaviour
     */
    public TimeFormat(TimeUnit unit) {
        this.unit = unit;
    }

    /**
     * Formats a delay expressed in a specified unit with this TimeFormat.<br>
     * TimeUnit onversions from finer to coarser granularities truncate, so lose precision.
     * 
     * @param delay  Delay to format
     * @param unit TimeUnit of the specified delay
     * @return       Formatted string.
     * @exception IllegalArgumentException if the Format cannot format the given
     *            object
     */
    public String format(long delay, TimeUnit unit) {
        return format( getTimeUnit().convert( delay , unit ) );
    }

    /**
     * Formats an object and appends the resulting text to a given string
     * buffer.
     * If the <code>pos</code> argument identifies a field used by the format,
     * then its indices are set to the beginning and end of the first such
     * field encountered.
     *
     * @param obj    The object to format
     * @param toAppendTo    where the text is to be appended
     * @param pos    A <code>FieldPosition</code> identifying a field
     *               in the formatted text
     * @return       the string buffer passed in as <code>toAppendTo</code>,
     *               with formatted text appended
     * @exception NullPointerException if <code>toAppendTo</code> or
     *            <code>pos</code> is null
     * @exception IllegalArgumentException if the Format cannot format the given
     *            object
     */
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        long time     = ((Number)obj).longValue();
        long nanoTime = TimeUnit.NANOSECONDS.convert( time , this.getTimeUnit() );

        /** Separate fields
         */
        long nanos  = nanoTime;
        long micros = nanoTime / 1000;
             nanos  = nanos - (micros * 1000);
        long millis = micros / 1000;
             micros = micros - (millis * 1000);
        long secs   = millis / 1000;
             millis = millis - (secs * 1000);
        long mins   = secs / 60;
             secs   = secs - (mins * 60);
        long hours  = mins / 60;
             mins   = mins - (hours * 60);
        long days   = hours / 24;
             hours  = hours - (days * 24);

        boolean forced = false;
        if( days  > 0 ) {
            DAY_FORMAT.format( days , toAppendTo , pos );
            toAppendTo.append("d");
            forced  = (hours + mins + secs + millis + micros + nanos) > 0;
        }
        if( forced || hours > 0 ) {
            if( forced ) toAppendTo.append(' ');
            HOUR_FORMAT.format( hours , toAppendTo , pos );
            toAppendTo.append("h");
            forced  = (mins + secs + millis + micros + nanos) > 0;
        }
        if( forced || mins > 0 ) {
            if( forced ) toAppendTo.append(' ');
            MINUTE_FORMAT.format( mins , toAppendTo , pos );
            toAppendTo.append("m");
            forced  = (secs + millis + micros + nanos) > 0;
        }
        if( forced || secs > 0 ) {
            if( forced ) toAppendTo.append(' ');
            SECOND_FORMAT.format( secs , toAppendTo , pos );
            toAppendTo.append("s");
            forced  = (millis + micros + nanos) > 0;
        }
        if( forced || millis > 0 ) {
            if( forced ) toAppendTo.append(' ');
            MILLISECOND_FORMAT.format( millis , toAppendTo , pos );
            toAppendTo.append("ms");
            forced  = (micros + nanos) > 0;
        }
        if( forced || micros > 0 ) {
            if( forced ) toAppendTo.append(' ');
            MICROSECOND_FORMAT.format( micros , toAppendTo , pos );
            toAppendTo.append("?s");
            forced  = (nanos) > 0;
        }
        if( forced || nanos > 0 ) {
            if( forced ) toAppendTo.append(' ');
            NANOSECOND_FORMAT.format( nanos , toAppendTo , pos );
            toAppendTo.append("ns");
        }
        return toAppendTo;
    }

    /**
     * Parses text from a string to produce an object.
     * <p>
     * The method attempts to parse text starting at the index given by
     * <code>pos</code>.
     * If parsing succeeds, then the index of <code>pos</code> is updated
     * to the index after the last character used (parsing does not necessarily
     * use all characters up to the end of the string), and the parsed
     * object is returned. The updated <code>pos</code> can be used to
     * indicate the starting point for the next call to this method.
     * If an error occurs, then the index of <code>pos</code> is not
     * changed, the error index of <code>pos</code> is set to the index of
     * the character where the error occurred, and null is returned.
     *
     * @param source A <code>String</code>, part of which should be parsed.
     * @param pos A <code>ParsePosition</code> object with index and error
     *            index information as described above.
     * @return An <code>Object</code> parsed from the string. In case of
     *         error, returns null.
     * @exception NullPointerException if <code>pos</code> is null.
     */
    public Object parseObject(String source, ParsePosition pos) {
        if( source == null ) return 0L;

        long resultInNanos = 0L;

        int valueStartIndex = -1;
        int valueEndIndex = -1;
        TimeUnit valueUnit = null;
        for(int i = 0 ; i < source.length() ; i++ ) {
            char car = source.charAt(i);
            if( Character.isWhitespace(car) ) continue;
            if( Character.isDigit(car) ) {
                if( valueStartIndex == -1 ) valueStartIndex = i;
            }
            else {
                if( valueStartIndex != -1 )
                    valueEndIndex = i+1;
                
                if( valueEndIndex != -1 ) 
                {
                    char nextCar = i+1 < source.length() ? source.charAt(i+1) : ' ';
                    
                         if( car == 'd' )                  valueUnit = TimeUnit.DAYS;
                    else if( car == 'h' )                  valueUnit = TimeUnit.HOURS;
                    else if( car == 'm' && nextCar == 's') valueUnit = TimeUnit.MILLISECONDS;
                    else if( car == 'm' )                  valueUnit = TimeUnit.MINUTES;
                    else if( car == 's' )                  valueUnit = TimeUnit.SECONDS;
                    else if( car == '?' && nextCar == 's') valueUnit = TimeUnit.MICROSECONDS;
                    else if( car == 'n' && nextCar == 's') valueUnit = TimeUnit.NANOSECONDS;

                    Number value = null;
                    try {
                        value = (Number)NANOSECOND_FORMAT.parseObject( source.substring(valueStartIndex , valueEndIndex ) );
                    }
                    catch(ParseException pe) {
                        pos.setIndex( 0 );
                        pos.setErrorIndex( pe.getErrorOffset() + valueStartIndex );
                        return null;
                    }
                    resultInNanos += TimeUnit.NANOSECONDS.convert( value.longValue() , valueUnit );

                    if( valueUnit == TimeUnit.MILLISECONDS || valueUnit == TimeUnit.MICROSECONDS )
                        i++;

                    /** end this flag
                     */
                    valueStartIndex = -1;
                    valueEndIndex   = -1;
                    valueUnit       = null;
                }
                else {
                    pos.setIndex(0);
                    pos.setErrorIndex(i);
                    return null;
                }
            }
        }
        pos.setIndex( source.length() );
        return getTimeUnit().convert( resultInNanos , TimeUnit.NANOSECONDS );
    }

    /** Return the {@link TimeUnit} used by this TimeFormat.
     *  <p>
     *  The {@link #format(Object)} will format a delay as ({@link Number} expressed in the returned {@link TimeUnit}.<br>
     *  The {@link #parseObject(String)} parse a string and result a delay as ({@link Number} expressed in the returned {@link TimeUnit}.
     */
    public TimeUnit getTimeUnit() {
        return this.unit;
    }
}