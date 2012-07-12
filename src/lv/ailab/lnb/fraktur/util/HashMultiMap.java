/**
 * This package contains generic data structures.
 */
package lv.ailab.lnb.fraktur.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Hashmap based multimap.
 */
public class HashMultiMap <K, V> extends HashMap <K, LinkedHashSet<V>>
{
	/**
	 * Add element to multimap.
	 */
	public void put (K key, V value)
	{
		LinkedHashSet<V> mapping = this.get(key);
		if (mapping != null)
		{
			mapping.add(value);
		}
		else 
		{
			mapping = new LinkedHashSet<V>();
			mapping.add(value);
			super.put(key, mapping);
		}
	}
	
	/**
	 * Add collection of elements under single key.
	 */
	public void putAll (K key, Collection<V> values)
	{
		if (values.isEmpty()) return;
		LinkedHashSet<V> mapping = this.get(key);
		if (mapping != null)
		{
			mapping.addAll(values);
		}
		else 
		{
			mapping = new LinkedHashSet<V>();
			mapping.addAll(values);
			super.put(key, mapping);
		}
	}
	
	/**
	 * Remove key - element set mapping from multimap.
	 */
	@Override
	public LinkedHashSet<V> remove (Object key)
	{
		return super.remove(key);	
	}
	
	/**
	 * Search for a value in multimap.
	 */
	@Override
	public boolean containsValue(Object value)
	{
		for (LinkedHashSet<V> valSet : super.values())
		{
			if (valSet.contains(value)) return true;
		}
		return false;
	}
	
	/**
	 * Remove single value from multimap. If removed value is the only one for
	 * some key then such a key is removed as well.
	 */
	public void removeValue(Object value)
	{
		for (K key : this.keySet())
		{
			LinkedHashSet<V> valSet = this.get(key);
			if (valSet.contains(value))
			{
				valSet.remove(value);
				if (valSet.isEmpty()) super.remove(key);
			}
		}
		
	}
}