/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * Objects.java is a part of this Commons library
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
package org.divxdede.commons;

import java.util.Comparator;

/**
 * Help class for manipulating general objects.
 *
 * @author André Sébastien (divxdede)
 */
public abstract class Objects {
    
    /** Give the native toString representation of an object even if the method has been overrided.
     *  @param o Object to give the native toString() representation
     *  @return the Native toString
     */
    public static String toNativeString( Object o ) {
        if( o == null ) return String.valueOf(o);
        return o.getClass().getName() + "@" + Integer.toHexString( System.identityHashCode(o) );
    }

    /** Compare 2 comparables objects in a safe way allowing <code>null</code> instances without throw any <code>NullPointerException</code>.
     *  <p>
     *  This method is usefull when you implement a <code>compareTo</code> of an object using comparison of attributes members that can be nullable.
     *  <pre>
     *      ...
     *      // Compare Exemple Objects by their color and if color are equals by it's title.
     *      // Theses attributes (Color and Title) may be null
     *      public int compareTo(Exemple other  ) {
     *         int result = Objects.compare( this.getColor() , other.getColor() );
     *         if( result != 0 ) return result;
     *         return Objects.compare( this.getTitle() , other.getTitle() );
     *      }
     *  </pre>
     *  <p>
     *  If the first argument is <code>null</code> and the second is not. this method will call a <code>compareTo</code> on the second argument passing it a <code>null</code> value.<br>
     *  That means, you must handle in your <code>compareTo</code> implementation the case of receivine a <code>null</code> value in parameter.
     * 
     *  @param a the first object to be compared.
     *  @param b the second object to be compared.
     *  @return a negative integer, zero, or a positive integer as the
     * 	        first argument is less than, equal to, or greater than the
     *	        second.
     *  @throws ClassCastException if the arguments' types prevent them from
     * 	        being compared by this comparator.
     *  @since 0.2
     */
    public static int compare( Comparable a , Comparable b ) {
        if( a == b ) return 0;
        if( a == null ) return b.compareTo(a) * -1;
        return a.compareTo(b);
    }

    /** Create a Natural comparator (using the Comparable logic of an object) in a safe way allowing  <code>null</code> instances.<br>
     *  This comparator delegate all comparison to the {@link #compare(java.lang.Comparable, java.lang.Comparable)} method
     *
     * @param <T> Object type to be able to compare.
     * @param type Object class of objects to compare
     * @return A Natural safe comparator
     * @since 0.2
     */
    public static <T extends Comparable> Comparator<T> createNaturalComparator( Class<T> type ) {
        return new Comparator<T>() {
            public int compare(T o1, T o2) {
                return Objects.compare(o1, o1);
            }
        };
    }

    /** Create a safely comparator that handle <code>null</code> instances before delegate the comparison logic to the specified comparator.<br>
     *  This method allow to write a comparator without writing any <code>null</code> case in it's comparison logic and wrap it by a safe comparator that will do it.
     *  <p>
     *  This method requires a <code>nullOrder</code> describing how a <code>null</code> value must be ordered with <code>non-null</code> values
     *
     * @param <T> Type of objects to compare by the comparator to create
     * @param unsafe Unsafe comparator that can't handle <code>null</code> values in it's comparison logic.
     * @param nullOrder A negativetmeans that a <code>null</code> value is lesser than a <code>non-null</code> value.  A positive value means that a <code>null</code> value is greater than a <code>non-null</code> value.
     * @return Safe comparator that can handle <code>null</code> values
     * @since 0.2
     */
    public static <T> Comparator<T> createSafeComparator( final Comparator<T> unsafe , final int nullOrder ) {
        return new Comparator<T>() {
            public int compare(T o1, T o2) {
                if( o1 == o2 ) return 0;
                if( o1 == null ) return nullOrder;
                if( o2 == null ) return nullOrder * -1;
                return unsafe.compare(o1,o2);
            }
        };
    }

    /**
     * Standard <code>equals</code> comparison with <strong>null</strong> instances checking.<br>
     * This method return <code>true</code> if both parameters are <code>null</code>.
     *
     * @param o1 First object to test
     * @param o2 Second object to test
     * @return <code>true</code> if theses parameters are equals accordingly to theses equals methods.
     */
    public static boolean areEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 != null) {
            if( o1 == o2 )   return true;
            if( o2 == null ) return false;
            return o1.equals(o2);
        }
        return false;
    }
}
