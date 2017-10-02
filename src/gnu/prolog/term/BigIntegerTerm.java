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

import gnu.prolog.vm.Evaluate;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * arbitrary precision Integer term.
 * 
 * @author Matt Lilley
 * @version 0.3.0
 */
public class BigIntegerTerm extends NumericTerm
{
	//private static final long serialVersionUID = 4778268363190379033L;

	private static final int CACHESIZE = 64 * 1024;
	public static final BigInteger INTEGER_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
	public static final BigInteger INTEGER_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

	private static Map<BigInteger, BigIntegerTerm> cache = new LinkedHashMap<BigInteger, BigIntegerTerm>()
							       {
								       protected boolean removeEldestEntry(Map.Entry eldest)
								       {
									       return size() > CACHESIZE;
								       }
							       };

	/**
	 * get biginteger term equal to val
	 *   If val is within range, instead return an integer term
	 * 
	 * @param val
	 *          value of [big]integer term
	 * @return new integer term
	 */
	public static NumericTerm get(BigInteger val) throws PrologException
	{
		if (INTEGER_MAX.compareTo(val) >= 0 && INTEGER_MIN.compareTo(val) <= 0)
		{
			return IntegerTerm.get(val.intValue());
		}
		if (Evaluate.isUnbounded)
		{
			BigIntegerTerm rc = cache.get(val);
			if (rc == null)
			{
				rc = new BigIntegerTerm(val);
				cache.put(val, rc);
			}
			return rc;
		}
		else
		{
			PrologException.evalutationError(TermConstants.intOverflowAtom);
			return null;
		}
	}

	protected static BigInteger parseBigInt(String str)
	{
		BigInteger val;
		int sign = 1;
		try
		{
			if (str.charAt(0) == '-')
			{
				sign = -1;
				str = str.substring(1);
			}
			// if first symbol is 0 do special processing
			// unless the next symbol is also a digit, in which case just ignore the leading 0
			while (str.charAt(0) == '0' && str.length() > 1 && str.charAt(1) >= '0' && str.charAt(1) <= '9')
			{
				str = str.substring(1);
			}
			if (str.charAt(0) == '0' && str.length() > 1)
			{
				switch (str.charAt(1))
				{
					case 'b':
					case 'B':
						// binary integer
						// System.out.println("binary begin = "+str.substring(2));
						val = new BigInteger(str.substring(2), 2);
						// System.out.println("binary test end "+val);
						break;
					case 'O':
					case 'o':
						// octal integer
						val = new BigInteger(str.substring(2), 8);
						break;
					case 'X':
					case 'x':
						// hexadecimal integer
						val = new BigInteger(str.substring(2), 16);
						break;
					case '\'':
						// character integer
						str = str.substring(2);
						long charVal = 0;
						chars: while (true)
						{
							switch (str.charAt(0))
							{
								case '\\':
									switch (str.charAt(1))
									{
										case '\\':
											charVal = '\\';
											break chars;
										case 'n':
											charVal = '\n';
											break chars;
										case '\'':
											charVal = '\'';
											break chars;
										case '\"':
											charVal = '\"';
											break chars;
										case 'b':
											charVal = '\b';
											break chars;
										case 'a':
											charVal = 7;
											break chars;
										case 'r':
											charVal = '\r';
											break chars;
										case 't':
											charVal = '\t';
											break chars;
										case 'v':
											charVal = 0x0b;
											break chars;
										case '\r':
											str = str.substring(str.charAt(2) == '\n' ? 3 : 2);
											continue chars;
										case '\n':
											str = str.substring(str.charAt(2) == '\r' ? 3 : 2);
											continue chars;
										case 'x':
											charVal = 0;
											int i = 2;
											while (str.charAt(i) != '\\')
											{
												charVal = charVal * 16 + Character.digit(str.charAt(i), 16);
												i++;
											}
											break chars;
										default:
											charVal = str.charAt(1);
											break chars;
									}
								default:
									charVal = str.charAt(0);
									break chars;
							}
						}
						val = BigInteger.valueOf(charVal);
						break;
					default:
						throw new IllegalArgumentException("argument should be integer number");
				}
			}
			else
			{
				val = new BigInteger(str);
			}
			if (sign == 1)
				return val;
			return val.negate();
		}
		catch (NumberFormatException ex)
		{
			throw new IllegalArgumentException("argument should be integer number");
		}
	}

	/**
	 * get integer term using string value
	 * 
	 * @param str
	 *          value of integer term
	 * @return new [big]integer term
	 * @throws IllegalArgumentException
	 *           when val could not be converted to integer
	 */
	public static NumericTerm get(String str)
	{
		try
		{
			return get(parseBigInt(str));
		}
		catch(PrologException impossible)
		{
			// This is not possible since we would not be calling get(String) if isUnbounded were false
			return null;
		}
	}

	/**
	 * get integer term using string value
	 * 
	 * @param str
	 *          value of integer term
	 * @throws IllegalArgumentException
	 *           when val could ne be converted to integer
	 */
	public BigIntegerTerm(String str)
	{
		this(parseBigInt(str));
	}

	/**
	 * a constructor
	 * 
	 * @param val
	 *          value of term
	 */
	public BigIntegerTerm(int val)
	{
		value = BigInteger.valueOf(val);
	}

	/**
	 * a constructor
	 * 
	 * @param val
	 *          value of term
	 */
	public BigIntegerTerm(BigInteger val)
	{
		value = val;
	}


	/** value of integer */
	public final BigInteger value;

	/**
	 * get type of term
	 * 
	 * @return type of term
	 */
	@Override
	public int getTermType()
	{
		return BIG_INTEGER;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof BigIntegerTerm && ((BigIntegerTerm) obj).value.equals(value))
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
		if (o instanceof BigIntegerTerm)
			return value.compareTo(((BigIntegerTerm)o).value);
		return o.hashCode() - hashCode();
	}
}
