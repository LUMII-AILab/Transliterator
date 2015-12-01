package lv.ailab.lnb.fraktur.ngram;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Tool for providing comparable rating for how "probable" each transliteration
 * variant (or arbitrary text string) is.
 */
public class VariantEvaluator
{
	public int maxNGramLevel;
	
	/**
	 * Tag denoting beginning of the word.
	 */
	private static String BEGIN_TAG;
	/**
	 * Tag denoting end of the word.
	 */
	private static String END_TAG;
	/**
	 * Tag denoting out-of-vocabulary characters.
	 */
	private static String UNK_TAG;

	/**
	 * Vocabulary (special constants BEGIN_TAG, END_TAG, UNK_TAG are not
	 * included).
	 */
	private HashSet<String> vocab;
	/**
	 * N-gram probabilities.
	 */
	private HashMap<String, Double> nGramProb;
	/**
	 * N-1-gram probabilities for back-propagation.
	 */
	private HashMap<String, Double> backpropProb;
	
	/**
	 * Constructor.
	 * @param beginTag	Tag denoting beginning of the word in the N-gram file.
	 *					<code>&lt;s></code> is used, if <code>null</code> or
	 *					empty string provided.
	 * @param endTag	Tag denoting end of the word in the N-gram file.
	 *					<code>&lt;\s></code> is used, if <code>null</code> or
	 *					empty string provided.
	 * @param unknownTag	Tag denoting out-of-dictionary character in the
	 *						N-gram file. <code>&lt;unk></code> is used, if
	 *						<code>null</code> or empty string provided.
	 * @param vocabFile	Path to vocabulary file - one character per line. If
	 *					length of line is more than 1, and line is not equal to
	 *					special values passed in variables (beginTag, endTag
	 *					unknownTag), error will rise. Space characters are not
	 *					supported.
	 * @param nGramFile	Path to N-gram statistics file. Statistics must be log.
	 * @param level	length of the longest N-grams provided.
	 *
	 */
	public VariantEvaluator(String beginTag, String endTag, String unknownTag,
		String vocabFile, String nGramFile, int level)
	throws IOException
	{
		// Set special values.
		BEGIN_TAG = !(beginTag == null || "".equals(beginTag.trim())) ?
			beginTag : "<s>";
		END_TAG = !(endTag == null || "".equals(endTag.trim())) ?
			endTag : "</s>";
		UNK_TAG = !(unknownTag == null || "".equals(unknownTag.trim())) ?
			unknownTag : "<unk>";
		maxNGramLevel = level;
		
		// Initialize data structures.
		vocab = new HashSet<String>();
		nGramProb = new HashMap<String, Double>();
		backpropProb = new HashMap<String, Double>();
		
		// Read the vocabulary.
		BufferedReader inVocab = new BufferedReader(new InputStreamReader(
			new FileInputStream(vocabFile), "UTF8"));
		String line = inVocab.readLine();
		while (line != null)
		{
			line = line.trim();
			if (!line.equals("") && line.length() == 1)
				vocab.add(line);
			else if (!BEGIN_TAG.equals(line) && !END_TAG.equals(line)
				&& !UNK_TAG.equals(line))
				throw new IllegalArgumentException ("Line \""
						+ line + "\" from vocabulary file is not valid entryy.");
			line = inVocab.readLine();
		}
		inVocab.close();
		
		// Read the n-grams.
		BufferedReader inNGram = new BufferedReader(new InputStreamReader(
			new FileInputStream(nGramFile), "UTF8"));
		line = inNGram.readLine();

		while (line != null)
		{
			line = line.trim();
			// Throw away the header, if there is one.
			if (line.matches("^\\\\data\\\\\\s*$"))
			{
				while (line != null && !line.matches("^\\\\\\d+-grams:\\s*$"))
				{
					line = inNGram.readLine();
				}
				if (line == null)
					throw new IllegalArgumentException (
						"N-gram input file contains no N-grams.");
			}
			
			// Process data lines.
			if (!line.startsWith("\\") && !line.equals(""))
			{
				String[] split = line.split("\t");
				// If both probabilities are given.
				if (split.length == 3)
				{
					nGramProb.put(split[1], Double.parseDouble(split[0]));
					backpropProb.put(split[1], Double.parseDouble(split[2]));
				} else if (split.length == 2) // If only one probability is given.
				{
					nGramProb.put(split[1], Double.parseDouble(split[0]));
				} else // Something gone wrong.
					throw new IllegalArgumentException ("Line \"" + line
						+ "\" from N-gram file could not be parsed.");
			}
			line = inNGram.readLine();
		}
		inNGram.close();
	}
	
	/**
	 * Calculate evaluation for this token.
	 */
	public double evaluateToken(String token)
	{
		if (token == null || "".equals(token)) return 0;
		ArrayList<String> charList = tokenToArray(token);
		ArrayList<String[]> substrings = makeNGrams(charList);
		
		//Calculate evaluation.
		double rez = 0;		//nGramProb.get(charList.get(0));
		
		// There is no need to add probability for beginning of the word.
		for (int pos = 1; pos < charList.size(); pos++)
		{
			int lev = pos + 1 < maxNGramLevel ? pos + 1 : maxNGramLevel;
			//System.out.println("call");
			double prob = calculateProb(lev, pos, charList, substrings);
			//System.out.println("prob " + prob);
			rez = rez + prob;
		}
		return rez;
	}
	
	/**
	 * Recursive method for calculating probability of one n-gram. Calculation
	 * is done as follows:
	 * <code>
	 *  			  | given_prob("string")				if available;
	 * Pr("string") = {
	 *				  | backprop_prob("strin") + Pr("tring")	else.
	 * </code>
	 * If no <code>backprop_prob("strin")</code> is available,
	 * <code>log(1) = 0</code> is used.
	 */
	private double calculateProb(int level, int position,
		ArrayList<String> charList, ArrayList<String[]> substrings)
	{
		try
		{
			if (level == 1) return nGramProb.get(charList.get(position));
		} catch (NullPointerException e)
		{
			if (charList.get(position).equals(UNK_TAG))
				throw new IllegalArgumentException(
					"N-gram file must provide probability for \"unknown symbol\".");
			else throw e;
		}
		if (level < 1)
			throw new IllegalArgumentException(level + "-grams can't be used");
			
		String nGram = substrings.get(level - 2)[position - level + 1];
		//System.out.println("query: " + nGram);
		if (nGramProb.containsKey(nGram)) return nGramProb.get(nGram);
		else
		{
			String prefix = level > 2 ?
				substrings.get(level - 3)[position - level + 1]
				: charList.get(position - 1);
			double res =
				backpropProb.containsKey(prefix) ? backpropProb.get(prefix) : 0;
			return res + calculateProb(level - 1, position, charList, substrings);
		}
	}
	
	/**
	 * Create all substrings in length from 2 to maxNGramLevel.
	 */
	private ArrayList<String[]> makeNGrams(ArrayList<String> charList)
	{
		ArrayList<String[]> substrings = new ArrayList<String[]>();
		
		// Make 2-grams.
		String[] diGrams = new String[charList.size() - 1]; // -2 + 1 = -1
		for (int pos = 0; pos < diGrams.length; pos++)
		{
			diGrams[pos] = charList.get(pos) + " " + charList.get(pos + 1);
		}
		substrings.add(diGrams);
		
		// Make N-grams for N > 2.
		for (int len = 3; len <= Math.min(maxNGramLevel, charList.size()); len++)
		{
			String[] nGrams = new String[charList.size() - len + 1];
			for (int pos = 0; pos < nGrams.length; pos++)
			{
				nGrams[pos] = substrings.get(len - 3)[pos] + " "
					+ charList.get(pos + len - 1);
			}
			substrings.add(nGrams);
		}
		
		return substrings;
	}
	
	/**
	 * Transform token to the array of vocabulary items.
	 */
	private ArrayList<String> tokenToArray(String token)
	{
		token = token.toLowerCase();
		ArrayList<String> charList = new ArrayList<String>();
		charList.add(BEGIN_TAG);
		for (int i = 0; i < token.length(); i++)
		{
			String cChar = token.substring(i, i + 1);
			if (vocab.contains(cChar)) charList.add(cChar);
			else charList.add(UNK_TAG);
		}
		charList.add(END_TAG);
		return charList;
	}
}
