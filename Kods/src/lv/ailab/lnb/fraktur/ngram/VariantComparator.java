/**
 * This package contains tools related to transliteration variant probability
 * evaluation.
 */
package lv.ailab.lnb.fraktur.ngram;

import lv.ailab.lnb.fraktur.translit.Variant;
import java.util.Comparator;

/**
 * This class provides transliteration variant ordering based on the fixed
 * <code>VariantEvaluator</code>.
 */
public class VariantComparator implements Comparator<Variant>
{
	/**
	 * Tool for calculating probability of each variant.
	 */
	public final VariantEvaluator evaluator;
	
	/**
	 * Constructor.
	 */
	public VariantComparator (VariantEvaluator tokenEvaluator)
	{
		evaluator = tokenEvaluator;
	}
	
	/**
	 * Compares two transliteration variants.
	 */
	@Override
	public int compare (Variant t1, Variant t2)
	{
		double ev1 = t1.estimate(evaluator);
		double ev2 = t2.estimate(evaluator);
		if (ev1 < ev2) return 1;
		else if (ev1 == ev2) return 0;
		else return -1;
	}
	
}