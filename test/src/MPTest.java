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
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.PrologException;
import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.vm.PrologCode.RC;

import java.math.BigInteger;
import org.junit.Test;
import org.junit.Ignore;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;



public class MPTest
{
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

        @Test
        public void shiftRightLarge() throws PrologException
        {
                Term result = callPredicate("shift_right_large");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(0));
        }

        @Test
        public void shiftLeftLarge() throws PrologException
        {
                Term result = callPredicate("shift_left_large");
                assertThat(result, instanceOf(BigIntegerTerm.class));
                assertThat(((BigIntegerTerm)result).value, equalTo(new BigInteger("-18446744073709551616")));
        }

        @Test
        public void largeMod() throws PrologException
        {
                Term result = callPredicate("large_mod");
                assertThat(result, instanceOf(BigIntegerTerm.class));
                assertThat(((BigIntegerTerm)result).value, equalTo(new BigInteger("99999999999999999999999999999999999999999999999997")));
        }

        @Test
        public void minInt() throws PrologException
        {
                Term result = callPredicate("min_int");
                assertThat(result, instanceOf(BigIntegerTerm.class));
                assertThat(((BigIntegerTerm)result).value, equalTo(new BigInteger("9223372036854775808")));
        }

        @Test
        public void big_int_rem() throws PrologException
        {
                Term result = callPredicate("big_int_rem");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(6));
        }

        @Test
        public void big_int_neg_rem() throws PrologException
        {
                Term result = callPredicate("big_int_neg_rem");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(-6));
        }

        @Test
        public void test_mod() throws PrologException
        {
                callPredicate("test_mod");
        }

        @Test
        public void min_int_div() throws PrologException
        {
                Term result = callPredicate("minint");
                assertThat(result, instanceOf(IntegerTerm.class));
                assertThat(((IntegerTerm)result).value, equalTo(-2147483648));
        }

}
