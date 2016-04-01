/* GNU Prolog for Java
 * Copyright (C) 2016       Matt Lilley
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA. The text of license can be also found
 * at http://www.gnu.org/copyleft/lgpl.html
 */
package gnu.prolog.term;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * arbitrary precision rational term.
 * 
 * @author Matt Lilley
 * @version 0.3.0
 */
public class RationalTerm extends NumericTerm
{
	//private static final long serialVersionUID = 4778268363190379033L;

	private static final int CACHESIZE = 64 * 1024;
	private static Map<Rational, RationalTerm> cache = new LinkedHashMap<Rational, RationalTerm>()
							       {
								       protected boolean removeEldestEntry(Map.Entry eldest)
								       {
									       return size() > CACHESIZE;
								       }
							       };

	/**
	 * get rational term equal to val
	 * 
	 * @param val
	 *          value of rational term
	 * @return new rational term
	 */
	public static RationalTerm get(Rational val)
	{
		RationalTerm rc = cache.get(val);
		if (rc == null)
		{
			rc = new RationalTerm(val);
			cache.put(val, rc);
		}
		return rc;
	}

	public static NumericTerm rationalize(Rational numerator, Rational denominator)
	{
		Rational result = numerator.divide(denominator);
		if (result.isInt32())
		{
			return IntegerTerm.get(result.getInt32());
		}
		else if (result.isBigInteger())
		{
			return BigIntegerTerm.get(result.getBigInteger());
		}
		else
		{
			return get(result);
		}
	}

	/**
	 * This is the primary constructor for RationalTerm
	 * Prolog code should ONLY create these using the arithmetic functions:
	 *    * rationalize/1
	 *    * rational/1
	 *    * rdiv/2
	 * In particular, do NOT write things like X = rdiv(2, 3)
	 * 
	 * @param val
	 *          value of term
	 */
	public RationalTerm(BigInteger numerator, BigInteger denominator)
	{
		value = new Rational(numerator, denominator);
	}


	public RationalTerm(Rational value)
	{
		this.value = value;
	}


	/** value of rational */
	public final Rational value;

	/**
	 * get type of term
	 * 
	 * @return type of term
	 */
	@Override
	public int getTermType()
	{
		return RATIONAL;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof RationalTerm && ((RationalTerm) obj).value.equals(value))
		{
			return true;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return value.hashCode();
        }

	public int compareTo(Object o)
	{
		if (o instanceof RationalTerm)
			return value.compareTo(((RationalTerm)o).value);
		return o.hashCode() - hashCode();
	}
}
