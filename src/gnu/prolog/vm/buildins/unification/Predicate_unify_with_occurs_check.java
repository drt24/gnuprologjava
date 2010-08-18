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
package gnu.prolog.vm.buildins.unification;

import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;

import java.util.ArrayList;
import java.util.List;

/**
 * Unify two terms occur check
 */
public class Predicate_unify_with_occurs_check extends ExecuteOnlyCode
{
	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		List<Term> stack = new ArrayList<Term>(10);
		stack.add(args[0]);
		stack.add(args[1]);
		int rc = SUCCESS_LAST;
		unify_loop: while (stack.size() > 0)
		{
			Term t1 = stack.remove(stack.size() - 1);
			Term t2 = stack.remove(stack.size() - 1);
			if (t1 == t2)
			{
			}
			else if (t1 instanceof VariableTerm)
			{
				VariableTerm vt1 = (VariableTerm) t1;
				if (!occurCheck(vt1, t2))
				{
					rc = FAIL;
					break unify_loop;
				}
				interpreter.addVariableUndo(vt1);
				vt1.value = t2;
			}
			else if (t2 instanceof VariableTerm)
			{
				VariableTerm vt2 = (VariableTerm) t2;
				if (!occurCheck(vt2, t1))
				{
					rc = FAIL;
					break unify_loop;
				}
				interpreter.addVariableUndo(vt2);
				vt2.value = t1;
			}
			else if (t1.getClass() != t2.getClass())
			{
				rc = FAIL;
				break unify_loop;
			}
			else if (t1 instanceof CompoundTerm && t2 instanceof CompoundTerm)
			{
				CompoundTerm ct1 = (CompoundTerm) t1;
				CompoundTerm ct2 = (CompoundTerm) t2;
				if (ct1.tag != ct2.tag)
				{
					rc = FAIL;
					break unify_loop;
				}
				Term args1[] = ct1.args;
				Term args2[] = ct2.args;
				for (int i = args2.length - 1; i >= 0; i--)
				{
					stack.add(args1[i].dereference());
					stack.add(args2[i].dereference());
				}
			}
			else if (t1 instanceof FloatTerm && t2 instanceof FloatTerm)
			{
				FloatTerm ct1 = (FloatTerm) t1;
				FloatTerm ct2 = (FloatTerm) t2;
				if (ct1.value != ct2.value)
				{
					rc = FAIL;
					break unify_loop;
				}
			}
			else if (t1 instanceof IntegerTerm && t2 instanceof IntegerTerm)
			{
				IntegerTerm ct1 = (IntegerTerm) t1;
				IntegerTerm ct2 = (IntegerTerm) t2;
				if (ct1.value != ct2.value)
				{
					rc = FAIL;
					break unify_loop;
				}
			}
			else if (t1 instanceof JavaObjectTerm && t2 instanceof JavaObjectTerm)
			{
				JavaObjectTerm ct1 = (JavaObjectTerm) t1;
				JavaObjectTerm ct2 = (JavaObjectTerm) t2;
				if (ct1.value != ct2.value)
				{
					rc = FAIL;
					break unify_loop;
				}
			}
			else
			{
				rc = PrologCode.FAIL;
				break unify_loop;
			}
		}
		return rc;
	}

	/**
	 * Perform occur check on variable
	 * 
	 * @param variable
	 * @param term
	 * 
	 * @return true if term does not contains variable
	 */
	public static boolean occurCheck(VariableTerm variable, Term term)
	{
		if (variable == term)
		{
			return false;
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			Term args[] = ct.args;
			for (int i = args.length - 1; i >= 0; i--)
			{
				if (!occurCheck(variable, args[i].dereference()))
				{
					return false;
				}
			}
		}
		return true;
	}
}
