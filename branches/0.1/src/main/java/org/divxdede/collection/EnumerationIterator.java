/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * EnumerationIterator.java is a part of this Commons library
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

import  java.util.*;

/**
 * Enumeration based on an {@link Iterator} or {@link Iterable}.<br>
 * This class is designed to be used with API that requires the old {@link Enumeration}
 * and you need to provide one from an {@link Iterator}
 *
 * @author André Sébastien - INFASS Systèmes (http://www.infass.com)
 */
public class EnumerationIterator<E> implements Enumeration<E>
{

    private Iterator<E> iterator = null;

    /** Constructor
     */
    public EnumerationIterator(Iterator<E> iterator)
    {   this.iterator = iterator; }
    
    /** Constructor
     */
    public EnumerationIterator(Iterable<E> iterable ) {
        this( iterable == null ? (Iterator<E>)null : iterable.iterator() );
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return  <code>true</code> if and only if this enumeration object
     *           contains at least one more element to provide;
     *          <code>false</code> otherwise.
     */
    public boolean hasMoreElements() {
        if( this.iterator == null ) return false;
        return this.iterator.hasNext();
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return     the next element of this enumeration.
     * @exception  NoSuchElementException  if no more elements exist.
     */
    public E nextElement() {
        if( iterator == null ) throw new NoSuchElementException();
        return this.iterator.next();
    }
}