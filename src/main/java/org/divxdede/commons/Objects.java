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
            return o1.equals(o2);
        }
        return false;
    }
}
