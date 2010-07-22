/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * Disposable.java is a part of this Commons library
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
 * General interface describing objet that can be disposed.
 * <p>
 * A disposable objet can free some resources when requested by invoking the {@link #dispose()}.<br>
 * The objet can have some state changes, but should still usable.<br>
 * By example, if this interface apply to a collection, the <code>dispose</code> method should clear
 * the list but it's still remain usable.
 * <p>
 * In anyway, if the dispose make the objet unusable, you should advertise clearly with the javadoc.
 *
 * @author André Sébastien (divxdede)
 */
public interface Disposable {
    
    /** Dispose this objet by freeing some resources.<br>
     *  This object can have some state changes by this invocation, but remain usable.
     */
    public void dispose();
}
