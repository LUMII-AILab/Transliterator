package lv.ailab.lnb.fraktur.translit;

import java.util.Collection;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Map;

/**
 * Data structure used by transliterator. Contains information about the token
 * currently being processed.
 */
class ProcessingData
{
	/**
	 * Look-up table for dynamic program. lookUpTable[i] contains
	 * transliteration variants when word is transliterated from beginning to
	 * i-th character.
	 * Boolean variables denote, whether fuzzy rules are needed to accomplish
	 * such transformation. 
	 */
	private HashMap<String, Boolean>[] lookUpTable;
	
	/**
	 * Constructor.
	 *
	 * @param length	length of the token.
	 */
	protected ProcessingData (int length)
	{
		lookUpTable = new HashMap[length];
		for (int i = 0; i < length; i++)
		{
			lookUpTable[i] = new HashMap<String, Boolean>();
		}
	}
	
	/**
	 * Copies all strings from position "from" to position "where". Each "new"
	 * string is concatenated with the given postfix.
	 */
	protected void add (int where, int from, String postfix, boolean fuzzy)
	{
		if (from < 0)
		{
			// If we can transliterate without fuzzy rules, that is better.
			if (lookUpTable[where].containsKey(postfix))
				lookUpTable[where].put(
					postfix, lookUpTable[where].get(postfix) && fuzzy);
			else lookUpTable[where].put(postfix, fuzzy);
		} else
		{
			for (Map.Entry<String, Boolean> var : lookUpTable[from].entrySet())
			{
				String k = var.getKey() + postfix;
					
				// If we can transliterate without fuzzy rules, that is better.
				if (lookUpTable[where].containsKey(k))
					lookUpTable[where].put(k, lookUpTable[where].get(k) && fuzzy);
				// If previous transliteration was "unfuzzy", but last step is
				// fuzzy, altogether we get an fuzzy transliteration.
				else lookUpTable[where].put(k, var.getValue() || fuzzy);
			}
		}
	}
	
	/**
	 * Does <code>add (int where, int from, String postfix)</code> for each
	 * postfix in the given collection.
	 */
	public void add (
		int where, int from, Collection<String> postfixes, boolean fuzzy)
	{
		for (String s : postfixes)
		{
			add(where, from, s, fuzzy);
		}
	}
	
	/**
	 * Returns all elements corresponding to the i-th position.
	 */
	protected HashMap<String, Boolean> elementsByPos(int position)
	{
		return lookUpTable[position];
	}
	
	/**
	 * Returns count of the elements corresponding to the i-th position.
	 */
	protected int countByPos (int position)
	{
		return lookUpTable[position].size();
	}
	
	/**
	 * Returns all transliteration variants.
	 */
	protected HashMap<String, Boolean> transliterations()
	{
		return lookUpTable[lookUpTable.length - 1];
	}
	
	/**
	 * Returns count of transliteration variants.
	 */
	protected int translitCount()
	{
		return lookUpTable[lookUpTable.length - 1].size();
	}
}
