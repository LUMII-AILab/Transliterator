/**
 * This is generic transliteration library.
 * Lauma Pretkalnina, AILab, LUIMCS.
 */
package lv.ailab.lnb.fraktur;

import lv.ailab.lnb.fraktur.translit.Engine;
import lv.ailab.lnb.fraktur.translit.Rules;
import lv.ailab.lnb.fraktur.translit.ResultData;
import lv.ailab.lnb.fraktur.translit.Variant;
import lv.ailab.lnb.fraktur.ngram.VariantComparator;
import lv.ailab.lnb.fraktur.ngram.VariantEvaluator;
import lv.ailab.lnb.fraktur.util.HashMultiMap;
import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;


/**
 * Start using transliteration library here!
 */
public class Transliterator
{
	/**
	 * This is singleton class.
	 */
	private static Transliterator itIsMe = null;
	
	/**
	 * Path to config file. 
	 */
	public static String PATH_FILE = "path.conf";
	
	/**
	 * Paths to external recources. Hashkeys are hardcoded.
	 */
	private Properties paths;
	
	/**
	 * Transliteration rules for each transliteration group.
	 */
	private HashMap<String, Rules> rules;
	
	/**
	 * Dictionary data structure.
	 */
	public WordList dict;
	
	/**
	 * Morphological analyzer.
	 */
	public Analyzer morph;
	
	/**
	 * N-gram statistics calculator.
	 */
	public VariantEvaluator nGrams;
	
	/**
	 * Transliteration variant comparator, based on n-grams.
	 */
	public VariantComparator comparator;
	
	/**
	 * Get singleton instance of this class.
	 */
	public static Transliterator getTransliterator()
	throws Exception
	{
		if (itIsMe == null)
			itIsMe = new Transliterator(PATH_FILE);
		return itIsMe;
	}
	
	/**
	 * Reload Transliterator. You should call this if PATH_FILE have changed.
	 */
	public static void reloadTransliterator()
	throws Exception
	{
		itIsMe = new Transliterator(PATH_FILE);
	}
	
	/**
	 * Return names of available rule sets.
	 */
	public Set<String> ruleSets()
	{
		return rules.keySet();
	}
	
	/**
	 * Check if given string is valid rule set ID.
	 */
	public boolean isValidRuleSet(String setName)
	{
		return rules.containsKey(setName);
	}
	
	/**
	 * Returns string used as key to dictionary (wordlist) ID.
	 */
	public String getDictIdKey()
	{
		return paths.get("dictID").toString();
	}
	
	/**
	 * Returns string used as key to dictionary webservice.
	 */
	public String getEntryUrlKey()
	{
		return paths.get("entryURL").toString();
	}
	
	/**
	 * Complete processing of one word: transliteration, and, if asked, fuzzy
	 * transliteration, and checking against dictionary.
	 */
	public ResultData processWord(String token, String group, boolean useFuzzy)
	{
		if (token == null) return null; // Nothing to do.
		token = token.trim();
		if (token.length() < 1) return null; // Nothing to do.
		Rules r = rules.get(group);
		if (r == null) return null; // No such group;
		
		HashMap<String, Boolean> transRez = Engine.transform(token, r, useFuzzy);
		ResultData res = new ResultData();
		
		for (Map.Entry<String, Boolean> e : transRez.entrySet())
		{
			// Transliteration variant;
			String var = e.getKey();
			
			// Do morphoanalysis.
			ArrayList<Wordform> analRes = morph.analyze(var).wordforms;
			
			// Lemmas with no guessing used.
			HashSet<String> trusted = new HashSet<String>();
			// Lemmas, SourceLemmas with guessing.
			HashSet<String> guessed = new HashSet<String>();
			
			// Sort out unique lemmas.
			// Without guessing.
			for (Wordform wf : analRes)
			{
				if(wf.getValue(AttributeNames.i_Lemma) == null)
					throw new NullPointerException(
						"Morphoanalyzer didn't return lemma for \"" + var + "\"!");
				if (wf.isMatchingStrong(
					AttributeNames.i_Guess, AttributeNames.v_NoGuess))
				{
					trusted.add(
						wf.getValue(AttributeNames.i_Lemma).toLowerCase().trim());
					String sl = wf.getValue(AttributeNames.i_SourceLemma);
					if (sl != null) trusted.add(sl.toLowerCase().trim());
				}
			}
			// Guessed lemmas.
			for (Wordform wf : analRes)
			{
				if(wf.getValue(AttributeNames.i_Lemma) == null)
					throw new NullPointerException(
						"Morphoanalyzer didn't return lemma for \"" + var + "\"!");
						
				String l = wf.getValue(AttributeNames.i_Lemma).toLowerCase().trim();
				
				if (!wf.isMatchingStrong(
					AttributeNames.i_Guess, AttributeNames.v_NoGuess))
				{
					if (!trusted.contains(l)) guessed.add(l);
					
					String sl = wf.getValue(AttributeNames.i_SourceLemma);
					if (sl != null && !trusted.contains(sl))
						guessed.add(sl.toLowerCase().trim());
				}
			}
			
			// Search lemmas in dictionaries.
			boolean found = false;
			for (String lemma: trusted)
			{
				Properties[] foundDict = dict.dictionaries(lemma);
				if (e.getValue())	// Fuzzy.
				{
					if (foundDict != null && foundDict.length > 0)
					{
						res.DICT_FUZZY.add(
							new Variant(var, nGrams), lemma, foundDict);
						found = true;
					}
				}
				else				// Exact.
				{
					if (foundDict != null && foundDict.length > 0)
					{
						res.DICT_EXACT.add(
							new Variant(var, nGrams), lemma, foundDict);
						found = true;
					}
				}
			}
			for (String lemma: guessed)
			{
				Properties[] foundDict = dict.dictionaries(lemma);
				if (e.getValue())	// Fuzzy.
				{
					if (foundDict != null && foundDict.length > 0)
					{
						res.DICT_FUZZY_GUESS.add(
							new Variant(var, nGrams), lemma, foundDict);
						found = true;
					}
				}
				else				// Exact.
				{
					if (foundDict != null && foundDict.length > 0)
					{
						res.DICT_EXACT_GUESS.add(
							new Variant(var, nGrams), lemma, foundDict);
						found = true;
					}
				}
			}
			
			// If none of lemmas was found in dictionaries.
			if (!found)
			{
				// Fuzzy.
				if (e.getValue()) res.NO_DICT_FUZZY.add(new Variant(var, nGrams));
				// Exact.
				else res.NO_DICT_EXACT.add(new Variant(var, nGrams));
			}
		}
		
		return res;
	}	
	
	//=== Supporting functions. ================================================
	
	/**
	 * Constructor.
	 */
	private Transliterator(String pathFile)
	throws Exception
	{
		paths = new Properties();
		paths.load(new InputStreamReader(new FileInputStream(pathFile),	"UTF8"));
		
		// Transliteration groups.
		rules = new HashMap <String, Rules>();
		initTranslit();
		
		// Dictionaries.
		dict = new WordList();
		initDict(new File(paths.getProperty("dictDir")));
		
		// Set up morphoanalyzer.
		morph = new Analyzer(paths.getProperty("morphLex"));
		morph.enableDiminutive = true;
		morph.enablePrefixes = true;
		morph.enableVocative = true;
		morph.enableGuessing = true;
		morph.enableAllGuesses = true;
		
		// N-grams.
		initNGramEval();
		if (nGrams != null) comparator = new VariantComparator(nGrams);
		else comparator = null;
	}
	
	/**
	 * Reads in all transliterarion rules.
	 */
	private void initTranslit()
	throws IOException, SAXException, ParserConfigurationException
	{
		String[] groups = paths.getProperty("groups", "").split(",");
		if (groups.length < 1)
			throw new IllegalArgumentException(
				"Path file \"" + PATH_FILE +
				"\" contains no translitaration groups.");
		for (String gr : groups)
		{
			rules.put(gr, new Rules(new File(paths.getProperty(gr))));
		}
	}
	
	/**
	 * Reads in all dictionaries.
	 */
	private void initDict(File dictDir)
	throws IOException
	{
		if (!dictDir.isDirectory())
			throw new IllegalArgumentException(
				"Path file \"" + PATH_FILE +
				"\" contains invalid \"dictDir\".");
		for (File f : dictDir.listFiles())
		{
			if (f.getName().endsWith(".txt"))
			{
				String absName = f.getPath().substring(
						0, f.getPath().length() - 4);
				
				dict.addDictionary(f, new File(absName + ".conf"));
			}
		}
	}
	
	/**
	 * Read in data for N-gram statistics.
	 */
	private void initNGramEval()
	throws IOException
	{
		int nGramLev = 0;
		try
		{
			nGramLev = Integer.parseInt(paths.getProperty("ngramLevel", "0"));
		} catch (NumberFormatException nfe)
		{
			throw new IllegalArgumentException("Path file \"" + PATH_FILE +
				"\" contains illegal value for parameter \"ngramLevel\".");
		}
		
		if (nGramLev < 1)
			nGrams = null;	// No N-grams given.
		else
		{
			nGrams = new VariantEvaluator(
				paths.getProperty("ngramBegin", "<s>"),
				paths.getProperty("ngramEnd", "</s>"),
				paths.getProperty("ngramUnknown", "<unk>"),
				paths.getProperty("ngramVocab"),
				paths.getProperty("ngramStats"),
				nGramLev);
		}		
	}
	


}