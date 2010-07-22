/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * ColorUtilities.java is a part of this Commons library
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

/**
 * Color Utilities helping you to derive colors on a brighter or darker forms.<br>
 * Theses implementations convert colors into a HSL color model and increase or decrease the lightness.
 * <p>
 * The main difference using a HSL color model instead of HSB is that a pure blue color (0,0,255) can be brightned.<br>
 * With an HSB, a pure blue remain a pure blue even the factor used (like the {@link Color#brighter()} result.<br>
 *
 * @author André Sébastien (divxdede)
 */
public class ColorUtilities {

    /**
     * Creates a new <code>Color</code> that is a brighter version of the specified <code>Color</code>.
     * <p>
     * This method convert color's RGB components into an HSL color model.<br>
     * After what the <strong>Lightness</strong> component is <strong>increased</strong> of 25.1% (BLACK is brightned to GRAY)
     * <p>
     * Be carefull, this method don't provide same result than the {@link Color#brighter()} method.
     *
     * @return a new <code>Color</code> object that is a brighter version of the specified <code>Color</code>.
     */
    public static Color brighter(Color color) {
        return brighter( color , 0.251f );
    }

    /**
     * Creates a new <code>Color</code> that is a brighter version of the specified <code>Color</code>.
     * <p>
     * This method convert color's RGB components into an HSL color model.<br>
     * After what the <strong>Lightness</strong> component is <strong>increased</strong> with the specified factor <code>(lightness = lightness * (1+factor))</code><br>
     * <ul>
     *   <li>A factor of 0.0f will result in the same color as the original.</li>
     *   <li>A factor of 1.0f will result in the WHITE color</li>
     * </ul>
     * <p>
     * Be carefull, this method don't provide same result than the {@link Color#brighter()} method.
     * 
     * @return a new <code>Color</code> object that is a brighter version of the specified <code>Color</code>.
     */
    public static Color brighter(Color color, float factor) {
        if( factor == 0f ) return color;
        if( factor >= 1f ) return new Color(255,255,255, color.getAlpha() );

        float[] hsl = org.jdesktop.swingx.graphics.ColorUtilities.RGBtoHSL( color.getRed(), color.getGreen(), color.getBlue() );
        if( hsl[2] == 0f ) {
            hsl[2] = factor;
        }
        else {
            hsl[2] = hsl[2] * (1f + factor);
            if( hsl[2] > 1f ) hsl[2] = 1f;
        }

        int[] rgb = org.jdesktop.swingx.graphics.ColorUtilities.HSLtoRGB( hsl[0] , hsl[1] , hsl[2] , null );
        return new Color( rgb[0] , rgb[1] , rgb[2] , color.getAlpha() );
    }

    /**
     * Creates a new <code>Color</code> that is a darker version of the specified <code>Color</code>.
     * <p>
     * This method convert color's RGB components into an HSL color model.<br>
     * After what the <strong>Lightness</strong> component is <strong>decreased</strong> of 25.1%
     * <p>
     * Be carefull, this method don't provide same result than the {@link Color#darker()} method.
     *
     * @return a new <code>Color</code> object that is a darker version of the specified <code>Color</code>.
     */
    public static Color darker(Color color) {
        return darker(color, 0.251f);
    }

    /**
     * Creates a new <code>Color</code> that is a darker version of the specified <code>Color</code>.
     * <p>
     * This method convert color's RGB components into an HSL color model.<br>
     * After what the <strong>Lightness</strong> component is <strong>decreased</strong> with the specified factor <code>(lightness = lightness * (1-factor))</code><br>
     * <ul>
     *   <li>A factor of 0.0f will result in the same color as the original.</li>
     *   <li>A factor of 1.0f will result in the BLACK color</li>
     * </ul>
     * <p>
     * Be carefull, this method don't provide same result than the {@link Color#darker()} method.
     *
     * @return a new <code>Color</code> object that is a darker version of the specified <code>Color</code>.
     */
    public static Color darker(Color color, float factor) {
        if( factor == 0f ) return color;
        if( factor >= 1f ) return new Color(0,0,0, color.getAlpha() );

        float[] hsl = org.jdesktop.swingx.graphics.ColorUtilities.RGBtoHSL( color.getRed(), color.getGreen(), color.getBlue() );
        hsl[2] = hsl[2] * (1f - factor);

        int[] rgb = org.jdesktop.swingx.graphics.ColorUtilities.HSLtoRGB( hsl[0] , hsl[1] , hsl[2] , null );
        return new Color( rgb[0] , rgb[1] , rgb[2] , color.getAlpha() );
    }
}
