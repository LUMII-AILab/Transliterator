
import lv.ailab.lnb.fraktur.Transliterator;
import lv.ailab.lnb.fraktur.translit.ResultData;
import lv.ailab.lnb.fraktur.translit.Variant;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * This class provides CLI for Transliterator for demonstration purposes.
 *
 * Usage: either provaiding arguments to <code>main()</code> method, or typing
 * arguments when interface prompts for it.
 *
 * Arguments must be in form: token -flag1 -flag2 -flag3 -flag4
 *
 * Avialable flags:
 * -F           use fuzzy and exact transliteration rules;
 * -E           use exact transliteration rules;
 * -group-name  name of the rule group;
 * -file        process first parameter as filename;
 * -order       order variants accordingly to n-gram statistics, if statistics
 *              are available (for single column files only).
 * All flags are optional.
 *
 * Technical note: if you want to take a look at code performing
 * <code>Transliterator</code> invocations, see method 
 * <code>doGreatStuff()</code> and file processing methods.
 */
public class TransliteratorCLI
{
	/**
	 * This is the transliterator this interface uses.
	 */
	private Transliterator t;
	
	/**
	 */
	private boolean fuzzy;
	private String group;
	private boolean isFile;
	private boolean doOrder;
	
	private TransliteratorCLI()
	throws Exception
	{
		t = Transliterator.getTransliterator();
		setDefaultSettings();
	}
	
	
	/**
	 * Entry point and test place. Use this, if you want to test transliterator
	 * libray without providing user interface.
	 *
	 * If started with no arguments, run CLI repeatedly prompting for arguments,
	 * else process arguments the same way CLI would do it and halt.
	 */
	public static void main(String[] args)
	throws Exception
	{
		System.out.println("Test interface for \"Periodika II\" transliterator.");
		System.out.println("AILab, IMCS, UL, 2011-2012.\r\n");

		// Init the transliterator
		TransliteratorCLI cli = new TransliteratorCLI();

		System.out.println(
			"Wordlists contain " + cli.t.dict.wordCount() + " entries.\r\n");

		if (args.length < 1)
			cli.interactiveInterface();
		else cli.oneLineInterface(args);
	}
	
	/**
	 * One-liner style interface: called if main() have recieved arguments.
	 */
	private void oneLineInterface(String[] args)
	{
		// Parse flags.
		HashSet<String> flags = new HashSet<String>();
		for (int i = 1; i < args.length; i++)
		{
			if (args[i].charAt(0) == '-') flags.add(args[i].substring(1));
			else flags.add(args[i]);
		}
		if (!parseFlags(flags)) return;
		
		// Transliterate.
		doGreatStuff(args[0]);
	}
	
	/**
	 * Interactive interface: prints help and recieves input from user.
	 */
	private void interactiveInterface()
	throws IOException
	{
		System.out.println("Usage: token -flag1 -flag2 -flag3 -flag4");
		System.out.println("Empty line: quit.");
		System.out.println(
			"Avialable flags:  -F           use fuzzy and exact transliteration rules;");
		System.out.println(
			"                  -E           use exact transliteration rules;");
		System.out.println(
			"                  -group-name  name of the rule group;");
		System.out.println(
			"                  -file        process first parameter as filename;");
		System.out.println(
			"                  -order       order variants accordingly to n-gram statistics,\r\n" +
			"                               if statistics are available (for single column\r\n" +
			"                               files only).");
		System.out.println("All flags are optional.");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true)
		{
			System.out.println("\r\nPlease provide token to be analyzed:");
			String token = in.readLine();
			if (token == null || token.trim().equals(""))
			{
				System.out.println("Good bye!");
				return;
			}
			
			String[] splitToken = token.split(" -");
			
			// Set default values to cancel anything prevously set.
			setDefaultSettings();
			if (splitToken.length > 1)
			{
				HashSet<String> flags = new HashSet<String>();
				flags.addAll(Arrays.asList(splitToken));
				flags.remove(splitToken[0]);
				
				// Parse flags.
				if (!parseFlags(flags)) continue;
				
				// Transliterate.
				doGreatStuff(splitToken[0]);
			}
		}
	}
	
	/**
	 * Process input as file or token, depending on current senttings.
	 */
	private void doGreatStuff(String input)
	{
		// Process file.
		if (isFile)
		{
		File io = new File(input);
		if (io.exists())
		{
			try
			{
				String prefix = "res-";
				if (doOrder) prefix = "res-ord-";
				processFile(input, prefix + input, group);
					System.out.println("Processing file finished.");
				} catch (IOException e)
				{
					System.out.println(
						"I/O error occoured while processing given file.");
					System.out.println("Details given below.");
					e.printStackTrace();
				}
				
			} else
			{
				System.out.println("File does not exist.");
			}
		}
		// Process single token.
		else
		{
			ResultData r = t.processWord(input, group, fuzzy);
			System.out.println(r.toString(t.comparator));
		//	System.out.println(
		//		r.toXML(t.getDictIdKey(), t.getEntryUrlKey(),t.comparator));
		}
	}
	
	/**
	 * Quick and dirty evaluation method for processing a single file.
	 * Information about file input/output formats see at
	 * <code>processFileBasic()</code>,
	 * <code>processFileOrder()</code>,
	 * <code>processFileWithAns()</code>.
	 *
	 * Input file format is deternined by checking if first line contains tab.
	 */
	private void processFile(
		String inpath, String outpath, String group)
	throws IOException
	{
		// Init I/O flows.
		BufferedReader in = new BufferedReader(new InputStreamReader(
			new FileInputStream(inpath), "UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(outpath), "UTF8"));
		
		// Check if, file contains answers.
		String firstLine = in.readLine();
		boolean hasAnswers = false;
		if (firstLine.contains("\t")) hasAnswers = true;

		// Call appropriate file processing method.
		if (hasAnswers)
			processFileWithAns(in, out, firstLine, group);
		else if (doOrder && t.comparator != null)
			processFileOrder(in, out, firstLine, group);
		else
			processFileBasic(in, out, firstLine, group);
		
		// Close I/O flows.			
		in.close();
		out.flush();
		out.close();
	}

	/**
	 * Process data file: transliterate all tokens given in the input stream,
	 * output all results.
	 * Input file must be:
	 *	*) one token per line.
	 *
	 * Output file has following tab-seperated columns:
	 *	1)  token itself;
	 *	2)  space seperated dictionary confirmed transliterations formed without
	 *		morhological guessing with exact rules only;
	 *	3)  number of variants in column #2;
	 *	4)  space seperated dictionary confirmed transliterations formed with
	 *		exact rules only and with morhological guessing;
	 *	5)  number of variants in column #4;
	 *	6)  space seperated dictionary confirmed transliterations formed with
	 *		all rules without morhological guessing (transliterations from #2,
	 *		#4 are not included 2nd time);
	 *	7)  number of variants in column #6;
	 *	8)	space seperated dictionary confirmed transliterations formed with
	 *		all rules and with morhological guessing (transliterations from #2,
	 *		#4 are not included 2nd time);
	 *	9)  number of variants in column #8;
	 *	10) space seperated unconfirmed transliterations formed with exact rules
	 *		only;
	 *	11) number of variants in column #10;
	 *	12) space seperated unconfirmed transliterations formed with all rules
	 *		(transliterations from the previos columns are not included 2nd
	 *		time);
	 *	13) number of variants in column #12.
	 *
	 * @param in		input stream according to the described format.
	 * @param out		output stream according to described format.
	 * @param firstLine	first line of the input stream, if it has been read
	 *					prevously.
	 */
	private void processFileBasic(
		BufferedReader in, BufferedWriter out, String firstLine, String group)
	throws IOException
	{
		String line = (firstLine == null) ? in.readLine() : firstLine;
		int counter = 1;
		
		// Print table header.
		out.write(
			"Token\tExact dict\tCount\tExact dict guess\tCount\tFuzzy dict\t"
			+ "Count\tFuzzy dict guess\tCount\tExact no-dict\tCount\t"
			+ "Fuzzy no-dict\tCount");
		out.newLine();
		// Process each line.
		while (line != null)
		{
			// Parse input.
			String word = line;
			word = word.trim();
			
			// Nothing to process in this line.
			if (word == null || word.equals(""))
			{
				out.newLine();
				line = in.readLine();
				continue;
			}
			
			// Process word.
			ResultData r = t.processWord(word.trim(), group, fuzzy);
			
			if (counter % 100 == 0)
				System.out.println("Processing token No." + counter);

			// Do output.
			out.write(word + "\t");
			StringBuilder tmp = new StringBuilder(" ");
			ArrayList<Variant> sorted = new ArrayList<Variant>(
				r.DICT_EXACT.data.keySet());
			if (t.comparator != null) Collections.sort(sorted, t.comparator);
			for (Variant v : sorted)
			{
				tmp.append(v.token + " ");
			}
			out.write(
				tmp.toString().trim() + "\t" + r.DICT_EXACT.data.size() + "\t");
				
			tmp = new StringBuilder(" ");
			sorted = new ArrayList<Variant>(r.DICT_EXACT_GUESS.data.keySet());
			if (t.comparator != null) Collections.sort(sorted, t.comparator);
			for (Variant v : sorted)
			{
				tmp.append(v.token + " ");
			}
			out.write(
				tmp.toString().trim() + "\t"
				+ r.DICT_EXACT_GUESS.data.size() + "\t");
				
			tmp = new StringBuilder(" ");
			sorted = new ArrayList<Variant>(r.DICT_FUZZY.data.keySet());
			if (t.comparator != null) Collections.sort(sorted, t.comparator);
			for (Variant v : sorted)
			{
				tmp.append(v.token + " ");
			}
			out.write(
				tmp.toString().trim() + "\t" + r.DICT_FUZZY.data.size() +"\t");
				
			tmp = new StringBuilder(" ");
			sorted = new ArrayList<Variant>(r.DICT_FUZZY_GUESS.data.keySet());
			if (t.comparator != null) Collections.sort(sorted, t.comparator);
			for (Variant v : sorted)
			{
				tmp.append(v.token + " ");
			}
			out.write(
				tmp.toString().trim() + "\t"
				+ r.DICT_FUZZY_GUESS.data.size() +"\t");
			
			tmp = new StringBuilder(" ");
			sorted = new ArrayList<Variant>(r.NO_DICT_EXACT);
			if (t.comparator != null) Collections.sort(sorted, t.comparator);
			for (Variant v : sorted)
			{
				tmp.append(v.token + " ");
			}
			out.write(
				tmp.toString().trim() + "\t" + r.NO_DICT_EXACT.size() + "\t");
				
			tmp = new StringBuilder(" ");
			sorted = new ArrayList<Variant>(r.NO_DICT_FUZZY);
			if (t.comparator != null) Collections.sort(sorted, t.comparator);
			for (Variant v : sorted)
			{
				tmp.append(v.token + " ");
			}
			out.write(tmp.toString().trim() + "\t" + r.NO_DICT_FUZZY.size());
			
			out.newLine();
			out.flush();
			line = in.readLine();
			counter++;
		}
		
	}

	/**
	 * Process data file: transliterate all tokens given in the input stream,
	 * orders tranliteration variants accordingly to probabilities calculated
	 * from N-grams.
	 * To use this function, <code>Transliterator</code> must have N-gram tools
	 * iniciated.
	 *
	 * Input file must be:
	 *	*) one token per line.
	 *
	 * Output file has following tab-seperated columns:
	 *	1)  	token itself;
	 *	2..n)	variants ordered by their estimated likelyhood (most likely
	 *			first).
	 *
	 * @param in		input stream according to the described format.
	 * @param out		output stream according to described format.
	 * @param firstLine	first line of the input stream, if it has been read
	 *					prevously.
	 */
	private void processFileOrder(
		BufferedReader in, BufferedWriter out, String firstLine, String group)
	throws IOException
	{
		String line = (firstLine == null) ? in.readLine() : firstLine;
		int counter = 1;
		
		// Print table header.
		out.write("Token\tVariants");
		out.newLine();
		// Process each line.
		while (line != null)
		{
			// Parse input.
			String word = line;
			word = word.trim();
			
			// Nothing to process in this line.
			if (word == null || word.equals(""))
			{
				out.newLine();
				line = in.readLine();
				continue;
			}
			
			// Process word.
			ResultData r = t.processWord(word.trim(), group, fuzzy);
			
			if (counter % 100 == 0)
				System.out.println("Processing token No." + counter);

			// Do output.
			StringBuilder tmp = new StringBuilder(word);
			tmp.append("\t");
			ArrayList<Variant> all = r.getAllVariants();
			if (t.comparator != null) Collections.sort(all, t.comparator);
			for (Variant v : all)
			{
				tmp.append(v.token).append(" (");
				tmp.append(v.estimate(t.nGrams)).append(", ");
				if (r.DICT_EXACT.data.containsKey(v))
					tmp.append("DICT_EXACT");
				else if (r.DICT_EXACT_GUESS.data.containsKey(v))
					tmp.append("DICT_EXACT_GUESS");
				else if (r.DICT_FUZZY.data.containsKey(v))
					tmp.append("DICT_FUZZY");
				else if (r.DICT_FUZZY_GUESS.data.containsKey(v))
					tmp.append("DICT_FUZZY_GUESS");
				else if (r.NO_DICT_EXACT.contains(v))
					tmp.append("NO_DICT_EXACT");
				else if (r.NO_DICT_FUZZY.contains(v))
					tmp.append("NO_DICT_FUZZY");
				else tmp.append("ERROR");
				tmp.append(")\t");
			}
			out.write(tmp.toString());

			out.newLine();
			out.flush();
			line = in.readLine();
			counter++;
		}
	}
		
	/**
	 * Process data file with "correct answers".
	 * Input file must be:
	 *	*) token + tab + correct transliteration per line.
	 *
	 * Output file has following tab-seperated columns:
	 *	1)	token itself;
	 *	2)	in which group of transliterations the correct answer was found
	 *		(admissible values: DICT_EXACT, DICT_FUZZY, NO_DICT_EXACT,
	 *		NO_DICT_FUZZY, ANS_NOT_PRODUCED - correct answer was not produced by
	 *		transliteration engine, ANS_NOT_GIVEN - input file doesn't contain
	 *		correct answer);
	 *	3)	this number indicates which was the correct transliteration in the
	 *		list of all transliteration variants ordered by estimated
	 *		likelyhood.
	 *	4)	the same as #3, but position is given for the the list containing
	 *		the one subset of transliteration variants that contains the correct
	 *		answer.
	 *	5)	answer given in input file;
	 *	6)	number of dictionary confirmed transliterations formed without
	 *		morhological guessing with exact rules only;
	 *	7)	number of dictionary confirmed transliterations formed with exact
	 *		rules only and with morhological guessing;
	 *	8)	number of dictionary confirmed transliterations formed with all
	 *		rules without morhological guessing (transliterations from #6, #7
	 *		are not included 2nd time);
	 *	9)	number of dictionary confirmed transliterations formed with all
	 *		rules and with morhological guessing (transliterations from #6, #7
	 *		are not included 2nd time);
	 * 	10)	number of unconfirmed transliterations formed with exact rules only;
	 *	11)	number of unconfirmed transliterations formed with all rules
	 *		(transliterations from #8 are not included 2nd time).
	 *
	 * @param in		input stream according to the described format.
	 * @param out		output stream according to described format.
	 * @param firstLine	first line of the input stream, if it has been read
	 *					prevously.
	 */
	private void processFileWithAns(
		BufferedReader in, BufferedWriter out, String firstLine, String group)
	throws IOException
	{
		String line = (firstLine == null) ? in.readLine() : firstLine;
		int counter = 1;
		
		// Print table header.
		out.write(
			"Token\tWhere\tNo.\tNo. in set\t\"Correct\" answer\tExact dict"
			+ "\tExact dict guess\tFuzzy dict\tFuzzy dict guess\tExact no-dict"
			+ "\tFuzzy no-dict");
		out.newLine();
				
		// Process each line.
		while (line != null)
		{
			// Parse input.
			String word = line;
			String answer = "";
			String[] tmp = word.split("\t");
			word = tmp[0];
			if (tmp.length > 1) answer = tmp[1].trim();
			word = word.trim();
			
			// Nothing to process in this line.
			if (word == null || word.equals(""))
			{
				out.newLine();
				line = in.readLine();
				continue;
			}
			
			// Process word.
			ResultData r = t.processWord(word.trim(), group, fuzzy);
			
			if (counter % 100 == 0)
				System.out.println("Processing token No." + counter);

			// Do output.
			out.write(word + "\t");
			if (answer == null || answer.equals(""))
				out.write("ANS_NOT_GIVEN\t\t");
			else
			{
				boolean found = false;
				Variant foundItem = null;
				ArrayList<Variant> foundSet = new ArrayList<Variant>();
				for (Variant v : r.DICT_EXACT.data.keySet())
				{
					if (v.token.equalsIgnoreCase(answer))
					{
						out.write("DICT_EXACT");
						found = true;
						foundItem = v;
						foundSet.addAll(r.DICT_EXACT.data.keySet());
						break;
					}
				}
				if (!found) for (Variant v : r.DICT_EXACT_GUESS.data.keySet())
				{
					if (v.token.equalsIgnoreCase(answer))
					{
						out.write("DICT_EXACT_GUESS");
						found = true;
						foundItem = v;
						foundSet.addAll(r.DICT_EXACT_GUESS.data.keySet());
						break;
					}
				}
				if (!found) for (Variant v : r.DICT_FUZZY.data.keySet())
				{
					if (v.token.equalsIgnoreCase(answer))
					{
						out.write("DICT_FUZZY");
						found = true;
						foundItem = v;
						foundSet.addAll(r.DICT_FUZZY.data.keySet());
						break;
					}
				}
				if (!found) for (Variant v : r.DICT_FUZZY_GUESS.data.keySet())
				{
					if (v.token.equalsIgnoreCase(answer))
					{
						out.write("DICT_FUZZY_GUESS");
						found = true;
						foundItem = v;
						foundSet.addAll(r.DICT_FUZZY_GUESS.data.keySet());
						break;
					}
				}
				
				if (!found) for (Variant v : r.NO_DICT_EXACT)
				{
					if (v.token.equalsIgnoreCase(answer))
					{
						out.write("NO_DICT_EXACT");
						found = true;
						foundItem = v;
						foundSet.addAll(r.NO_DICT_EXACT);
						break;
					}
				}
				if (!found) for (Variant v : r.NO_DICT_FUZZY)
				{
					if (v.token.equalsIgnoreCase(answer))
					{
						out.write("NO_DICT_FUZZY");
						found = true;
						foundItem = v;
						foundSet.addAll(r.NO_DICT_FUZZY);
						break;
					}
				}
				if (!found) out.write("ANS_NOT_PRODUCED\t\t");
				else if (t.comparator == null) out.write("\t\t");
				else
				{
					ArrayList<Variant> all = r.getAllVariants();
					if (t.comparator != null) Collections.sort(all, t.comparator);
					int pos = all.indexOf(foundItem) + 1;
					out.write("\t" + pos);
					
					if (t.comparator != null) Collections.sort(foundSet, t.comparator);
					pos = foundSet.indexOf(foundItem) + 1;
					out.write("\t" + pos);
				}
			}
			
			out.write("\t" + answer + "\t" + r.DICT_EXACT.data.size() + "\t"
				+ r.DICT_EXACT_GUESS.data.size() + "\t"
				+ r.DICT_FUZZY.data.size() +"\t"
				+ r.DICT_FUZZY_GUESS.data.size() +"\t"
				+ r.NO_DICT_EXACT.size() + "\t" + r.NO_DICT_FUZZY.size());
		
			out.newLine();
			out.flush();
			line = in.readLine();
			counter++;
		}
	}	
	//=== Supporting functions for mor convinient CLI work. ====================
	
	/**
	 * Set default processing settings.
	 */
	private void setDefaultSettings()
	{
		fuzzy = true;
		group = "core";
		isFile = false;
		doOrder = false;
	}
	
	/**
	 * Parse CLI flags and set settings. If parse error occours, default
	 * settings are set.
	 */
	private boolean parseFlags(HashSet<String> flags)
	{
		// Parse using fuzzy or exact rules.
		if (flags.contains("E"))
		{
			fuzzy = false;
			flags.remove("E");
		}
		if (flags.contains("F"))
		{
			fuzzy = true;
			flags.remove("F");
		}
		
		// Parse file flag
		if (flags.contains("file"))
		{
			isFile = true;
			flags.remove("file");
		}
				
		// Parse order flag
		if (flags.contains("order"))
		{
			doOrder = true;
			flags.remove("order");
		}
	
		// Parse group.
		if (flags.size() > 1)
		{
			System.out.println("Too much flags found.");
			setDefaultSettings();
			return false;
		}
		if (!flags.isEmpty())
		{
			String f = flags.iterator().next();
			if (t.isValidRuleSet(f))
				group = f;
			else
			{
				System.out.println("Invalid flags found:" + f);
				setDefaultSettings();
				return false;
			}
		}
		return true;
	}

}