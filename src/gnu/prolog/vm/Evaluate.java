/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
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
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;

import java.util.Random;

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

	private static Pair<Double, Double> toDouble(Term arg0, Term arg1)
	{
		double d0 = 0;
		double d1 = 0;
		if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
		{
			IntegerTerm i0 = (IntegerTerm) arg0;
			IntegerTerm i1 = (IntegerTerm) arg1;
			d0 = i0.value;
			d1 = i1.value;
		}
		else if (arg0 instanceof FloatTerm && arg1 instanceof IntegerTerm)
		{
			FloatTerm f0 = (FloatTerm) arg0;
			IntegerTerm i1 = (IntegerTerm) arg1;
			d0 = f0.value;
			d1 = i1.value;
		}
		else if (arg0 instanceof IntegerTerm && arg1 instanceof FloatTerm)
		{
			IntegerTerm i0 = (IntegerTerm) arg0;
			FloatTerm f1 = (FloatTerm) arg1;
			d0 = i0.value;
			d1 = f1.value;
		}
		else if (arg0 instanceof FloatTerm && arg1 instanceof FloatTerm)
		{
			FloatTerm f0 = (FloatTerm) arg0;
			FloatTerm f1 = (FloatTerm) arg1;
			d0 = f0.value;
			d1 = f1.value;
		}
		return new Pair<Double, Double>(d0, d1);
	}

	public static Term evaluate(Term term) throws PrologException
	{
		if (term instanceof FloatTerm)
		{
			return term;
		}
		else if (term instanceof IntegerTerm)
		{
			return term;
		}
		else if (term instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			CompoundTermTag tag = ct.tag;
			int i, arity = tag.arity;
			Term sargs[] = ct.args;
			Term args[] = new Term[arity];
			for (i = 0; i < arity; i++)
			{
				args[i] = evaluate(sargs[i].dereference());
			}
			if (tag == add2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					long res = (long) i0.value + (long) i1.value;
					if (res > Integer.MAX_VALUE || res < Integer.MIN_VALUE)
					{
						intOverflow();
					}
					return IntegerTerm.get((int) res);
				}
				else if (arg0 instanceof FloatTerm && arg1 instanceof IntegerTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					double res = f0.value + i1.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				else if (arg0 instanceof IntegerTerm && arg1 instanceof FloatTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					FloatTerm f1 = (FloatTerm) arg1;
					double res = i0.value + f1.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				else if (arg0 instanceof FloatTerm && arg1 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					FloatTerm f1 = (FloatTerm) arg1;
					double res = f0.value + f1.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
			}
			else if (tag == sub2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					long res = (long) i0.value - (long) i1.value;
					if (res > Integer.MAX_VALUE || res < Integer.MIN_VALUE)
					{
						intOverflow();
					}
					return IntegerTerm.get((int) res);
				}
				else if (arg0 instanceof FloatTerm && arg1 instanceof IntegerTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					double res = f0.value - i1.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				else if (arg0 instanceof IntegerTerm && arg1 instanceof FloatTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					FloatTerm f1 = (FloatTerm) arg1;
					double res = i0.value - f1.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				else if (arg0 instanceof FloatTerm && arg1 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					FloatTerm f1 = (FloatTerm) arg1;
					double res = f0.value - f1.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
			}
			else if (tag == mul2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					long res = (long) i0.value * (long) i1.value;
					if (res > Integer.MAX_VALUE || res < Integer.MIN_VALUE)
					{
						intOverflow();
					}
					return IntegerTerm.get((int) res);
				}
				else if (arg0 instanceof FloatTerm && arg1 instanceof IntegerTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					IntegerTerm i1 = (IntegerTerm) arg1;
					double res = f0.value * i1.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				else if (arg0 instanceof IntegerTerm && arg1 instanceof FloatTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					FloatTerm f1 = (FloatTerm) arg1;
					double res = i0.value * f1.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
				else if (arg0 instanceof FloatTerm && arg1 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					FloatTerm f1 = (FloatTerm) arg1;
					double res = f0.value * f1.value;
					if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
					{
						floatOverflow();
					}
					return new FloatTerm(res);
				}
			}
			else if (tag == intdiv2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (!(arg0 instanceof IntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg0);
				}
				if (!(arg1 instanceof IntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg1);
				}
				IntegerTerm i0 = (IntegerTerm) arg0;
				IntegerTerm i1 = (IntegerTerm) arg1;
				if (i1.value == 0)
				{
					zeroDivizor();
				}
				int res = i0.value / i1.value;
				return IntegerTerm.get(res);
			}
			else if (tag == div2) // ***************************************
			{
				Pair<Double, Double> doubles = toDouble(args[0], args[1]);
				double d0 = doubles.left;
				double d1 = doubles.right;

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
			else if (tag == rem2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (!(arg0 instanceof IntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg0);
				}
				if (!(arg1 instanceof IntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg1);
				}
				IntegerTerm i0 = (IntegerTerm) arg0;
				IntegerTerm i1 = (IntegerTerm) arg1;
				if (i1.value == 0)
				{
					zeroDivizor();
				}
				int res = i0.value % i1.value;
				return IntegerTerm.get(res);
			}
			else if (tag == mod2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				if (!(arg0 instanceof IntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg0);
				}
				if (!(arg1 instanceof IntegerTerm))
				{
					PrologException.typeError(TermConstants.integerAtom, arg1);
				}
				IntegerTerm i0 = (IntegerTerm) arg0;
				IntegerTerm i1 = (IntegerTerm) arg1;
				if (i1.value == 0)
				{
					zeroDivizor();
				}
				int res = i0.value - (int) Math.floor((double) i0.value / i1.value) * i1.value;
				return IntegerTerm.get(res);
			}
			else if (tag == neg1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					if (i0.value == Integer.MIN_VALUE)
					{
						intOverflow();
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
			}
			else if (tag == abs1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					if (i0.value == Integer.MIN_VALUE)
					{
						intOverflow();
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
			}
			else if (tag == sqrt1) // ***************************************
			{
				double d0 = 0;
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					d0 = i0.value;
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					d0 = f0.value;
				}
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
					return IntegerTerm.get(i0.value >= 0 ? 1 : -1);
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					double res = f0.value >= 0 ? 1 : -1;
					return new FloatTerm(res);
				}
			}
			else if (tag == intpart1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					PrologException.typeError(floatAtom, arg0);
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					int sign = f0.value >= 0 ? 1 : -1;
					double res = sign * Math.floor(Math.abs(f0.value));
					return new FloatTerm(res);
				}
			}
			else if (tag == fractpart1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					PrologException.typeError(floatAtom, arg0);
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					int sign = f0.value >= 0 ? 1 : -1;
					double res = f0.value - sign * Math.floor(Math.abs(f0.value));
					return new FloatTerm(res);
				}
			}
			else if (tag == float1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					return new FloatTerm(i0.value);
				}
				else if (arg0 instanceof FloatTerm)
				{
					return arg0;
				}
			}
			else if (tag == floor1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					PrologException.typeError(floatAtom, arg0);
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					double res = Math.floor(f0.value);
					if (res < Integer.MIN_VALUE || res > Integer.MAX_VALUE)
					{
						intOverflow();
					}
					return IntegerTerm.get((int) Math.round(res));
				}
			}
			else if (tag == truncate1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					PrologException.typeError(floatAtom, arg0);
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					int sign = f0.value >= 0 ? 1 : -1;
					double res = sign * Math.floor(Math.abs(f0.value));
					if (res < Integer.MIN_VALUE || res > Integer.MAX_VALUE)
					{
						intOverflow();
					}
					return IntegerTerm.get((int) Math.round(res));
				}
			}
			else if (tag == round1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					PrologException.typeError(floatAtom, arg0);
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					double res = Math.floor(f0.value + 0.5);
					if (res < Integer.MIN_VALUE || res > Integer.MAX_VALUE)
					{
						intOverflow();
					}
					return IntegerTerm.get((int) Math.round(res));
				}
			}
			else if (tag == ceiling1) // ***************************************
			{
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					PrologException.typeError(floatAtom, arg0);
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					double res = -Math.floor(-f0.value);
					if (res < Integer.MIN_VALUE || res > Integer.MAX_VALUE)
					{
						intOverflow();
					}
					return IntegerTerm.get((int) Math.round(res));
				}
			}
			else if (tag == power2) // ***************************************
			{
				Pair<Double, Double> doubles = toDouble(args[0], args[1]);
				double d0 = doubles.left;
				double d1 = doubles.right;

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
			else if (tag == sin1) // ***************************************
			{
				double d0 = 0;
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					d0 = i0.value;
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					d0 = f0.value;
				}
				double res = Math.sin(d0);
				return new FloatTerm(res);
			}
			else if (tag == cos1) // ***************************************
			{
				double d0 = 0;
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					d0 = i0.value;
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					d0 = f0.value;
				}
				double res = Math.cos(d0);
				return new FloatTerm(res);
			}
			else if (tag == atan1) // ***************************************
			{
				double d0 = 0;
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					d0 = i0.value;
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					d0 = f0.value;
				}
				double res = Math.atan(d0);
				return new FloatTerm(res);
			}
			else if (tag == exp1) // ***************************************
			{
				double d0 = 0;
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					d0 = i0.value;
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					d0 = f0.value;
				}
				double res = Math.exp(d0);
				if (res == Double.POSITIVE_INFINITY || res == Double.NEGATIVE_INFINITY)
				{
					floatOverflow();
				}
				return new FloatTerm(res);
			}
			else if (tag == log1) // ***************************************
			{
				double d0 = 0;
				Term arg0 = args[0];
				if (arg0 instanceof IntegerTerm)
				{
					IntegerTerm i0 = (IntegerTerm) arg0;
					d0 = i0.value;
				}
				else if (arg0 instanceof FloatTerm)
				{
					FloatTerm f0 = (FloatTerm) arg0;
					d0 = f0.value;
				}
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
				typeTestInt(arg0);
				typeTestInt(arg1);
				IntegerTerm i0 = (IntegerTerm) arg0;
				IntegerTerm i1 = (IntegerTerm) arg1;
				int res = i0.value >> i1.value;
				return IntegerTerm.get(res);
			}
			else if (tag == blshift2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				typeTestInt(arg0);
				typeTestInt(arg1);
				IntegerTerm i0 = (IntegerTerm) arg0;
				IntegerTerm i1 = (IntegerTerm) arg1;
				int res = i0.value << i1.value;
				return IntegerTerm.get(res);
			}
			else if (tag == band2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				typeTestInt(arg0);
				typeTestInt(arg1);
				IntegerTerm i0 = (IntegerTerm) arg0;
				IntegerTerm i1 = (IntegerTerm) arg1;
				int res = i0.value & i1.value;
				return IntegerTerm.get(res);
			}
			else if (tag == bor2) // ***************************************
			{
				Term arg0 = args[0];
				Term arg1 = args[1];
				typeTestInt(arg0);
				typeTestInt(arg1);
				IntegerTerm i0 = (IntegerTerm) arg0;
				IntegerTerm i1 = (IntegerTerm) arg1;
				int res = i0.value | i1.value;
				return IntegerTerm.get(res);
			}
			else if (tag == bnot1) // ***************************************
			{
				Term arg0 = args[0];
				typeTestInt(arg0);
				IntegerTerm i0 = (IntegerTerm) arg0;
				int res = ~i0.value;
				return IntegerTerm.get(res);
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
		if (term instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		PrologException.typeError(TermConstants.integerAtom, term);
	}
}
