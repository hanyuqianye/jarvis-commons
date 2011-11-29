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

import org.divxdede.commons.Filter;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator filtering an underlying terator using a {@link Filter} as rule for accepting/rejecting entries.
 * <p>
 * This iterator don't support the {@link #remove()} method and throw <code>UnsupportedOperationException</code>
 * 
 * @author André Sébastien (divxdede)
 * @since 0.2.2
 */
public class FilteredIterator<E> implements Iterator<E> {

    private final Filter<E>   filter;
    private final Iterator<E> iterator;
    
    private boolean poolNext = false;
    private E       next     = null;
    
    /** Create an filtered iterator from a filter and an underlying iterator to filter
     * @param filter Filter to use as the rule for accept or reject entries.
     * @param iterator Iterator to filter.
     */
    public FilteredIterator(Filter<E> filter , Iterator<E> iterator) {
        this.filter   = filter;
        this.iterator = iterator;
    }
    
    @Override
    public boolean hasNext() {
        if( poolNext ) return true;
        while( iterator.hasNext() ) {
            next = iterator.next();
            if( this.filter == null || this.filter.accept(next) ) {
                poolNext = true;
                return true;
            }
        }
        next      = null;
        poolNext = false;
        return false;
    }

    @Override
    public E next() {
        if( ! hasNext() ) throw new NoSuchElementException();
        try {
            return next;
        }
        finally {
            next     = null;
            poolNext = false;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}