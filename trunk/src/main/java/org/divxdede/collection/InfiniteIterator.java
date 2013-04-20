/*
 * Copyright (c) 2011 ANDRE Sébastien (divxdede).  All rights reserved.
 * Condition.java is a part of this Commons library
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
package org.divxdede.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Create an infinite iterator from an {@link Iterable}.
 * <p>
 * This iterator will iterate the specified iterable until it reach the end.<br>
 * When it's the case, this iterator will instanciate a new iterator from the {@link Iterable} and continue to iterate over.<br>
 * This iterator never ends until the iterable becomes empty and provide an iterator without any element.
 * 
 * @author André Sébastien (divxdede)
 * @since 0.2.2
 */
public class InfiniteIterator<E> implements Iterator<E> {

    private final Iterable<E> iterable;
    private       Iterator<E> current = null;
    
    /** Create an infinite iterator from an {@link Iterable}
     */
    public InfiniteIterator( Iterable<E> iterable ) {
        this.iterable = iterable;
    }

    public boolean hasNext() {
        if( current != null ) {
            if( !current.hasNext() ) current = null; // start a new iterator
            else                     return true;
        }
        
        if( current == null ) {
            current = iterable.iterator();
            if( ! current.hasNext() ) return false; // iterable don't provide anymore elements, we are force to end here
            else                      return true;
        }
        return false;
    }

    public E next() {
        if( ! hasNext() ) throw new NoSuchElementException();
        return current.next();
    }

    public void remove() {
        if( current == null ) throw new IllegalStateException();
        current.remove();
    }
}