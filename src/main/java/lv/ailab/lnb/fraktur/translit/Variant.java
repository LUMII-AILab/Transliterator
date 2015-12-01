package lv.ailab.lnb.fraktur.translit;

import lv.ailab.lnb.fraktur.ngram.VariantEvaluator;


/**
 * This class stores one token / transliteration variant and remembers N-gram
 * estimation once it is calculated.
 */
public class Variant
{
	/**
	 * Token / transliteration variant.
	 */
	public final String token;
	/**
	 * Logarithmic N-gram estimate.
	 */
	private Double estimate;
	/**
	 * Token evaluator class, who did the estimate;
	 */
	private VariantEvaluator evaluatedBy;
	
	/**
	 * Constructor.
	 * @param token	Token or transliteration variant.
	 * @param ev	N-gram evaluator to calculate the estimate for this token or
	 *				null.
	 */
	public Variant (String token, VariantEvaluator ev)
	{
		this.token = token;
		evaluatedBy = ev;
		if (ev == null)
			estimate = null;
		else estimate = ev.evaluateToken(token);
	}
	
	/**
	 * Estimate token: return previously calculated estimate if such exists, use
	 * provided estimator otherwise. 
	 */
	public Double estimate(VariantEvaluator ev)
	{
		if (ev == null)
		{
			evaluatedBy = null;
			estimate = null;
			return null;
		} else if (evaluatedBy == null || !evaluatedBy.equals(ev))
		{
			evaluatedBy = ev;
			estimate = ev.evaluateToken(token);
			return estimate;
		} else return estimate;
	}
	
	/**
	 * Equivalence is determined by comparing tokens. Probabilities are not
	 * taken into account.
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o == null) return false;
		return o.equals(token);
	}
	
	/**
	 * Returns a hash code for this object.
	 */
	@Override
	public int hashCode()
	{
		if (token == null) return 0;
		return token.hashCode();
	}
	
	/**
	 * String representation for contents of this object. For debugging
	 * purposes.
	 */
	@Override
	public String toString()
	{
		if (estimate == null) return token;
		return token + " (" + String.format("%.4f", estimate) + ")";
	}
}