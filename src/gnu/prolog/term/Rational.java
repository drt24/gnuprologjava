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
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.MathContext;

public class Rational implements Comparable
{
	private MathContext mc = MathContext.DECIMAL64;
	private BigInteger numerator;
	private BigInteger denominator;
	public static final BigInteger INTEGER_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
	public static final BigInteger INTEGER_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

	public Rational(BigInteger numerator, BigInteger denominator)
	{
		if (denominator.equals(BigInteger.ZERO))
		{
			throw new ArithmeticException("Division by zero");
		}
		BigInteger gcd = numerator.gcd(denominator);
		if (denominator.signum() == -1)
		{
			// multiply by -1/-1
			numerator = numerator.negate();
			denominator = denominator.negate();
		}

		if (gcd.equals(BigInteger.ONE))
		{
			// Save 2 division operations here
			this.numerator = numerator;
			this.denominator = denominator;
		}
		else
		{
			this.numerator = numerator.divide(gcd);
			this.denominator = denominator.divide(gcd);
		}
	}

	@Override
	public boolean equals(Object o)
	{
		return (o instanceof Rational &&
			((Rational)o).numerator.equals(numerator) &&
			((Rational)o).denominator.equals(denominator));
	}

	@Override
	public int compareTo(Object o)
	{
		if (o instanceof Rational)
		{
			Rational subtrahend = (Rational)o;
			return numerator.multiply(subtrahend.denominator).subtract(subtrahend.numerator.multiply(denominator)).signum();
		}
		return hashCode() - o.hashCode();
	}

	@Override
	public String toString()
	{
		// Note that this just prints out the value to 20dp
		// We do have more precision than that, but we have to draw the line somewhere when asked to print
		return numerator.toString() + " rdiv " + denominator.toString();
		//return new BigDecimal(numerator).divide(new BigDecimal(denominator), mc).toString();
	}

	public double doubleValue()
	{
		return new BigDecimal(numerator).divide(new BigDecimal(denominator), mc).doubleValue();
	}

	public static Rational get(int val)
	{
		return new Rational(BigInteger.valueOf(val), BigInteger.valueOf(1));
	}

	public static Rational get(BigInteger val)
	{
		return new Rational(val, BigInteger.valueOf(1));
	}

        private static final double DBL_EPSILON = 0.00000000000000022204;
        public static Rational getApproximate(double val)
        {
                // This is borrowed directly from SWI Prolog
                // The source code there references a paper at http://www.cs.dartmouth.edu/~brd/papers/rotations-scg92.pdf
                // which is sadly no longer available for investigation
                double e0 = val, p0 = 0.0, q0 = 1.0;
                double e1 = -1.0, p1 = 1.0, q1 = 0.0;
                double d;

                do
                {
                        double r = Math.floor(e0/e1);
                        double e00 = e0, p00 = p0, q00 = q0;
                        double p1_q1;		/* see (*) */
                        e0 = e1;
                        p0 = p1;
                        q0 = q1;
                        e1 = e00 - r*e1;
                        p1 = p00 - r*p1;
                        q1 = q00 - r*q1;
                        p1_q1 = p1/q1;
                        d = p1_q1 - val;
                } while(Math.abs(d) > DBL_EPSILON);
                return new Rational(BigInteger.valueOf((long)p1), BigInteger.valueOf((long)q1));
        }

	public static Rational get(double val)
	{
		long bits = Double.doubleToLongBits(val);
		long sign = bits >>> 63;
		int exponent = (int)((bits >>> 52) ^ (sign << 11)) - 1023;
		long fraction = bits << 12;

		long a = 1L;
		long b = 1L;

		for (int i = 63; i >= 12; i--)
		{
			a = a * 2 + ((fraction >>> i) & 1);
			b *= 2;
		}
		BigInteger numerator;
		BigInteger denominator;
		if (exponent > 0)
		{
			numerator = BigInteger.valueOf(a).shiftLeft(exponent);
			denominator = BigInteger.valueOf(b);
		}
		else
		{
			numerator = BigInteger.valueOf(a);
			denominator = BigInteger.valueOf(b).shiftLeft(-exponent);
		}

		if (sign == 1)
			return new Rational(numerator.negate(), denominator);
		else
			return new Rational(numerator, denominator);
	}

	public Rational divide(Rational r)
	{
		BigInteger n = numerator.multiply(r.denominator);
		BigInteger d = denominator.multiply(r.numerator);
		return new Rational(n, d);
	}

	public boolean isInt32()
	{
		return (denominator.intValue() == 1 && INTEGER_MAX.compareTo(numerator) >= 0 && INTEGER_MIN.compareTo(numerator) <= 0);
	}

	public int getInt32()
	{
		return numerator.intValue();
	}

	public boolean isBigInteger()
	{
		return (denominator.intValue() == 1 && !(INTEGER_MAX.compareTo(numerator) >= 0 && INTEGER_MIN.compareTo(numerator) <= 0));
	}

	public BigInteger getBigInteger()
	{
		return numerator;
	}

	public int signum()
	{
		return numerator.signum();
	}

	public Rational abs()
	{
		if (numerator.signum() == 0)
			return this;
		return new Rational(numerator.abs(), denominator);
	}

	public Rational negate()
	{
		return new Rational(numerator.negate(), denominator);
	}

	public Rational add(Rational addend)
	{
		return new Rational(numerator.multiply(addend.denominator).add(addend.numerator.multiply(denominator)),
				    addend.denominator.multiply(denominator));
	}

	public Rational subtract(Rational subtrahend)
	{
		return new Rational(numerator.multiply(subtrahend.denominator).subtract(subtrahend.numerator.multiply(denominator)),
				    subtrahend.denominator.multiply(denominator));
	}

	public Rational multiply(long i)
	{
		return new Rational(numerator.multiply(BigInteger.valueOf(i)), denominator);
	}

	public Rational multiply(Rational multiplicand)
	{
		return new Rational(numerator.multiply(multiplicand.numerator), denominator.multiply(multiplicand.denominator));
	}


	public BigInteger denominator()
	{
		return denominator;
	}

	public BigInteger numerator()
	{
		return numerator;
	}

	public Rational pow(int i)
	{
		return new Rational(numerator.pow(i),
				    denominator.pow(i));
	}

}
