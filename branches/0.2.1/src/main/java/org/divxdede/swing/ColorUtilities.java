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
 * @author Romain Guy <romain.guy@mac.com> (HSLtoRGB part taken from swing-x library)
 */
public class ColorUtilities {

    private ColorUtilities() {
    }
    
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

        float[] hsl = RGBtoHSL( color.getRed(), color.getGreen(), color.getBlue() );
        if( hsl[2] == 0f ) {
            hsl[2] = factor;
        }
        else {
            hsl[2] = hsl[2] * (1f + factor);
            if( hsl[2] > 1f ) hsl[2] = 1f;
        }

        int[] rgb = HSLtoRGB( hsl[0] , hsl[1] , hsl[2] , null );
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

        float[] hsl = RGBtoHSL( color.getRed(), color.getGreen(), color.getBlue() );
        hsl[2] = hsl[2] * (1f - factor);

        int[] rgb = HSLtoRGB( hsl[0] , hsl[1] , hsl[2] , null );
        return new Color( rgb[0] , rgb[1] , rgb[2] , color.getAlpha() );
    }

    /** Derive a color to a translucent color.<br>
     *  Create a color from another one by replacing the <code>alpha</code> channel but keeping <code>rgb</code> components.
     *
     *  @param color Color to derive.
     *  @param alpha Alpha to use on the new derivated color. The alpha channel mst be in range [0 ~ 255]. 255 means an opaque color
     *  @return The translucent color
     *  @since 0.2
     */
    public static Color translucent(Color color , int alpha) {
        if( color.getAlpha() == alpha ) return color;
        return new Color( color.getRed() , color.getGreen() , color.getBlue() , alpha );
    }


    /**
     * <p>Returns the HSL (Hue/Saturation/Luminance) equivalent of a given
     * RGB color. All three HSL components are between 0.0 and 1.0.</p>
     *
     * Copyright 2005 Sun Microsystems, Inc., 4150 Network Circle,
     * Santa Clara, California 95054, U.S.A. All rights reserved.
     *
     * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
     * All rights reserved.
     *
     * @param color the RGB color to convert
     * @return a new array of 3 floats corresponding to the HSL components
     */
    private static float[] RGBtoHSL(Color color) {
        return RGBtoHSL(color.getRed(), color.getGreen(), color.getBlue(), null);
    }

    /**
     * <p>Returns the HSL (Hue/Saturation/Luminance) equivalent of a given
     * RGB color. All three HSL components are between 0.0 and 1.0.</p>
     *
     * Copyright 2005 Sun Microsystems, Inc., 4150 Network Circle,
     * Santa Clara, California 95054, U.S.A. All rights reserved.
     *
     * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
     * All rights reserved.
     *
     * @param color the RGB color to convert
     * @param hsl a pre-allocated array of floats; can be null
     * @return <code>hsl</code> if non-null, a new array of 3 floats otherwise
     * @throws IllegalArgumentException if <code>hsl</code> has a length lower
     *   than 3
     */
    private static float[] RGBtoHSL(Color color, float[] hsl) {
        return RGBtoHSL(color.getRed(), color.getGreen(), color.getBlue(), hsl);
    }

    /**
     * <p>Returns the HSL (Hue/Saturation/Luminance) equivalent of a given
     * RGB color. All three HSL components are between 0.0 and 1.0.</p>
     *
     * Copyright 2005 Sun Microsystems, Inc., 4150 Network Circle,
     * Santa Clara, California 95054, U.S.A. All rights reserved.
     *
     * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
     * All rights reserved.
     *
     * @param r the red component, between 0 and 255
     * @param g the green component, between 0 and 255
     * @param b the blue component, between 0 and 255
     * @return a new array of 3 floats corresponding to the HSL components
     */
    private static float[] RGBtoHSL(int r, int g, int b) {
        return RGBtoHSL(r, g, b, null);
    }

    /**
     * <p>Returns the HSL (Hue/Saturation/Luminance) equivalent of a given
     * RGB color. All three HSL components are floats between 0.0 and 1.0.</p>
     *
     * Copyright 2005 Sun Microsystems, Inc., 4150 Network Circle,
     * Santa Clara, California 95054, U.S.A. All rights reserved.
     *
     * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
     * All rights reserved.
     *
     * @param r the red component, between 0 and 255
     * @param g the green component, between 0 and 255
     * @param b the blue component, between 0 and 255
     * @param hsl a pre-allocated array of floats; can be null
     * @return <code>hsl</code> if non-null, a new array of 3 floats otherwise
     * @throws IllegalArgumentException if <code>hsl</code> has a length lower
     *   than 3
     */
    private static float[] RGBtoHSL(int r, int g, int b, float[] hsl) {
        if (hsl == null) {
            hsl = new float[3];
        } else if (hsl.length < 3) {
            throw new IllegalArgumentException("hsl array must have a length of" +
                                               " at least 3");
        }

        if (r < 0) r = 0;
        else if (r > 255) r = 255;
        if (g < 0) g = 0;
        else if (g > 255) g = 255;
        if (b < 0) b = 0;
        else if (b > 255) b = 255;

        float var_R = (r / 255f);
        float var_G = (g / 255f);
        float var_B = (b / 255f);

        float var_Min;
        float var_Max;
        float del_Max;

        if (var_R > var_G) {
            var_Min = var_G;
            var_Max = var_R;
        } else {
            var_Min = var_R;
            var_Max = var_G;
        }
        if (var_B > var_Max) {
            var_Max = var_B;
        }
        if (var_B < var_Min) {
            var_Min = var_B;
        }

        del_Max = var_Max - var_Min;

        float H, S, L;
        L = (var_Max + var_Min) / 2f;

        if (del_Max - 0.01f <= 0.0f) {
            H = 0;
            S = 0;
        } else {
            if (L < 0.5f) {
                S = del_Max / (var_Max + var_Min);
            } else {
                S = del_Max / (2 - var_Max - var_Min);
            }

            float del_R = (((var_Max - var_R) / 6f) + (del_Max / 2f)) / del_Max;
            float del_G = (((var_Max - var_G) / 6f) + (del_Max / 2f)) / del_Max;
            float del_B = (((var_Max - var_B) / 6f) + (del_Max / 2f)) / del_Max;

            if (var_R == var_Max) {
                H = del_B - del_G;
            } else if (var_G == var_Max) {
                H = (1 / 3f) + del_R - del_B;
            } else {
                H = (2 / 3f) + del_G - del_R;
            }
            if (H < 0) {
                H += 1;
            }
            if (H > 1) {
                H -= 1;
            }
        }

        hsl[0] = H;
        hsl[1] = S;
        hsl[2] = L;

        return hsl;
    }

    /**
     * <p>Returns the RGB equivalent of a given HSL (Hue/Saturation/Luminance)
     * color.</p>
     *
     * Copyright 2005 Sun Microsystems, Inc., 4150 Network Circle,
     * Santa Clara, California 95054, U.S.A. All rights reserved.
     *
     * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
     * All rights reserved.
     *
     * @param h the hue component, between 0.0 and 1.0
     * @param s the saturation component, between 0.0 and 1.0
     * @param l the luminance component, between 0.0 and 1.0
     * @return a new <code>Color</code> object equivalent to the HSL components
     */
    private static Color HSLtoRGB(float h, float s, float l) {
        int[] rgb = HSLtoRGB(h, s, l, null);
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    /**
     * <p>Returns the RGB equivalent of a given HSL (Hue/Saturation/Luminance)
     * color. All three RGB components are integers between 0 and 255.</p>
     *
     * Copyright 2005 Sun Microsystems, Inc., 4150 Network Circle,
     * Santa Clara, California 95054, U.S.A. All rights reserved.
     *
     * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
     * All rights reserved.
     *
     * @param h the hue component, between 0.0 and 1.0
     * @param s the saturation component, between 0.0 and 1.0
     * @param l the luminance component, between 0.0 and 1.0
     * @param rgb a pre-allocated array of ints; can be null
     * @return <code>rgb</code> if non-null, a new array of 3 ints otherwise
     * @throws IllegalArgumentException if <code>rgb</code> has a length lower
     *   than 3
     */
    private static int[] HSLtoRGB(float h, float s, float l, int[] rgb) {
        if (rgb == null) {
            rgb = new int[3];
        } else if (rgb.length < 3) {
            throw new IllegalArgumentException("rgb array must have a length of" +
                                               " at least 3");
        }

        if (h < 0) h = 0.0f;
        else if (h > 1.0f) h = 1.0f;
        if (s < 0) s = 0.0f;
        else if (s > 1.0f) s = 1.0f;
        if (l < 0) l = 0.0f;
        else if (l > 1.0f) l = 1.0f;

        int R, G, B;

        if (s - 0.01f <= 0.0f) {
            R = (int) (l * 255.0f);
            G = (int) (l * 255.0f);
            B = (int) (l * 255.0f);
        } else {
            float var_1, var_2;
            if (l < 0.5f) {
                var_2 = l * (1 + s);
            } else {
                var_2 = (l + s) - (s * l);
            }
            var_1 = 2 * l - var_2;

            R = (int) (255.0f * hue2RGB(var_1, var_2, h + (1.0f / 3.0f)));
            G = (int) (255.0f * hue2RGB(var_1, var_2, h));
            B = (int) (255.0f * hue2RGB(var_1, var_2, h - (1.0f / 3.0f)));
        }

        rgb[0] = R;
        rgb[1] = G;
        rgb[2] = B;

        return rgb;
    }

    /**
     * Copyright 2005 Sun Microsystems, Inc., 4150 Network Circle,
     * Santa Clara, California 95054, U.S.A. All rights reserved.
     *
     * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
     * All rights reserved.
     */
     private static float hue2RGB(float v1, float v2, float vH) {
        if (vH < 0.0f) {
            vH += 1.0f;
        }
        if (vH > 1.0f) {
            vH -= 1.0f;
        }
        if ((6.0f * vH) < 1.0f) {
            return (v1 + (v2 - v1) * 6.0f * vH);
        }
        if ((2.0f * vH) < 1.0f) {
            return (v2);
        }
        if ((3.0f * vH) < 2.0f) {
            return (v1 + (v2 - v1) * ((2.0f / 3.0f) - vH) * 6.0f);
        }
        return (v1);
    }
}