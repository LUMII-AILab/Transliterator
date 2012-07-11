package lv.ailab.lnb.fraktur;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

/**
 * This class handles dictionaries (wordlists).
 */
public class WordList
{
	/**
	 * Maps words from wordlist to the internal ID of dictionary.
	 */
	private HashMap<String, ArrayList<String>> word2id;
	
	/**
	 * Maps dictionary ID to the metadata.
	 */
	private HashMap<String, Properties> id2meta;
	
	/**
	 * To maintain easy deneration of unical IDs, last ID is stored.
	 */
	private int lastId;
	
	/**
	 * Constructor.
	 */
	public WordList ()
	{
		word2id = new HashMap<String, ArrayList<String>>();
		id2meta = new HashMap<String, Properties>();
		lastId = -1;
	}
	
	/**
	 * Load new dictionary.
	 */
	public void addDictionary(File wordlist, File attributes)
	throws IOException
	{
		String id = lastId + "-" + wordlist.getName();
		
		// Process attributes.	
		Properties attribData = new Properties();
		attribData.load(
			new InputStreamReader(new FileInputStream(attributes), "UTF8"));
		attribData.setProperty("attribFile", attributes.getName());
		attribData.setProperty("dataFile", wordlist.getName());
		id2meta.put(id, attribData);
		
		// Read wordlist.
		// Open file.
		BufferedReader in = new BufferedReader(new InputStreamReader(
			new FileInputStream(wordlist), "UTF8"));
		
		// Process each line.
		String line = in.readLine();
		while (line != null)
		{
			line = line.trim().toLowerCase();
			if (line.length() > 0)
			{
				// Add new data.
				if (word2id.containsKey(line))
				{
					word2id.get(line).add(id);
				} else
				{
					ArrayList<String> tmp = new ArrayList<String>();
					tmp.add(id);
					word2id.put(line, tmp);
				}
			}
			line = in.readLine();
		}
		
		// Close file.
		in.close();
	}
	
	/**
	 * Check if the word is in dictionary.
	 */
	public boolean containsWord(String token)
	{
		return word2id.containsKey(token.toLowerCase());
	}
	
	/**
	 * How many dictionaries contain the given word.
	 */
	public int countDict (String token)
	{
		token = token.toLowerCase();
		if (!word2id.containsKey(token)) return 0;
		return word2id.get(token).size();
	}
	
	/**
	 * How many entries are in the word list?
	 */
	public int wordCount ()
	{
		return word2id.size();
	}
	
	/**
	 * Returns attributes for dictionaries containing given word.
	 */
	public Properties[] dictionaries (String token)
	{
		token = token.toLowerCase();
		if (!word2id.containsKey(token)) return null;
		Properties[] res = new Properties[countDict(token)];
		for (int i = 0; i < countDict(token); i++)
		{
			res[i] = id2meta.get(word2id.get(token).get(i));
		}
		return res;
	}

	/**
	 * Returns attributes for all dictionaries constituting this wordlist.
	 */	
	public HashSet<Properties> allDictionaries()
	{
		return new HashSet<Properties>(id2meta.values());
	}
}