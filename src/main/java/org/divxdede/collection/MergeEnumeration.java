/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * MergeEnumeration.java is a part of this Commons library
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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A merge enumeration instanciate from some others {@link Enumeration}.
 * <p>
 * This implementation iterate sequentially over each enumeration (in the parameter's order).
 *
 * @author André Sébastien - INFASS Systèmes (http://www.infass.com)
 */
public class MergeEnumeration<E> implements Enumeration<E>
{
    
    private Enumeration<E>[] enumerations       = null;
    private int              index              = -1;
    private Enumeration<E>   currentEnumeration = null;
    
    /** Construtor from enumerations
     *  @param enumerations Enumerations to merge
     */
    public MergeEnumeration(Enumeration<E>... enumerations)
    {   this.enumerations = enumerations; }
    
    /**
     * Tests if this enumeration contains more elements.
     *
     * @return  <code>true</code> if and only if this enumeration object
     *           contains at least one more element to provide;
     *          <code>false</code> otherwise.
     */
    public boolean hasMoreElements()
    {   if( currentEnumeration != null )
        {   if( currentEnumeration.hasMoreElements() ) return true;
            
            currentEnumeration = null;
        }
        
        if( enumerations == null )         return false;
        
        index = index + 1;
        if( index >= enumerations.length ) return false;
        
        currentEnumeration = enumerations[index];
        return hasMoreElements();
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return     the next element of this enumeration.
     * @exception  NoSuchElementException  if no more elements exist.
     */
    public E nextElement()
    {   if( ! hasMoreElements() )
            throw new NoSuchElementException();
        
        return this.currentEnumeration.nextElement();
    }
}