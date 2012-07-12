package lv.ailab.lnb.fraktur.translit;

import lv.ailab.lnb.fraktur.ngram.VariantComparator;
import lv.ailab.lnb.fraktur.util.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

/**
 * Transliteration results, sorted by trust.
 */
public class ResultData
{
	/**
	 * Trusted results - transliterated words found in dictionary. No guessing
	 * used for lemma obtaining.
	 */
	public ResultDataWithDic DICT_EXACT;
	/**
	 * Less trusted results - transliterated words found in dictionary. Lemma
	 * obtained with the help of guessing.
	 */
	public ResultDataWithDic DICT_EXACT_GUESS;
	
	/**
	 * Semi-trusted results - fuzzy-transliterated words found in dictionary. No
	 * guessing used for lemma obtaining.
	 */
	public ResultDataWithDic DICT_FUZZY;
	/**
	 * Semi-trusted results - fuzzy-transliterated words found in dictionary.
	 * Lemma obtained with the help of guessing.
	 */
	public ResultDataWithDic DICT_FUZZY_GUESS;
	
	/**
	 * Untrasted results - transliterated forms that could not be found in
	 * dictionary.
	 */
	public HashSet<Variant> NO_DICT_EXACT;
	/**
	 * Extremely untrasted results - fuzzy-transliterated forms that could not
	 * be found in dictionary.
	 */
	public HashSet<Variant> NO_DICT_FUZZY;
	
	/**
	 * Constructor for empty data structure.
	 */
	public ResultData()
	{
		DICT_EXACT = new ResultDataWithDic();
		DICT_EXACT_GUESS = new ResultDataWithDic();
		DICT_FUZZY = new ResultDataWithDic();
		DICT_FUZZY_GUESS = new ResultDataWithDic();
		NO_DICT_EXACT = new HashSet<Variant>();
		NO_DICT_FUZZY = new HashSet<Variant>();
	}

	/**
	 * Compose new <code>Collection</code> containing all variants contained
	 * in provided data set.
	 */
	public ArrayList<Variant> getAllVariants()
	{
		ArrayList<Variant> all = new ArrayList<Variant>();
		all.addAll(DICT_EXACT.data.keySet());
		all.addAll(DICT_EXACT_GUESS.data.keySet());
		all.addAll(DICT_FUZZY.data.keySet());
		all.addAll(DICT_FUZZY_GUESS.data.keySet());
		all.addAll(NO_DICT_EXACT);
		all.addAll(NO_DICT_FUZZY);
		return all;
	}
	
	/**
	 * Convert to XML.
	 *
	 * @param dictIdKey		key linking to dictionary ID.
	 * @param entryUrlKey	key linking to URL of entry. Placeholder 
	 *						<code>{word}</code> will be replaced with lemma.
	 * 
	 */
	public String toXML(String dictIdKey, String entryUrlKey)
	{
		return toXML(dictIdKey, entryUrlKey, null);
	}
	
	/**
	 * Convert to XML.
	 *
	 * @param dictIdKey		key linking to dictionary ID.
	 * @param entryUrlKey	key linking to URL of entry. Placeholder 
	 *						<code>{word}</code> will be replaced with lemma.
	 * @param comp			comparator for ordering tokens.
	 */
	public String toXML(
		String dictIdKey, String entryUrlKey, VariantComparator comp)
	{
		StringBuilder res = new StringBuilder();
		res.append("<transliterations>\n");
		
		// Process variants with dictionaries.
		
		res.append("\t<group opt_rules=\"no\" dict=\"yes\" guess=\"no\">\n");
		res.append(DICT_EXACT.toXML(dictIdKey, entryUrlKey, "\t\t", comp));
		res.append("\t</group>\n");		

		res.append("\t<group opt_rules=\"no\" dict=\"yes\" guess=\"yes\">\n");
		res.append(DICT_EXACT_GUESS.toXML(dictIdKey, entryUrlKey, "\t\t", comp));
		res.append("\t</group>\n");		

		res.append("\t<group opt_rules=\"yes\" dict=\"yes\" guess=\"no\">\n");
		res.append(DICT_FUZZY.toXML(dictIdKey, entryUrlKey, "\t\t", comp));
		res.append("\t</group>\n");		

		res.append("\t<group opt_rules=\"yes\" dict=\"yes\" guess=\"yes\">\n");
		res.append(DICT_FUZZY_GUESS.toXML(dictIdKey, entryUrlKey, "\t\t", comp));
		res.append("\t</group>\n");		

		// Process variants with no dictionaries.
		
		res.append("\t<group opt_rules=\"no\" dict=\"no\" guess=\"yes\">\n");
		ArrayList<Variant> sorted = new ArrayList<Variant>(NO_DICT_EXACT);
		if (comp != null) Collections.sort(sorted, comp);
		for (Variant v : sorted)
		{
			Double eval = null;
			if (comp != null) eval = v.estimate(comp.evaluator);
			if (eval != null)
			{
				res.append("\t\t<variant wordform=\"").append(v.token)
					.append("\">\n");
				res.append("\t\t\t<estimate>").append(eval)
					.append("</estimate>\n");
				res.append("\t\t</variant>\n");
			} else
				res.append("\t\t<variant wordform=\"").append(v.token)
					.append("\">\n");
		}
		res.append("\t</group>\n");		

		sorted = new ArrayList<Variant>(NO_DICT_FUZZY);
		if (comp != null) Collections.sort(sorted, comp);
		res.append("\t<group opt_rules=\"yes\" dict=\"no\" guess=\"yes\">\n");
		for (Variant v : sorted)
		{
			Double eval = null;
			if (comp != null) eval = v.estimate(comp.evaluator);
			if (eval != null)
			{
				res.append("\t\t<variant wordform=\"").append(v.token)
					.append("\">\n");
				res.append("\t\t\t<estimate>").append(eval)
					.append("</estimate>\n");
				res.append("\t\t</variant>\n");
			} else
				res.append("\t\t<variant wordform=\"").append(v.token)
					.append("\">\n");
		}
		res.append("\t</group>\n");		
		
		res.append("</transliterations>\n");
		return res.toString();	
	}

	/**
	 * Convert to string. For debugging purposes.
	 */
	@Override
	public String toString()
	{
		return toString(null);
	}
	
	/**
	 * Convert to string. For debugging purposes. Token order is determined by
	 * provided comparator.
	 */
	public String toString (Comparator<Variant> comp)
	{
		StringBuilder res = new StringBuilder();
		res.append("DICT_EXACT => ").append(DICT_EXACT.toString(comp));
		res.append("\r\nDICT_EXACT_GUESS => ");
		res.append(DICT_EXACT_GUESS.toString(comp));
		res.append("\r\nDICT_FUZZY => ").append(DICT_FUZZY.toString(comp));
		res.append("\r\nDICT_FUZZY_GUESS => ");
		res.append(DICT_FUZZY_GUESS.toString(comp));
		
		res.append("\r\nNO_DICT_EXACT => {\r\n");	
		ArrayList<Variant> sorted = new ArrayList<Variant>(NO_DICT_EXACT);
		if (comp != null) Collections.sort(sorted, comp);
		for (Variant trans : sorted)
		{
			res.append("\t").append(trans).append(",\r\n");
		}
		
		res.append("\r\n}\r\nNO_DICT_FUZZY => {\r\n");
		sorted = new ArrayList<Variant>(NO_DICT_FUZZY);
		if (comp != null) Collections.sort(sorted, comp);
		for (Variant trans : sorted)
		{
			res.append("\t").append(trans).append(",\r\n");
		}
		
		res.append("\r\n}");
		return res.toString();		
	}
	
	/**
	 * Data structure linking transliterations to list of lemmas together with
	 * source dictionary information.
	 */
	public static class ResultDataWithDic
	{
		/**
		 * Data structure.
		 * Hash keys are transliterations. Hash elements are lists of lemmas.
		 */
		public HashMap<Variant, ArrayList<Tuple<String, Properties[]>>> data;
		
		/**
		 * Constructor.
		 */
		public ResultDataWithDic()
		{
			data = new HashMap<Variant, ArrayList<Tuple<String, Properties[]>>>();
		}
		
		/**
		 * Add new result.
		 */
		public void add(
			Variant token, String lemma,  Properties[] dictionaries)
		{
			ArrayList<Tuple<String, Properties[]>> previous = data.get(token);
								
			if (previous == null)
				previous = new ArrayList<Tuple<String, Properties[]>>();
							
			previous.add(new Tuple<String, Properties[]>(lemma, dictionaries));
			data.put(token, previous);
		}
		
		/**
		 * Convert to list of XML elements.
		 *
		 * @param dictIdKey		key linking to dictionary ID.
		 * @param entryUrlKey	key linking to URL of entry. Placeholder 
		 *						<code>{word}</code> will be replaced with lemma.
		 */
		public String toXML(String dictIdKey, String entryUrlKey)
		{
			return toXML(dictIdKey, entryUrlKey, "", null);
		}
		
		/**
		 * Convert to list of XML elements.
		 *
		 * @param dictIdKey		key linking to dictionary ID.
		 * @param entryUrlKey	key linking to URL of entry. Placeholder 
		 *						<code>{word}</code> will be replaced with lemma.
		 * @param comp			comparator for ordering tokens.
		 */
		public String toXML(
			String dictIdKey, String entryUrlKey, VariantComparator comp)
		{
			return toXML(dictIdKey, entryUrlKey, "", comp);
		}
		
		/**
		 * Convert to list of XML elements.
		 *
		 * @param dictIdKey		key linking to dictionary ID.
		 * @param entryUrlKey	key linking to URL of entry. Placeholder 
		 *						<code>{word}</code> will be replaced with lemma.
		 * @param indent		indentation string to be appended in front of
		 *						each line, e.g. "\t" or "\t\t\t".
		 * @param comp			comparator for ordering tokens.
		 */
		public String toXML(
			String dictIdKey, String entryUrlKey, String indent,
			VariantComparator comp)
		{
			StringBuilder res = new StringBuilder();
			
			String newInd1 = indent + "\t";
			String newInd2 = indent + "\t\t";
			String newInd3 = indent + "\t\t\t";
			
			// Process each transliteration variant.
			ArrayList<Variant> sorted = new ArrayList<Variant>(data.keySet());
			if (comp != null) Collections.sort(sorted, comp);
			for (Variant trans : sorted)
			{
				res.append(indent).append("<variant wordform=\"")
					.append(trans.token).append("\">\n");
				
				// Print N-gram estimate, if available.
				Double eval = null;
				if (comp != null) eval = trans.estimate(comp.evaluator);
				if (eval != null)
					res.append(newInd1).append("<estimate>").append(eval)
						.append("</estimate>\n");
				
				// Process each lemma.
				for (Tuple <String, Properties[]> t : data.get(trans))
				{
					res.append(newInd1).append("<lemma word=\"")
						.append(t.first).append("\">\n");
					
					// Process each dictionary.
					for (Properties p : t.second)
					{
						res.append(newInd2).append("<dict>\n");
						
						res.append(newInd3).append("<desc attr=\"dictID\">")
							.append(p.get(dictIdKey)).append("</desc>\n");
						if (p.containsKey(entryUrlKey))
							res.append(newInd3)
								.append("<desc attr=\"entryURL\">")
								.append(p.get(entryUrlKey).toString()
									.replace("{word}", t.first))
								.append("</desc>\n");
						
						/*for (Object k : p.keySet())
						{
							res.append(newInd3).append("<desc item=\"")
								.append(k).append("\">").append(p.get(k));
							
							if ("entryURL".equalsIgnoreCase(k.toString()))
								res.append(t.first);
								
							res.append("</desc>\n");
								
						}//*/
						res.append(newInd2).append("</dict>\n");
					}
					
					res.append(newInd1).append("</lemma>\n");
				}
				res.append(indent).append("</variant>\n");				
			}
			
			return res.toString();
		}
		
		/**
		 * Converts to string. For debugging purposes.
		 */
		@Override
		public String toString()
		{
			return toString("", null);
		}
		
		/**
		 * Converts to string. For debugging purposes.
		 *
		 * @param comp		comparator for ordering tokens.
		 */
		public String toString(Comparator<Variant> comp)
		{
			return toString("", comp);
		}
		/**
		 * Converts to string. For debugging purposes.
		 *
		 * @param indent	indentation string to be appended in front of each
		 *					line, e.g. "\t" or "\t\t\t".
		 * @param comp		comparator for ordering tokens.
		 */
		public String toString(String indent, Comparator<Variant> comp)
		{
			StringBuilder res = new StringBuilder(indent);
			res.append("{\r\n");
			
			String newInd1 = indent + "\t";
			String newInd2 = indent + "\t\t";
			String newInd3 = indent + "\t\t\t";
			
			// Process each transliteration variant.
			ArrayList<Variant> sorted = new ArrayList<Variant>(data.keySet());
			if (comp != null) Collections.sort(sorted, comp);
			for (Variant trans : sorted)
			{
				res.append(newInd1).append(trans).append(" => [\r\n");
				
				// Process each lemma.
				for (Tuple <String, Properties[]> t : data.get(trans))
				{
					res = res.append(newInd2).append(t.first).append(" => [\r\n");
					
					// Process each dictionary.
					for (Properties p : t.second)
					{
						res.append(dictToString(p, newInd3, t.first))
							.append(",\r\n");
					}
					res.append(newInd2).append("],\r\n");
				}
				res.append(newInd1).append("],\r\n");
				
			}
			res.append(indent).append("}");
			return res.toString();
		}
		
		/**
		 * Converts arbitrary hash map representing dictionary information from
		 * <code>*.conf</code> file to string. For debugging purposes.
		 * Key value <code>entryURL</code> is treated as URL template - lemma
		 * provided as parameter to this function is concatinated to the value.
		 */
		private static <K> CharSequence dictToString(
			Map<K, ?> m, String indent, String lemma)
		{
			if (m == null) return indent + "null";
			
			StringBuilder res = new StringBuilder(indent);
			res.append("{\r\n");
			String newInd = indent + "\t";
			for (K k : m.keySet())
			{
				res.append(newInd).append(k).append(" => ").append(m.get(k));
				
				if ("entryURL".equalsIgnoreCase(k.toString()))
					res.append(lemma);
				
				res.append(",\r\n");
			}
			res.append(indent).append("}");
			return res;
		}
	}
}