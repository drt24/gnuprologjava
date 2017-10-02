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
package gnu.prolog.vm;

import gnu.prolog.database.Pair;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.BigIntegerTerm;
import gnu.prolog.term.RationalTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.NumericTerm;
import gnu.prolog.term.VariableTerm;

import java.util.Random;
import java.math.BigInteger;
import gnu.prolog.term.Rational;

/**
 * Evaluates mathematical expressions as instructed by the
 * {@link gnu.prolog.vm.buildins.arithmetics.Predicate_is is} predicate and
 * {@link gnu.prolog.vm.buildins.arithmetics equality testing terms}.
 * 
 */
public class Evaluate
{
	private Evaluate()
	{}

	public final static CompoundTermTag add2 = CompoundTermTag.get("+", 2);
	public final static CompoundTermTag sub2 = CompoundTermTag.get("-", 2);
	public final static CompoundTermTag mul2 = CompoundTermTag.get("*", 2);
        public final static CompoundTermTag intdiv2 = CompoundTermTag.get("//", 2);
        public final static CompoundTermTag safeintdiv2 = CompoundTermTag.get("div", 2);
	public final static CompoundTermTag div2 = CompoundTermTag.get("/", 2);
	public final static CompoundTermTag rem2 = CompoundTermTag.get("rem", 2);
	public final static CompoundTermTag mod2 = CompoundTermTag.get("mod", 2);
	public final static CompoundTermTag neg1 = CompoundTermTag.get("-", 1);
	public final static CompoundTermTag abs1 = CompoundTermTag.get("abs", 1);
	public final static CompoundTermTag sqrt1 = CompoundTermTag.get("sqrt", 1);
	public final static CompoundTermTag sign1 = CompoundTermTag.get("sign", 1);
	public final static CompoundTermTag intpart1 = CompoundTermTag.get("float_integer_part", 1);
	public final static CompoundTermTag fractpart1 = CompoundTermTag.get("float_fractional_part", 1);
	public final static CompoundTermTag float1 = CompoundTermTag.get("float", 1);
	public final static CompoundTermTag floor1 = CompoundTermTag.get("floor", 1);
	public final static CompoundTermTag truncate1 = CompoundTermTag.get("truncate", 1);
	public final static CompoundTermTag round1 = CompoundTermTag.get("round", 1);
	public final static CompoundTermTag ceiling1 = CompoundTermTag.get("ceiling", 1);
	public final static CompoundTermTag power2 = CompoundTermTag.get("**", 2);
	public final static CompoundTermTag sin1 = CompoundTermTag.get("sin", 1);
	public final static CompoundTermTag cos1 = CompoundTermTag.get("cos", 1);
	public final static CompoundTermTag atan1 = CompoundTermTag.get("atan", 1);
	public final static CompoundTermTag exp1 = CompoundTermTag.get("exp", 1);
	public final static CompoundTermTag log1 = CompoundTermTag.get("log", 1);
	public final static CompoundTermTag brshift2 = CompoundTermTag.get(">>", 2);
	public final static CompoundTermTag blshift2 = CompoundTermTag.get("<<", 2);
	public final static CompoundTermTag band2 = CompoundTermTag.get("/\\", 2);
	public final static CompoundTermTag bor2 = CompoundTermTag.get("\\/", 2);
	public final static CompoundTermTag bnot1 = CompoundTermTag.get("\\", 1);
        public final static CompoundTermTag rdiv2 = CompoundTermTag.get("rdiv", 2);
        public final static CompoundTermTag rationalize1 = CompoundTermTag.get("rationalize", 1);
        public final static CompoundTermTag rational1 = CompoundTermTag.get("rational", 1);
        public final static CompoundTermTag gcd2 = CompoundTermTag.get("gcd", 2);
        public final static AtomTerm atomNan = AtomTerm.get("nan");

        public final static boolean isUnbounded = true;
	public final static boolean strictISO = false;

	/**
	 * Implementation of the random/1 predicate <a
	 * href="http://www.swi-prolog.org/man/arith.html#random/1">defined in
	 * SWI-Prolog</a>
	 * 
	 * random(+IntExpr)
	 * 
	 * Evaluates to a random integer i for which 0 =< i < IntExpr.
	 * 
	 */
	public final static CompoundTermTag random1 = CompoundTermTag.get("random", 1);
	private final static Random random = new Random();

	public final static AtomTerm floatAtom = AtomTerm.get("float");

	private static void zeroDivizor() throws PrologException
	{
		PrologException.evalutationError(TermConstants.zeroDivizorAtom);
	}

	private static void intOverflow() throws PrologException
	{
		PrologException.evalutationError(TermConstants.intOverflowAtom);
	}

	private static void floatOverflow() throws PrologException
	{
		PrologException.evalutationError(TermConstants.floatOverflowAtom);
	}

	private static void undefined() throws PrologException
	{
		PrologException.evalutationError(TermConstants.undefinedAtom);
	}

	// The rule is that we need both args to be of the same type before we can add them
	// If they are not, then convert them both to the highest value in this mapping:
	// Integer: 0
	// BigInteger: 1
	// Float: 2
	// Rational: 3
	// This means that 10**500 + 0.2 is an overflow error, but 10**500 + 1 rdiv 5 is a rational
	// Perhaps it would be better to detect this particular case and make them both rationals, but
	// this is the behaviour SWI-Prolog uses so for now it is probably good enough.

	public static int commonType(Term arg0, Term arg1) throws PrologException
	{
		int targetType = 0;
		if (arg0 instanceof IntegerTerm)
		{
		}
		else if (arg0 instanceof BigIntegerTerm)
		{
			if (targetType < 1)
				targetType = 1;
		}
		else if (arg0 instanceof FloatTerm)
		{
			if (targetType < 2)
				targetType = 2;
		}
		else if (arg0 instanceof RationalTerm)
		{
			if (targetType < 3)
				targetType = 3;
		}
		else
		{
			PrologException.typeError(TermConstants.numericAtom, arg0);
		}

		if (arg1 instanceof IntegerTerm)
		{
		}
		else if (arg1 instanceof BigIntegerTerm)
		{
			if (targetType < 1)
				targetType = 1;
		}
		else if (arg1 instanceof FloatTerm)
		{
			if (targetType < 2)
				targetType = 2;
		}
		else if (arg1 instanceof RationalTerm)
		{
			if (targetType < 3)
				targetType = 3;
		}
		else
		{
			PrologException.typeError(TermConstants.numericAtom, arg1);
		}
		return targetType;
	}

	// Maybe these should be in NumericTerm?
	public static double[] toDouble(Term... args) throws PrologException
	{
		double[] result = new double[args.length];
		int i = 0;
		for (Term t : args)
		{
			if (t instanceof IntegerTerm)
			{
				result[i++] = ((IntegerTerm)t).value;
			}
			else if (t instanceof FloatTerm)
			{
				result[i++] = ((FloatTerm)t).value;
			}
			else if (t instanceof BigIntegerTerm)
			{
				double d0 = ((BigIntegerTerm)t).value.doubleValue();
				if (d0 == Double.POSITIVE_INFINITY || d0 == Double.NEGATIVE_INFINITY)
					floatOverflow();
				result[i++] = d0;
			}
			else if (t instanceof RationalTerm)
			{
				double d0 = ((RationalTerm)t).value.doubleValue();
				if (d0 == Double.POSITIVE_INFINITY || d0 == Double.NEGATIVE_INFINITY)
					floatOverflow();
				result[i++] = d0;
			}
			else
			{
				PrologException.typeError(TermConstants.numericAtom, t);
			}

		}
		return result;
	}

	public static BigInteger[] toBigInteger(Term... args) throws PrologException
	{
		BigInteger[] result = new BigInteger[args.length];
		int i = 0;
		for (Term t : args)
		{
			if (t instanceof IntegerTerm)
			{
				result[i++] = BigInteger.valueOf(((IntegerTerm)t).value);
			}
			else if (t instanceof BigIntegerTerm)
			{
				result[i++] = ((BigIntegerTerm)t).value;
			}
			else
			{
				PrologException.typeError(TermConstants.integerAtom, t);
			}

		}
		return result;
	}

	public static Rational[] toRational(Term... args) throws PrologException
	{
		Rational[] result = new Rational[args.length];
		int i = 0;
		for (Term t : args)
		{
			if (t instanceof IntegerTerm)
			{
				result[i++] = Rational.get(((IntegerTerm)t).value);
			}
			else if (t instanceof BigIntegerTerm)
			{
				result[i++] = Rational.get(((BigIntegerTerm)t).value);
			}
			else if (t instanceof FloatTerm)
			{
				result[i++] = Rational.get(((FloatTerm)t).value);
			}
			else if (t instanceof RationalTerm)
			{
				result[i++] = ((RationalTerm)t).value;
			}
			else
			{
				PrologException.typeError(TermConstants.integerAtom, t);
			}

		}
		return result;
	}


	public static Term evaluate(Term term) throws PrologException
        {
                term = term.dereference();// ensure we are looking at most instantiated
		// value
		if (term instanceof NumericTerm)
		{
			return term;
		}
		else if (term instanceof VariableTerm)
		{
			PrologException.instantiationError(term);
                }
                else if (atomNan.equals(term) && !strictISO)
                {
                        return new FloatTerm(Float.NaN);
                }
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			CompoundTermTag tag = ct.tag;
			int i, arity = tag.arity;
			Term sargs[] = ct.args;
			Term args[] = new Term[arity];
			for (i = 0; i < arity; i++)
			{// TODO: we need to check whether tag represents an evaluable function
				// before we try to evaluate it's arguments
				args[i] = evaluate(sargs[i].dereference());
			}
			if (tag == add2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				int targetType = commonType(arg0, arg1);
				switch(targetType)
				{
				case 0: // int + int
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					long res = (long) i0.value + (long) i1.value;
					if (!(res > Integer.MAX_VALUE || res < Integer.MIN_VALUE))
					{
						return IntegerTerm.get((int) res);
					}
					else if (!isUnbounded)
					{
						intOverflow();
					}
					// Otherwise, fall-through to biginteger case and try again
				}
				case 1: // bigint + bigint
				{
					BigInteger[] bi = toBigInteger(arg0, arg1);
					BigInteger res = bi[0].add(bi[1]);
					return BigIntegerTerm.get(res);
				}
				case 2: // float * float
				{
					double[] f = toDouble(arg0, arg1);
					double res = f[0] + f[1];
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				case 3: // rational * rational
				{
					Rational[] bi = toRational(arg0, arg1);
					Rational res = bi[0].add(bi[1]);
					return RationalTerm.get(res);
				}
				}
			}
			else if (tag == sub2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				int targetType = commonType(arg0, arg1);
				switch(targetType)
				{
				case 0: // int - int
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					long res = (long) i0.value - (long) i1.value;
					if (!(res > Integer.MAX_VALUE || res < Integer.MIN_VALUE))
					{
						return IntegerTerm.get((int) res);
					}
					else if (!isUnbounded)
					{
						intOverflow();
					}
					// Otherwise, fall-through to biginteger case and try again
				}
				case 1: // bigint - bigint
				{
					BigInteger[] bi = toBigInteger(arg0, arg1);
					BigInteger res = bi[0].subtract(bi[1]);
					return BigIntegerTerm.get(res);
				}
				case 2: // float - float
				{
					double[] f = toDouble(arg0, arg1);
					double res = f[0] - f[1];
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				case 3: // rational - rational
				{
					Rational[] bi = toRational(arg0, arg1);
					Rational res = bi[0].subtract(bi[1]);
					return RationalTerm.get(res);
				}
				}
			}
			else if (tag == mul2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				int targetType = commonType(arg0, arg1);

				switch(targetType)
				{
				case 0: // integer * integer
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					long res = (long) i0.value * (long) i1.value;
					if (!(res > Integer.MAX_VALUE || res < Integer.MIN_VALUE))
					{
						return IntegerTerm.get((int) res);
					}
					else if (!isUnbounded)
					{
						intOverflow();
					}
					// Otherwise, fall-through to biginteger case and try again
				}
				case 1: // bigint * bigint
				{
					BigInteger[] bi = toBigInteger(arg0, arg1);
					BigInteger product = bi[0].multiply(bi[1]);
					return BigIntegerTerm.get(product);
				}
				case 2: // float * float
				{
					double[] f = toDouble(arg0, arg1);
					double res = f[0] * f[1];
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				case 3: // rational * rational
				{
					Rational[] r = toRational(arg0, arg1);
					Rational product = r[0].multiply(r[1]);
					return RationalTerm.get(product);
				}
				}
			}
			else if (tag == intdiv2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (!(arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg0);
				}
				if (!(arg1 instanceof IntegerTerm || arg1 instanceof BigIntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg1);
				}
				if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					if (i1.value == 0)
					{
						zeroDivizor();
					}
					int res = i0.value / i1.value;
					return IntegerTerm.get(res);
				}
				else
				{
					// Otherwise upgrade both to BigIntegers if one of them is already
					BigInteger b0;
					BigInteger b1;
					if (arg0 instanceof BigIntegerTerm)
						b0 = ((BigIntegerTerm)arg0).value;
					else
						b0 = BigInteger.valueOf(((IntegerTerm)arg0).value);
					if (arg1 instanceof BigIntegerTerm)
						b1 = ((BigIntegerTerm)arg1).value;
					else
						b1 = BigInteger.valueOf(((IntegerTerm)arg1).value);
					if (b1.equals(BigInteger.ZERO))
					{
						zeroDivizor();
					}
					BigInteger res = b0.divide(b1);
					// Note that BigIntegerTerm.get may return an IntegerTerm if appropriate
					return BigIntegerTerm.get(res);
				}
                        }
                        else if (tag == safeintdiv2 && !strictISO) // ***************************************
                        {
                                // This is div/2. SWI-Prolog seems to think this is ISO, but it doesnt seem to be in the ISO standard
                                // The SWI-Prolog documentation states:
                                // Integer division, defined as Result is (IntExpr1 - IntExpr1 mod IntExpr2) // IntExpr2.
                                // In other words, this is integer division that rounds towards -infinity.
                                // This function guarantees behaviour that is consistent with mod/2,
                                //   i.e., the following holds for every pair of integers X,Y where Y =\= 0.
                                //      Q is div(X, Y),
                                //      M is mod(X, Y),
                                //      X =:= Y*Q+M.
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (!(arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg0);
				}
				if (!(arg1 instanceof IntegerTerm || arg1 instanceof BigIntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg1);
				}
				if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					if (i1.value == 0)
					{
						zeroDivizor();
                                        }
                                        // Round down
                                        int res = i0.value / i1.value;
                                        if ((i0.value > 0) != (i1.value > 0) && (i0.value % i1.value != 0))
                                                res--;
                                        return IntegerTerm.get(res);
				}
				else
				{
					// Otherwise upgrade both to BigIntegers if one of them is already
					BigInteger b0;
					BigInteger b1;
					if (arg0 instanceof BigIntegerTerm)
						b0 = ((BigIntegerTerm)arg0).value;
					else
						b0 = BigInteger.valueOf(((IntegerTerm)arg0).value);
					if (arg1 instanceof BigIntegerTerm)
						b1 = ((BigIntegerTerm)arg1).value;
					else
						b1 = BigInteger.valueOf(((IntegerTerm)arg1).value);
					if (b1.equals(BigInteger.ZERO))
					{
						zeroDivizor();
					}
                                        BigInteger[] res = b0.divideAndRemainder(b1);
                                        BigInteger quotient = res[0];
                                        BigInteger remainder = res[1];
                                        // Round /down/ here
                                        if (remainder.equals(BigInteger.ZERO) || quotient.signum() >= 0)
                                                return BigIntegerTerm.get(quotient);
                                        return BigIntegerTerm.get(quotient.subtract(BigInteger.ONE));
                                }
			}
			else if (tag == div2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				// If a rational is NOT involved, convert everything to a float and do float division
				if (arg0 instanceof RationalTerm || arg1 instanceof RationalTerm)
				{
					// Rational arithmetic
					// Convert both to rationals
					Rational[] r = toRational(arg0, arg1);
					Rational res = r[0].divide(r[1]);
					// This will either return an IntegerTerm or a BigIntegerTerm
					return RationalTerm.get(res);
				}
				else
				{
					// Convert to floats
					double[] doubles = toDouble(args[0], args[1]);
					double d0 = doubles[0];
					double d1 = doubles[1];

					if (d1 == 0)
					{
						zeroDivizor();
					}
					double res = d0 / d1;
					if (Double.isInfinite(res))
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
			}
			else if (tag == rem2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (!(arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg0);
				}
				if (!(arg1 instanceof IntegerTerm || arg1 instanceof BigIntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg1);
				}
				if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					if (i1.value == 0)
					{
						zeroDivizor();
					}
					int res = i0.value % i1.value;
					return IntegerTerm.get(res);
				}
				else
				{
					// Otherwise upgrade both to BigIntegers if one is already
					BigInteger b0;
					BigInteger b1;
					if (arg0 instanceof BigIntegerTerm)
						b0 = ((BigIntegerTerm)arg0).value;
					else
						b0 = BigInteger.valueOf(((IntegerTerm)arg0).value);
					if (arg1 instanceof BigIntegerTerm)
						b1 = ((BigIntegerTerm)arg1).value;
					else
						b1 = BigInteger.valueOf(((IntegerTerm)arg1).value);
					BigInteger res = b0.remainder(b1);
					// Note that BigIntegerTerm.get may return an IntegerTerm if appropriate
					return BigIntegerTerm.get(res);
				}
			}
			else if (tag == mod2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (!(arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg0);
				}
				if (!(arg1 instanceof IntegerTerm || arg1 instanceof BigIntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg1);
				}
				if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					if (i1.value == 0)
					{
						zeroDivizor();
					}
					int res = i0.value - (int) Math.floor((double) i0.value / i1.value) * i1.value;
					return IntegerTerm.get(res);
				}
				else
				{
					// Otherwise upgrade both to BigIntegers if one is already
					BigInteger b0;
					BigInteger b1;
					if (arg0 instanceof BigIntegerTerm)
						b0 = ((BigIntegerTerm)arg0).value;
					else
						b0 = BigInteger.valueOf(((IntegerTerm)arg0).value);
					if (arg1 instanceof BigIntegerTerm)
						b1 = ((BigIntegerTerm)arg1).value;
					else
						b1 = BigInteger.valueOf(((IntegerTerm)arg1).value);
					BigInteger res = b0.mod(b1);
					// Note that BigIntegerTerm.get may return an IntegerTerm if appropriate
					return BigIntegerTerm.get(res);
				}

			}
			else if (tag == neg1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					if (i0.value == Integer.MIN_VALUE)
					{
						if (isUnbounded)
						{
							return new BigIntegerTerm(BigInteger.valueOf(i0.value).negate());
						}
						else
						{
							intOverflow();
						}
					}
					return IntegerTerm.get(-i0.value);
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					double res = -f0.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				else if (arg0 instanceof BigIntegerTerm)
				{
					BigIntegerTerm f0 = (BigIntegerTerm) arg0;
					BigInteger res = f0.value.negate();
					return new BigIntegerTerm(res);
				}
				else if (arg0 instanceof RationalTerm)
				{
					RationalTerm f0 = (RationalTerm) arg0;
					Rational res = f0.value.negate();
					return new RationalTerm(res);
				}

			}
			else if (tag == abs1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					if (i0.value == Integer.MIN_VALUE)
					{
						if (isUnbounded)
						{
							return new BigIntegerTerm(BigInteger.valueOf(i0.value).abs());
						}
						else
						{
							intOverflow();
						}
					}
					return IntegerTerm.get(Math.abs(i0.value));
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					double res = Math.abs(f0.value);
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				else if (arg0 instanceof BigIntegerTerm)
				{
					BigIntegerTerm f0 = (BigIntegerTerm) arg0;
					BigInteger res = f0.value.abs();
					return BigIntegerTerm.get(res);
				}
				else if (arg0 instanceof RationalTerm)
				{
					RationalTerm f0 = (RationalTerm) arg0;
					Rational res = f0.value.abs();
					return RationalTerm.get(res);
				}
			}
			else if (tag == sqrt1) // ***************************************
			{
				double d0 = toDouble(args[0])[0];
				double res = Math.sqrt(d0);
				if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
				{
					floatOverflow();
				}
				return new FloatTerm(res);
			}
			else if (tag == sign1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
                                        return IntegerTerm.get((int)Math.signum(i0.value));
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
                                        double res = Math.signum(f0.value);
					return new FloatTerm(res);
				}
				else if (arg0 instanceof BigIntegerTerm)
				{
					BigIntegerTerm i0 = (BigIntegerTerm) arg0;
					return IntegerTerm.get(i0.value.signum());
				}
				else if (arg0 instanceof RationalTerm)
				{
					RationalTerm i0 = (RationalTerm) arg0;
					return IntegerTerm.get(i0.value.signum());

				}

			}
			else if (tag == intpart1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm)
				{
					if (strictISO)
					{
						PrologException.typeError(floatAtom, arg0);
					}
					else
					{
						// This seems a lot more logical to me
						return arg0;
					}
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					int sign = f0.value >= 0 ? 1 : -1;
					double res = sign * Math.floor(Math.abs(f0.value));
					return new FloatTerm(res);
				}
				else if (arg0 instanceof RationalTerm)
				{
					RationalTerm f0 = (RationalTerm) arg0;
					BigInteger quotient = f0.value.numerator().divide(f0.value.denominator());
					return BigIntegerTerm.get(quotient);
				}
			}
			else if (tag == fractpart1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm)
				{
					if (strictISO)
					{
						PrologException.typeError(floatAtom, arg0);
					}
					else
					{
						// This seems a lot more logical to me
						return IntegerTerm.get(0);
					}
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					int sign = f0.value >= 0 ? 1 : -1;
					double res = f0.value - sign * Math.floor(Math.abs(f0.value));
					return new FloatTerm(res);
				}
				else if (arg0 instanceof RationalTerm)
				{
					RationalTerm f0 = (RationalTerm) arg0;
					BigInteger quotient = f0.value.numerator().divide(f0.value.denominator());
					Rational r = new Rational(quotient, BigInteger.ONE);
					Rational res = f0.value.subtract(r);
					return new RationalTerm(res);
				}
			}
			else if (tag == float1) // ***************************************
			{
				if (args[0] instanceof FloatTerm)
					return args[0];
				return new FloatTerm(toDouble(args[0])[0]);
			}
			else if (tag == floor1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm)
				{
					if (strictISO)
					{
						PrologException.typeError(floatAtom, arg0);
					}
					else
					{
						return arg0;
					}
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					double res = Math.floor(f0.value);
					if (res < Integer.MIN_VALUE || res > Integer.MAX_VALUE)
					{
						if (isUnbounded)
						{
							return RationalTerm.get(Rational.get(res));
						}
						else
						{
							intOverflow();
						}
					}
					return IntegerTerm.get((int) Math.round(res));
				}
				else if (arg0 instanceof RationalTerm)
				{
					RationalTerm r0 = (RationalTerm)arg0;
					BigInteger[] info = r0.value.numerator().divideAndRemainder(r0.value.denominator());
					BigInteger quotient = info[0];
					BigInteger remainder = info[1];
					// Round down
					if (remainder.equals(BigInteger.ZERO) || quotient.signum() >= 0)
						return BigIntegerTerm.get(quotient);
					return BigIntegerTerm.get(quotient.subtract(BigInteger.ONE));
				}
			}
			else if (tag == truncate1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm)
				{
					if (strictISO)
					{
						PrologException.typeError(floatAtom, arg0);
					}
					else
					{
						return arg0;
					}
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					int sign = f0.value >= 0 ? 1 : -1;
					double res = sign * Math.floor(Math.abs(f0.value));
					if (res < Integer.MIN_VALUE || res > Integer.MAX_VALUE)
					{
						if (isUnbounded)
						{
							return RationalTerm.get(Rational.get(res));
						}
						else
						{
							intOverflow();
						}
					}
					return IntegerTerm.get((int) Math.round(res));
				}
				else if (arg0 instanceof RationalTerm)
				{
					RationalTerm r0 = (RationalTerm)arg0;
					BigInteger[] info = r0.value.numerator().divideAndRemainder(r0.value.denominator());
					BigInteger quotient = info[0];
					BigInteger remainder = info[1];
					// Round toward zero
					return BigIntegerTerm.get(quotient);
				}
			}
			else if (tag == round1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm)
				{
					if (strictISO)
					{
						PrologException.typeError(floatAtom, arg0);
					}
					else
					{
						return arg0;
					}
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					double res = Math.floor(f0.value + 0.5);
					if (res < Integer.MIN_VALUE || res > Integer.MAX_VALUE)
					{
						if (isUnbounded)
						{
							return RationalTerm.get(Rational.get(res));
						}
						else
						{
							intOverflow();
						}
					}
					return IntegerTerm.get((int) Math.round(res));
				}
				else if (arg0 instanceof RationalTerm)
				{
					RationalTerm r0 = (RationalTerm)arg0;
					BigInteger[] info = r0.value.numerator().divideAndRemainder(r0.value.denominator());
					BigInteger quotient = info[0];
					BigInteger remainder = info[1];
					// Round down - this is what the original implementation in Evaluate does for floats too
					if (remainder.equals(BigInteger.ZERO) || quotient.signum() >= 0)
						return BigIntegerTerm.get(quotient);
					return BigIntegerTerm.get(quotient.subtract(BigInteger.ONE));
				}
			}
			else if (tag == ceiling1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm)
				{
					if (strictISO)
					{
						PrologException.typeError(floatAtom, arg0);
					}
					else
					{
						return arg0;
					}
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					double res = -Math.floor(-f0.value);
					if (res < Integer.MIN_VALUE || res > Integer.MAX_VALUE)
					{
						if (strictISO)
						{
							PrologException.typeError(floatAtom, arg0);
						}
						else
						{
							return arg0;
						}
					}
					return IntegerTerm.get((int) Math.round(res));
				}
				else if (arg0 instanceof RationalTerm)
				{
					RationalTerm r0 = (RationalTerm)arg0;
					BigInteger[] info = r0.value.numerator().divideAndRemainder(r0.value.denominator());
					BigInteger quotient = info[0];
					BigInteger remainder = info[1];
					// Round up
					if (remainder.equals(BigInteger.ZERO) || quotient.signum() < 0)
						return BigIntegerTerm.get(quotient);
					return BigIntegerTerm.get(quotient.add(BigInteger.ONE));
				}
			}
			else if (tag == power2) // ***************************************
			{
				// If either argument is a float, then the result is a float
				// If the exponent is an rdiv, then the result is a float
				// If the base is a rational and the exponent is a [big]integer, then the result is a rational
				// If the base and rational are both [big]integers then the result is a [big]integer
				// int ** int is either int or bigint
				// Actually if the exponent is a bigint, and the base is an int, we have a very big problem
				// Unless the base is 1, every number is going to result in memory exhaustion
				if (args[0] instanceof FloatTerm ||
				    args[1] instanceof FloatTerm ||
				    args[1] instanceof RationalTerm)
				{
					double[] doubles = toDouble(args[0], args[1]);
					double d0 = doubles[0];
					double d1 = doubles[1];

					if (d0 == 0 && d1 < 0)
					{
						undefined();
					}
					double res = Math.pow(d0, d1);
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				else if (args[0] instanceof RationalTerm)
				{
					// args[1] is a kind of integer, the result is a rational, except...
					RationalTerm r = (RationalTerm)args[0];
					if (args[1] instanceof BigIntegerTerm)
					{
						// This is what SWI-Prolog does, but they do it for IntegerTerm as well
						// Here we take things a bit further and return (2/3)**10 as (2**10)/(3**10)
						double[] doubles = toDouble(args[0], args[1]);
						double d0 = doubles[0];
						double d1 = doubles[1];
						if (d0 == 0 && d1 < 0)
						{
							undefined();
						}
						double res = Math.pow(d0, d1);
						if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
						{
							floatOverflow();
						}
						return new FloatTerm(res);
					}
					IntegerTerm i0 = (IntegerTerm)args[1];
					if (r.value.numerator().equals(BigInteger.ZERO) && i0.value <= 0)
					{
						undefined();
					}
					return RationalTerm.get(r.value.pow(i0.value));
				}
				else
				{
					if (args[1] instanceof BigIntegerTerm)
					{
						if (args[0] instanceof IntegerTerm)
						{
							if (((IntegerTerm)args[0]).value == 1)
								return IntegerTerm.get(1);
							else if (((IntegerTerm)args[0]).value == 0)
								return IntegerTerm.get(0);
							else if (((IntegerTerm)args[0]).value == -1)
							{
								BigInteger mod = ((BigIntegerTerm)args[0]).value.mod(BigInteger.valueOf(2));
								if (mod.equals(BigInteger.ONE))
									return IntegerTerm.get(-1);
								else
									return IntegerTerm.get(1);

							}
						}
						// Otherwise this is just too big!
						intOverflow();
					}
					// base is either an integer or a biginteger. Exponent is an integer
					BigInteger[] bi = toBigInteger(args[0]);
					IntegerTerm i0 = (IntegerTerm)args[1];
					if (bi[0].equals(BigInteger.ZERO) && i0.value < 0)
					{
						undefined();
					}
					return BigIntegerTerm.get(bi[0].pow(i0.value));
				}
			}
			else if (tag == sin1) // ***************************************
			{
				double d0 = toDouble(args[0])[0];
				double res = Math.sin(d0);
				return new FloatTerm(res);
			}
			else if (tag == cos1) // ***************************************
			{
				double d0 = toDouble(args[0])[0];
				double res = Math.cos(d0);
				return new FloatTerm(res);
			}
			else if (tag == atan1) // ***************************************
			{
				double d0 = toDouble(args[0])[0];
				double res = Math.atan(d0);
				return new FloatTerm(res);
			}
			else if (tag == exp1) // ***************************************
			{
				double d0 = toDouble(args[0])[0];
				double res = Math.exp(d0);
				if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
				{
					floatOverflow();
				}
				return new FloatTerm(res);
			}
			else if (tag == log1) // ***************************************
			{
				double d0 = toDouble(args[0])[0];
				if (d0 <= 0)
				{
					undefined();
				}
				double res = Math.log(d0);
				if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
				{
					floatOverflow();
				}
				return new FloatTerm(res);
			}
			else if (tag == brshift2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
                                if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm && (!isUnbounded || ((IntegerTerm)arg1).value <= 32))
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
                                        IntegerTerm i1 = (IntegerTerm) arg1;
                                        int res;
                                        // Only the low 5 bits are used in right shifts on integers. If we want more, we need to use the BigInteger values below
                                        if (i1.value >= 32)
                                        {
                                                res = (i0.value > 0)?0 : -1;
                                        }
                                        else
                                        {
                                                res = i0.value >> i1.value;
                                        }
                                        return IntegerTerm.get(res);
				}
				else if ((arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm) &&
					 (arg1 instanceof IntegerTerm || arg1 instanceof BigIntegerTerm))
				{
					// Upgrade first to bigint
					BigInteger[] bi = toBigInteger(arg0);
					// If second is bigint, then complain
                                        if (arg1 instanceof BigIntegerTerm)
                                        {
                                                // BigInteger can only hold Integer.MAX_VALUE bits.
                                                // If we are shifting right by that much or more, the answer is always going to be 0
                                                return IntegerTerm.get(0);
                                        }
					return BigIntegerTerm.get(bi[0].shiftRight(((IntegerTerm)arg1).value));
				}
				else
				{
					// One of these must fail
					typeTestInt(arg0);
					typeTestInt(arg1);
				}
			}
			else if (tag == blshift2) // ***************************************
                        {
				Term arg0 = args[0];
                                Term arg1 = args[1];
                                // Note that if we are unbounded then do not even bother trying to do this as an integer shift
                                if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm && !isUnbounded)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					int res = i0.value << i1.value;
					return IntegerTerm.get(res);
				}
				else if ((arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm) &&
					 (arg1 instanceof IntegerTerm || arg1 instanceof BigIntegerTerm))
				{
					// Upgrade first to bigint
                                        BigInteger[] bi = toBigInteger(arg0);
                                        // If second is bigint, then complain
                                        if (arg1 instanceof BigIntegerTerm)
                                        {
                                                intOverflow();
                                        }
					return BigIntegerTerm.get(bi[0].shiftLeft(((IntegerTerm)arg1).value));
				}
				else
				{
					// One of these must fail
					typeTestInt(arg0);
					typeTestInt(arg1);
				}
			}
			else if (tag == band2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					int res = i0.value & i1.value;
					return IntegerTerm.get(res);
				}
				else if ((arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm) &&
					 (arg1 instanceof IntegerTerm || arg1 instanceof BigIntegerTerm))
				{
					// Upgrade both to bigint
					BigInteger[] bi = toBigInteger(arg0, arg1);
					return BigIntegerTerm.get(bi[0].and(bi[1]));
				}
				else
				{
					// One of these must fail
					typeTestInt(arg0);
					typeTestInt(arg1);
				}
			}
			else if (tag == bor2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					int res = i0.value | i1.value;
					return IntegerTerm.get(res);
				}
				else if ((arg0 instanceof IntegerTerm || arg0 instanceof BigIntegerTerm) &&
					 (arg1 instanceof IntegerTerm || arg1 instanceof BigIntegerTerm))
				{
					// Upgrade both to bigint
					BigInteger[] bi = toBigInteger(arg0, arg1);
					return BigIntegerTerm.get(bi[0].or(bi[1]));
				}
				else
				{
					// One of these must fail
					typeTestInt(arg0);
					typeTestInt(arg1);
				}
			}
			else if (tag == bnot1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					int res = ~i0.value;
					return IntegerTerm.get(res);
				}
				else if (arg0 instanceof BigIntegerTerm)
				{
					BigIntegerTerm bi0 = (BigIntegerTerm) arg0;
					return BigIntegerTerm.get(bi0.value.not());
				}
				else
				{
					PrologException.typeError(TermConstants.integerAtom, arg0);
				}
			}
			else if (tag == random1) // ***************************************
			{
				Term arg0 = args[0];
				if (!(arg0 instanceof IntegerTerm))
				{
					undefined();
				}
				IntegerTerm limit = (IntegerTerm) arg0;
				double rand;
				synchronized (random)
				{// avoid concurrency issues
					rand = random.nextDouble();// rand is uniformly distributed from 0 to
					// 1
				}
				int res = (int) (rand * limit.value);// scale it and cast it
				return IntegerTerm.get(res);
			}
			else if (tag == rdiv2 && isUnbounded) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (!(arg0 instanceof NumericTerm))
				{
					PrologException.typeError(TermConstants.numericAtom, arg0);
				}
				if (!(arg1 instanceof NumericTerm))
				{
					PrologException.typeError(TermConstants.numericAtom, arg1);
				}
				NumericTerm dividendTerm = (NumericTerm) arg0;
				NumericTerm divisorTerm = (NumericTerm) arg1;
				Rational dividend = null;
				Rational divisor = null;
				if (dividendTerm instanceof FloatTerm)
					dividend = Rational.get(((FloatTerm)dividendTerm).value);
				else if (dividendTerm instanceof IntegerTerm)
					dividend = new Rational(BigInteger.valueOf(((IntegerTerm)dividendTerm).value), BigInteger.valueOf(1));
				else if (dividendTerm instanceof BigIntegerTerm)
					dividend = new Rational(((BigIntegerTerm)dividendTerm).value, BigInteger.valueOf(1));
                                else if (dividendTerm instanceof RationalTerm)
					dividend = ((RationalTerm)dividendTerm).value;
				else
					undefined();

				if (divisorTerm instanceof FloatTerm)
					divisor = Rational.get(((FloatTerm)divisorTerm).value);
				else if (divisorTerm instanceof IntegerTerm)
					divisor = new Rational(BigInteger.valueOf(((IntegerTerm)divisorTerm).value), BigInteger.valueOf(1));
				else if (divisorTerm instanceof BigIntegerTerm)
					divisor = new Rational(((BigIntegerTerm)divisorTerm).value, BigInteger.valueOf(1));
				else if (divisorTerm instanceof RationalTerm)
					divisor = ((RationalTerm)divisorTerm).value;
				else
					undefined();
				return RationalTerm.rationalize(dividend, divisor);
                        }
                        else if (tag == rationalize1 && isUnbounded) // ***************************************
			{
				Term arg0 = args[0];
                                if (!(arg0 instanceof FloatTerm))
				{
                                        PrologException.typeError(TermConstants.floatAtom, arg0);
                                }
                                return RationalTerm.get(Rational.getApproximate(((FloatTerm)arg0).value));
                        }
                        else if (tag == rational1 && isUnbounded) // ***************************************
			{
				Term arg0 = args[0];
                                if (!(arg0 instanceof FloatTerm))
				{
                                        PrologException.typeError(TermConstants.floatAtom, arg0);
                                }
                                return RationalTerm.get(Rational.get(((FloatTerm)arg0).value));
                        }
                        else if (tag == gcd2 && !strictISO)
                        {
                                Term arg0 = args[0];
                                Term arg1 = args[1];
                                typeTestInt(arg0);
                                typeTestInt(arg1);
                                if (arg0 instanceof IntegerTerm &&  arg1 instanceof IntegerTerm)
                                {
                                        return IntegerTerm.get(gcd(((IntegerTerm)arg0).value, ((IntegerTerm)arg1).value));
                                }
                                BigInteger[] bi = toBigInteger(arg0, arg1);
                                return BigIntegerTerm.get(bi[0].gcd(bi[1]));
                        }
			else
			// ***************************************
			{
				PrologException.typeError(TermConstants.evaluableAtom, tag.getPredicateIndicator());
			}
		}
		else
		{
			if (term instanceof AtomTerm)
			{
				// because it is expected to be an expression
				term = new CompoundTerm(CompoundTermTag.divide2, term, IntegerTerm.int_0);
			}
			PrologException.typeError(TermConstants.evaluableAtom, term);
		}
		return null; // fake return
	}

        // Euclidean algorithm for GCD. java.lang.Math doesnt appear to have this
        private static int gcd(int a, int b)
        {
                return b==0 ? a : gcd(b, a%b);
        }

	/**
	 * Test the term for an integer term
	 * 
	 * @param term
	 * @throws PrologException
	 */
	protected static void typeTestInt(Term term) throws PrologException
	{
		if (term instanceof IntegerTerm)
		{
			return;
		}
		if (term instanceof BigIntegerTerm)
		{
			return;
		}
		if (term instanceof VariableTerm)
		{
			PrologException.instantiationError(term);
		}
		PrologException.typeError(TermConstants.integerAtom, term);
	}
}
