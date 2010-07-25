/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * Arrays.java is a part of this Commons library
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
 * Utilities for manipulating arrays.
 * <p>
 * <ul>
 *  <li>{@link #random(T[])} for randomize items inside an array</li>
 * </ul>
 *
 * @author André Sébastien (divxdede)
 * @since 0.2
 */
public class Arrays {

    /* Randomize the specified array.<br>
     * The randomize algorithm use {@link Math#random()} method.
     *
     *  @param array Array to randomize
     */
    public static <T> T[] random(T[] array) {

        RandomizedItem[] randArray = new RandomizedItem[ array.length ];
        for( int i = 0 ; i < array.length ; i++ ) {
            randArray[i] = new RandomizedItem( array[i] );
        }
        java.util.Arrays.sort(randArray); // It's sort randomly

        for( int i = 0 ; i < array.length ; i++ ) {
            array[i] = (T)randArray[i].getItem();
        }

        return array;
    }


    /** Randomizer object
     *  Private implementation
     */
    private static class RandomizedItem implements Comparable<RandomizedItem> {

        private Object item       = null;
        private double randomizer = Math.random();

        public RandomizedItem(Object o) {
            this.item = o;
        }

        public Object getItem() {
            return this.item;
        }

        public double getRandomizer() {
            return this.randomizer;
        }

        public int compareTo(RandomizedItem o) {
            if( this == o ) return 0;
            if( o == null ) return -1;

            if( this.getRandomizer() < o.getRandomizer() ) return -1;
            if( this.getRandomizer() > o.getRandomizer() ) return 1;
            return 0;
        }
    }
}