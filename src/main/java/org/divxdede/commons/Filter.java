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
package org.divxdede.commons;

/**
 * Represent any filter rule that can accept or refuse an item.
 * @author André Sébastien (divxdede)
 * @since 0.2.2
 */
public interface Filter<E> {

    /** Accept or refuse the specified item.
     *  @param object Item to check
     *  @return  <code>true</code> if the specified item is accepted. <code>false</code> otherwise.
     */
    public boolean accept(E object);
}