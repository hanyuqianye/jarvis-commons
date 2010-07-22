package org.divxdede.commons.formatter;

/**
 * Formatteur d'une chaine de caractères substituant les motifs %% par des arguments.
 * L'objectif est de proposer une substitution simple et rapide (sans conversion & mise en forme) contrairement à java.util.Formatter (plus complexe et plus riche)
 *
 * Utilisation:
 *   String result = SimpleFormatter.format("%% factures générées par %%" , 125 , "infass" );
 *
 * @author André Sébastien
 */
public class SimpleFormatter {
    
    /** Creates a new instance of SimpleFormatter */
    private SimpleFormatter() {
        
    }

    /** Mise en forme d'une chaîne de caractères en remplacant les motifs %% par les arguments spécifiés
     *  Exemple: SimpleFormatter.format("Bonjour M. %%, voulez vous %% avec moi" , "Anderson" , "venir"); 
     *
     *  @param format Chaine à substitué en remplacant ces motifs %%
     *  @param args Arguments de substitutions
     *  
     *  @return chaine formattée
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
            if( formatPositionFin < 0 ) throw new IllegalArgumentException("Nombre d'arguments incorrect vis à vis du format");

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
