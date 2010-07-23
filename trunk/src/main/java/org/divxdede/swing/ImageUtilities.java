/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * ImageUtilities.java is a part of this Commons library
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

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * Utilities for manipulating Images like resizing (with Hadware benefits).
 * @author André Sébastien (divxdede)
 */
public abstract class ImageUtilities {
    
    // Used for waitFor() method
    protected final static Component    component      = new Component() {};
    protected final static MediaTracker tracker        = new MediaTracker(component);    
    private         static int          mediaTrackerID = 0;
    
    /** Convert an image into a BufferedImage
     *  If the source is already a buffered image this method return it casted
     *  @param image
     *  @return BufferedImage
     */
    public static BufferedImage toBufferedImage(Image image)
    {   if( image == null ) return null;
        
        if( image instanceof BufferedImage ) return (BufferedImage)image;
        return getScaledImage(image,-1,-1);
    }

    /** Wait for this image until it is completely loaded
     *  @param image to wait for complete loading
     *  @return true if this image is completely loaded after this call
     */
    public static boolean waitFor(Image image)
    {   if( image == null ) return true;
        
        synchronized(tracker)
        {
            int id = ++mediaTrackerID;
            
            tracker.addImage(image, id);
	    try 
            {
		tracker.waitForID(id, 0);
	    }
            catch (InterruptedException e) 
            {
		System.out.println("INTERRUPTED while loading Image");
	    }
            
            try
            {
                if( tracker.statusID(id, false) != tracker.COMPLETE ) return false;
                return true;
            }
            finally
            {
                tracker.removeImage(image, id);
            }
        }
    }
    
    /** Creates a scaled version of an image that can fit a certain width and height
     *  this method use getScaledImage(...) method
     *  @param image Image to scale
     *  @param maxWidth Maximum width usable
     *  @param maxHeight Maximum height usable
     */
    public static BufferedImage getFittedImage(Image image, int maxWidth , int maxHeight)
    {   
        if( image == null ) return null;
        waitFor(image);
        
        int width   = image.getWidth(null);
        int height  = image.getHeight(null);
        float ratio = (float)width / (float)height;
        
        int widthScaled  = (int)(maxHeight * ratio);
        int heighScaled  = (int)(maxWidth / ratio);
        
        if( widthScaled <= maxWidth ) return getScaledImage(image , -1 , maxHeight );
        return getScaledImage(image , maxWidth , -1 );
    }
    
    /**
     * Creates a scaled version of an image using Graphics2D (hardware resize benefit)
     * A new <code>Image</code> object is returned which will render 
     * the image at the specified <code>width</code> and 
     * <code>height</code> by default.
     * <p>
     * If either <code>width</code> 
     * or <code>height</code> is a negative number then a value is 
     * substituted to maintain the aspect ratio of the original image 
     * dimensions. If both <code>width</code> and <code>height</code>
     * are negative, then the original image dimensions are used.
     *
     * @param width the width to which to scale the image.
     * @param height the height to which to scale the image.
     * @return     a scaled version of the image.
     * @exception IllegalArgumentException if <code>width</code>
     *             or <code>height</code> is zero.
     */
    public static BufferedImage getScaledImage(Image image, int width, int height) 
    {   if( width == 00 || height == 0 ) throw new IllegalArgumentException("size can't be set to 0 for resampling");
        if( image == null )              throw new IllegalArgumentException("image can't be null");
        
        // ensure this image is completely loaded
        waitFor(image);
        
        // Original ratio
        float   originalRatio = (float)image.getWidth(null) / (float) image.getHeight(null);
        boolean aspectRatioRespected = width < 0 || height < 0;
        boolean sizePreserved = width < 0 && height < 0;

        // new size
        int newWidth  = width;
        int newHeight = height;

        // if all size are to 0, don't perform a resising (it's use for image conversion into a BufferedImage)
        if( newWidth < 0 && newHeight < 0 ) 
        {
            newWidth  = image.getWidth(null);
            newHeight = image.getHeight(null);
        }
        else
        {
             if ( newWidth  < 0 ) newWidth  = (int)(newHeight * originalRatio);
             else if ( newHeight < 0 )  newHeight = (int)(newWidth / originalRatio);
             else {
                /** Precision is not garanteed, but it's not a big problem here
                 */
                aspectRatioRespected = ( (float)newWidth / (float)newHeight ) == originalRatio;
             }
        }
        sizePreserved = sizePreserved || ( newWidth == image.getWidth(null) && newHeight == image.getWidth(null) );

        /** define type of destination image from source image
         */
        int opacity = Transparency.OPAQUE;
        int type    = BufferedImage.TYPE_INT_RGB;
        if( image instanceof BufferedImage ) {
            opacity = ((BufferedImage)image).getTransparency();
            type    = ((BufferedImage)image).getType();
        }

        /** Indicate if it's a recuce scale or not
         *  It's better to reduce an image by factor of 2 until we reach the desired size.
         *  So, if the ratio is preserved and if this scaling is a reduce scale, we will go by iteration
         */
        boolean reducingByIteration = aspectRatioRespected && !sizePreserved && newWidth <= image.getWidth(null) && newHeight <= image.getHeight(null);
        if( reducingByIteration ) {
            Image   source      = image;
            boolean isWidth     = image.getWidth(null) > image.getHeight(null);
            int     currentSize = isWidth ? image.getWidth(null) : image.getHeight(null);
            int     refSize     = isWidth ? newWidth : newHeight;
            float   ratio       = isWidth ? ( (float)newHeight / (float)newWidth ) : ( (float)newWidth / (float)newHeight );

            while( currentSize > refSize ) {
                int myWidth  = 0;
                int myHeight = 0;
                
                currentSize /= 2;
                if( currentSize < refSize ) {
                    myWidth  = newWidth;
                    myHeight = newHeight;
                }
                else {
                    if( isWidth ) {
                        myWidth  = currentSize;
                        myHeight = Math.round( myWidth * ratio );
                        if( myHeight < newHeight ) myHeight = newHeight;
                    }
                    else {
                        myHeight = currentSize;
                        myWidth  = Math.round( myHeight * ratio );
                        if( myWidth < newWidth ) myWidth = newWidth;
                    }
                }

                source      = getScaledImageImpl( source , myWidth , myHeight ,type , opacity );
                currentSize = isWidth ? myWidth : myHeight;
            }
            return (BufferedImage)source;
        }
        else {
            return getScaledImageImpl( image , newWidth , newHeight , type , opacity );
        }
    }

    /** Internal method for scale an image
     */
    private static BufferedImage getScaledImageImpl( Image source , int width , int height , int type , int opacity ) {

        // Create the result
        BufferedImage scaledImage = GraphicsEnvironment.isHeadless() ? new  BufferedImage(width, height, type ) :
                                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width,height,opacity);

        // render
        Graphics2D    graphics2D  = scaledImage.createGraphics();
        try {
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage( source , 0, 0, width , height , null);
        }
        finally {
            graphics2D.dispose();
        }
        return scaledImage;
    }
}
