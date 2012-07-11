package lv.ailab.lnb.fraktur.util;

/**
 * Two element tuple template.
 */
public class Tuple<T1, T2>
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
	 * Constructor.
	 */
	public Tuple(T1 t1, T2 t2)
	{
		first = t1;
		second = t2;
	}


	/**
	 * Returns a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return first + " " + second;
	}

	/**
	 * Returns a hash code value for the object.
	 */
	@Override
	public int hashCode()
	{
		return first.hashCode() * 17 + second.hashCode() ;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	@Override
	public boolean equals(Object o)
	{
		try
		{
			return first.equals(((Tuple<?, ?>) o).first)
					&& second.equals(((Tuple<?, ?>) o).second);
		}
		catch (Exception e)
		{
			return false;
		}
	}
}