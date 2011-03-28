/*
 * Copyright (c) 2010 INFASS Systèmes (http://www.infass.com) All rights reserved.
 * SimpleFormatter.java is a part of this Commons library
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
package org.divxdede.text.formatter;

/**
 * A simple string formatter giving an easy, readable and efficient way to concatenates strings.
 * <p>
 * This <code>formatter</code> provide a substituion of any <strong><code>%%</code></strong> by the specified argument.<br>
 * Each argument's are formatted accordingly their {@link Object#toString()} methods.
 * <p>
 * Exemple:
 * <pre>
 *    String result = SimpleFormatter.format("%% documents sent by %%" , 125 , "Mr. Anderson");
 *    System.out.println(result);
 * </pre>
 * giving:
 * <pre>
 *    125 documents sent by Mr. Anderson
 * </pre>
 *
 * @author André Sébastien (divxdede)
 * @since 0.2
 */
public final class SimpleFormatter {
    
    /** private constructor, no instanciation
     */
    private SimpleFormatter() {
    }

    /** Format a {@link String} by replacing each <strong><code>%%</code></strong> occurrences by the specified arguments.<br>
     *  Each arguments are formatted accordingly to their {@link Object.toString()} methods.
     *  <p>
     *  Exemple:
     *  <pre>
     *      String result = SimpleFormatter.format("Hello Mr. %% !! You take the %% pill" , "Anderson" , "pill");
     *  </pre>
     *  <p>
     *  For some performance optimization, this method don't perform all pre-checks call.<br>
     *  If you give less arguments than <code>%%</code> occurences then this method don't fails and let extra %% occurences as is.<br>
     *  But in the counter parts, if you provide more argument's than <code>%%</code> occurences, this method will throw an IllegalArgumentException
     *
     *  @param format String used to build the result, each <code>%%</code> are replaced by the specified arguments.
     *  @param args Subtitue arguments used for replace each <code>%%</code> occurences. The first argument replace the first %%, etc..
     *  
     *  @return String result
     *  @throws IllegalArgumentException If args's length is not equals to the <code>%%</code> occurences.
     *  @throws NullPointerException if <code>format</code> is null and args is no-empty
     */
    public static String format(String format , Object... args ) {
        if( args == null || args.length == 0 ) return format;
        
        /** On tranforme les arguments en chaine de caracteres et on determine la taille
         */
        String[] strArgs       = new String[ args.length ];
        int      strArgsLength = 0;
        for(int index = 0 ; index < args.length ; index++ ) {
            strArgs[index]  = ( args[index] == null ? "null" : args[index].toString() );
            strArgsLength  += strArgs[index].length();
        }
        
        /** Construction du résultat
         */
        char[] result = new char[ format.length() + strArgsLength - (2 * args.length) ];
        int resultPosition      = 0;
        
        int    strArgsIndex        = 0;
        int    formatPositionDebut = 0;
        int    formatPositionFin   = 0;
        String currentArgs         = null;
        while( strArgsIndex < args.length ) {
            formatPositionFin = format.indexOf("%%",formatPositionDebut);
            if( formatPositionFin < 0 ) throw new IllegalArgumentException("Incorrect argument's count");

            format.getChars( formatPositionDebut , formatPositionFin , result , resultPosition);   
            resultPosition += (formatPositionFin - formatPositionDebut);
            
            currentArgs = strArgs[strArgsIndex++];
            currentArgs.getChars( 0 , currentArgs.length() , result , resultPosition );
            resultPosition += currentArgs.length();
            
            formatPositionDebut = formatPositionFin + 2;
        }
        if( formatPositionDebut < format.length() ) {
            format.getChars( formatPositionDebut , format.length() , result , resultPosition);   
        }
        return new String( result );
    }
}