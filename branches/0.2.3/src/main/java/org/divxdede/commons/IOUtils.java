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

import java.io.Closeable;
import java.io.IOException;

/**
 * Class tools providing convenience methods for closing streams or any disposable objects.
 * 
 * @author André Sébastien (divxdede)
 * @since 0.2.2
 */
public class IOUtils {
    
    /** Close the specified {@link Closeable} object.<br>
     * Exemple:
     * <pre>
     *    InputStream is = ....;
     *    try {
     *      ...
     *    }
     *    finally {
     *       is = IOUtils.close(is);
     *    }
     *  </pre>
     * 
     * 
     * @param closeable Object to close
     * @return <code>null</code> if the object was closed successfully. return the object otherwise
     */
    public static Closeable close(Closeable closeable) {
        if( closeable == null ) return null;
        try {
            closeable.close();
            return null;
        }
        catch(IOException e) {
            return closeable;
        }
    }
    
    /** Close the specified {@link Disposable} object/
     * @param disposable Object to dispose
     * @return <code>null</code> if the object was disposed successfully. return the object otherwise
     */
    public static Disposable dispose(Disposable disposable) {
        if( disposable == null ) return null;
        disposable.dispose();
        return null;
    }
}