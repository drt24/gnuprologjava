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
import gnu.prolog.term.Term;
import gnu.prolog.vm.Evaluate;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;

/**
 * prolog code
 */
public class Predicate_greater_than extends ExecuteOnlyCode
{

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term arg0 = Evaluate.evaluate(args[0]);
		Term arg1 = Evaluate.evaluate(args[1]);
		if (arg0 instanceof IntegerTerm && arg1 instanceof IntegerTerm)
		{
			IntegerTerm i0 = (IntegerTerm) arg0;
			IntegerTerm i1 = (IntegerTerm) arg1;
			return i0.value > i1.value ? SUCCESS_LAST : FAIL;
		}
		else if (arg0 instanceof FloatTerm && arg1 instanceof IntegerTerm)
		{
			FloatTerm f0 = (FloatTerm) arg0;
			IntegerTerm i1 = (IntegerTerm) arg1;
			return f0.value > i1.value ? SUCCESS_LAST : FAIL;
		}
		else if (arg0 instanceof IntegerTerm && arg1 instanceof FloatTerm)
		{
			IntegerTerm i0 = (IntegerTerm) arg0;
			FloatTerm f1 = (FloatTerm) arg1;
			return i0.value > f1.value ? SUCCESS_LAST : FAIL;
		}
		else if (arg0 instanceof FloatTerm && arg1 instanceof FloatTerm)
		{
			FloatTerm f0 = (FloatTerm) arg0;
			FloatTerm f1 = (FloatTerm) arg1;
			return f0.value > f1.value ? SUCCESS_LAST : FAIL;
		}
		return FAIL;
	}
}
