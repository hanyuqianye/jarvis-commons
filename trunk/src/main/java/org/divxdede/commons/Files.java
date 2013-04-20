/*
 * Copyright (c) 2013 ANDRE Sébastien (divxdede).  All rights reserved.
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Class tools providing methods handling files manipulations.
 * 
 * 
 * @author André Sébastien (divxdede)
 * @since 0.2.3
 */
public class Files {
	
    /** Return the basename of the specified file.
     *  <p>
     *  The basename is the name of the file without it's extension.
     *  
     *  @param file Specified file to return it's basename
     *  @return basename of the specified file
     *   
	 */
	public static String getBasename( File file ) {
		String filename = file.getName();
		if( file.isDirectory() ) return filename;
		int dotPos = filename.lastIndexOf('.');
		if( dotPos >= 0 ) return filename.substring(0,dotPos);
		return filename;
	}
	
	/** Return the extension of the specified file (in lowercase).
	 * 
	 *  @param file Specified file to return it's extension
	 *  @return extension of the specified file. this result will never be null but may be "" if the file has not extension of if it's a directory.
	 */
	public static String getExtension( File file ) {
		if( file.isDirectory() ) return "";
		String filename  = file.getName();
		int    dotPos   = filename.lastIndexOf('.');
		if( dotPos >= 0 ) {
			return filename.substring( dotPos + 1 ).trim().toLowerCase();
		}
		return "";
	}

	/** Return the extension of the specified URL (in lowercase).
	 * 
	 *  @param url Specified url to return it's extension.
	 *  @return extension of the specified url.
	 */
	public static String getExtension( URL url ) {
		String path = url.getPath().trim();
		String name = path;
		
		if( path.endsWith("/") ) path = path.substring( 0 , path.length() - 1 );
		int pos = path.lastIndexOf('/');
		if( pos >= 0 ) {
			name = path.substring( pos + 1);
		}
		
		String extension = "";
		pos = name.lastIndexOf('.');
		if( pos >= 0 ) {
			extension = name.substring( pos + 1 );
		}
		return extension.trim().toLowerCase();
	}
	
	/** Copy a {@link File} to another one.
	 * 
	 *  @param from {@link File} source
	 *  @param to file destination
	 *  @throws IOException if an error occurs during the copy
	 */
	public static void copy( File from , File to ) throws IOException  {
		copy( from.toURI().toURL() , to );
	}
	
	/** Copy the content of the specified {@link URL} into a {@link File}.
	 *  
	 *  @param from {@link URL} source
	 *  @param to file destination
	 *  @throws IOException if an error occurs during the copy
	 */
	public static void copy( URL from , File to ) throws IOException {
		copy ( from.openStream() , to );
	}
	
	/** Copy the content of the specified {@link InputStream} into a {@link File}.
	 *  <p>
	 *  The {@link InputStream} will be closed at the end of the copy even if an {@link IOException} occurs.
	 *  
	 *  @param from {@link InputStream} source
	 *  @param to file destination
	 *  @throws IOException if an error occurs during the copy
	 */
	public static void copy( InputStream from , File to ) throws IOException {
		BufferedInputStream  bis = null;
		FileOutputStream     fos = null;
		BufferedOutputStream bos = null;
		
		byte[] buffer 		= new byte[64 * 1024];
		int    bufferLength = 0;
		try {
			if( from instanceof BufferedInputStream ) {
				bis = (BufferedInputStream)from;
			}
			else {
				bis = new BufferedInputStream(from);
			}
			fos   = new FileOutputStream( to );
			bos   = new BufferedOutputStream(fos);
			
			while( true ) {
				bufferLength = bis.read( buffer );
				if( bufferLength < 0 ) {
					break;
				}
				bos.write( buffer , 0 , bufferLength );
			}
		}
		finally {
			if( bis != null ) {
				bis.close();
			}
			if( from != null ) {
				from.close();
			}
			if( bos != null ) {
				bos.close();
			}
			if( fos != null ) {
				fos.close();
			}
			bis = null;
			fos = null;
			bos = null;
		}
	}
	
	
    /**
     * Creates an empty directory in the default temporary-file directory, using the given prefix to generate its name.
     * <p>
     * Be careful, You have the responsability to delete the directory.
     * 
     * @param prefix Prefix of the temporary directory
     * 
     */
    public static File createTempDir(String prefix) throws IOException {
	    File   tmpDir   = new File(System.getProperty("java.io.tmpdir"));
	    String baseName = prefix  + "_" + Long.toHexString( System.currentTimeMillis() );
	    
	    for (int i = 0; i < 5000 ; i++) {
	      File result = new File( tmpDir, baseName + "_" + i);
	      if( result.mkdir() ) {
	    	  return result;
	      }
	    }
	    throw new IOException("Unable to create a temporary directory...");
	} 
    
	
	/** Return a {@link File} instance based from the specified {@link File} but with another extension.
	 *  <p>
	 *  The new {@link File} preserve the directory and the basename of the original {@link File} but replace it's extension.
	 *  
	 *  @param file File to change it's extension.
	 *  @param extension Extension to use for the new File
	 *  
	 *  @return A new file based from the specified {@link File} but with a new extension.
	 *   
	 */
	public static File deriveFile( File file , String extension ) {
		File   dir    = file.getParentFile();
		String name   = file.getName();
		String suffix = extension != null && extension.length() > 0 ? "." + extension : ""; 
		
		
		int pos = name.lastIndexOf('.');
		if( pos >=  0 ) {
			name = name.substring( 0 , pos) + suffix;
		}
		else {
			name = name + suffix;
		}
		return new File( dir , name );
	}

	
	/** Read an InputStream into a String using the specified Charset for character interpretation
	 */
	public static String readInputStreamToString( InputStream is , String charsetName ) throws IOException {
		BufferedInputStream   bis  = null;
		ByteArrayOutputStream baos = null;
		
		byte[] buffer 		= new byte[4096];
		int    bufferLength = 0;
		try {
			bis   = new BufferedInputStream(is);
			baos  = new ByteArrayOutputStream();
			
			while( true ) {
				bufferLength = bis.read( buffer );
				if( bufferLength < 0 ) {
					break;
				}
				baos.write( buffer , 0 , bufferLength );
			}
			return baos.toString( charsetName );
		}
		finally {
			if( bis != null ) {
				bis.close();
			}
			if( is != null ) {
				is.close();
			}
			if( baos != null ) {
				baos.close();
			}

			is  = null;
			bis = null;
			baos = null;
		}
	}
}