package lv.ailab.lnb.fraktur.util;

/**
 * Two element tuple template.
 */
public class Triplet<T1, T2, T3>
{
	/**
	 * First object.
	 */
	public T1 first;
	
	/**
	 * Second object.
	 */
	public T2 second;
	
	/**
	 * Third object.
	 */
	public T3 third;

	/**
	 * Constructor.
	 */
	public Triplet(T1 t1, T2 t2, T3 t3)
	{
		first = t1;
		second = t2;
		third = t3;
	}


	/**
	 * Returns a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return first + " " + second + " " + third;
	}

	/**
	 * Returns a hash code value for the object.
	 */
	@Override
	public int hashCode()
	{
		return first.hashCode() * 17 + second.hashCode() * 1063
			+ third.hashCode();
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	@Override
	public boolean equals(Object o)
	{
		try
		{
			return first.equals(((Triplet<?, ?, ?>) o).first)
					&& second.equals(((Triplet<?, ?, ?>) o).second)
					&& third.equals(((Triplet<?, ?, ?>) o).third);
		}
		catch (Exception e)
		{
			return false;
		}
	}
}