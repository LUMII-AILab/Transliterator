package lv.ailab.lnb.fraktur.translit;

import lv.ailab.lnb.fraktur.util.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class for handling and representing transliteration rules.
 */
public class Rules
{
	/**
	 * Constants representing in which positions given rulle can be applied.
	 */
	public static enum Pos
	{
		/**
		 * Begining of the token.
		 */
		BEGIN,
		/**
		 * End of the token.
		 */
		END,
		/**
		 * Everywhere in the token.
		 */
		ALL,
		/**
		 * Match all token.
		 */
		EXACT;
		
		/**
		 * Parse Pos value from string (ALL is default value).
		 */
		static Pos fromString(String s)
		{
			 if (s == null) return ALL;
			 String normalized = s.trim().toLowerCase();
			 if (normalized.equals("begin")) return BEGIN;
			 if (normalized.equals("end")) return END;
			 if (normalized.equals("exact")) return EXACT;
			 return ALL;
			 
		}
	}
	/**
	 * Unambiguous case insensitive transiteration rules.
	 */
	public HashMap<String, Tuple<String, Pos>> exact;
	
	/**
	 * Unambiguous case sensitive transiteration rules.
	 */
	public HashMap<String, Tuple<String, Pos>> exactSense;
	
	
	/**
	 * Ambiguous case insensitive transiteration rules.
	 */
	public HashMultiMap<String, Tuple<String, Pos>> fuzzy;
	/**
	 * Ambiguous case sensitive transiteration rules.
	 */
	public HashMultiMap<String, Tuple<String, Pos>> fuzzySense;
	
	/**
	 * Reads translation rules from given XML file. DOM used.
	 */
	public Rules (File ruleFile)
	throws SAXException,IOException, ParserConfigurationException
	{
		// Create DOM from XML file.
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);
		Document doc = dbf.newDocumentBuilder().parse(ruleFile);
			
		// "exact" and "fuzzy".
		NodeList nl = doc.getDocumentElement().getChildNodes();
		
		exact = new HashMap<String, Tuple<String, Pos>>();
		exactSense = new HashMap<String, Tuple<String, Pos>>();
		fuzzy = new HashMultiMap<String, Tuple<String, Pos>>();
		fuzzySense = new HashMultiMap<String, Tuple<String, Pos>>();
		
		// Process all "exact" and "fuzzy" elements.
		for (int i = 0; i <  nl.getLength(); i++)
		{
			if (nl.item(i).getNodeName().equals("exact"))
				processExact(nl.item(i));
			if (nl.item(i).getNodeName().equals("fuzzy"))
				processFuzzy(nl.item(i));
		}
		
		// An exception is thrown if no rules are found.
		if (exact.size() < 1 && fuzzy.size() < 1)
		{
			throw new IllegalArgumentException(
				"Rule file \"" + ruleFile.getName() + "\" contains no rules.");
		}
		
	}
	
	
	/**
	 * Returns unambiguous transliteration rules associated with given string.
	 */
	/*public String getExactReplacement (String what)
	{
		return exact.get(what);
	}//*/
	
	/**
	 * Returns ambiguous transliteration rules associated with given string.
	 */
	/*public ArrayList<String> getFuzzyReplacement (String what)
	{
		return fuzzy.get(what);
	}//*/
	
	/**
	 * Returns all transliteration rules associated with given string.
	 */
	/*public ArrayList<String> getAllReplacement (String what)
	{
		if (!exact.containsKey(what) && fuzzy.containsKey(what)) return null;
		
		ArrayList<String> res = new ArrayList<String>();
		if (exact.containsKey(what)) res.add(exact.get(what));
		if (fuzzy.containsKey(what)) res.addAll(fuzzy.get(what));
		
		return res;
	}//*/
	
	
	
	//=== Supporting functions. ================================================

	/**
	 * Processing rules enlisted in "exact" node.
	 */
	private void processExact(Node n)
	{
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++)
		{
			Tuple<String,ArrayList<Triplet<String, Pos, Boolean>>> tmpRes =
				processRule(nl.item(i));
			if (tmpRes == null) continue; // Comments and other unrelated stuff.
			if (tmpRes.second.size() != 1)
			{
				throw new IllegalArgumentException(
					"Invalid XML provided: rule \"" + tmpRes.first
					+ "\" has invalid replacement.");

			}
			Triplet<String, Pos, Boolean> tmpRepl = tmpRes.second.get(0);
			if (tmpRepl.third)
				exactSense.put(tmpRes.first, firstTwo(tmpRepl));
			else exact.put(tmpRes.first.toLowerCase(),
				new Tuple<String, Pos>(tmpRepl.first.toLowerCase(), tmpRepl.second));
		}
	}
	
	/**
	 * Processing rules enlisted in "fuzzy" node.
	 */
	private void processFuzzy(Node n)
	{
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++)
		{
			//if (nl.item(i).getNodeName().equals("#text"))
			//	continue;
			Tuple<String,ArrayList<Triplet<String, Pos, Boolean>>> tmpRes =
				processRule(nl.item(i));
			if (tmpRes == null) continue; // Comments and other unrelated stuff.
			if (tmpRes.second.size() < 1)
			{
				throw new IllegalArgumentException(
					"Invalid XML provided: rule \"" + tmpRes.first
					+ "\" has no replacement.");

			}
			for (Triplet<String, Pos, Boolean> tmpRepl : tmpRes.second)
			{
				if (tmpRepl.third)
				{
					String key = tmpRes.first;
					Tuple<String, Pos> value = firstTwo(tmpRepl);
					fuzzySense.putOne(key, value);
				}
				else
				{
					String key = tmpRes.first.toLowerCase();
					Tuple<String, Pos> value = new Tuple<String, Pos>(
							tmpRepl.first.toLowerCase(), tmpRepl.second);
					fuzzy.putOne(key, value);
				}
				
			}
			//fuzzy.putOne(tmpRes.first, tmpRes.second);
		}

	}
	
	/**
	 * Process single "r" element (replacement directions for one target string.
	 * "r" element can take one of following forms:
	 * <code><r target="w" replace="v" /></code>,
	 * <code><r target="w" >v</r></code>,
	 * <code><r target="w"><replace position="all">v</replace></r></code>,
	 * <code><r target="w"><replace>v</replace><replace>m</replace></r></code>.
	 * Position attribute is optional. Position can take values
	 * <code>all</code> (default), <code>begin</code>, <code>end</code>,
	 * <code>exact</code> (acts as begin + end).
	 */
	private Tuple<String,ArrayList<Triplet<String, Pos, Boolean>>> processRule(Node n)
	{
		// Check the correctness of the XML.
		if (!n.getNodeName().equals("r")) return null;
		
		// Retrieving target.
		String target = n.getAttributes().getNamedItem("target").getTextContent();
			
		// Retrieving replacements.
		ArrayList<Triplet<String, Pos, Boolean>> replacements =
			new ArrayList<Triplet<String, Pos, Boolean>>();
		
		// Get sensitivity argument.
		Boolean globalSens = false;
		Boolean tmpSens = getSensitivity(n);
		if (tmpSens != null) globalSens = tmpSens;
			
		// Get position argument.
		Pos globalPos = Pos.ALL;
		Pos tmpPos = getPosition(n);
		if (tmpPos != null) globalPos = tmpPos;

		// Replacement given as attribute.	
		Node replNode = n.getAttributes().getNamedItem("replace");
		if (replNode != null)
		{
			replacements.add(
				new Triplet<String, Pos, Boolean>(
					replNode.getTextContent(), globalPos, globalSens));
		}
		
		NodeList nl = n.getChildNodes();
		
		// If there is no children...
		if (nl == null || nl.getLength() < 1) {/* Do nothing. */}
		
		// If there is only one child, this could be text element.
		else if (nl.getLength() == 1)
		{
			Pos position = globalPos;
			Boolean sensitive = globalSens;
			replNode = nl.item(0);
			
			// If replacement is enclosed in some tags.
			if(replNode.getNodeType() != Node.TEXT_NODE)
			{
				// Check the correctness of the XML.
				if (!replNode.getNodeName().equals("replace"))
					throw new IllegalArgumentException(
						"Invalid XML provided: element \"replace\" expected, \""
						 + replNode.getNodeName() + "\" found.");
						 
				// Get position if it is given.
				tmpPos = getPosition(replNode);
				if (tmpPos != null) position = tmpPos;
				
				// Get sensitivity if it is given.
				tmpSens = getSensitivity(replNode);
				if (tmpSens != null) sensitive = tmpSens;
				
				replNode = replNode.getChildNodes().item(0);
				// Check the correctness of the XML.
				if(replNode.getNodeType() != Node.TEXT_NODE)
				{
					throw new IllegalArgumentException(
						"Invalid XML provided: unexpected emptyness in element \""
						+ replNode.getNodeName() + "\" found.");
				}				
			}
			
			replacements.add(
				new Triplet<String, Pos, Boolean>(
					replNode.getTextContent(), position, sensitive));
		}
		
		// Multiple children - multiple replacements.
		else
		{
			for (int i = 0; i < nl.getLength(); i++)
			{
				Pos position = globalPos;
				Boolean sensitive = globalSens;
				Node tmp = nl.item(i);
				
				// Skip the nodes, containing indentation from source file.
				if (tmp.getNodeType() == Node.TEXT_NODE) continue;
				
				// Check the correctness of the XML.
				if (!tmp.getNodeName().equals("replace")
					&& !tmp.getNodeName().equals("#text"))
					throw new IllegalArgumentException(
						"Invalid XML provided: element \"replace\" expected, \""
						 + tmp.getNodeName() + "\" found.");
					 
				if (tmp.getChildNodes().getLength() > 1
					|| tmp.getChildNodes().item(0).getNodeType() != Node.TEXT_NODE)
					throw new IllegalArgumentException(
						"Invalid XML provided: unexpected content found in element \""
						 + tmp.getNodeName() + "\".");
				
				// Get position if it is given.
				tmpPos = getPosition(tmp);
				if (tmpPos != null) position = tmpPos;
				
				// Get sensitivity if it is given.
				tmpSens = getSensitivity(tmp);
				if (tmpSens != null) sensitive = tmpSens;

				replacements.add(new Triplet<String, Pos, Boolean>(
					tmp.getChildNodes().item(0).getTextContent(), position, sensitive));	 
			}
		}
		
		// Happy ending, everything is colected.
		return new Tuple<String,ArrayList<Triplet<String, Pos, Boolean>>>(
			target, replacements);
	}
	
	/**
	 * Get node's position attribute converted to Pos enumeration values.
	 */
	private static Pos getPosition (Node n)
	{
		Node posNode = n.getAttributes().getNamedItem("position");
		if (posNode == null) return null;
		String posString = posNode.getTextContent();
		return Pos.fromString(posString);
	}
	
	/**
	 * Get node's sensitivity attribute converted to Boolean.
	 */
	private static Boolean getSensitivity (Node n)
	{
		Node sensNode = n.getAttributes().getNamedItem("sensitive");
		if (sensNode == null) return null;
		return sensNode.getTextContent().trim().equals("1");
	}
	
	/**
	 * Create tuple from first two elements of a triplet.
	 */
	private static <T1, T2> Tuple<T1, T2> firstTwo(Triplet<T1, T2, ?> t)
	{
		return new Tuple<T1, T2> (t.first, t.second);
	}
}