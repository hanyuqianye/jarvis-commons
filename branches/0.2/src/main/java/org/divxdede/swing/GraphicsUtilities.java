/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * GraphicsUtilities.java is a part of this Commons library
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
package org.divxdede.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 * Graphics Utilities helping you to paint some effects on graphics device.
 *
 * @author André Sébastien (divxdede)
 * @since 0.2
 */
public class GraphicsUtilities {

    /**
     * Paints a 3-D highlighted rectangle filled with the current <strong>paint</strong>.
     * The edges of the rectangle are highlighted so that it appears
     * as if the edges were beveled and lit from the upper left corner.
     * The colors used for the highlighting effect and for filling are
     * determined from the current <code>Color</code>.
     * <p>
     * <strong>Instead of the {@link Graphics2D#fill3DRect(int, int, int, int, boolean)}, 
     * this method uses the current <code>Paint</code> for filling the rectangle.
     *
     * @param x the x coordinate of the rectangle to be filled.
     * @param y the y coordinate of the rectangle to be filled.
     * @param       width the width of the rectangle to be filled.
     * @param       height the height of the rectangle to be filled.
     * @param       raised a boolean value that determines whether the
     *                      rectangle appears to be raised above the surface
     *                      or etched into the surface.
     * @see         java.awt.Graphics#fill3DRect
     */
    public static void paint3DRect( Graphics2D g , int x, int y, int width, int height, boolean raised) {
        Paint p = g.getPaint();
        Color c = g.getColor();
        Color brighter = ColorUtilities.brighter(c);
        Color darker = ColorUtilities.darker(c);

        if( p != c ) g.setPaint( p );
        else         g.setColor( raised ? c : darker);
        g.fillRect(x + 1 , y + 1 , width - 2 , height - 2);

        g.setColor( raised ? brighter : darker );
        g.fillRect(x , y , 1 , height);
        g.fillRect(x + 1 , y , width - 2 , 1);

        g.setColor( raised ? darker : brighter );
        g.fillRect(x + 1, y + height - 1, width - 1, 1);
        g.fillRect(x + width - 1, y, 1, height - 1);

        g.setPaint(p);
    }
}