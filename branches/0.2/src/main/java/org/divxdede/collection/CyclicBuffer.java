/*
 * Copyright (c) 2010 INFASS Systèmes (http://www.infass.com) All rights reserved.
 * CyclicBuffer.java is a part of this Commons library
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
package org.divxdede.collection;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Cyclic buffer implementing the {@link Queue} interface.
 * <p>
 * This buffer has a fixed size ({@link #getCapacity()}, {@link #setCapacity(int)}.<br>
 * When a object is inserted inside this buffer, if the buffer is out of capacity (too small for receive the new object),
 * the last object inserted inide this buffer will be removed for let the place to the new one.<br>
 * The fact to privilegiate newers insertion over oldest make this buffer a <strong>cyclic buffer</strong>.
 * <p>
 * This buffer don't implements {@link java.util.List} collection but own somme confortable methods like:
 * <ul>
 *   <li>{@link #get(int)}</li>
 *   <li>{@link #set(int, java.lang.Object)}</li>
 * </ul>
 * <p>
 * Methods from the {@link Queue} interface are thread-safe but others are not.
 * Methods from the {@link Collection} interface are <strong>not</strong> thread safe.
 *
 * @author André Sébastien (divxdede)
 */
public class CyclicBuffer<E> extends AbstractCollection<E> implements Queue<E> {
    
    private Object[] array    = null;
    private int      offset   = 0;
    private int      count    = 0;
    private int      modCount = 0;

    /** Create a default cyclic buffer with a capacity of 10
     */
    public CyclicBuffer() {
        this(10);
    }
    
    /** Creates a new instance of CyclicBuffer */
    public CyclicBuffer(int capacity) {
        setCapacity(capacity);
    }
   
    /* Returns the number of elements in this collection.  If this collection
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     * 
     * @return the number of elements in this collection
     */
    public int size() {
        return count;
    }
    
    /**
     * Returns an iterator over the elements in this collection.  
     * Older for most recent order
     * 
     * @return an <tt>Iterator</tt> over the elements in this collection
     */
    public Iterator<E> iterator() {
        return new Itr();
    }
    
    /**
     * Returns an array containing all of the elements in this collection.  If
     * the collection makes any guarantees as to what order its elements are
     * returned by its iterator, this method must return the elements in the
     * same order.<p>
     *
     * The returned array will be "safe" in that no references to it are
     * maintained by this collection.  (In other words, this method must
     * allocate a new array even if this collection is backed by an array).
     * The caller is thus free to modify the returned array.<p>
     *
     * This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this collection
     */
    public Object[] toArray() {
        Object[] a = new Object[ size() ];
        if( a.length == 0 ) return a;
        return toArrayImpl(a);
    }
    
    /**
     * Returns an array containing all of the elements in this collection; 
     * the runtime type of the returned array is that of the specified array.  
     * If the collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.<p>
     *
     * If the collection fits in the specified array with room to spare (i.e.,
     * the array has more elements than the collection), the element in the
     * array immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the
     * collection <i>only</i> if the caller knows that the collection does
     * not contain any <tt>null</tt> elements.)<p>
     *
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order. <p>
     *
     * If the array isn't big-enough, only most-recents elements on the array
     *
     * @param  a the array into which the elements of the collection are to
     * 	       be stored, if it is big enough; otherwise, a new array of the
     * 	       same runtime type is allocated for this purpose.
     * @return an array containing the elements of the collection.
     * 
     * @throws NullPointerException if the specified array is <tt>null</tt>.
     * 
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in this
     *         collection.
     */
    public <T> T[] toArray(T[] a) {
        return (T[])toArrayImpl( a);
    }
    
    /**
     * Ensures that this collection contains the specified element (optional
     * operation).  Returns <tt>true</tt> if the collection changed as a
     * result of the call.  (Returns <tt>false</tt> if this collection does
     * not permit duplicates and already contains the specified element.)
     * Collections that support this operation may place limitations on what
     * elements may be added to the collection.  In particular, some
     * collections will refuse to add <tt>null</tt> elements, and others will
     * impose restrictions on the type of elements that may be added.
     * Collection classes should clearly specify in their documentation any
     * restrictions on what elements may be added.<p>
     *
     * This implementation always throws an
     * <tt>UnsupportedOperationException</tt>.
     *
     * @param o element whose presence in this collection is to be ensured.
     * @return <tt>true</tt> if the collection changed as a result of the call.
     * 
     * @throws UnsupportedOperationException if the <tt>add</tt> method is not
     *		  supported by this collection.
     * 
     * @throws NullPointerException if this collection does not permit
     * 		  <tt>null</tt> elements, and the specified element is
     * 		  <tt>null</tt>.
     * 
     * @throws ClassCastException if the class of the specified element
     * 		  prevents it from being added to this collection.
     * 
     * @throws IllegalArgumentException if some aspect of this element
     *            prevents it from being added to this collection.
     */
    public boolean add(E o) {
        array[offset++] = o;
        if( count < getCapacity() ) count++;
        if( offset >= getCapacity() ) offset = 0;
        
        modCount++;
        return true;
    }
    
    /** Retrieve a element from it's index inside this buffer.
     *  <p>
     *  Be careful, this buffer is cyclic, that mean that an object can be moved inside this buffer.<br>
     *  This method is not thread-safe, and should be used carefully
     */
    public E get(int index) {
        if( index < 0 || index >= size() ) throw new IllegalArgumentException("index " + index + " out of bound");
        return (E)array[ logicalIndexToPhysicalIndex(index) ];
    }

    /** Returns the last inserted item inside this cyclic buffer.
     *  @return The last inserted item inside this cyclic buffer.
     *  @see #getFirst()
     *  @since 0.2
     */
    public E getLast() {
        if( isEmpty() ) return null;
        int i = offset--;
        if( i < 0 ) i = getCapacity() - 1;
        return (E)array[i];
    }

    /** Returns the oldest inserted item inside this cyclic buffer that was not yet evicted.
     *  @return The oldest inserted item inside this cyclic buffer that was not yet evicted.
     *  @see #getLast()
     *  @since 0.2
     */
    public E getFirst() {
        if( isEmpty() ) return null;
        return (E)array[ getFirstElementPhysicalIndex() ];
    }

    /** Set an object at a specified index
     *  Be careful, this buffer is cyclic, that mean that an object can be moved inside this buffer.<br>
     *  This method is not thread-safe, and should be used carefully
     */
    public void set(int index , E object ) {
        if( index < 0 || index >= size() ) throw new IllegalArgumentException("index " + index + " out of bound");
        array[ logicalIndexToPhysicalIndex(index) ] = object;
    }
    
    /** Return the physical index inside the backed array for the first element of this buffer
     */
    private int getFirstElementPhysicalIndex() {
        if( size() < getCapacity() ) return 0;
        int start = offset + 1;
        return start == getCapacity() ? 0 : start;
    }
    
    /** Convert a logical index (index in the buffer coordinate) into a physical index (index in the backed array coordinate)
     */
    private int logicalIndexToPhysicalIndex( int logicalIndex ) {
        return ( ( getFirstElementPhysicalIndex() + logicalIndex ) % getCapacity() );
    }
    
    /** Unsupported operation
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("remove(Object) is unsupported by CyclicBuffer, use peek() or remove() for removing last entry");
    }
    
    /** Unsupported operation
     */
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll(Collection) is unsupported by CyclicBuffer");
    }
    
    /** Unsupported operation
     */
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll(Collection) is unsupported by CyclicBuffer");
    }
    
    /**
     * Removes all of the elements from this collection (optional operation).
     * The collection will be empty after this call returns (unless it throws
     * an exception).<p>
     *
     * This implementation iterates over this collection, removing each
     * element using the <tt>Iterator.remove</tt> operation.  Most
     * implementations will probably choose to override this method for
     * efficiency.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's <tt>iterator</tt> method does not implement the
     * <tt>remove</tt> method and this collection is non-empty.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> method is
     * 		  not supported by this collection.
     */
    public void clear() {
        count  = 0;
        offset = 0;
        Arrays.fill( array , 0 , array.length , null );
        modCount++;
    }
    
    /**
     * Inserts the specified element into this buffer. 
     * If the buffer is full, the oldest-entry is removed for allowing this insertion.
     *
     * @param o the element to insert.
     * @return <tt>true</tt>
     */
    public synchronized boolean offer(E o) {
        return add(o);
    }

    /**
     * Retrieves and removes the head of this queue, or <tt>null</tt>
     * if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this
     *         queue is empty.
     */
    public synchronized E poll() {
        if( count == 0 ) return null;
        
        offset--;
        if( offset < 0 ) offset = getCapacity() - 1;
        
        modCount++;
        return (E)array[offset];
    }

    /**
     * Retrieves and removes the head of this queue.  This method
     * differs from the <tt>poll</tt> method in that it throws an
     * exception if this queue is empty.
     *
     * @return the head of this queue.
     * @throws NoSuchElementException if this queue is empty.
     */
    public synchronized E remove() {
        if( count == 0 ) throw new NoSuchElementException();
        return poll();
    }

    /**
     * Retrieves, but does not remove, the head of this queue,
     * returning <tt>null</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue
     * is empty.
     */
    public synchronized E peek() {
        if( count == 0 ) return null;
        
        int index = offset - 1;
        if( index < 0 ) index = getCapacity() - 1;
        
        modCount++;
        return (E)array[index];
    }

    /**
     * Retrieves, but does not remove, the head of this queue.  This method
     * differs from the <tt>peek</tt> method only in that it throws an
     * exception if this queue is empty.
     *
     * @return the head of this queue.
     * @throws NoSuchElementException if this queue is empty.
     */
    public synchronized E element() {
        if( count == 0 ) throw new NoSuchElementException();
        return peek();
    }
    
    /** Define the new capacity of this cyclic buffer.
     *  If the new capacity is less than current, only most-recent elements are preserved to fit the new capacity.
     *  @param new capacity, must be > 0
     */
    public void setCapacity(int newCapacity) {
        if( newCapacity <= 0 ) throw new IllegalArgumentException("capacity can't be less than 1");
        this.array  = toArrayImpl( new Object[newCapacity] );
        
        this.count  = size() > newCapacity ? newCapacity : size();
        
        this.offset = this.count;
        if( this.offset >= size() ) this.offset = 0;
        
        modCount++;
    }
    
    /** Retrieve the capacity of this cyclic buffer
     */
    public int getCapacity() {
        return this.array == null ? 0 : this.array.length;
    }
    
    /** Internal method for populate a new array with datas from this buffer
     */
    private Object[] toArrayImpl( Object[] result ) {
        int newSize = result.length;
        if( newSize <= 0) throw new IllegalArgumentException("size can't be less than 1");

        if( array != null ) {
            /** 
             *                                   OFFSET
             *                    0     1     2     3     4     5     6     7    8      9    10    11  
             *    Source        [10]  [11]  [12]  [04]  [05]  [06]  [07]  [08]  [09]                    (offset=3, count=9 , size=9)
             *    Cas A (>>)    [04]  [05]  [06]  [07]  [08]  [09]  [10]  [11]  [12]  [  ]  [  ]  [  ]  (offset=9, count=9 , size=11)
             *    Cas B (<<)    [07]  [08]  [09]  [10]  [11]  [12]                                      (offset=0, count=6 , size=6)
             *                   
             */
            
            // Nombre de données à reprendre
            int newCount = count > newSize ? newSize : count;   // Cas A = 9  ;;; Cas B = 6                     
            int copied   = newCount;                            // Cas A = 9  ;;; Cas B = 6                     
            int skipped  = count - copied;                      // Cas A = 0  ;;; Cas B = 3
            
            int traited   = 0;  
            int newOffset = 0;
            if(  count > offset ) {
                // Si les données à partir d'offset sont occupées (c'est d'anciennes données), alors on commence par les recopier.
                int skippable = getCapacity() - offset;               // Cas A = 6  ;;; Cas B = 6               
                if( skipped < skippable ) {                    // Arrive dans les deux cas
                    int length = skippable - skipped;          // Cas A = 6  ;;; Cas B = 3

                    // Cas A : On copie à partir de 3 jusqu'a 8 => [04]  [05]  [06]  [07]  [08]  [09]
                    // Cas B : On copie à partir de 6 jusqu'a 8 => [07]  [08]  [09]
                    System.arraycopy( array , offset + skipped , result , newOffset , length );
                    skipped    = 0;                            // Cas A = 0  ;;; Cas B = 0
                    copied     = copied - length;              // Cas A = 3  ;;; Cas B = 3
                    newOffset += length;                       // Cas A = 6  ;;; Cas B = 3
                }
                else {
                    // On veut ignorer plus de slots que l'on peut en copier lors de cette phase, on ne fait rien, si ce n'est compter ceux déja ignorer
                    skipped = skipped - skippable;
                }
            }
            if( copied > 0)  {
                //                                                    0     1     2      3     4     5      6     7     8
                // Cas A : On copie à partir de 0 jusqu'a 2 EN 6 => [04]  [05]  [06]   [07]  [08]  [09] + [10]  [11]  [12]
                // Cas B : On copie à partir de 0 jusqu'a 2 EN 3 => [07]  [08]  [09] + [10]  [11]  [12]
                System.arraycopy( array , 0 + skipped , result , newOffset , copied );
            }
        }
        return result;
    }

    /** Internal iterator
     */
    private class Itr implements Iterator<E> {
        
        int shift            = size() > offset ? offset : 0;
        int current          = 0;
        
        int expectedModCount = modCount;
        
        public boolean hasNext() {
            checkForComodification();
            return current < size();
        }

        public E next() {
            if( ! hasNext() ) throw new NoSuchElementException();
            
            try {
                int index = current + shift;
                if( index >= getCapacity() ) index = index - getCapacity();
                
                Object result = array[index];
                current++;  

                return (E)result;
            }
            catch(Exception e) {
            	e.printStackTrace() ;
                checkForComodification();
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        private void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }        
    }
}
