/*
 * Copyright (c) 2010 INFASS Systèmes (http://www.infass.com) All rights reserved.
 * Cache.java is a part of this Commons library
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

import org.divxdede.commons.Disposable;
import org.divxdede.text.formatter.SimpleFormatter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

/** Create a cache instance that hold like a Map a pair of Key/Value.
 *  A Cache has a maximum size and hold a Removal Entry Policy that decide which entry to remove when the cache reach it's maximum size.
 *  <p>
 *  This implementation can allow 3 Removal entry policies
 *  <ul>
 *    <li>LeastRecentlyUsed: The least recently used entry will be removed when the cache reach it's maximum size</li>
 *    <li>LeastFrequentlyUsed: The least frequently used entry will be removed when the cache reach it's maximum size</li>
 *    <li>OldestInsertion: The oldest inserted entry will be removed when the cache reach it's maximum size</li>
 *  </ul>
 *  <p>
 *  Exemple:
 *  <pre>
 *       Cache<String,Color> cache = new Cache<String,Color>( 2 , RemovalEntryPolicy.LeastRecentlyUsed );
 *       cache.put("sophie" , Color.RED );
 *       cache.put("tom" , Color.BLUE );
 *       
 *       cache.get("tom");   
 *       cache.get("sophie");
 *       
 *       cache.put("robin" , Color.GREEN); // sophie is accessed more recently than "tom", then robin will be remove "tom" entry
 *  </pre>
 *  <p>
 *  This cache implementation is thread-safe by default because we thinks that these objects are often build for be used in a concurrent architecture.
 *
 * @author André Sébastien (divxdede)
 */
public class Cache<K,V> implements Disposable , Iterable<V> {

	/** Removal entry policy applicable to a Cache when it's size reach it's maximum size.
	 */
	public enum RemovalEntryPolicy { 
		
		/** This removal policy remove the least recently used entry when the cache reach it's maximum size.
		 *  Any new entry is considered as the most recently entry used
		 */
		LeastRecentlyUsed , 
		
		/** This removal policy remove the least frequently used entry when the cache reach it's maximum size.
		 *  If the cache is full, any new entry will be sucessfully inserted to let it a chance to be frequently used :P
		 */
		LeastFrequentlyUsed , 
		
		/** This removal policy remove the oldest inserted entry when the cache reach it's maximum size
		 */
		OldestInsertion 
	};
	
	/** Internal implementation of this map regarding to it's cache policy
	 */
	private final CacheHelper<K,V> helper;
	
	private final RemovalEntryPolicy policy;
	
	public Cache(int maximumSize , RemovalEntryPolicy policy ) {
		this.policy = policy;
		switch(this.policy) {
			case LeastFrequentlyUsed : helper = new FrequencyMap<K,V>(maximumSize);
			                           break;
			case LeastRecentlyUsed   : helper = new FixedMap<K,V>(maximumSize,true);
			                           break;
			case OldestInsertion     : helper = new FixedMap<K,V>(maximumSize,false);            
			                           break;
		    default                  : throw new IllegalArgumentException("policy can't be null");
		}
	}
	
	/** Return the <tt>Removal entry policy</tt> of this cache when it's size reach it's maximum size
	 * @return <tt>Removal entry policy</tt> of this cache
	 */
	public RemovalEntryPolicy getRemovalEntryPolicy() {
		return policy;
	}
	
    /**
     * Associates the specified value with the specified key in this cache.
     * If the cache previously contained a mapping for this key, the old value is replaced.
     * <p>
     * This method record an access to the specified entry in order to respect this cache policy when it's size reach it's maximum size.
     * That mean if the specified entry already exists, this method don't reset previous record accesses for the existing key.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.  
     *         A <tt>null</tt> return can also indicate that the HashMap previously associated <tt>null</tt> with the specified key.
     */
	public synchronized V put(K key,V value) {
		return helper.put(key,value);
	}
	
    /**
     * Returns the value to which the specified key is mapped in this cache or <tt>null</tt> if the cache contains no mapping for this key.
     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate that the cache contains no mapping for the key; it is also possible that the cache explicitly maps the key to <tt>null</tt>.
     * The <tt>containsKey</tt> method may be used to distinguish these two cases.
     * <p>
     * This method record an access to the specified entry in order to respect this cache policy when it's size reach it's maximum size.
     * You can have a cache respecting the Least Recently Used order or Least Frequently Used order or simply Insertion order 
     *
     * @param   key the key whose associated value is to be returned.
     * @return  the value to which this map maps the specified key, or
     *          <tt>null</tt> if the map contains no mapping for this key.
     * @see #put(Object, Object)
     */
	public synchronized V get(K key) {
		return helper.get(key);
	}
	
    /**
     * Returns <tt>true</tt> if this cache contains a mapping for the specified key.
     * <p>
     * This method <tt>don't record</tt> any access to the specified entry and do not participate to help the cache's policy
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */		
	public synchronized boolean containsKey(K key) {
		return helper.containsKey(key);
	}
	
    /**
     * Returns an iterator over the <tt>values<tt> contents of this cache .
     * This iterator order begin from the eldest entry to the newest entry regarding the current policy of this cache.
     * 
     * @return an Iterator.
     */	
	public synchronized Iterator<V> iterator() {
		return helper.iterator();
	}
	
    /**
     * Returns an iterator over the <tt>keys<tt> contents of this cache .
     * This iterator order begin from the eldest entry to the newest entry regarding the current policy of this cache.
     * 
     * @return an Iterator.
     */
	public synchronized Iterator<K> iteratorKeys() {
		return helper.iteratorKeys();
	}

	/** Return the maximum size of this cache.
	 *  <tt>
	 *  When you reach the maximum size, the removal entry policy is used to determine which entry must be removed to preserve the maximum size.
	 *  
	 *  @return Maximum size of this cache
	 */
	public int getMaximumSize() {
		return helper.getMaximumSize();
	}
	
	/** Clear the cache contents.
	 *  This cache can be reuse at any time even after a dispose invocation.
	 */
	public synchronized void dispose() {
	    helper.dispose();
	}
	
	/** Return the current size of this cache.
	 *  @return Current size of this cache
	 */
	public synchronized int getSize() {
		return helper.getSize();
	}
	
	/** Interface describing the helper class responsible to hold cache entry.
	 *  This help must respect the requested policy when the cache reach it's maximum size
	 *   
	 *  @author André Sébastien
	 */
	public static interface CacheHelper<K,V> extends Iterable<V> , Disposable {
		
		/** Return the maximum size of this cache
		 *  @return Maximum size of this cache
		 */
		public int getMaximumSize();
		
		/** Return the current size of this cache.
		 *  @return Current size of this cache
		 */
		public int getSize();
		
	    /**
	     * Associates the specified value with the specified key in this cache.
	     * If the map previously contained a mapping for this key, the old value is replaced.
	     * <p>
	     * This method record an access to the specified entry in order to respect this cache policy when it's size reach it's maximum size.
	     * That mean if the specified entry already exists, this method don't reset previous record accesses for the existing key.
	     *
	     * @param key key with which the specified value is to be associated.
	     * @param value value to be associated with the specified key.
	     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.  
	     *         A <tt>null</tt> return can also indicate that the HashMap previously associated <tt>null</tt> with the specified key.
	     */
		public V put(K key,V value);
		
	    /**
	     * Returns the value to which the specified key is mapped in this cache or <tt>null</tt> if the cache contains no mapping for this key.
	     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate that the cache contains no mapping for the key; it is also possible that the cache explicitly maps the key to <tt>null</tt>.
	     * The <tt>containsKey</tt> method may be used to distinguish these two cases.
	     * <p>
	     * This method record an access to the specified entry in order to respect this cache policy when it's size reach it's maximum size.
	     * You can have a cache respecting the Least Recently Used order or Least Frequently Used order or simply Insertion order 
	     *
	     * @param   key the key whose associated value is to be returned.
	     * @return  the value to which this map maps the specified key, or
	     *          <tt>null</tt> if the map contains no mapping for this key.
	     * @see #put(Object, Object)
	     */		
		public V get(K key);
		
	    /**
	     * Returns <tt>true</tt> if this cache contains a mapping for the specified key.
	     * <p>
	     * This method <tt>don't record</tt> any access to the specified entry and do not participate to help the cache's policy
	     *
	     * @param   key   The key whose presence in this map is to be tested
	     * @return <tt>true</tt> if this map contains a mapping for the specified
	     * key.
	     */		
		public boolean containsKey(K key);
		
	    /**
	     * Returns an iterator over the <tt>values<tt> contents of this cache .
	     * This iterator order begin from the eldest entry to the newest entry regarding the current policy of this cache.
	     * 
	     * @return an Iterator.
	     */
		public Iterator<V> iterator();
		
	    /**
	     * Returns an iterator over the <tt>keys<tt> contents of this cache .
	     * This iterator order begin from the eldest entry to the newest entry regarding the current policy of this cache.
	     * 
	     * @return an Iterator.
	     */
		public Iterator<K> iteratorKeys();
	}	
	
    /** LinkedHashMap implementation with a maximum size that can manage cache policy in 2 ways:
     *  <ul>
     *    <li><tt>false</tt> : Insertion order (Oldest entries removed first)</li>
     *    <li><tt>true</tt>  : Access order (Least Recently Used removed first)</li>
     *  </ul>
     */
    private static class FixedMap<K,V> extends LinkedHashMap<K,V> implements CacheHelper<K,V> {
    	
    	/** Maximum size
    	 */
    	private final int 			     maximumSize;
    	
    	/** Constructor with a specified order
    	 *  @param maximumSize Maximum size of this map
    	 *  @param accessOrder <tt>false</tt> for an insertion order, <tt>true</tt> for a least recently used order
    	 */
    	public FixedMap(int maximumSize , boolean accessOrder) {
    		super( 16 , 0.75f , accessOrder ); // default INITIAL_CAPACITY and LOAD_FACTOR
    		this.maximumSize = maximumSize;
    	}
    	
	    /**
	     * Returns an iterator over the <tt>values<tt> contents of this cache .
	     * This iterator order begin from the eldest entry to the newest entry regarding the current policy of this cache.
	     * 
	     * @return an Iterator.
	     */
    	public Iterator<V> iterator() {
    		return super.values().iterator();
    	}
    	
	    /**
	     * Returns an iterator over the <tt>keys<tt> contents of this cache .
	     * This iterator order begin from the eldest entry to the newest entry regarding the current policy of this cache.
	     * 
	     * @return an Iterator.
	     */    	
    	public Iterator<K> iteratorKeys() {
    		return super.keySet().iterator();
    	}
    	
    	@Override
    	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
    		return size() > getMaximumSize();
    	}
    	
    	/* Return the maximum size allowed in this cache map
    	 */
    	public int getMaximumSize() {
    		return maximumSize;
    	}
    	
    	public void dispose() {
    		this.clear();
    	}
    	
    	public int getSize() {
    		return size();
    	}
    }	
	
	/** CacheHelper implementation for <tt>Least Frequently Used</tt> removal policy.
	 *  <p>
	 *  This cache use an HashMap 
	 *  but maintain each entries linked by "frequency access order" for determine which entry to remove when this cache is full.
	 *  <p>
	 *  
	 */
	private static class FrequencyMap<K,V> implements CacheHelper<K,V> {
			
		/** The first bullet (less frequently usage bullet) of this map
		 */
		private FrequencyBullet<K,V>	        firstBullet = null;    
		
		private int                             maximumSize = 0;
		private HashMap<K,FrequencyEntry<K,V>>  map    	    = null;
		
		/** Constructor
    	 */
		public FrequencyMap(int maximumSize) {
			 this.maximumSize = maximumSize; 
			 this.map 		  = new HashMap<K,FrequencyEntry<K,V>>();
		}
		
    	/* Return the maximum size allowed in this cache map
    	 */
    	public int getMaximumSize() {
    		return maximumSize;
    	}
		
	    /**
	     * Associates the specified value with the specified key in this cache.
	     * If the map previously contained a mapping for this key, the old value is replaced.
	     * <p>
	     * This method record an access to the specified entry in order to respect this cache policy when it's size reach it's maximum size.
	     * That mean if the specified entry already exists, this method don't reset previous record accesses for the existing key.
	     *
	     * @param key key with which the specified value is to be associated.
	     * @param value value to be associated with the specified key.
	     * @return previous value associated with specified key, or <tt>null</tt> if there was no mapping for key.  
	     *         A <tt>null</tt> return can also indicate that the HashMap previously associated <tt>null</tt> with the specified key.
	     */
		public V put(K key,V value) {
			FrequencyEntry<K,V> entry = map.get(key);
			if( entry != null ) {
				V oldValue = entry.getValue();
				
				entry.setValue(value);
				recordAccess( entry );

				return oldValue;
			}
			else {
				if( getSize() == getMaximumSize() ) {
					removeEldestEntry();
				}
				entry     = new FrequencyEntry<K,V>(key,value);
				if( firstBullet == null || firstBullet.getFrequency() > 1 ) {
					// we must create a new bullet for contains our new entry
					FrequencyBullet<K,V> newBullet = new FrequencyBullet<K,V>(1);
					if( firstBullet != null ) newBullet.insertBefore(firstBullet);
					newBullet.add( entry );
					
					// Now the new bullet is the first bullet
					firstBullet = newBullet;
				}
				else {
					// we can reuse the first bullet because it is a 0-frequency bullet
					firstBullet.add( entry );
				}
				
				map.put(key,entry);
				return null;
			}
		}
		
	    /**
	     * Returns an iterator over the <tt>values<tt> contents of this cache .
	     * This iterator order begin from the eldest entry to the newest entry regarding the current policy of this cache.
	     * 
	     * @return an Iterator.
	     */		
		public Iterator<V> iterator() {
			if( firstBullet == null ) return (Iterator<V>)Collections.EMPTY_LIST.iterator();
			return new Iterator<V>() {
				
				Iterator<FrequencyEntry<K,V>> i = firstBullet.iterator();
				Iterator<K>         		  j = map.keySet().iterator();
				
			    public boolean hasNext() {
			    	return i.hasNext();
			    }

			    public V next() {
			    	if( ! hasNext() ) throw new NoSuchElementException();
			    	try {
			    		return i.next().getValue();
			    	}
			    	finally {
			    		j.next(); // just for manage ConcurrentException
			    	}
			    }

			    public void remove() {
			    	throw new UnsupportedOperationException();
			    }
			};
		}
		
	    /**
	     * Returns an iterator over the <tt>keys<tt> contents of this cache .
	     * This iterator order begin from the eldest entry to the newest entry regarding the current policy of this cache.
	     * 
	     * @return an Iterator.
	     */   
		public Iterator<K> iteratorKeys() {
			if( firstBullet == null ) return (Iterator<K>)Collections.EMPTY_LIST.iterator();
			return new Iterator<K>() {
				
				Iterator<FrequencyEntry<K,V>> i = firstBullet.iterator();
				Iterator<K>         		  j = map.keySet().iterator();
				
			    public boolean hasNext() {
			    	return i.hasNext();
			    }

			    public K next() {
			    	if( ! hasNext() ) throw new NoSuchElementException();
			    	try {
			    		return i.next().getKey();
			    	}
			    	finally {
			    		j.next(); // just for manage ConcurrentException
			    	}
			    }

			    public void remove() {
			    	throw new UnsupportedOperationException();
			    }
			};
		}
		
	    /**
	     * Returns <tt>true</tt> if this cache contains a mapping for the specified key.
	     * <p>
	     * This method <tt>don't record</tt> any access to the specified entry and do not participate to help the cache's policy
	     *
	     * @param   key   The key whose presence in this map is to be tested
	     * @return <tt>true</tt> if this map contains a mapping for the specified
	     * key.
	     */		
		public boolean containsKey(K key) {
			return map.containsKey(key);
		}
		
	    /**
	     * Returns the value to which the specified key is mapped in this cache or <tt>null</tt> if the cache contains no mapping for this key.
	     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate that the cache contains no mapping for the key; it is also possible that the cache explicitly maps the key to <tt>null</tt>.
	     * The <tt>containsKey</tt> method may be used to distinguish these two cases.
	     * <p>
	     * This method record an access to the specified entry in order to respect this cache policy when it's size reach it's maximum size.
	     * You can have a cache respecting the Least Recently Used order or Least Frequently Used order or simply Insertion order 
	     *
	     * @param   key the key whose associated value is to be returned.
	     * @return  the value to which this map maps the specified key, or
	     *          <tt>null</tt> if the map contains no mapping for this key.
	     * @see #put(Object, Object)
	     */		
		public V get(K key) {
			 FrequencyEntry<K,V> entry = map.get(key);
			 if( entry != null ) {
				 recordAccess( entry );
				 return entry.getValue();
			 }
			 return null;
		}
		
		/** dispose
		 */
		public void dispose() {
			this.map.clear();
			this.firstBullet = null;
		}
		
		/** Current Size
		 */
		public int getSize() {
			return this.map.size();
		}
		
		/** Record an access on a FrequencyEntry
		 */
		private void recordAccess(FrequencyEntry<K,V> entry) {
			FrequencyBullet<K,V> suggestedFirstBullet = entry.recordAccess();
			if( suggestedFirstBullet != null ) firstBullet = suggestedFirstBullet;
		}
		 
		/** EldestEntryRemover implementation for removing the Least Frequently Used entry
		 */
		private void removeEldestEntry() {
		
			FrequencyEntry<K,V> eldest = this.firstBullet.firstEntry;

			this.firstBullet = this.firstBullet.remove(eldest);
			map.remove( eldest.getKey() );
		}
	}
	
	/** FrequencyEntry represent a Key/Value pair like a Map.Entry do.
	 *  But in addition, a FrequencyEntry is doubly-linked with all other entries with the <tt>same</tt> frequency.
	 *  This set of FrequencyEntry is called FrequencyBullet.
	 *  <p>
	 *  The double link don't refer any special order of theses entries since all of them has the same frequency.
	 *  <p>
	 *  This class has a recordAccess() method incrementing it's access count by 1 and moving this entry to the right bullet.
	 *  
	 * @author André Sébastien
	 */
	private static class FrequencyEntry<K,V> {
		
		private K   key = null;
		private V   value = null;
		
		// linked
		FrequencyBullet<K,V> bullet  = null;
		FrequencyEntry<K,V> previous = null;
		FrequencyEntry<K,V> next     = null;
		
		/** Create a new FrequencyEntry without bound to any bullet
		 *  @param key user-key
		 *  @param value user-value
		 */
		public FrequencyEntry(K key , V value) {
			this.key   = key;
			this.value = value;
		}
		
		/** Retrieve the user-key 
		 */
		public K getKey() {
			return key;
		}

		/** Retrieve the access frequency of this key.
		 *  Note that this frequency value is deducted from it's bullet container.
		 *  @throws NullPointerException if this entry is not bound to any bullet
		 */
		public int getFrequency() {
			return bullet.getFrequency();
		}

		/** Retrieve the user-value
		 */
		public V getValue() {
			return this.value;
		}
		
		/** Set the user-value
		 */
		public void setValue(V value) {
			this.value = value;
		}
		
		/** Record an access to this entry.
		 *  This implementation move when it's required this entry to another bullet in order to reflet the new frequency count value.
		 *  
		 *  @return The first-bullet in the whole-bullet-chain <tt>if it change</tt>, <tt>null</tt> otherwise
		 */
		public FrequencyBullet<K,V> recordAccess() {
			FrequencyBullet<K,V> eldest = null;
			
			int newFrequency = bullet.getFrequency() + 1;
			if( bullet.nextBullet != null && bullet.nextBullet.getFrequency() == newFrequency ) {
				// Create a new bullet and move to it this entry
				FrequencyBullet<K,V> newBullet = bullet.nextBullet;
				
				// remove from the old bullet
				eldest = bullet.remove(this);
				if( eldest != null && eldest.previousBullet != null ) eldest = null; 
				
				// add to the new bullet
				newBullet.add(this); 
			}
			else {
				// If the bullet contains only this entry, we can increment the frequency of this bullet container directly
				if( bullet.firstEntry == this && this.next == null ) {
					bullet.frequency++;
				}
				else {
					// We create a new bullet and move this entry to it
					FrequencyBullet<K,V> newBullet = new FrequencyBullet<K,V>( newFrequency );
					
					// Insert the new bullet 
					newBullet.insertAfter( bullet );
					
					// remove this entry from the old bullet
					eldest = bullet.remove(this);
					if( eldest != null && eldest.previousBullet != null ) eldest = null;
					
					// add it to the new bullet
					newBullet.add(this);
				}
			}
			return eldest;
		}		
		
		/** Return an human readable interpretation of this entry
		 */
		public String toString() {
			return SimpleFormatter.format("Entry[@%%,key=%%,value=%%,bullet=%%]", Integer.toHexString( System.identityHashCode(this) ) , getKey() , getValue() , this.bullet ); 
		}
	}
	
	/** A FrequencyBullet is a container that holds all FrequencyEntries for the same specified frequency.
	 *  <p>
	 *  Bullets are doubly-linked in an defined order.
	 *  Each bullet refer to the previous bullet (less frequently used) and it's next bullet (more frequently used).
	 *  <p>
	 *  When a FrequencyEntry record an access, it update it's frequency count and change it's FrequencyBullet container.
	 *  This move-on can create a new bullet if this one is not already used by another entry.
	 *  <p>
	 *  Theses bullet are designed to make a recordAccess time constant over the contents of the cache.
	 *  Since each recordAccess() re-order entries from the less used to the more used.
	 *  Without bullets, If you have thousands entries with the same frequency and one of them is accessed. we should iterate over all
	 *  equally entries for found it's new place. This bullet-design allow to safe-fast throw out all theses equally entries.
	 *     
	 *  @author André Sébastien
	 */
	private static class FrequencyBullet<K,V> implements Iterable<FrequencyEntry<K,V>> {
		
		int frequency;
		
		FrequencyBullet<K,V> previousBullet = null;
		FrequencyBullet<K,V> nextBullet 	= null;
		
		FrequencyEntry<K,V>  firstEntry     = null;
		
		public FrequencyBullet(int frequency) {
			this.frequency = frequency;
		}
		
		public int getFrequency() {
			return frequency;
		}
		
		/** Insert a THIS bullet BEFORE the specified one
		 */
		public void insertBefore( FrequencyBullet<K,V> postBullet ) {
			this.previousBullet = postBullet.previousBullet;
			this.nextBullet     = postBullet;
			
			if( postBullet.previousBullet != null ) postBullet.previousBullet.nextBullet = this;
			postBullet.previousBullet = this;
		}
		
		/** Insert a THIS bullet AFTER the specified one
		 */
		public void insertAfter( FrequencyBullet<K,V> precedingBullet ) {
			this.previousBullet = precedingBullet;
			this.nextBullet = precedingBullet.nextBullet;
			
			if( precedingBullet.nextBullet != null ) precedingBullet.nextBullet.previousBullet = this;
			precedingBullet.nextBullet = this;
		}
		
		/** Remove this bullet from the bullet's chain
		 */
		public void remove() {
			if( firstEntry != null ) throw new IllegalStateException("can't remove a non-empty bullet from the bullet's chain");
			
			if( this.previousBullet != null ) this.previousBullet.nextBullet = this.nextBullet;
			if( this.nextBullet != null )     this.nextBullet.previousBullet = this.previousBullet;
			
			this.previousBullet = null;
			this.nextBullet = null;
		}
		
		/** add an entry to this bullet
		 * @param entry Entry to add to this bullet
		 */
		public void add( FrequencyEntry<K,V> entry ) {
			if( entry.bullet != null ) throw new IllegalStateException("Can't add an already linked-entry to a bullet");
			entry.bullet = this;
			
			entry.previous = null;
			entry.next     = this.firstEntry;

			if( this.firstEntry != null ) this.firstEntry.previous = entry;
			this.firstEntry = entry;
		}
		
		/** Remove an entry from this bullet
		 *  @return this bullet if not empty or the next bullet if this one is removed 
		 */
		private FrequencyBullet<K,V> remove(FrequencyEntry<K,V> entry) {
			if( entry.bullet != this ) {
				throw new IllegalStateException("Can't remove a bullet from it's non-owner bullet");
			}
			
			FrequencyBullet<K,V> result = this;
			
			FrequencyEntry<K,V> oldPreviousEntry = entry.previous;
			FrequencyEntry<K,V> oldNextEntry     = entry.next;
			
			if( this.firstEntry == entry )
				this.firstEntry = entry.next;
				
			// re-link entries from the bullet
			if( oldPreviousEntry != null ) oldPreviousEntry.next = oldNextEntry;
			if( oldNextEntry != null )     oldNextEntry.previous = oldPreviousEntry;

			if( this.firstEntry == null ) {
				// this bullet is now empty, we can remove it from the bullet chains
				result = nextBullet;
				this.remove();
			}
			
			// entry is now unlinked
			entry.previous = null;
			entry.next     = null;
			entry.bullet   = null;
			
			return result;
		}		
		
		/** Return an iterator over all entries of this bullet container and <tt>next ones</tt>
		 *  Be careful, this iterator don't stop at the and of <tt>this</tt> bullet and throw out to another bullets
		 */
		public Iterator<FrequencyEntry<K,V>> iterator() {
			return new Iterator<FrequencyEntry<K,V>>() {
			  
				FrequencyBullet<K,V> currentBullet = Cache.FrequencyBullet.this;
				FrequencyEntry<K,V>  currentEntry  = currentBullet.firstEntry;
				
				public boolean hasNext() {
					if( currentEntry != null ) return true;
			    	currentBullet = currentBullet.nextBullet;
			    	if( currentBullet == null ) return false;
			    	currentEntry = currentBullet.firstEntry;
			    	return currentEntry != null;
			    }

			    public FrequencyEntry<K,V> next() {
			    	if( ! hasNext() ) throw new NoSuchElementException();
			    	try {
			    		return currentEntry;
			    	}
			    	finally {
			    		currentEntry = currentEntry.next;
			    	}
			    }

			    public void remove() {
			    	throw new UnsupportedOperationException();
			    }
			};
		}
		
		/** Return an human readable interpretation of this entry
		 */
		public String toString() {
			return SimpleFormatter.format("Bullet[@%%,frequency=%%]", Integer.toHexString( System.identityHashCode(this) ) , getFrequency() );
		}
	}	
}