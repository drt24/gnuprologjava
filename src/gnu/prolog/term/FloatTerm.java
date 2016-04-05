/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2010       Daniel Thomas
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

/**
 * Floating point number term (uses double)
 * 
 * @author Constantine Plotnikov
 * @version 0.0.1
 */
public class FloatTerm extends NumericTerm
{
	private static final long serialVersionUID = -5988244457397590539L;

	/**
	 * The "slack" used in float comparison
	 */
	// public static final double FLOAT_EPSILON = 0.0000001d;

	/**
	 * get floating point number term
	 * 
	 * @param str
	 *          string representation of float number
	 * @throws IllegalArgumentException
	 *           when str is not valid string
	 */
	public FloatTerm(String str)
        {
                double d;
		try
		{
                        d = new Double(str).doubleValue();
		}
		catch (NumberFormatException ex)
                {
                        // Could be a very large integer in a non-Java format, and we do not have unbounded processing
                        if (!Evaluate.isUnbounded && (str.charAt(0) == '0' && str.length() > 1) || (str.charAt(0) == '-' && str.charAt(1) == '0' && str.length() > 2))
                        {
                                try
                                {
                                        d = BigIntegerTerm.parseBigInt(str).doubleValue();
                                }
                                catch(NumberFormatException otherEx)
                                {
                                        // Ignore this one and just through the first exception?
                                        throw new IllegalArgumentException("argument should be floating point number", ex);
                                }
                        }
                        else
                        {
                                throw new IllegalArgumentException("argument should be floating point number", ex);
                        }
                }
                value = d;
	}

	/**
	 * a constructor
	 * 
	 * @param val
	 *          double value
	 */
	public FloatTerm(double val)
	{
		value = val;
	}

	/** value of term */
	public final double value;

	/**
	 * get type of term
	 * 
	 * @return type of term
	 */
	@Override
	public int getTermType()
	{
		return FLOAT;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof FloatTerm)
		{
			return equals((FloatTerm) obj);
		}
		else
		{
			return false;
		}
	}

	public boolean equals(FloatTerm oft)
	{
		if (oft != null && value == oft.value
		/* && Math.abs(value - oft.value) < FLOAT_EPSILON */)
		{
			return true;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new Double(value).hashCode();
	}
}
