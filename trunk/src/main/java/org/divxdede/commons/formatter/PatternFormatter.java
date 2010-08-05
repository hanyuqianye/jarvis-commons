package org.divxdede.commons.formatter;

import java.text.DateFormatSymbols;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formattable;
import java.util.Formatter;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.TimeZone;
import sun.misc.FormattedFloatingDecimal; // too complex to re-implement it...

/**
 * PatternFormatter is a printf-style string formatting implementation like {@link java.util.Formatter Formatter}.
 * <h3><a name="objectives">Objectives</a></h3>
 * If this class has closely the same expressivity than the sun's implementation, <tt>PatternFormatter</tt> is expected to be more <b>flexible</b> and <b>efficient</b>:
 * <ul>
 *   <li>Pattern can be compiled for multiple reuse. <tt>Compile</tt> feature avoid pattern parsing each formatting requests.
 *       This design is intended to increase performance when formatting always the same pattern with differents arguments.</li>
 *
 *   <li><tt>ArgumentNameSpace</tt> provide a way to custom argument's identifier for create more specific pattern's formatter.
 * </ul>
 * <h3><a name="diff">Differences with {@link java.util.Formatter Formatter}</a></h3>
 * This class behaviour may have slightly differences with the {@link java.util.Formatter} implementation.
 * The most of all is that all inapplicables flags are silently ignored instead of throwing an exception.
 * <p>
 * Numerics conversions accept all number's type for any conversion's modifier.
 * <ul>
 *   <li><tt>%d</tt> <i>(and similare)</i> accept any floating numbers like <tt>float,Float,double,Double,BigDecimal</tt> but theses numbers will be converted to a <tt>long</tt> value</li>
 *   <li><tt>%f</tt> <i>(and similare)</i> accept any non-floating numbers like <tt>byte,Byte,short,Short,int,Integer,long,Long,BigInteger</tt> but theses numbers will be converted to a <tt>double</tt> value</li>
 *   <li><tt>BigInteger</tt> and <tt>BigDecimal</tt> are not handled for print theses extra-capacity like {@link java.util.Formatter} does</li>
 * </ul>
 * <p>
 * <h3><a name="org">Argument's specification</a></h3>
 * An argument specification has an expression form like it:
 * <blockquote><pre>
 *   %[argument_index$][flags][width][.precision]conversion[datetime-conversion]
 * </pre></blockquote>
 * <ul>
 *   <li><tt>argument_index</tt> can be an explicit 1-based index or '&lt;' for reuse the precedent argument formatted.</li>
 *   <li><tt>flags</tt> can be  <tt>- # + 0 , ( and space ' '</tt></li>
 *   <li><tt>width</tt> must be an integer</li>
 *   <li><tt>precision</tt> must be an integer</li>
 *   <li><tt>conversion</tt> can be a conversion modifier <tt>d,o,x,X,e,E,g,G,f,a,A,c,C,t,T,b,B,s,S,h,H</tt></li>
 *   <li><tt>datetime-conversion</tt> can be a conversion modifier <tt>H,I,k,l,M,N,L,Q,p,s,S,T,z,Z,a,A,b,B,C,d,e,h,j,m,y,Y,r,R,c,D,F</tt></li>
 * </ul>
 * <p>
 * <tt>Formattable</tt> interface is respected with <tt>%s</tt> or <tt>%S</tt> conversions. But this interface requires a Formatter
 * then we create it when it's needed. The formatter is 'empty' and it just serve to get the result of the #formatTo method.
 * <p>
 * For more documentations of applicable patterns, flags, width, precision and conversion's modifiers details, 
 * please refer to the {@link java.util.Formatter}
 * <h3><a name="examples">Simple examples</a></h3>
 * Example <b>without</b> use of <tt>compile</tt> feature:
 * <blockquote><pre>
 *      String stringA = PatternFormatter.format("Your name is %s and you are %d year old" , "John doe" , 25 );
 *      String stringB = PatternFormatter.format("Your name is %s and you are %d year old" , "Dupont" , 39 );   // the pattern is parsed a new time...
 * </pre></blockquote>
 * <p>
 * Example <b>with</b> use of <tt>compile</tt> feature:
 * <blockquote><pre>
 *      Pattern pattern = PatternFormatter.compile("Your name is %s and you are %d year old");
 *
 *      String stringA = PatternFormatter.format(pattern , "John doe" , 25 );
 *      String stringB = PatternFormatter.format(pattern , "Dupont" , 39 );   // the compiled-pattern is reused
 * </pre></blockquote>
 * <h3><a name="namespace">NameSpaces and customization</a></h3>
 * An argument namespace is a way to provide custom identifiers instead of standards one. Theses identifiers can have a length greater than one character.
 * The namespace must define which standard conversion's type theses custom identifiers it refer. 
 * And as a second goal, a namespace can control how to bind an identifier and it's argument. For achieve this binding, some interfaces can be used:
 * <ul>
 *   <li><tt>IndexableArgumentNameSpace</tt>: this namespace bind an identifier to it's argument by providing the 0-index position of the requested argument</li>
 *   <li><tt>AttributeArgumentNameSpace</tt>: this namespace bind an identifier to a particular property of the argument choice with standard pattern's rules</li>
 *   <li><tt>DetailedArgumentNameSpace</tt>: this namespace can control more informations from arguments like flags,width and precision </li>
 * </ul>
 * Two implementations of theses interfaces are provided for an easy-use of theses:
 * <ul>
 *  <li><tt>DefaultArgumentNameSpace</tt> providing the standard namespace support</li>
 *  <li><tt>CompositeArgumentNameSpace</tt> providing a way to compose a namespace from differents ones (like DefaultArgumentNameSpace)</li>
 * </ul>
 * <p>
 * A pattern using an <tt>IndexableArgumentNameSpace</tt> can't use anymore [argument_index$] specification like "%2$d" and will throw an exception.<br>
 * A pattern using an <tt>AttributeArgumentNameSpace</tt> can use [argument_index$] for define which argument must be used for getting the underlying attribute.<br>
 * A pattern using an <tt>DetailedArgumentNameSpace</tt> can use [argument_index$] for define which argument must be used too.<br>
 * <p>
 * Example of a color namespace:
 * <blockquote><pre>
 *   public class ColorArgumentNameSpace implements AttributeArgumentNameSpace , DetailedArgumentNameSpace {
 *
 *          // sub formatter required for print %rgb and %RGB
 *          private Pattern          rgbPattern    = PatternFormatter.compile("#%red%1$green%1$blue",this);
 *          private Pattern          rgbPatternHex = PatternFormatter.compile("#%RED%1$GREEN%1$BLUE",this);
 *
 *          public String[] symbols() {
 *              return new String[]{ "red" , "green" , "blue" , "rgb" , "alpha" , "RED" , "GREEN" , "BLUE" , "RGB" , "ALPHA" };
 *          }
 *
 *          // return requested color's attribute
 *          public Object getAtttribute(Object value, String symbol , Locale locale ) {
 *              if( value instanceof Color ) {
 *                  Color c = (Color)value;
 *                  if( symbol.equalsIgnoreCase("red") )   return c.getRed();
 *                  if( symbol.equalsIgnoreCase("green") ) return c.getGreen();
 *                  if( symbol.equalsIgnoreCase("blue") )  return c.getBlue();
 *                  if( symbol.equalsIgnoreCase("alpha") ) return c.getAlpha();
 *                  if( symbol.equals("rgb") )             return PatternFormatter.format( rgbPattern , locale, value );
 *                  if( symbol.equals("RGB") )             return PatternFormatter.format( rgbPatternHex , locale, value );
 *              }
 *
 *              return value;
 *          }
 *
 *          // set default width and zero padding
 *          public ArgumentDetails getDetails(ArgumentDetails details, String symbol) {
 *              if( getConversion(symbol) == Conversion.DECIMAL_INTEGER ) {
 *                  if( details.getWidth() &lt; 0 ) {
 *                      details.setWidth(3);                                            // minimum 3 digits if not specified
 *                      details.setFlags( details.getFlags().and( Flags.ZERO_PAD ) );   // digits completed with '0'
 *                  }
 *              }
 *              if( getConversion(symbol) == Conversion.HEXADECIMAL_INTEGER ) {
 *                  if( details.getWidth() &lt; 0 ) {
 *                      details.setWidth(2);                                            // minimum 2 digits if not specified
 *                      details.setFlags( details.getFlags().and( Flags.ZERO_PAD ) );   // digits completed with '0'
 *                  }
 *              }
 *              return details;
 *          }
 *
 *          // decimal/hexadecimal/string
 *          public Conversion getConversion(String symbol) {
 *              if( symbol.equalsIgnoreCase("rgb") ) {
 *                  return Conversion.STRING;
 *              }
 *              if( Character.isUpperCase( symbol.charAt(0) ) ) return Conversion.HEXADECIMAL_INTEGER;
 *              return Conversion.DECIMAL_INTEGER;
 *         }
 *
 *          public void use(String symbol) {}
 *          public String getDateSeparator(String symbol) { return null; }
 *      }
 * </pre></blockquote>
 * And can be used like it:
 * <blockquote><pre>
 *      // create an extended namespace that can use standard argument and new identifiers for color objects
 *      ArgumentNameSpace extendedNameSpace = new CompositeArgumentNameSpace( new ColorArgumentNameSpace() , new DefaultArgumentNameSpace() );
 *
 *      // compile our pattern with our custom namespace
 *      Pattern pattern = PatternFormatter.compile("I got color %RGB the %tc" , extendedNameSpace );
 *
 *      // format one
 *      System.out.println( PatternFormatter.format( pattern , Color.RED , System.currentTimeMillis() ) );
 * 
 * </pre></blockquote>
 * That give an output like this sample:
 * <blockquote><pre>
 *      I got color #ff0000 the lun. août 31 21:33:36 CEST 2009
 * </pre></blockquote>
 *
 * @author André Sébastien (divxdede)
 * @see java.util.Formatter
 * @since 0.2
 */
public class PatternFormatter {

    /** An arguments namespace allow to define <strong>custom</strong> conversion's identifiers instead of using defaults.<br>
     *  With a namespace, conversion's modifier can use more than one character.
     */
    public interface ArgumentNameSpace {

        /** Returns all conversion's identifiers handled by this namespace
         * @return All conversion's modifiers
         */
        public String[] symbols();

        /** Return for a specified symbol (custom conversion's identifier) the standard conversion to use for formatting the underlying argument
         * @param symbol Custom conversion's identifier
         * @return Standard conversion's modifier to use for formatting the underlying argument
         */
        public Conversion getConversion(String symbol);

        /** When an argument is a date-time field, a complementary date-time conversion is required.
         *  This method return which separator must be used to separate a specified symbol from it's date-time conversion.
         *  @return date-time separator to use with a specified symbol
         */
        public String getDateSeparator(String symbol);

        /** Invoked before using a symbol by a pattern
         * @param symbol Symbol marked as <tt>used</tt>
         */
        public void use(String symbol);
    }

    /** <tt>IndexableArgumentNameSpace</tt> is a namespace that can define which argument must be used for formatting a specified conversion's identifier.
     *  This kind of namespace handle argument's choice and the pattern can't try to specify any index informations.
     */
    public interface IndexableArgumentNameSpace extends ArgumentNameSpace {

        /** Returns which argument's must be used for formatting the specified symbol
         * @param symbol Symbol to format
         * @return index ArgumentDetails's index 0-based.
         */
        public int index(String symbol);
    }

    /** AttributeArgumentNameSpace is a namespace binding conversion's identifier to a particular property of the argument.
     *  This namespace get a method for retrieve the property value of an argument depending of the conversion's identifier.
     */
    public interface AttributeArgumentNameSpace extends ArgumentNameSpace {

        /** Return the related property for the specified symbol and the specified argument
         * @param value argument to format
         * @param symbol Symbol describing which property must be formatted
         * @param locale Locale to use
         * @return the requested property
         */
        public Object getAtttribute(Object value,String symbol,Locale locale);
    }

    /** This extended implementation of <tt>ArgumentNameSpace</tt> allow to control more informations on arguments like:
     *  <ul>
     *   <li>Flags that can modify the output format</li>
     *   <li>Width that give the minium number of characters</li>
     *   <li>Precision that restrict the number of characters (depend of the conversion)</li>
     *  </ul>
     */
    public interface DetailedArgumentNameSpace extends ArgumentNameSpace {

        /** Return details to use for a specified symbol
         * @param details Current details informations picked-up from the pattern specification
         * @param symbol Which symbol to apply the details
         * @return New details to use for this argument
         */
        public ArgumentDetails getDetails(ArgumentDetails details, String symbol);
    }

    /** Describe all conversion types managed by this formatter
     */
    public enum Conversion {

        DECIMAL_INTEGER('d', false, null , null ),
        OCTAL_INTEGER('o', false, null ,"0"),
        HEXADECIMAL_INTEGER('x', false, null,"0x"),
        HEXADECIMAL_INTEGER_UPPER('X', true, HEXADECIMAL_INTEGER,"0X"),
        SCIENTIFIC('e', false, null,null),
        SCIENTIFIC_UPPER('E', true, SCIENTIFIC,null),
        GENERAL('g', false, null,null),
        GENERAL_UPPER('G', true, GENERAL,null),
        DECIMAL_FLOAT('f', false, null,null),
        HEXADECIMAL_FLOAT('a', false, null,null),
        HEXADECIMAL_FLOAT_UPPER('A', true, HEXADECIMAL_FLOAT,null),
        CHARACTER('c', false, null,null),
        CHARACTER_UPPER('C', true, CHARACTER,null),
        DATE_TIME('t', false, null,null),
        DATE_TIME_UPPER('T', true, DATE_TIME,null),
        BOOLEAN('b', false, null,null),
        BOOLEAN_UPPER('B', true, BOOLEAN,null),
        STRING('s', false, null,null),
        STRING_UPPER('S', true, STRING,null),
        HASHCODE('h', false, null,null),
        HASHCODE_UPPER('H', true, HASHCODE,null);

        private char car;
        private boolean upper = false;
        private Conversion effective = null;
        private String alternate = null;

        private Conversion(char car, boolean upper, Conversion conversion, String alternate) {
            this.car = car;
            this.upper = upper;
            this.effective = conversion == null ? this : conversion;
            this.alternate = alternate;
        }

        /** Retrieve the conversion's modifier character
         *  @return conversion's modifier character
         */
        public char getChar() {
            return this.car;
        }

        /** Indicate if this conversion's modifier request an UPPERCASE formatting
         *  @return true if this conversion's modifier request an UPPERCASE formatting
         */
        public boolean isUpper() {
            return this.upper;
        }

        /** Return the effective conversion. Be exemple STRING_UPPER return STRING because it's the same conversion excepting the UPPER flag
         *  @return effective conversion
         */
        public Conversion getEffectiveConversion() {
            return this.effective;
        }

        /** Return the alternate prefix to use with this conversion if alternate flag is specified
         *  @return alternate prefix
         */
        public String getAlternate() {
            return this.alternate;
        }

        /** Parse a character and return the underlying conversion's enum
         *  @param car Conversion's modifier character
         *  @return Conversion's enum (return null if no correspondance found)
         */
        public static Conversion parse(char car) {
            Conversion[] all = values();
            for (Conversion c : all) {
                if (c.getChar() == car) {
                    return c;
                }
            }
            return null;
        }
    };

    /** Describe all date-time conversion's suffix modifiers
     */
    public enum DateTimeConversion {

        HOUR_OF_DAY_0('H'),
        HOUR_0('I'),
        HOUR_OF_DAY('k'),
        HOUR('l'),
        MINUTE('M'),
        NANOSECOND('N'),
        MILLISECOND('L'),
        MILLISECOND_SINCE_EPOCH('Q'),
        AM_PM('p'),
        SECONDS_SINCE_EPOCH('s'),
        SECOND('S'),
        TIME('T'),
        ZONE_NUMERIC('z'),
        ZONE('Z'),

        NAME_OF_DAY_ABBREV('a'),
        NAME_OF_DAY('A'),
        NAME_OF_MONTH_ABBREV('b'),
        NAME_OF_MONTH('B'),
        CENTURY('C'),
        DAY_OF_MONTH_0('d'),
        DAY_OF_MONTH('e'),
        NAME_OF_MONTH_ABBREV_X('h'),
        DAY_OF_YEAR('j'),
	MONTH('m'),
        YEAR_2('y'),
        YEAR_4('Y'),

        TIME_12_HOUR('r'),
        TIME_24_HOUR('R'),
        DATE_TIME('c'),
        DATE('D'),
       	ISO_STANDARD_DATE('F');

        private char car;

        private DateTimeConversion(char car) {
            this.car = car;
        }

        /** Retrieve the date-time conversion's modifier character
         *  @return date-time conversion's modifier character
         */
        public char getChar() {
            return this.car;
        }

        /** Parse a character and return the underlying date-time conversion's enum
         *  @param car Date-time conversion's modifier character
         *  @return Date-time conversion's enum (return null if no correspondance found)
         */
        public static DateTimeConversion parse(char car) {
            DateTimeConversion[] all = values();
            for (DateTimeConversion c : all) {
                if (c.getChar() == car) {
                    return c;
                }
            }
            return null;
        }
    };

    /** Compiled pattern form.
     *  <p>
     *  Some usecase need to format the same pattern with differents argument's value.
     *  In this case, compiling such pattern and reuse theses forms for the formatting process is more efficient.
     *  <p>
     *  This class represent a compiled-pattern with {@link PatternFormatter#compile} and can be reuse with {@link PatternFormatter#format}
     */
    public static final class Pattern {
        private String                  pattern         = null; // last-compiled pattern
        private List<PatternElement>    elements        = null; // compiled structure
        private ArgumentNameSpace       nameSpace       = null; // namespace used

        /** Default constructor
         */
        private Pattern(String pattern, ArgumentNameSpace nameSpace) {
            this.pattern   = pattern;
            this.nameSpace = nameSpace;
        }

        /** Retrieve the source-pattern compiled to this form
         *  @return Source pattern of this compiled form
         */
        public String getPattern() {
            return this.pattern;
        }

        /** Retrieve the namespace used for this compiled-pattern
         *  @return NameSpace used for this compiled-pattern
         */
        public ArgumentNameSpace getNameSpace() {
            return this.nameSpace;
        }

        /** Get a compiled pattern's element
         */
        private PatternElement getElements(int index) {
            if( this.elements == null ) return null;
            return this.elements.get(index);
        }

        /** Get numbers of compiled pattern's elements
         */
        private int size() {
            if( this.elements == null ) return 0;
            return this.elements.size();
        }

        /** Add a compiled pattern's element
         */
        private void addElement(PatternElement element) {
            if( this.elements == null ) this.elements = new ArrayList<PatternElement>(10);
            this.elements.add(element);
        }
    }


    /** Create an empty PatternFormatter
     */
    public PatternFormatter() {
    }

    /** Compile a pattern that can be re-used for formatting string with the #toString(Object... args) method.
     *  @param pattern pattern to compile
     */
    public static Pattern compile(String pattern) {
        return compile(pattern, null);
    }

    /** Compile a pattern that can be re-used for formatting string with the #toString(Object... args) method.
     *  @param pattern pattern to compile
     *  @param namespace namespace to use
     */
    public static Pattern compile(String pattern, ArgumentNameSpace namespace) {
        Pattern result = new Pattern(pattern,namespace);

        int start = 0;
        int end = 0;
        while (start < pattern.length()) {
            end = pattern.indexOf('%', start);
            if (end >= 0) {
                if (end > start) {
                    /** We have a fixed-string before the current argument
                     */
                    result.addElement( new StringElement(pattern.substring(start, end)) );
                }

                /** Parse the argument with an ArgumentParser
                 */
                ArgumentParser ap = new ArgumentParser( result.getPattern() , end, result.getNameSpace() );
                result.addElement( ap.parse() );

                /** Move to the end of all bytes consumed by the argument parser
                 */
                start = ap.getEndArgumentPosition();
            } else {
                /** No more argument, add the fixed-tailer of this pattern
                 */
                end = pattern.length();
                if (end > start) {
                    result.addElement( new StringElement(pattern.substring(start, end)) );
                    break;
                }
            }
        }

        return result;
    }

    /** Format the pattern with the given arguments and the default locale
     *  This method <tt>compile</tt> the specified pattern if it is different from the last compiled one
     *
     *  @param format Pattern to format
     *  @param args arguments to use for format the pattern
     *
     *  @return the pattern formatted
     */
    public static String format(String format, Object... args) {
        return format(format, Locale.getDefault() , args );
    }

    /** Format the pattern with the given arguments and the specified locale
     *  This method <tt>compile</tt> the specified pattern if it is different from the last compiled one
     *
     *  @param format Pattern to format
     *  @param locale Locale to use
     *  @param args arguments to use for format the pattern
     *
     *  @return the pattern formatted
     */
    public static String format(String format, Locale locale , Object... args) {
        return format( compile(format) , locale , args );
    }

    /** Format a compiled-pattern with the given arguments and the default locale
     *  This method don't compile any pattern and reuse a previously compiled one.
     * 
     *  @param pattern Compiled-Pattern to format
     *  @param args arguments to use for format the pattern
     *
     *  @return the pattern formatted
     */
    public static String format(Pattern pattern, Object... args) {
        return format(pattern, Locale.getDefault() , args );
    }
    
    /** Format a compiled-pattern with the given arguments and the specified locale
     *  This method don't compile any pattern and reuse a previously compiled one.
     *
     *  @param pattern Compiled-Pattern to format
     *  @param locale Locale to use
     *  @param args arguments to use for format the pattern
     *
     *  @return the pattern formatted
     */
    public static String format(Pattern pattern, Locale locale , Object... args) {
        if( locale == null )
            locale = Locale.getDefault();
        
        String[] array = new String[ pattern.size() ];
        int arrayLength = 0;

        int last = -1;  // last index used
        int lasto = -1; // last implicit index used (ordinary)

        for (int i = 0; i < pattern.size(); i++) {
            PatternElement pe = pattern.getElements(i);
            int index = pe.index();
            try {
                switch (index) {
                    case -2:  // fixed string, "%n", or "%%"
                        array[i] = pe.format( null , locale );
                        break;
                    case -1:  // relative index
                        if (last < 0 || (args != null && last > args.length - 1)) {
                            throw new MissingFormatArgumentException(pe.toString());
                        }
                        array[i] = pe.format( (args == null ? null : args[last]) , locale );
                        break;
                    case 0:  // ordinary index
                        lasto++;
                        last = lasto;
                        if (args != null && lasto > args.length - 1) {
                            throw new MissingFormatArgumentException(pe.toString());
                        }
                        array[i] = pe.format( (args == null ? null : args[lasto]) , locale );
                        break;
                    default:  // explicit index
                        last = index - 1;
                        if (args != null && last > args.length - 1) {
                            throw new MissingFormatArgumentException(pe.toString());
                        }
                        array[i] = pe.format( (args == null ? null : args[last]) , locale );
                        break;
                }
                arrayLength += (array[i] == null ? 0 : array[i].length());
            } finally {
            }
        }

        /** Constructing result
         */
        char[] result = new char[arrayLength];
        int resultPos = 0;
        for (String e : array) {
            e.getChars(0, e.length(), result, resultPos);
            resultPos += e.length();
        }
        return new String(result);
    }

    /** Details how an argument must be formatted with more informations
     *  <ul>
     *   <li>Flags that can modify the output format</li>
     *   <li>Width that give the minium number of characters</li>
     *   <li>Precision that restrict the number of characters (depend of the conversion)</li>
     *  </ul>
     */
    public static class ArgumentDetails {
        
        private Flags              flags          = Flags.NONE;
        private int                width          = -1;
        private int                precision      = -1;
        private DateTimeConversion timeConversion = null;

        /** Private constructor
         */
        private ArgumentDetails(Flags flags,int width,int precision,DateTimeConversion datetimeConversion) {
            setFlags(flags);
            setWidth(width);
            setPrecision(precision);
            setDateTimeConversion(datetimeConversion);

        }

        /** Retrieve flags to use with the argument
         *  @return FLags to use
         */
        public Flags getFlags() {
            return this.flags;
        }

        /** Define a new flags set to use
         * @param flags new flag set to use
         */
        public void setFlags(Flags flags) {
            this.flags = flags;
        }

        /** Retrieve the width to use (-1 if no width is specified)
         *  @return Width to use (-1 if no one)
         */
        public int getWidth() {
            return this.width;
        }

        /** Define the new width to use
         * @param width new width
         */
        public void setWidth(int width) {
            this.width = width;
        }

        /** Retrieve the precision to use (--1 if no precision is specified)
         *  @return Precision to use (-1 if no one)
         */
        public int getPrecision() {
            return this.precision;
        }

        /** Define the new precision to use
         *  @param precision new precision
         */
        public void setPrecision(int precision) {
            this.precision = precision;
        }

        /** Retrieve the date-time complementary conversion to use when the argurment is a date-time
         *  @return date-time complementary conversion to use when the argument is a date-time
         */
        public DateTimeConversion getDateTimeConversion() {
            return this.timeConversion;
        }

        /** Define the new date-time complementaty conversion
         * @param dateTimeConversion new date-time complementary conversion
         */
        public void setDateTimeConversion(DateTimeConversion dateTimeConversion) {
            this.timeConversion = dateTimeConversion;
        }
    }

    /** This class helps to parse an argument of the form %[argument_index$][flags][width][.precision]conversion
     *  This class also interpret to <tt>special</tt> arguments: %n (line feed) and %% (character %)
     *  <p>
     *  <tt>conversiont</tt> part can be also replaced by an identifier from a specified arguments namespace
     *  <p>
     *  The parsing process return a PatternElement and manage all kinds of <tt>ArgumentNameSpace</tt>
     */
    private static class ArgumentParser {

        /** Line separator for implementing %n
         */
        static String lineSeparator = System.getProperty("line.separator");

        /** Global members on the pattern
         */
        private String            pattern          = null; // global pattern being to be parsed
        private int               startArgumentPos = 0;    // start index from the pattern of the argument to parse
        private int               endArgumentPos   = 0;    // end index from the pattern of the argument parsed (set at the end of the parse process)
        private ArgumentNameSpace nameSpace        = null; // namespace used for the conversion's modifier

        /** Members used for scan process
         */
        private int phase         = 0; // current phase of the scan
                                       //    0 = argument_index
                                       //    1 = flags
                                       //    2 = witdh
                                       //    3 = precision
                                       //    4 = conversion
        private int pos           = 0; // current index of the character to parse for the current phase scan
        private int startPhasePos = 0; // start index from the pattern of the current phase scan

        /** Result members
         */
        int        argument_index = 0;
        String     flags          = null;
        int        width          = -1;
        int        precision      = -1;
        Conversion conversion     = null;

        /** Constructor of the parser
         * @param pattern global pattern being parser
         * @param startArgumentPos Start index of the argument to parse from the pattern. <tt>This index must refer a '%' character</tt>
         * @param nameSpace NameSpace to use for identify arguments (maybe be null)
         */
        public ArgumentParser(String pattern, int startArgumentPos, ArgumentNameSpace nameSpace) {
            this.pattern = pattern;
            this.startArgumentPos = startArgumentPos;
            this.nameSpace = nameSpace;
        }

        /** Return the ending position (exclusive) from the global pattern of the scanned argument.
         *  The index represent the first character that is not take part of the scanned argument.
         *  <p>
         *  This method can be invoked only after the argument is parsed
         * 
         *  @return End of argument position
         */
        public int getEndArgumentPosition() {
            return this.endArgumentPos;
        }

        /** Parse the argument of the form %[argument_index$][flags][width][.precision]conversion
         *  This implementation works as follow:
         *    - The scan is on a particular  phase (starting to 0, ending by completing phase 4)
         *    - Take a character, if the character is compatible for the current phase place it on a buffer and continue with the next character
         *    - If the character is not compatible (called breaker character) with the current phase:
         *         - Check if buffer's characters for the ended phase are legits, if true: the phase consume theses chars and replay the breaker character for the new phase
         *         - If buffer's characters ARE NOT LEGITS then flush buffer and theses characters will be re-scan for the new phase.
         *              - Buffer's characters can be not legits if the set don't respect phase format (exemple: phase 0 must end by a '$' and phase 3 must start by a '.')
         *  <p>
         *  This method treat also two specials argument's form: %n (line feed) and %% (isolated %)
         *  <p>
         *  Example:
         *    - ArgumentDetails %20d
         *    - Phase 0 , scan '2' : character compatible (digit) : hold this character that give a buffer "2"
         *    - Phase 0 , scan '0' : character compatible (digit) : hold this character that give a buffer "20"
         *    - Phase 0 , scan 'd' : character INCOMPATIBLE
         *        - Check legit for buffer "20" for the phase 0 : buffer NOT LEGIT because phase 0 need to end by a '$'
         *        - Change to phase 2, flush the buffer that will replay theses characters
         *    - Phase 1 , scan '2' : character INCOMPATIBLE
         *        - No buffer, change to phase 2
         *    - Phase 2 , scan '2' : character compatible (digit) : hold this character that give a buffer "2"
         *    - Phase 2 , scan '0' : character compatible (digit) : hold this character that give a buffer "20"
         *    - Phase 2 , scan 'd' : character INCOMPATIBLE
         *         - Check legit for buffer "20" for the phase 2 : buffer LEGIT , consume buffer and get a <tt>WIDTH of 20</tt>
         *         - Change to phase 3, we will replay the 'd' but not '2' and '0' because they are consumed by the phase 2
         *    - Phase 3 , scan 'd' :  character INCOMPATIBLE
         *        - No buffer, change to phase 4
         *    - Phase 4 , scan 'd' : character compatible, this phase just peek one character and get the <tt>CONVERSION of d</tt>
         *    - parsing END
         *
         *  In this example, this method scan a set of 9 characters ('2' , '0' , 'd' , '2' , '2' , '0' , 'd' , 'd' , 'd')
         *
         *  @return PatternElement describing this argument after parsing
         */
        private PatternElement parse() {
            if (pattern.charAt(startArgumentPos) != '%') {
                throw new IllegalStateException("argument pattern must start by %");
            }

            this.pos = startArgumentPos + 1; // skip %
            this.phase = 0;
            this.startPhasePos = pos;
            while (pos < pattern.length()) {
                if (phase == 4) {
                    return getPatternElement();
                }
                switch (pattern.charAt(pos)) {
                    case '-':
                    case '#':
                    case '+':
                    case ' ':
                    // case '0' : // processed in default case since it is also a digit character
                    case ',':
                    case '(': if (phase == 1) {
                                pos++; // continue scan
                                break;
                              }
                              newPhase();
                              break;
                    case '$': if( phase == 0 ) pos++;  // the $ take part on the phase 0
                              newPhase();
                              break;
                    case '.': if (phase == 3 && pos == startPhasePos) {
                                 pos++; // continue scan
                                 break; // it's the beggining of the phase 3
                              }
                              newPhase();
                              break;
                    case '<': if (phase == 0) {
                                 pos++; // continue scan
                                 break; // it's a part of the phase 0
                              }
                              newPhase();
                              break;
                    case '%':
                    case 'n': if (phase == 0 && pos == (startArgumentPos + 1)) {
                                 this.endArgumentPos = Math.min(pattern.length(), pos + 1);
                                  if (pattern.charAt(pos) == '%') {
                                    return new StringElement("%");
                                  }
                                 return new StringElement(lineSeparator);
                              }
                    default:  if (Character.isDigit(pattern.charAt(pos))) {
                                    if (phase == 0 || phase == 2 || phase == 3) {
                                        pos++; // continue scan
                                        break; // part of theses phases
                                    }
                                    if (phase == 1 && pattern.charAt(pos) == '0') {
                                        pos++; // continue scan
                                        break; // part of this phase
                                    }
                              }
                              newPhase();
                }
            }
            throw new IllegalStateException("unable to fin conversion properties on last argument");
        }

        /** This method mark the end of the current phase and some characters have been buffered for this ending phase.
         *  Theses characters will be consumed by the ending phase if they are legits.
         *  If they aren't legits, theses characters will be flush to be re-scanned for the new phase.
         *  <ul>
         *    <li>phase 0 : characters are legits if they are ending by a '$'</li>
         *    <li>phase 1 : characters are legits if they start by '-' or '#' or '+' or ' ' or '0' or ',' or '('</li>
         *    <li>phase 2 : characters are legits if they start by a digit character</li>
         *    <li>phase 3 : characters are legits if they start by '.'</li>
         *    <li>phase 4 : not managed by this method but by #getPatternElement()</li>
         *  </ul>
         *  <p>
         *  In all case, this method start the next phase aand manage some stuff
         *  <ul>
         *   <li>
         */
        private void newPhase() {
            int endPhasePos = pos;

            /** Flag for determine if the ending phase consums bufferised characters
             */
            boolean consumed = false;

            /** Consum characters if possible
             */
            switch(phase) {
                case 0 : {
                            if (endPhasePos > startPhasePos && pattern.charAt(endPhasePos - 1) == '$') {
                                if (nameSpace != null && nameSpace instanceof IndexableArgumentNameSpace ) {
                                    throw new IllegalStateException("can't specify an index ordering statement with an IndexArgumentNameSpace");
                                }
                                String s = this.pattern.substring(startPhasePos, endPhasePos - 1);
                                if (s.equals("<")) {
                                    this.argument_index = -1; // preceding index
                                } else {
                                    this.argument_index = Integer.parseInt(s);
                                }
                                consumed = true;
                            } else {
                                this.argument_index = 0; // ordinary index
                            }
                         }
                         break;
                case 1 : {
                            if (endPhasePos > startPhasePos) {
                                char firstChar = this.pattern.charAt(startPhasePos);
                                if (firstChar == '-' || firstChar == '#' || firstChar == '+' || firstChar == ' ' || firstChar == '0' || firstChar == ',' || firstChar == '(') {
                                    this.flags = this.pattern.substring(startPhasePos, endPhasePos);
                                    consumed = true;
                                }
                            }
                         }
                         break;
                case 2 : {
                            if (endPhasePos > startPhasePos) {
                                char firstChar = this.pattern.charAt(startPhasePos);
                                if (Character.isDigit(firstChar)) {
                                    String s = this.pattern.substring(startPhasePos, endPhasePos);
                                    this.width = Integer.parseInt(s);
                                    consumed = true;
                                }
                            }
                         }
                         break;
                case 3 : {
                            if (endPhasePos > startPhasePos) {
                                char firstChar = this.pattern.charAt(startPhasePos);
                                if (firstChar == '.') {
                                    String s = this.pattern.substring(startPhasePos + 1, endPhasePos);
                                    this.precision = Integer.parseInt(s);
                                    consumed = true;
                                }
                            }
                         }
                         break;
            }

            /** Pass to the next phase
             */
            phase++;
            if (consumed) startPhasePos = endPhasePos;
            else          pos           = startPhasePos; // replay from the last start point
        }

        /** This method is responsible to parse the <tt>conversion</tt> part of the argument pattern.
         *  A conversion modifier take place in a single character that must match with the <tt>Conversion</tt> enumeration.
         *  <p>
         *  In case of use of an argument's namespace, this method find the biggest <tt>custom symbol</tt> that will give:
         *  <ul>
         *    <li>The Conversion flag to use</li>
         *    <li>The argument's index to use (specified index in the argument pattern is incompatible with a namespace)</li>
         *  </ul>
         *  <p>
         *  #getPatternElement() is then end of the scan process and return the <tt>PatternElement</tt> refleting the argument.
         *
         *  @return PatternElement describing this argument after parsing
         */
        private PatternElement getPatternElement() {
            int                length      = 1;
            DateTimeConversion dtc         = null;
            String             matchSymbol = null;

            if (nameSpace != null) {
                for (String symbol : nameSpace.symbols()) {
                    if (pattern.startsWith(symbol, startPhasePos)) {
                        if (matchSymbol == null || symbol.length() > matchSymbol.length()) {
                            matchSymbol = symbol;
                            length = symbol.length();
                        }
                    }
                }
                if (matchSymbol != null) {
                    nameSpace.use(matchSymbol); // mark this symbol "used"
                    if( nameSpace instanceof IndexableArgumentNameSpace ) {
                        this.argument_index = ((IndexableArgumentNameSpace)nameSpace).index( matchSymbol ) + 1; // namespace is 0-index but the property is 1-index
                    }
                    this.conversion = nameSpace.getConversion(matchSymbol);
                }

                if (this.conversion == null) {
                    throw new IllegalArgumentException("unknown [" + pattern.substring(startPhasePos) + "] symbol's namespace");
                }
            } else {
                this.conversion = Conversion.parse(pattern.charAt(startPhasePos));
                if (conversion == null) {
                    throw new IllegalArgumentException("unknown [" + pattern.charAt(startPhasePos) + "] conversion's flag");
                }
            }

            /** If the conversion's modifier is a date-time, it must follow a suffix that determines how print this date
             */
            switch(this.conversion) {
                case DATE_TIME       :
                case DATE_TIME_UPPER : if( this.nameSpace != null ) {
                                           String datetimeSeparator = this.nameSpace.getDateSeparator( matchSymbol );
                                           if( datetimeSeparator != null && datetimeSeparator.length() > 0 ) {
                                               if( ! pattern.startsWith( datetimeSeparator , startPhasePos + length ) ) {
                                                   throw new IllegalArgumentException("date-time suffix from '" + matchSymbol + "' must be separate with '" + datetimeSeparator + "'");
                                               }
                                           }
                                           length += datetimeSeparator == null ? 0 : datetimeSeparator.length();
                                       }
                                       char charDTC = pattern.charAt( startPhasePos + (length++) );
                                       dtc = DateTimeConversion.parse(charDTC);
                                       if( dtc == null ) throw new IllegalArgumentException("unknown [" + charDTC + "] Date-Time suffix");
                default              : break;
            }

            this.endArgumentPos = Math.min(pattern.length(), startPhasePos + length);

            ArgumentDetails details = new ArgumentDetails( Flags.parse( flags , conversion ) , width , precision , dtc);
            if( nameSpace instanceof DetailedArgumentNameSpace ) {
                details = ((DetailedArgumentNameSpace)nameSpace).getDetails( details , matchSymbol );
            }

            ArgumentElement result = new ArgumentElement(argument_index, conversion , details );
            if( nameSpace instanceof AttributeArgumentNameSpace ) {
                result.setNameSpace( (AttributeArgumentNameSpace)nameSpace , matchSymbol );
            }
            return result;
        }
    }

    /** When a pattern is compiled, this pattern is represented by a sequence of PatternElement
     *  Each PatternElement can format a fragment of the entire pattern depending on an argument.
     *
     *  A PatternElement know which argument is needed for formatting itself with the help of the #index() method.
     *  This method return the index inside the argument's array of the requested argument for this PatternElement
     *  <ul>
     *     <li>-2 : This PatternElement don't need any argument, it's probably a constant fragment like a String litteral</li>
     *     <li>-1 : This PatternElement use the same argument than the previous PatternElement</li>
     *     <li> 0 : This PatternElement use the next implicit argument on the array</li>
     *     <li> x : This PatternElement use the specified index argument, this method is 1-based index and the array is 0-based index
     *  </ul>
     *  <p>
     *  After what, each PatternElement can format itself with the #format(Object arg,Locale locale)
     *  For have the final result, we need to append each PatternElement result.
     */
    private interface PatternElement {

        /** Donne l'index de l'argument ï¿½ utiliser pour formatter cet ï¿½lement
         *  @return Index de l'argument ï¿½ utiliser pour formatter cet ï¿½lï¿½ment
         *          -2 : Aucun car il s'agit d'un ï¿½lement fixe
         *          -1 : Prendre l'argument prï¿½cedent au dernier argument utilisï¿½
         *           0 : Prochain argument
         *           x : Index prï¿½cis
         */
        public int index();

        /** Mise en forme de l'ï¿½lement 
         *  @param arg ArgumentDetails ï¿½ utiliser pour mettre en forme l'ï¿½lement
         */
        public String format(Object arg , Locale locale );
    }

    /** PatternElement implementation for holding a String-litteral fragment of the compiled pattern
     */
    private static class StringElement implements PatternElement {

        /** String-litteral
         */
        private String s = null;

        /** Constructor
         */
        public StringElement(String s) {
            this.s = s;
        }

        /** Formatting the literral return simply the 
         */
        public String format(Object arg , Locale locale ) {
            return s;
        }

        /** -2 car n'a pas besoin d'argument
         */
        public int index() {
            return -2;
        }
    }

    /** PatternElement holding an argument configuration and formatting implementation
     */
    private static class ArgumentElement implements PatternElement {

        /** configuration members
         */
        int                         argument_index     = 0;
        ArgumentDetails             argument_details   = null;

        /** Conversion's attributes
         */
        Conversion                  conversion         = null;

        /** Namespace's attributes
         */
        AttributeArgumentNameSpace  nameSpace          = null;
        String                      nameSpaceSymbol    = null;

        /** DecimalFormatSymbols
         *  And some related informations depending on the local used for formatting numbers
         */
        Locale               loc = null;
        DecimalFormatSymbols dfs = null;
        char                 groupSeparator   = ' ';
        char                 decimalSeparator = '.';
        char                 zero             = '0';
        char[]               buffer           = null; // used for decimal formatting

        /** Some members for formatting dates
         */
        Calendar             calendar         = null;
        DateFormatSymbols    datefs           = null;

        /** Construct an anrgument formatter that hold all properties needed for formatting it
         */
        public ArgumentElement(int index, Conversion conversion , ArgumentDetails details ) {
            this.argument_index     = index;
            this.argument_details   = details;
            this.conversion         = conversion.getEffectiveConversion();

            /** Init the buffer for decimal formatting
             */
            switch( this.conversion ) {
                case DECIMAL_FLOAT :
                case GENERAL       :
                case SCIENTIFIC    : buffer = new char[100]; // too complex to determine which size we need
                                                             // 100 seems to be big enough
                                     break;
                case DATE_TIME     : calendar = Calendar.getInstance();
                                     break;
                default            : break;
            }
        }

        /** Define a AttributeArgumentNameSpace to use for formatting this argument
         * @param nameSpace AttributeArgumentNameSpace to use for formatting this argument
         * @param symbol Namespace's symbol used with this argument
         */
        public void setNameSpace( AttributeArgumentNameSpace nameSpace , String symbol ) {
            this.nameSpace = nameSpace;
            this.nameSpaceSymbol = symbol;
        }

        /** Index de l'argument ï¿½ utiliser
         */
        public int index() {
            return argument_index;
        }

        /** Return flags to use
         */
        public Flags getFlags() {
            return argument_details.getFlags();
        }

        /** return Width to use
         */
        public int getWidth() {
            return argument_details.getWidth();
        }

        /** Return Precision to use
         */
        public int getPrecision() {
            return argument_details.getPrecision();
        }

        /** return DateTimeConversion to use
         */
        public DateTimeConversion getDateTimeConversion() {
            return argument_details.getDateTimeConversion();
        }
        
        /** Formatting the argument
         *  Depending on the conversion modifier, the right formatting implementation will be invoked
         */
        public String format(Object arg , Locale locale ) {
            /** deffer the value from the namespace
             */
            if( nameSpace != null ) {
                arg = nameSpace.getAtttribute( arg , nameSpaceSymbol , locale );
            }

            switch (this.conversion) {
                case DATE_TIME              : return formatDateTime(arg , locale );

                case DECIMAL_INTEGER        :
                case OCTAL_INTEGER          :
                case HEXADECIMAL_INTEGER    : return formatInteger(arg , locale );

                case SCIENTIFIC             :
                case GENERAL                :
                case DECIMAL_FLOAT          :
                case HEXADECIMAL_FLOAT      : return formatFloat(arg , locale );

                case CHARACTER              : return formatCharacter(arg , locale );
                case BOOLEAN                : return formatBoolean(arg , locale );
                case STRING                 : return formatString(arg , locale );
                case HASHCODE               : return formatHashCode(arg , locale );
                default                     : return "";
            }
        }

        /** Format an integer,
         * @param arg ArgumentDetails to format, this must be a Number instance in order to formatting the argument
         * @param locale Localization to use for formatting the number (used for group separator, decimal separator, ...)
         * @return number format
         */
        private String formatInteger(Object arg , Locale locale ) {
	    if (arg == null) return "null";

            String result = null;

            if( arg instanceof Number ) {
                long value   = ((Number)arg).longValue();

                if( value < 0 && ( conversion == Conversion.HEXADECIMAL_INTEGER || conversion == Conversion.OCTAL_INTEGER ) ) {
                    /** HEXADECIMAL and OCTAL hasn't negative number and represent absolute bits.
                     *
                     *  As exemple, a byte value has 8 bits and have theses representation in binary
                     *    Max value +127  == 01111111
                     *                 ....
                     *        value +1    == 00000001
                     *        value  0    == 00000000
                     *        value -1    == 11111111  (substract 1 to 0 and you wrap to the end of bits
                     *                 ....
                     *    Min value -128  == 10000000
                     *
                     *  If you convert a  byte decimal value (8 bits) into a short decimal value (16 bits)
                     *  the -128 decimal representation in 8 bits become +128 decimal representation in 16 bits.
                     *
                     * But when JAVA cast a byte to short decimal value, the negative number is adapted to the target and the negative bit is moved and the original bits representation is broken
                     * In this case, we must restore the original bits representation.
                     *
                     * The original bits representation can be restored by adding 256 (2^8) in the case of a source value in 8 bits (byte)
                     * We must add 2^(size_in_bits_of_source_value) that can be done by adding (1 &lt;&lt; size_in_bits_of_source_value)
                     */
                    int bitSize = 0;

                    if( arg instanceof Integer )      bitSize = Integer.SIZE;
                    else if( arg instanceof Short )   bitSize = Short.SIZE;
                    else if( arg instanceof Byte )    bitSize = Byte.SIZE;

                    value += (1 << bitSize );
                }

                result = formatInteger(value,locale);
            }

            if( result == null ) fail(arg);
            return justify(result);
        }

        /** Format a float
         */
        private String formatFloat(Object arg , Locale locale ) {
            if (arg == null) return "null";

            String result = null;
            if( arg instanceof Number ) {
                result = formatFloat( ((Number)arg).doubleValue() , locale );
            }

            if( result == null ) fail(arg);
            return justify(result);
        }

        /** Format a character
         */
        private String formatCharacter(Object arg , Locale locale ) {
            if( arg == null ) return "null";

            String result = null;
            if( arg instanceof Character ) {
                result = ((Character)arg).toString();
            }
            else {
                if( arg instanceof Byte ) {
                    byte b = ((Byte)arg).byteValue();
                    if( Character.isValidCodePoint(b) ) result =  new String( Character.toChars( b ) );
                    else throw new IllegalFormatCodePointException(b);
                }
                else if( arg instanceof Short ) {
                    short s = ((Short)arg).shortValue();
                    if( Character.isValidCodePoint(s) ) result =  new String( Character.toChars( s ) );
                    else throw new IllegalFormatCodePointException(s);
                }
                else if( arg instanceof Integer ) {
                    int i = ((Integer)arg).intValue();
                    if( Character.isValidCodePoint(i) ) result =  new String( Character.toChars( i ) );
                    else throw new IllegalFormatCodePointException(i);
                }
            }
            if( result == null ) fail(arg);

            return formatString(result,locale);
        }

        /** Format a boolean
         *  If the argument is not a Boolean or boolean types, aby not-null argument will return <tt>true</tt>, a null argument will return <tt>false</tt>
         *  @return Boolean formatted
         */
        private String formatBoolean(Object arg, Locale locale) {
            String result = null;
            if( arg == null ) result = Boolean.FALSE.toString();
            else {
                if (arg instanceof Boolean) {
                    result = formatString( ((Boolean) arg).toString() , locale );
                }
                else
                    result = Boolean.TRUE.toString();
            }
            return formatString(result,locale);
        }

        /** Format a date-time
         */
        private String formatDateTime(Object arg , Locale locale ) {
            if( arg == null ) return "null";
            
            Calendar date = null;
            if( arg instanceof Number ) {
                date = this.calendar;
                date.setTimeInMillis( ((Number)arg).longValue() );
            }
            else if( arg instanceof Date ) {
                date = this.calendar;
                date.setTime( (Date)arg );
            }
            else if( arg instanceof Calendar ) {
                date = (Calendar)arg;
            }

            if( date == null ) fail(arg);
            return formatDateTime( date , getDateTimeConversion() , locale );
        }

        /** Format a date-time
         */
        private String formatDateTime( Calendar date , DateTimeConversion c , Locale locale ) {
            switch( c ) {
                case HOUR_OF_DAY_0:             int hour = date.get(Calendar.HOUR_OF_DAY);
                                                return formatLocalizedNumber( Integer.toString(hour), false , 2 , Flags.ZERO_PAD , locale );

                case HOUR_0:                    hour = date.get(Calendar.HOUR) + 1;
                                                return formatLocalizedNumber( Integer.toString(hour), false , 2 , Flags.ZERO_PAD , locale );

                case HOUR_OF_DAY:               hour = date.get(Calendar.HOUR_OF_DAY);
                                                return Integer.toString(hour);

                case HOUR:                      hour = date.get(Calendar.HOUR) + 1;
                                                return Integer.toString(hour);

                case MINUTE:                    int minute = date.get(Calendar.MINUTE);
                                                return formatLocalizedNumber( Integer.toString(minute), false , 2 , Flags.ZERO_PAD , locale );

                case NANOSECOND:                int nano = date.get(Calendar.MILLISECOND) * 1000000;
                                                return formatLocalizedNumber( Integer.toString(nano), false , 9 , Flags.ZERO_PAD , locale );

                case MILLISECOND:               int milli = date.get(Calendar.MILLISECOND);
                                                return formatLocalizedNumber( Integer.toString(milli), false , 3 , Flags.ZERO_PAD , locale );

                case MILLISECOND_SINCE_EPOCH:   long millis = date.getTimeInMillis();
                                                return formatLocalizedNumber( Long.toString(millis), false , locale );
                                                
                case AM_PM:                     DateFormatSymbols datefs = getDateFormatSymbols(locale);
                                                String s = justify( datefs.getAmPmStrings()[ date.get(Calendar.AM_PM)] );
                                                if( getFlags().contains(Flags.UPPER) ) return s.toUpperCase(locale);
                                                return s;

                case SECONDS_SINCE_EPOCH:       long seconds = date.getTimeInMillis() / 1000;
                                                return formatLocalizedNumber( Long.toString(seconds), false , locale );
                                                
                case SECOND:                    int second = date.get( Calendar.SECOND );
                                                return formatLocalizedNumber( Integer.toString(second), false , 2 , Flags.ZERO_PAD , locale );
                                                
                case TIME:                      // %tH:%tM:%tS
                                                String sHour    = formatDateTime(date , DateTimeConversion.HOUR_OF_DAY_0 , locale);
                                                String sMinute  = formatDateTime(date , DateTimeConversion.MINUTE , locale);
                                                String sSeconde = formatDateTime(date , DateTimeConversion.SECOND , locale);
                                                char[] result   = new char[ 8 ];
                                                sHour.getChars( 0 , 2 , result , 0 );
                                                result[2] = ':';
                                                sMinute.getChars( 0 , 2 , result , 3 );
                                                result[5] = ':';
                                                sSeconde.getChars( 0 , 2 , result , 6 );
                                                return new String(result);
                                                
                case ZONE_NUMERIC:              int i = date.get( Calendar.ZONE_OFFSET );
                                                String prefix = i < 0 ? "-" : "+";
                                                i = Math.abs(i);
                                                int min = i / 60000;
                                    		int offset = (min / 60) * 100 + (min % 60);
                                                return formatLocalizedNumber( Integer.toString(offset) , false , 4 , Flags.ZERO_PAD , prefix , null , locale );

                case ZONE:                      TimeZone tz = date.getTimeZone();
                                                return tz.getDisplayName( date.get( Calendar.DST_OFFSET ) != 0  , TimeZone.SHORT , locale );

                case NAME_OF_DAY_ABBREV:        datefs = getDateFormatSymbols(locale);
                                                s = justify( datefs.getShortWeekdays()[ date.get( Calendar.DAY_OF_WEEK) ] );
                                                if( getFlags().contains(Flags.UPPER) ) return s.toUpperCase(locale);
                                                return s;

                case NAME_OF_DAY:               datefs = getDateFormatSymbols(locale);
                                                s = justify( datefs.getWeekdays()[ date.get( Calendar.DAY_OF_WEEK) ] );
                                                if( getFlags().contains(Flags.UPPER) ) return s.toUpperCase(locale);
                                                return s;

                case NAME_OF_MONTH_ABBREV_X:
                case NAME_OF_MONTH_ABBREV:      datefs = getDateFormatSymbols(locale);
                                                s = justify( datefs.getShortMonths()[ date.get( Calendar.MONTH) ] );
                                                if( getFlags().contains(Flags.UPPER) ) return s.toUpperCase(locale);
                                                return s;

                case NAME_OF_MONTH:             datefs = getDateFormatSymbols(locale);
                                                s = justify( datefs.getMonths()[ date.get( Calendar.MONTH) ] );
                                                if( getFlags().contains(Flags.UPPER) ) return s.toUpperCase(locale);
                                                return s;

                case CENTURY:                   int century = date.get( Calendar.YEAR ) / 100;
                                                return formatLocalizedNumber( Integer.toString(century), false , 2 , Flags.ZERO_PAD , locale );

                case DAY_OF_MONTH_0:            int day = date.get( Calendar.DAY_OF_MONTH );
                                                return formatLocalizedNumber( Integer.toString(day), false , 2 , Flags.ZERO_PAD , locale );

                case DAY_OF_MONTH:              day = date.get( Calendar.DAY_OF_MONTH );
                                                return Integer.toString(day);

                case DAY_OF_YEAR:               day = date.get( Calendar.DAY_OF_YEAR );
                                                return formatLocalizedNumber( Integer.toString(day), false , 3 , Flags.ZERO_PAD , locale );
                                                
                case MONTH:                     int month = date.get( Calendar.MONTH ) + 1;
                                                return formatLocalizedNumber( Integer.toString(month), false , 2 , Flags.ZERO_PAD , locale );

                case YEAR_2:                    int year = date.get( Calendar.YEAR );
                                                return formatLocalizedNumber( Integer.toString(year), false , 4 , Flags.ZERO_PAD , locale ).substring(2);

                case YEAR_4:                    year = date.get( Calendar.YEAR );
                                                return formatLocalizedNumber( Integer.toString(year), false , 4 , Flags.ZERO_PAD , locale );

                case TIME_12_HOUR:              // %tI:%tM %tS %tp
                                                       sHour    = formatDateTime(date , DateTimeConversion.HOUR_0 , locale);
                                                       sMinute  = formatDateTime(date , DateTimeConversion.MINUTE , locale);
                                                       sSeconde = formatDateTime(date , DateTimeConversion.SECOND , locale);
                                                String sAP_AM   = formatDateTime(date , DateTimeConversion.AM_PM , locale);
                                                result = new char[ sHour.length() + sMinute.length() + sSeconde.length() + sAP_AM.length() + 3 ];
                                                offset = 0;
                                                sHour.getChars(0 , sHour.length() , result , offset ); offset += sHour.length();
                                                result[offset++] = ':';
                                                sMinute.getChars(0 , sMinute.length() , result , offset ); offset += sMinute.length();
                                                result[offset++] = ' ';
                                                sSeconde.getChars(0 , sSeconde.length() , result , offset ); offset += sSeconde.length();
                                                result[offset++] = ' ';
                                                sAP_AM.getChars(0 , sAP_AM.length() , result , offset ); offset += sAP_AM.length();
                                                return new String(result);

                case TIME_24_HOUR:              // %tH:%tM
                                                sHour   = formatDateTime(date , DateTimeConversion.HOUR_OF_DAY_0 , locale);
                                                sMinute = formatDateTime(date , DateTimeConversion.MINUTE , locale);
                                                result = new char[ 5 ];
                                                sHour.getChars( 0 , 2 , result , 0 );
                                                result[2] = ':';
                                                sMinute.getChars( 0 , 2 , result , 3 );
                                                return new String(result);

                case DATE_TIME:                 // %ta %tb %td %tT %tZ %tY (Sat Nov 04 12:02:33 EST 1999)
                                                String sNameDay   = formatDateTime(date , DateTimeConversion.NAME_OF_DAY_ABBREV , locale);
                                                String sNameMonth = formatDateTime(date , DateTimeConversion.NAME_OF_MONTH_ABBREV , locale);
                                                String sDay       = formatDateTime(date , DateTimeConversion.DAY_OF_MONTH_0 , locale);
                                                String sTime      = formatDateTime(date , DateTimeConversion.TIME , locale);
                                                String sZone      = formatDateTime(date , DateTimeConversion.ZONE , locale);
                                                String sYear      = formatDateTime(date , DateTimeConversion.YEAR_4 , locale);
                                                result = new char[ sNameDay.length() + sNameMonth.length() + sDay.length() + sTime.length() + sZone.length() + sYear.length() + 5 ];
                                                offset = 0;
                                                sNameDay.getChars(0 , sNameDay.length() , result , offset ); offset += sNameDay.length();
                                                result[offset++] = ' ';
                                                sNameMonth.getChars(0 , sNameMonth.length() , result , offset ); offset += sNameMonth.length();
                                                result[offset++] = ' ';
                                                sDay.getChars(0 , sDay.length() , result , offset ); offset += sDay.length();
                                                result[offset++] = ' ';
                                                sTime.getChars(0 , sTime.length() , result , offset ); offset += sTime.length();
                                                result[offset++] = ' ';
                                                sZone.getChars(0 , sZone.length() , result , offset ); offset += sZone.length();
                                                result[offset++] = ' ';
                                                sYear.getChars(0 , sYear.length() , result , offset ); offset += sYear.length();
                                                return new String(result);
                                                
                case DATE:                      // %tm/%td/%ty
                                                String sMonth = formatDateTime(date , DateTimeConversion.MONTH , locale);
                                                       sDay   = formatDateTime(date , DateTimeConversion.DAY_OF_MONTH_0 , locale);
                                                       sYear  = formatDateTime(date , DateTimeConversion.YEAR_2 , locale);
                                                result = new char[8];
                                                sMonth.getChars(0 , 2 , result , 0 );
                                                result[2] = '/';
                                                sDay.getChars(0 , 2 , result , 3 );
                                                result[5] = '/';
                                                sYear.getChars(0 , 2 , result , 6 );
                                                return new String(result);

                case ISO_STANDARD_DATE:         // %tY-%tm-%td
                                                sYear  = formatDateTime(date , DateTimeConversion.YEAR_4 , locale);
                                                sMonth = formatDateTime(date , DateTimeConversion.MONTH , locale);
                                                sDay   = formatDateTime(date , DateTimeConversion.DAY_OF_MONTH_0 , locale);
                                                result = new char[10];
                                                sYear.getChars(0 , 4 , result , 0 );
                                                result[4] = '-';
                                                sMonth.getChars(0 , 2 , result , 5 );
                                                result[7] = '-';
                                                sDay.getChars(0 , 2 , result , 8 );
                                                return new String(result);

                default : fail(date);
                          return null;
            }
        }

        /** Format a String, Formattable or any other objects
         */
        private String formatString(Object arg , Locale locale ) {
            if( arg == null ) return "null";
            
            if( arg instanceof Formattable ) {
                Formattable f = (Formattable)arg;

                Formatter formatter = new Formatter();
                f.formatTo(  formatter , getFlags().getValue() , getWidth(), getPrecision() );
                return formatter.toString();
            }
            return formatString( arg.toString() , locale );
        }

        /** Format String
         */
        private String formatString(String s , Locale locale ) {
            if ( getPrecision() != -1 && getPrecision() < s.length()) {
                s = s.substring(0, getPrecision() ); // truncate
            }
            if( getFlags().contains(Flags.UPPER) ) {
                s = s.toUpperCase(locale);
            }
            return justify(s);
        }
        
        /** Mise en forme d'un hascode
         */
        private String formatHashCode(Object arg , Locale locale ) {
            return formatString( Integer.toHexString(arg.hashCode() ) , locale );
        }

        /** Justification
         */
        private String justify(String s) {
            if ( getWidth() == -1) {
                return s;
            }
            if (s.length() > getWidth() ) {
                return s;
            }

            int lengthJustification = getWidth() - s.length();
            int offsetJustification = ( getFlags().contains( Flags.LEFT_JUSTIFY ) ? s.length() : 0 );
            int offsetString = ( getFlags().contains( Flags.LEFT_JUSTIFY ) ? 0 : lengthJustification );
            int endJustification = offsetJustification + lengthJustification;
            char[] result = new char[getWidth()];
            for (int i = offsetJustification; i < endJustification; i++) {
                result[i] = ' ';
            }
            s.getChars(0, s.length(), result, offsetString);
            return new String(result);
        }

        private String formatInteger(long value , Locale locale ) {
            String result    = null; // intermediate and final result
            char[] array     = null; // array representing the final result
            int    length    = 0;    // length of the array

            switch(conversion) {
                case DECIMAL_INTEGER     : 
                                           String prefix = null;
                                           String suffix = null;
                                           if( value < 0 ) {
                                               if( getFlags().contains( Flags.PARENTHESE ) ) {
                                                   prefix = "(";
                                                   suffix = ")";
                                               }
                                               else
                                                   prefix = "-";
                                           }
                                           else {
                                                if( getFlags().contains( Flags.PLUS ) ) prefix = "+";
                                                else if( getFlags().contains( Flags.LEADING ) ) prefix = " ";
                                           }
                                           result = formatLocalizedNumber( Long.toString(value) , value < 0 , prefix , suffix , locale ); // localize the raw string

                                           break;
	        case HEXADECIMAL_INTEGER : result = Long.toHexString(value);
                case OCTAL_INTEGER       : if( result == null ) result = Long.toOctalString(value);
                                           length = ( getFlags().contains( Flags.ZERO_PAD ) ? ( result.length() > getWidth() ? result.length() : getWidth() ) : result.length() ) + (getFlags().contains( Flags.ALTERNATE ) ? conversion.getAlternate().length() : 0 );

                                           array = new char[length];
                                           int pos   = 0;

                                           if( getFlags().contains( Flags.ALTERNATE ) ) {
                                               String alt = conversion.getAlternate();
                                               alt.getChars( 0 , alt.length() , array , pos );
                                               pos += alt.length();
                                           }
                                           if( getFlags().contains( Flags.ZERO_PAD ) && ( result.length() < getWidth() ) ) {
                                               int endPaddingPosition = ( getWidth() - result.length() ) + pos;
                                               for(int i = pos ; i < endPaddingPosition ; i++ ) {
                                                   array[i] = '0';
                                               }
                                               pos = endPaddingPosition;
                                           }
                                           result.getChars( 0 , result.length() , array , pos );
                                           result = new String(array);
                                           break;
                default                  : fail(value);
            }

            if( result == null ) fail(value);
            return result;
        }

        private String formatFloat(double value , Locale locale ) {
	    if( Double.isNaN(value) )       return getFlags().contains( Flags.UPPER ) ? "NAN" : "NaN";
            if( Double.isInfinite(value) )  return getFlags().contains( Flags.UPPER ) ? "INFINITY" : "Infinity";

            boolean negative = (Double.compare(value, 0.0) < 0);

            FormattedFloatingDecimal ffd       = null;
            int                      precision = this.getPrecision() == -1 ? 6 : this.getPrecision();
            int                      length    = 0;

            switch( conversion ) {
                case DECIMAL_FLOAT : ffd = new FormattedFloatingDecimal(value, precision , FormattedFloatingDecimal.Form.DECIMAL_FLOAT);
                                     length = ffd.getChars(buffer);


                case SCIENTIFIC    : ffd = new FormattedFloatingDecimal(value, precision, FormattedFloatingDecimal.Form.SCIENTIFIC);
                                     length = ffd.getChars(buffer);

                case GENERAL       : if( precision == 0 ) precision = 1;
                                     ffd = new FormattedFloatingDecimal(value, precision, FormattedFloatingDecimal.Form.GENERAL);
                                     length = ffd.getChars(buffer);
                                     
                default            : fail(value);
            }

            return null;
        }

        /** Return the DecimalFormatSymbols to use for the specified locale
         * @param locale Specified locale
         * @return DecimalFormatSymbols to use
         */
        private DecimalFormatSymbols getDecimalFormatSymbols(Locale locale) {
            if( locale != loc || dfs == null ) {
                 dfs = new DecimalFormatSymbols(locale);
                 groupSeparator   = dfs.getGroupingSeparator();
                 decimalSeparator = dfs.getDecimalSeparator();
                 zero             = dfs.getZeroDigit();
                 loc = locale;
            }
            return dfs;
        }

        /** Return the DateFormatSymbols to use for the specified locale
         *  @param locale Specified locale
         *  @return DateFormatSymbols to use
         */
        private DateFormatSymbols getDateFormatSymbols(Locale locale) {
            if( locale != loc || datefs == null ) {
                datefs = new DateFormatSymbols(locale);
            }
            return datefs;
        }

        /** Formatting a besically string created bye an Long.toString(...) methods into a localized form (with correct separators, zero and groups)
         *  This method manage also the zero-padding and all flags from the ArgumentDetails configuration.
         *
         *  @param basicString String to localize, this string must represent a number in a Java Standard form like Integer.toString(), Float.toString(), etc...
         *  @param negative <tt>true</tt>for localize a negative number
         *  @param locale Locale to use for determines separators, zero...)
         *
         *  @return localized number as a String
         */
        private String formatLocalizedNumber(String basicString , boolean negative ,  Locale locale) {
            return formatLocalizedNumber(basicString, negative, null , null , locale);
        }

        /** Formatting a besically string created bye an Long.toString(...) methods into a localized form (with correct separators, zero and groups)
         *  This method manage also the zero-padding and all flags from the ArgumentDetails configuration.
         * 
         *  @param basicString String to localize, this string must represent a number in a Java Standard form like Integer.toString(), Float.toString(), etc...
         *  @param negative <tt>true</tt>for localize a negative number
         *  @param prefix optionnal prefix to add before the number (may be null)
         *  @param suffix optionnal suffix to aff after the number (may be null)
         *  @param locale Locale to use for determines separators, zero...)
         *
         *  @return localized number as a String
         */
        private String formatLocalizedNumber(String basicString , boolean negative , String prefix , String suffix , Locale locale) {
            return formatLocalizedNumber(basicString , negative , this.getWidth() , this.getFlags() , prefix , suffix , locale);
        }

        /** Formatting a besically string created bye an Long.toString(...) methods into a localized form (with correct separators, zero and groups)
         *
         *  @param basicString String to localize, this string must represent a number in a Java Standard form like Integer.toString(), Float.toString(), etc...
         *  @param negative <tt>true</tt>for localize a negative number
         *  @param width Width to use for formatting (it's the minimum size in characters of the number)
         *  @param groupFlags <tt>true</tt> for enabling group separators
         *  @param zeroPadFlags <tt>true</tt> for enabling zero padding for having the <tt>width</tt> size
         *  @param locale Locale to use for determines separators, zero...)
         *
         *  @return localized number as a String
         */
        private String formatLocalizedNumber( String basicString , boolean negative , int width , Flags myFlags , Locale locale) {
            return formatLocalizedNumber(basicString , negative , width , myFlags , null , null , locale);
        }
        
        /** Formatting a besically string created bye an Long.toString(...) methods into a localized form (with correct separators, zero and groups)
         *
         *  @param basicString String to localize, this string must represent a number in a Java Standard form like Integer.toString(), Float.toString(), etc...
         *  @param negative <tt>true</tt>for localize a negative number
         *  @param width Width to use for formatting (it's the minimum size in characters of the number)
         *  @param groupFlags <tt>true</tt> for enabling group separators
         *  @param zeroPadFlags <tt>true</tt> for enabling zero padding for having the <tt>width</tt> size
         *  @param prefix optionnal prefix to add before the number (may be null)
         *  @param suffix optionnal suffix to aff after the number (may be null)
         *  @param locale Locale to use for determines separators, zero...)
         *
         *  @return localized number as a String
         */
        private String formatLocalizedNumber( String basicString , boolean negative , int width , Flags myFlags , String prefix , String suffix , Locale locale) {
            /** Configure dfs
             */
            getDecimalFormatSymbols(locale);
            
            int  dotPosition      = basicString.length(); // dot position on the basicString

            /** Computing the buffer to allocate depending on the basic string length and transformation to process
             */
            int length  = basicString.length();          // original length
            if( myFlags.contains( Flags.GROUP ) ) {
                /** check where is the 'dot'. The position will help us to find when add groups separator
                 */
                 for(int i = 0 ; i < basicString.length() ; i++ ) {
                     if( basicString.charAt(i) == '.' ) {
                         dotPosition = i;
                         break;
                     }
                 }
                 length += ( dotPosition / 3 ); // place holder for groups separators (3 == groupingSize)
            }
            length += ( prefix == null ? 0 : prefix.length() ) + ( suffix == null ? 0 : suffix.length() ); // include prefix+suffix for padding

            int     arrayPosition = 0; // index where we can insert characters result in the buffer array
            int     padLength     = 0;
            if( myFlags.contains( Flags.ZERO_PAD ) && width > length ) {
                padLength = (width - length);
                length = width;
            }
            

            /** Allocate the array and fill with the zero padding before starting
             */
            char[] array     = new char[ length ];
            if( prefix != null ) {
                prefix.getChars( 0 , prefix.length() , array , 0 );
                arrayPosition += prefix.length();
            }
            if( myFlags.contains( Flags.ZERO_PAD ) ) {
                int endPos = arrayPosition + padLength;
                for( ; arrayPosition < endPos ; arrayPosition++ ) {
                    array[arrayPosition] = zero;
                }
            }
            
            boolean decimalPart   = false;
            for( int i = (negative ? 1 : 0 ) ; i < basicString.length() ; i++ ) {
                char source = basicString.charAt(i);
                char dest   = source;

                if( source == '.' ) {
                    dest = decimalSeparator;
                    decimalPart = true;
                }
                else {
                    dest = (char)( ( source - '0') + zero ); // slice characters depending on the "zero" digit to use
                }
                array[arrayPosition++] = dest;
                if( myFlags.contains( Flags.GROUP ) && !decimalPart && i < (dotPosition - 1) && ( (dotPosition - i) % 3 ) == 1 ) {
                    array[arrayPosition++] = groupSeparator;
                }
            }

            if( suffix != null ) {
                suffix.getChars( 0 , suffix.length() , array , arrayPosition );
            }

            /** result it
             */
            return new String(array,0,arrayPosition);
        }

        /** Throw an IllegalFormatConversionException because the formatting fail
         */
        private void fail(Object arg) {
            if( arg == null ) throw new IllegalFormatConversionException( conversion.getChar() , null );
            throw new IllegalFormatConversionException( conversion.getChar() , arg.getClass() );
        }
    }

    /** Represent flags set usable for an ArgumentDetails
     *  This class is compatible with the java.util.Formatter$Flags class in bit values and can be used with <tt>java.util.Formattable</tt> objects
     *  <p>
     *  Flags can't be instanciate directly. To create a flags set, you have two way:
     *  <ul>
     *   <li>Parse one from their characters with Flags.parse(String s)</li>
     *   <li>Compose a flags set from standard ones with Flags.and(Flags)</li>
     *  </ul>
     * <blockquote><pre>
     *   Flags flagsA = Flags.parse("+,");
     *   Flags flagsB = Flags.PLUS.and( Flags.GROUP );
     * </pre></blockquote>
     */
    public static class Flags {

        /** no flags
         */
        public static final Flags NONE         = new Flags(0);


        /** Flag '-' : Left justiciation
         */
        public static final Flags LEFT_JUSTIFY = new Flags(1);

        /** Shadow flag (inheritance from the conversion's modifier) : Upper Case
         */
        public static final Flags UPPER        = new Flags(2);

        /** Flag '#' : Alternate prefix like 0x for hexadecimal
         */
        public static final Flags ALTERNATE    = new Flags(4);

        /** Flag '+' : Plus prefix for positive numbers
         */
        public static final Flags PLUS         = new Flags(8);

        /** Flag ' ' : Space prefix for positive numbers
         */
        public static final Flags LEADING      = new Flags(16);

        /** Flag '0' : Zero padding for numbers
         */
        public static final Flags ZERO_PAD     = new Flags(32);

        /** Flag ',' : Group padding for numbers
         */
        public static final Flags GROUP        = new Flags(64);

        /** Flag '(" : Parentheses enclosing negative numbers
         */
        public static final Flags PARENTHESE   = new Flags(128);

        /** current flags set
         */
        private int     value   = 0;

        /** Indicate if this flags set is mutable or not
         */
        private boolean mutable = false;

        /** Create an unmutable flags set (for constants)
         */
        private Flags(int v) {
            this(v,false);
        }

        /** Create a mutable flags set (for parse and alterate flags set)
         */
        private Flags(int v , boolean mutable) {
            this.value = v;
            this.mutable = mutable;
        }

        /** Return the current value of the flags set
         */
        public int getValue() {
            return this.value;
        }

        /** Indicate if this flags set contains another flags set
         */
        public boolean contains( Flags flag ) {
            return ( getValue() & flag.getValue() ) == flag.getValue();
        }

        /** Add flags
         */
        public Flags and( Flags flags ) {
            if( ! mutable ) {
                return new Flags( getValue() | flags.getValue() , true );
            }
            this.value |= flags.getValue();
            return this;
        }

        /** Remove flags
         */
        public Flags remove( Flags flags ) {
            if( ! mutable ) {
                return new Flags( getValue() & ~flags.getValue() , true );
            }
            this.value &= ~flags.getValue();
            return this;
        }

        /** Parse a String litteral in a flags set
         */
        public static Flags parse( String flags  , Conversion conversion ) {
            if (flags == null) {
                return Flags.NONE;
            }

            Flags result = Flags.NONE;
            for (int i = 0; i < flags.length(); i++) {
                char c = flags.charAt(i);
                switch (c) {
                    case '-': result = result.and(Flags.LEFT_JUSTIFY);
                              break;
                    case '#': result = result.and(Flags.ALTERNATE);
                              break;
                    case '+': result = result.and(Flags.PLUS);
                              break;
                    case ' ': result = result.and(Flags.LEADING);
                              break;
                    case '0': result = result.and(Flags.ZERO_PAD);
                              break;
                    case ',': result = result.and(Flags.GROUP);
                              break;
                    case '(': result = result.and(Flags.ALTERNATE);
                              break;
                    default:
                        break;
                }
            }
            if( conversion.isUpper() ) {
                result = result.and( Flags.UPPER );
            }
            return result;
        }
    }

    /** Default implementation of <tt>ArgumentNameSpace</tt> accepting standard symbols for all supported conversions.
     *  This class is intended to use with <tt>CompositeArgumentNameSpace</tt>  for creating namespace implementations supporting default conversion's modifier.
     */
    public static class DefaultArgumentNameSpace implements ArgumentNameSpace {

        /** Returns all conversion's identifiers handled by this namespace
         * @return All conversion's modifiers
         */
        public String[] symbols() {
            Conversion[] c      = Conversion.values();
            String[]     result = new String[ c.length ];
            for(int i = 0 ; i < c.length ; i++ ) {
                result[i] = ((Character)c[i].getChar()).toString();
            }
            return result;
        }

        /** Return for a specified symbol (custom conversion's identifier) the standard conversion to use for formatting the underlying argument
         * @param symbol Custom conversion's identifier
         * @return Standard conversion's modifier to use for formatting the underlying argument
         */
        public Conversion getConversion(String symbol) {
            return Conversion.parse( symbol.charAt(0) );
        }

        /** Invoked before using a symbol by a pattern
         * @param symbol Symbol marked as <tt>used</tt>
         */
        public void use(String symbol) {
            /** do nothing */
        }

        /** When an argument is a date, a complementary date-time conversion is required.
         *  This method return which separator must be used to separate the symbol from the date-time conversion.
         *  @return date-time separator
         */
        public String getDateSeparator(String symbol) {
            return "";
        }
    }

    /** This namespace implementation allow to compose a namespace from differents one.
     *  Each symbol's management is delegate to it's owner namespace.
     *  <p>
     *  This composite can't use any <tt>IndexableArgumentNameSpace</tt> because they are not designed for it with their absolute indexing feature.
     *  But <tt>AttributeArgumentNameSpace</tt>, <tt>DetailedArgumentNameSpace</tt> or standard <tt>ArgumentNameSpace</tt> can be mixed.
     */
    public static class CompositeArgumentNameSpace implements AttributeArgumentNameSpace , DetailedArgumentNameSpace {

        private String[]                      symbols    = null;
        private Map<String,ArgumentNameSpace> namespaces = null;

        public CompositeArgumentNameSpace(ArgumentNameSpace... ns) {
            this.symbols = mergeSymbols(ns);
        }

        /** This method merge all symbols from all namespace into a single array.
         *  @param ns All namespace
         *  @return an array with all symbols merged
         *  @throws IllegalArgumentException if we encounteer conflicting symbols or if a namespace implements IndexableArgumentNameSpace
         */
        private String[] mergeSymbols(ArgumentNameSpace... ns) {
            namespaces = new HashMap<String,ArgumentNameSpace>();

            String[] result = null;

            int resultLength = 0;
            int newResultLength = 0;
            for(ArgumentNameSpace namespace : ns ) {
                if( namespace instanceof IndexableArgumentNameSpace )
                    throw new IllegalArgumentException("can't compose a namespace from an IndexArgumentNameSpace");

                String[] toAdd  = namespace.symbols();

                resultLength    = ( result != null ? result.length : 0 );
                newResultLength = resultLength + toAdd.length;

                String[] newResult = new String[ newResultLength ];
                if( result != null ) System.arraycopy(result , 0 , newResult , 0 , result.length );
                System.arraycopy(toAdd  , 0 , newResult , resultLength , toAdd.length );

                /** swap
                 */
                result = newResult;

                for(String symbol : toAdd ) {
                    if( namespaces.containsKey(symbol) ) throw new IllegalArgumentException("Conflicting symbol '" + symbol + "' on " + this );
                    namespaces.put(symbol,namespace);
                }
            }

            return result;
        }

        /** Returns all conversion's identifiers handled by this namespace
         * @return All conversion's modifiers
         */
        public String[] symbols() {
            return symbols;
        }

        /** Return for a specified symbol (custom conversion's identifier) the standard conversion to use for formatting the underlying argument
         * @param symbol Custom conversion's identifier
         * @return Standard conversion's modifier to use for formatting the underlying argument
         */
        public Conversion getConversion(String symbol) {
            ArgumentNameSpace ns = this.namespaces.get(symbol);
            if( ns == null ) return Conversion.STRING;
            return ns.getConversion(symbol);
        }

        /** Return the related property for the specified symbol and the specified argument
         * @param value argument to format
         * @param symbol Symbol describing which property must be formatted
         * @return the requested property
         */
        public Object getAtttribute(Object value,String symbol,Locale locale) {
            ArgumentNameSpace ns = this.namespaces.get(symbol);
            if( ! (ns instanceof AttributeArgumentNameSpace ) ) return value;
            return ((AttributeArgumentNameSpace)ns).getAtttribute(value,symbol,locale);
        }

        /** When an argument is a date-time, a complementary date-time conversion is required.
         *  This method return which separator must be used to separate the symbol from the date-time conversion.
         *  @return date-time separator
         */
        public String getDateSeparator(String symbol) {
            ArgumentNameSpace ns = this.namespaces.get(symbol);
            if( ns == null ) return "";
            return ns.getDateSeparator(symbol);
        }

        /** Return details to use for a specified symbol
         * @param details Current details informations picked-up from the pattern specification
         * @param symbol Which symbol to apply the details
         * @return New details to use for this argument
         */
        public ArgumentDetails getDetails(ArgumentDetails details, String symbol) {
            ArgumentNameSpace ns = this.namespaces.get(symbol);
            if( ns instanceof DetailedArgumentNameSpace ) {
                return ((DetailedArgumentNameSpace)ns).getDetails(details, symbol);
            }
            return details;
        }

        /** Invoked before using a symbol by a pattern
         * @param symbol Symbol marked as <tt>used</tt>
         */
        public void use(String symbol) {
            ArgumentNameSpace ns = this.namespaces.get(symbol);
            if( ns != null ) ns.use(symbol);
        }
    }
}
