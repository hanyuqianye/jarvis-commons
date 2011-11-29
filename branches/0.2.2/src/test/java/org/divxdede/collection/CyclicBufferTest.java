package org.divxdede.collection;

import java.util.Iterator;
import junit.framework.TestCase;

/**
 *
 * @author André Sébastien (divxdede)
 */
public class CyclicBufferTest extends TestCase {


    public void testCyclic() {
        CyclicBuffer<Long> buffer = new CyclicBuffer<Long>(10);
        assertEquals( buffer.getCapacity() , 10 );
        assertEquals( buffer.size() , 0 );
        assertTrue( buffer.isEmpty() );
        assertEquals( buffer , "[]");

        buffer.add(1L);
        assertEquals( buffer.size() , 1 );
        assertEquals( buffer.getFirst() , (Long)1L );
        assertEquals( buffer.getLast() , (Long)1L );
        assertEquals( buffer , "[1]");

        buffer.add(2L);
        buffer.add(3L);
        buffer.add(4L);
        buffer.add(5L);
        buffer.add(6L);
        buffer.add(7L);
        buffer.add(8L);
        buffer.add(9L);
        // 1 2 3 4 5 6 7 8 9 [9/10]
        assertEquals( buffer.size() , 9 );
        assertEquals( buffer.getFirst() , (Long)1L );
        assertEquals( buffer.getLast() , (Long)9L );
        assertEquals( buffer , "[1,2,3,4,5,6,7,8,9]");

        buffer.add(10L);
        // 1 2 3 4 5 6 7 8 9 10 [10/10]
        assertEquals( buffer.size() , 10 );
        assertEquals( buffer.getFirst() , (Long)1L );
        assertEquals( buffer.getLast() , (Long)10L );
        assertEquals( buffer , "[1,2,3,4,5,6,7,8,9,10]");

        buffer.add(11L);
        // 2 3 4 5 6 7 8 9 10 11 [10/10]
        assertEquals( buffer.size() , 10 );
        assertEquals( buffer.getFirst() , (Long)2L );
        assertEquals( buffer.getLast() , (Long)11L );
        assertEquals( buffer , "[2,3,4,5,6,7,8,9,10,11]");

        buffer.add(12L);
        // 3 4 5 6 7 8 9 10 11 12 [10/10]
        assertEquals( buffer.size() , 10 );
        assertEquals( buffer.getFirst() , (Long)3L );
        assertEquals( buffer.getLast() , (Long)12L );
        assertEquals( buffer , "[3,4,5,6,7,8,9,10,11,12]");

        buffer.add(13L);
        buffer.add(14L);
        buffer.add(15L);
        buffer.add(16L);
        buffer.add(17L);
        buffer.add(18L);
        // 9 10 11 12 13 14 15 16 17 18 [10/10]
        assertEquals( buffer.size() , 10 );
        assertEquals( buffer.getFirst() , (Long)9L );
        assertEquals( buffer.getLast() , (Long)18L );
        assertEquals( buffer , "[9,10,11,12,13,14,15,16,17,18]" );

        buffer.setCapacity(8);
        // 11 12 13 14 15 16 17 18 [8/8]
        assertEquals( buffer.getCapacity() , 8 );
        assertEquals( buffer.size() , 8 );
        assertEquals( buffer.getFirst() , (Long)11L );
        assertEquals( buffer.getLast() , (Long)18L );
        assertEquals( buffer , "[11,12,13,14,15,16,17,18]" );

        buffer.setCapacity( 12 );
        // 11 12 13 14 15 16 17 18 [8/12]
        assertEquals( buffer.getCapacity() , 12 );
        assertEquals( buffer.size() , 8 );
        assertEquals( buffer.getFirst() , (Long)11L );
        assertEquals( buffer.getLast() , (Long)18L );
        assertEquals( buffer , "[11,12,13,14,15,16,17,18]" );

        buffer.add(19L);
        buffer.add(20L);
        buffer.add(21L);
        buffer.add(22L);
        buffer.add(23L);
        assertEquals( buffer , "[12,13,14,15,16,17,18,19,20,21,22,23]");
        
        // 12 13 14 15 16 17 18 19 20 21 22 23 [12/12]
        assertEquals( buffer.getCapacity() , 12 );
        assertEquals( buffer.size() , 12 );
        assertEquals( buffer.getFirst() , (Long)12L );
        assertEquals( buffer.getLast() , (Long)23L );

        // 12 13 14 15 16 17 18 19 20 21 22 23 [12/12]
        assertEquals( buffer.get(4) , (Long)16L );

        assertEquals( buffer.element() , (Long)23L );
        assertEquals( buffer.element() , (Long)23L );

        assertEquals( buffer.peek() , (Long)23L );
        assertEquals( buffer.peek() , (Long)23L );
        assertEquals( buffer , "[12,13,14,15,16,17,18,19,20,21,22,23]");
        
        assertEquals( buffer.poll() , (Long)23L );
        assertEquals( buffer.poll() , (Long)22L );
        assertEquals( buffer.size() , 10 );
        assertEquals( buffer , "[12,13,14,15,16,17,18,19,20,21]");

        buffer.set(5 , (Long)66L );
        assertEquals( buffer , "[12,13,14,15,16,66,18,19,20,21]");

        assertEquals( buffer.isEmpty() , false );
        buffer.clear();
        assertEquals( buffer.size() , 0 );
        assertEquals( buffer.isEmpty() , true );
    }

    public void testToArray() {
         CyclicBuffer<Long> buffer = new CyclicBuffer<Long>(5);
         buffer.add( (Long)1L );
         buffer.add( (Long)2L );
         buffer.add( (Long)3L );
         buffer.add( (Long)4L );
         buffer.add( (Long)5L );
         buffer.add( (Long)6L ); // [ 6 2 3 4 5 ] start=1 , end=1, count=5
         assertEquals( buffer , "[2,3,4,5,6]");

         buffer.add( (Long)7L ); // [ 6 7 3 4 5 ] start=2, end=2, count=5
         assertEquals( buffer , "[3,4,5,6,7]");

         buffer.remove(); // [ 6 X 3 4 5 ] start=2, end=1, count=4
         assertEquals( buffer , "[3,4,5,6]");

         buffer.setCapacity(3); // [ 4 5 6 ] start = 0 , end = 0 , count = 3
         assertEquals( buffer , "[4,5,6]");

         buffer.add( (Long)8L ); // [ 8 5 6 ] start=1, end=1, count=3
         assertEquals( buffer , "[5,6,8]");

         buffer.setCapacity(6); // [ 5 6 8 ]
         assertEquals( buffer , "[5,6,8]");

         buffer.add( (Long)9L ); // [ 5 6 8 9 ]
         assertEquals( buffer , "[5,6,8,9]");
    }

    private static void assertEquals( CyclicBuffer buffer , String contents ) {
        assertEquals( toStringByIteration(buffer)  , contents);
        assertEquals( toStringByList(buffer) , contents );
        assertEquals( toStringByArray(buffer) , contents);
    }

    private static String toStringByList( CyclicBuffer buffer ) {
        String result = "[";
        for(int i = 0 ; i < buffer.size() ; i++ ) {
            result += buffer.get(i);
            if( i < buffer.size() - 1 ) result += ",";
        }
        result += "]";
        return result;
    }

    private static String toStringByIteration( CyclicBuffer buffer ) {
        String result = "[";
        Iterator i = buffer.iterator();
        while( i.hasNext() ) {
            result += i.next();
            if( i.hasNext() ) result += ",";
        }
        result += "]";
        return result;
    }

    private static String toStringByArray(CyclicBuffer buffer) {
        String result = "[";

        Object[] array = buffer.toArray();
        for(int i = 0 ; i < array.length ; i++) {
            result += array[i];
            if( i < array.length - 1 ) result += ",";
        }
        result += "]";
        return result;
    }
}
