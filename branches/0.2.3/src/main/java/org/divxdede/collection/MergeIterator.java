/*
 * Copyright (c) 2010 INFASS Systèmes (http://www.infass.com) All rights reserved.
 * MergeIterator.java is a part of this Commons library
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
 * A merge iterator instanciate from some others {@link Iterator}.
 * <p>
 * This implementation iterate sequentially over each iterators (in the parameter's order).
 *
 * @author André Sébastien (divxdede)
 */
public class MergeIterator<E> implements Iterator<E> {
    
    private Iterator<E>[] iterators       = null;
    private int           index           = -1;
    private Iterator<E>   currentIterator = null;
    
    /** Construtor from iterators
     *  @param iterators Iterators to merge
     */
    public MergeIterator(Iterator<E>... iterators) {
        this.iterators = iterators;
    }
    
    /** Constructor from iterable
     *  @param iterables Iterable to merge
     */
    public MergeIterator(Iterable<E>... iterables) {
        if( iterables == null) return;
        
        this.iterators = new Iterator[ iterables.length ];
        
        for(int i = 0 ; i < iterables.length ; i++ ) {
            if( iterables[i] == null ) continue;
            this.iterators[i] = iterables[i].iterator();
        }
    }
    
    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        if( currentIterator != null ) {
            if( currentIterator.hasNext() ) return true;
            
            currentIterator = null;
        }
        
        if( iterators == null )         return false;
        
        index = index + 1;
        if( index >= iterators.length ) return false;
        
        currentIterator = iterators[index];
        return hasNext();
    }
    
    /**
     * Returns the next element in the iteration.  Calling this method
     * repeatedly until the {@link #hasNext()} method returns false will
     * return each element in the underlying collection exactly once.
     *
     * @return the next element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     */
    public E next() {
        if( ! hasNext() ) throw new NoSuchElementException();
        
        return this.currentIterator.next();
    }
    
    /**
     * 
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).  This method can be called only once per
     * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
     * the underlying collection is modified while the iteration is in
     * progress in any way other than by calling this method.
     *
     * @exception UnsupportedOperationException if the <tt>remove</tt>
     *		  operation is not supported by this Iterator.
     
     * @exception IllegalStateException if the <tt>next</tt> method has not
     *		  yet been called, or the <tt>remove</tt> method has already
     *		  been called after the last call to the <tt>next</tt>
     *		  method.
     */
    public void remove() {
        if( this.currentIterator == null ) throw new UnsupportedOperationException();
        this.currentIterator.remove();
    }
}