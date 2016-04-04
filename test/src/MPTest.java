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

package gnu.prolog.test;

import gnu.prolog.term.Term;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.BigIntegerTerm;
import gnu.prolog.term.RationalTerm;
import gnu.prolog.term.Rational;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.Evaluate;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.TermConstants;
import gnu.prolog.vm.PrologException;
import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.vm.PrologCode.RC;

import java.math.BigInteger;
import org.junit.Test;
import org.junit.Ignore;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;



public class MPTest
{
        private static Term intOverflowError = new CompoundTerm(PrologException.errorTag, new CompoundTerm(PrologException.evaluationErrorTag, TermConstants.intOverflowAtom), PrologException.errorAtom);
        private static AtomTerm atomBounded = AtomTerm.get("bounded");
        private static AtomTerm atomUnbounded = AtomTerm.get("unbounded");
        public Term callPredicate(String name, Term... args) throws PrologException
        {
                VariableTerm result = new VariableTerm("Result");
                Term[] goalArgs = new Term[args.length+1];
                System.arraycopy(args, 0, goalArgs, 0, args.length);
                goalArgs[args.length] = result;
                Term goalTerm = new CompoundTerm(name, goalArgs);
                RC rc = createInterpreter().runOnce(goalTerm);
                assertEquals(RC.SUCCESS_LAST, rc);
                return result.dereference();
        }

        public Interpreter createInterpreter()
        {
                Environment env = new Environment();
		Interpreter interpreter = env.createInterpreter();
                env.ensureLoaded(AtomTerm.get("mp.pl"));
		env.runInitialization(interpreter);
		for (PrologTextLoaderError error : env.getLoadingErrors())
		{
                        System.err.println("Prolog compile error: " + error);
                }
                assertEquals(0, env.getLoadingErrors().size());
                return interpreter;
        }

        @Test
        public void testBigIntegerParsing() throws PrologException
        {
                Term result = callPredicate("create_biginteger");
                assertThat(result, instanceOf(BigIntegerTerm.class));
                assertThat(((BigIntegerTerm)result).value, equalTo(new BigInteger("1099511627776")));
        }

        @Test
        public void testBigIntegerParsingNotGarbage() throws PrologException
        {
                Term result = callPredicate("create_biginteger");
                assertThat(result, instanceOf(BigIntegerTerm.class));
                assertThat(((BigIntegerTerm)result).value, not(equalTo(new BigInteger("1099511627777"))));
        }

        @Test
        public void testBigIntegerCreatedByExp() throws PrologException
        {
                Term result = callPredicate("create_biginteger_by_exp");
                assertThat(result, instanceOf(BigIntegerTerm.class));
                assertThat(((BigIntegerTerm)result).value, equalTo(new BigInteger("2199023255552")));
        }

        @Test
        public void testRationalCreatedByRdiv() throws PrologException
        {
                Term result = callPredicate("create_rational_by_rdiv");
                assertThat(result, instanceOf(RationalTerm.class));
                assertThat(((RationalTerm)result).value, equalTo(new Rational(BigInteger.valueOf(2), BigInteger.valueOf(3))));
        }

        @Test
        public void testRationalCanonicalization1() throws PrologException
        {
                Term result = callPredicate("create_canonical_rational_1");
                assertThat(result, instanceOf(RationalTerm.class));
                assertThat(((RationalTerm)result).value, equalTo(new Rational(BigInteger.valueOf(2), BigInteger.valueOf(3))));
        }

        @Test
        public void testRationalCanonicalization2() throws PrologException
        {
                Term result = callPredicate("create_canonical_rational_2");
                assertThat(result, instanceOf(RationalTerm.class));
                assertThat(((RationalTerm)result).value, equalTo(new Rational(BigInteger.valueOf(-2), BigInteger.valueOf(3))));
        }

        @Test
        public void testRationalCanonicalization3() throws PrologException
        {
                Term result = callPredicate("create_canonical_rational_3");
                assertThat(result, instanceOf(RationalTerm.class));
                assertThat(((RationalTerm)result).value, equalTo(new Rational(BigInteger.valueOf(2), BigInteger.valueOf(3))));
        }


        /* Now run all the SWI Prolog tests */
        // div
        @Test
        public void testDivMod() throws PrologException
        {
                Term result = callPredicate("test_div_mod");
        }

        @Test
        public void testDivMinint() throws PrologException
        {
                Term result = callPredicate("test_div_minint");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(-2147483648));
        }

        // gdiv
        @Test
        public void testGdivMinint() throws PrologException
        {
                if (Evaluate.isUnbounded)
                {
                        Term result = callPredicate("test_gdiv_minint");
                        assertThat(result, instanceOf(BigIntegerTerm.class));
                        assertThat(((BigIntegerTerm)result).value, equalTo(new BigInteger("9223372036854775808")));
                }
                else
                {
                        try
                        {
                                Term result = callPredicate("test_gdiv_minint");
                        }
                        catch(PrologException pe)
                        {
                                assertThat(pe.getTerm(), equalTo(intOverflowError));
                        }
                }
        }

        // rem
        @Test
        public void testRemSmall() throws PrologException
        {
                Term result = callPredicate("test_rem_small");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(2));
        }

        @Test
        public void testRemSmallDivneg() throws PrologException
        {
                Term result = callPredicate("test_rem_small_divneg");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(2));
        }

        @Test
        public void testRemSmallNeg() throws PrologException
        {
                Term result = callPredicate("test_rem_small_neg");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(-2));
        }

        @Test
        public void testRemBig() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_rem_big");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(6));
        }

        @Test
        public void testRemBigNeg() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_rem_big_neg");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(-6));
        }

        @Test
        public void testRemExhaust() throws PrologException
        {
                Term result = callPredicate("test_rem_exhaust");
        }

        @Test
        public void testRemBig2() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_rem_big2");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(-3));
        }

        @Test
        public void testRemAllq() throws PrologException
        {
                Term result = callPredicate("test_rem_allq");
        }

        // mod
        @Test
        public void testModSmall() throws PrologException
        {
                Term result = callPredicate("test_mod_small");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(2));
        }

        @Test
        public void testModSmallDivNeg() throws PrologException
        {
                Term result = callPredicate("test_mod_small_divneg");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(-1));
        }

        @Test
        public void testModSmallNeg() throws PrologException
        {
                Term result = callPredicate("test_mod_small_neg");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(1));
        }

        @Test
        public void testModBig() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_mod_big");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(6));
        }

        @Test
        public void testModBigNeg() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_mod_big_neg");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(4));
        }

        @Test
        public void testModExhaust() throws PrologException
        {
                Term result = callPredicate("test_mod_exhaust");
        }

        @Test
        public void testModBig2() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_mod_big2");
                assertThat(result, instanceOf(BigIntegerTerm.class));
                assertThat(((BigIntegerTerm)result).value, equalTo(new BigInteger("99999999999999999999999999999999999999999999999997")));
        }

        // shift
        @Test
        public void testShiftRightLarge1() throws PrologException
        {
                Term result = callPredicate("test_shift_right_large_1");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(0));
        }

        @Test
        public void testShiftRightLarge2() throws PrologException
        {
                Term result = callPredicate("test_shift_right_large_2");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(-1));
        }

        @Test
        public void testShiftRightLarge3() throws PrologException
        {
                Term result = callPredicate("test_shift_right_large_3");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(0));
        }

        @Test
        public void testShiftRightLarge4() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_shift_right_large_4");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(0));
        }

        @Test
        public void testShiftLeftLarge() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_shift_left_large");
                assertThat(result, instanceOf(BigIntegerTerm.class));
                assertThat(((BigIntegerTerm)result).value, equalTo(new BigInteger("-18446744073709551616")));
        }

        // gcd
        @Test
        public void testGcd() throws PrologException
        {
                Term result = callPredicate("test_gcd");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(4));
        }

        @Test
        public void testGcd2() throws PrologException
        {
                Term result = callPredicate("test_gcd_2");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(4));
        }

        // hyperbolic (not implemented!)
        /*
        @Test
        public void testHyperbolicSinh() throws PrologException
        {
                Term result = callPredicate("test_hyperbolic_sinh");
                assertThat(result, instanceOf(FloatTerm.class));
                assertThat(((FloatTerm)result).value, equalTo(1.175));
        }

        @Test
        public void testHyperbolicCosh() throws PrologException
        {
                Term result = callPredicate("test_hyperbolic_cosh");
                assertThat(result, instanceOf(FloatTerm.class));
                assertThat(((FloatTerm)result).value, equalTo(1.543));
        }

        @Test
        public void testHyperbolicTanh() throws PrologException
        {
                Term result = callPredicate("test_hyperbolic_tanh");
                assertThat(result, instanceOf(FloatTerm.class));
                assertThat(((FloatTerm)result).value, equalTo(0.762));
        }

        @Test
        public void testHyperbolicASinh() throws PrologException
        {
                Term result = callPredicate("test_hyperbolic_asinh");
                assertThat(result, instanceOf(FloatTerm.class));
                assertThat(((FloatTerm)result).value, equalTo(1.000));
        }

        @Test
        public void testHyperbolicACosh() throws PrologException
        {
                Term result = callPredicate("test_hyperbolic_acosh");
                assertThat(result, instanceOf(FloatTerm.class));
                assertThat(((FloatTerm)result).value, equalTo(1.000));
        }

        @Test
        public void testHyperbolicATanh() throws PrologException
        {
                Term result = callPredicate("test_hyperbolic_atanh");
                assertThat(result, instanceOf(FloatTerm.class));
                assertThat(((FloatTerm)result).value, equalTo(1.000));
        }
        */
        // rationalize
        @Test
        public void testRationalize() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_rationalize");
                assertThat(result, instanceOf(RationalTerm.class));
                assertThat(((RationalTerm)result).value, equalTo(new Rational(BigInteger.valueOf(51), BigInteger.valueOf(10))));
        }

        @Test
        public void testRational() throws PrologException
        {
                assumeThat(Evaluate.isUnbounded, is(true));
                Term result = callPredicate("test_rational");
                assertThat(result, instanceOf(RationalTerm.class));
                assertThat(((RationalTerm)result).value, equalTo(new Rational(new BigInteger("2871044762448691"), new BigInteger("562949953421312"))));
        }


        // minint
        @Test
        public void testMinInt() throws PrologException
        {
                Term result = callPredicate("minint_tests", Evaluate.isUnbounded?atomUnbounded:atomBounded);
        }

        // minint_promotion
        @Test
        public void testMinIntPromotion() throws PrologException
        {
                Term result = callPredicate("minint_promotion_tests", Evaluate.isUnbounded?atomUnbounded:atomBounded);
        }

        // maxint
        @Test
        public void testMaxInt() throws PrologException
        {
                Term result = callPredicate("maxint_tests", Evaluate.isUnbounded?atomUnbounded:atomBounded);
        }

        // maxint_promotion
        @Test
        public void testMaxIntPromotion() throws PrologException
        {
                Term result = callPredicate("maxint_promotion_tests", Evaluate.isUnbounded?atomUnbounded:atomBounded);
        }

        // float_zero (not implemented)

        // float_special
        @Test
        public void testFloatSpecial() throws PrologException
        {
                Term result = callPredicate("test_float_zero_cmp");
        }

        // arith_misc (not implemented)
        /*
        @Test
        public void testArithMisc() throws PrologException
        {
                Term result = callPredicate("test_arith_misc_string");
        }
        */

}
