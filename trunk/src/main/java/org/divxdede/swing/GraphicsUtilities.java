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

    public static void paint3DBorder(Graphics2D g , int x, int y, int width, int height, int thickness , boolean raised) {
	Paint p = g.getPaint();
	Color c = g.getColor();
	Color brighter = ColorUtilities.brighter(c);
	Color darker = ColorUtilities.darker(c);

        if( thickness * 2 > height ) thickness = height / 2;
        if( thickness * 2 > width  ) thickness = width / 2;

	g.setColor( raised ? brighter : darker);
	g.fillRect(x, y , thickness , height);
	g.fillRect(x + thickness , y , width - (thickness * 2) , thickness );

	g.setColor(raised ? darker : brighter);
	g.fillRect(x + thickness, y + height - thickness, width - thickness, thickness);
	g.fillRect(x + width - thickness, y, thickness, height - thickness);

        // inner border
        g.setColor( raised ? c : darker );
        g.fillRect( x + 1 , y + 1 , width - 2 , thickness - 2 );                       // top
        g.fillRect( x + 1 , y + height - thickness + 1 , width - 2 , thickness - 2 );  // bottom
        g.fillRect( x + 1 , y + 1 , thickness - 2 , height - 2 );                      // left
        g.fillRect( x + width - thickness + 1 , y + 1 , thickness - 2 , height - 2 );  // right
        
	g.setPaint(p);
    }


    public static void paintRaisedBevelBorder(Graphics g, int x, int y, int width, int height, int thickness) {
        if (thickness < 2) {
            throw new IllegalArgumentException("Thichness must be greater than 1 and must be a pair number");
        }
        if (thickness % 2 != 0) {
            throw new IllegalArgumentException("Thichness must be greater than 1 and must be a pair number");
        }
        if( width < thickness * 2 ) {
            throw new IllegalArgumentException("Width must be greater or equals to " + (thickness * 2) + " (thickness *2)");
        }
        if( height < thickness * 2 ) {
            throw new IllegalArgumentException("Height must be greater or equals to " + (thickness * 2) + " (thickness *2)");
        }
        int weight = thickness / 2;

        Color oldColor = g.getColor();

        Color highlightOuterColor = ColorUtilities.brighter(oldColor,0.5f);
        Color highlightInnerColor = ColorUtilities.brighter(oldColor);
        Color shadowOuterColor    = ColorUtilities.darker(oldColor,0.5f);
        Color shadowInnerColor    = ColorUtilities.darker(oldColor);

        g.translate(x, y);
        g.setColor(highlightOuterColor);
        g.fillRect(0, 0, width - weight, weight);
        g.fillRect(0, weight, weight, height - thickness);

        g.setColor(highlightInnerColor);
        g.fillRect(weight, weight, width - thickness - weight, weight);
        g.fillRect(weight, thickness, weight, height - (thickness * 2));

        g.setColor(shadowOuterColor);
        g.fillRect(0, height - weight, width, weight);
        g.fillRect(width - weight, 0, weight, height - weight);

        g.setColor(shadowInnerColor);
        g.fillRect(weight, height - thickness, width - thickness, weight);
        g.fillRect(width - thickness, weight, weight, height - thickness - weight);

        g.translate(-x, -y);
        g.setColor(oldColor);
    }
}