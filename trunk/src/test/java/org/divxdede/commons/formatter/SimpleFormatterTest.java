package org.divxdede.commons.formatter;

import java.awt.Color;

/**
 * Unit-Test of SimpleFormatter
 * @author André Sébastien (divxdede)
 */
public class SimpleFormatterTest extends junit.framework.TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /** Correct calls
     */
    public void testSuccess() {
        String result = SimpleFormatter.format(null);
        assertNull(result);

        result = SimpleFormatter.format("Test %% case", "first" );
        assertEquals(result,"Test first case");

        result = SimpleFormatter.format("Test %% , %% , %%" , "A" , 25 , Color.RED );
        assertEquals(result,"Test A , 25 , java.awt.Color[r=255,g=0,b=0]");

        result = SimpleFormatter.format("A=%%=%%" , 10 );
        assertEquals(result,"A=10=%%");

        result = SimpleFormatter.format("%%");
        assertEquals(result,"%%");

        result = SimpleFormatter.format("aaaa");
        assertEquals(result,"aaaa");

        Object a = null;
        result = SimpleFormatter.format("[u=%%]" , a );
        assertEquals(result , "[u=null]");
    }

    /** Incorrect call
     */
    public void testFailures() {

        try {
            String result = SimpleFormatter.format("A 2 args %% , %%" , "25" , "32" , "48");
            fail("Incorrect argument's number must throw an IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            // ok
        }

        try {
            String result = SimpleFormatter.format(null,"25");
            fail("Incorrect argument's number must throw an IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            // ok
            fail("Should throw an NullPointerException");
        }
        catch(NullPointerException e) {
            // ok
        }
    }
}
