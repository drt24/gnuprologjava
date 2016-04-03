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
package gnu.prolog.vm.buildins.arithmetics;

import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.BigIntegerTerm;
import gnu.prolog.term.RationalTerm;
import gnu.prolog.term.Rational;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Evaluate;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import java.math.BigInteger;

/**
 * prolog code
 */
public class Predicate_not_equal extends ExecuteOnlyCode
{

	@Override
	public RC execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term arg0 = Evaluate.evaluate(args[0]);
		Term arg1 = Evaluate.evaluate(args[1]);
		int targetType = Evaluate.commonType(arg0, arg1);
		switch(targetType)
		{
		case 0: // int, int
		{
			IntegerTerm i0 = (IntegerTerm) arg0;
			IntegerTerm i1 = (IntegerTerm) arg1;
			return i0.value == i1.value ? RC.FAIL : RC.SUCCESS_LAST;
		}
		case 1: // bigint, bigint
		{
			BigInteger[] bi = Evaluate.toBigInteger(arg0, arg1);
			return bi[0].equals(bi[1]) ? RC.FAIL : RC.SUCCESS_LAST;
		}
		case 2: // float, float
		{
			double[] f = Evaluate.toDouble(arg0, arg1);
			return f[0] == f[1] ? RC.FAIL : RC.SUCCESS_LAST;
		}
		case 3: // rational, rational
		{
			Rational[] r = Evaluate.toRational(arg0, arg1);
			return r[0].equals(r[1]) ? RC.FAIL : RC.SUCCESS_LAST;
		}
		}
		return RC.FAIL;
	}
}
