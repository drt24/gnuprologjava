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
package gnu.prolog.vm.buildins.allsolutions;

import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;
import gnu.prolog.vm.interpreter.Predicate_call;

import java.util.ArrayList;
import java.util.List;

/**
 * prolog code
 */
public class Predicate_findall extends ExecuteOnlyCode
{
	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		List<Term> list = new ArrayList<Term>();
		checkList(args[2]);
		int rc = findall(interpreter, backtrackMode, args[0], args[1], list);
		if (rc == SUCCESS_LAST)
		{
			return interpreter.unify(args[2], CompoundTerm.getList(list));
		}
		return FAIL;
	}

	/**
	 * 
	 * @param interpreter
	 *          interpreter in which context code is executed
	 * @param backtrackMode
	 *          true if predicate is called on backtracking and false otherwise
	 * @param template
	 * @param goal
	 * @param list
	 * @return either {@link #SUCCESS_LAST} or {@link #FAIL}
	 * @throws PrologException
	 */
	public static int findall(Interpreter interpreter, boolean backtrackMode, Term template, Term goal, List<Term> list)
			throws PrologException
	{
		int startUndoPosition = interpreter.getUndoPosition();
		BacktrackInfo startBi = interpreter.peekBacktrackInfo();
		try
		{
			try
			{
				boolean callBacktrackMode = false;
				int rc;
				do
				{
					rc = Predicate_call.staticExecute(interpreter, callBacktrackMode, goal);
					callBacktrackMode = true;
					if (rc != FAIL)
					{
						list.add((Term) template.clone());
					}
				} while (rc == SUCCESS);
				if (rc == SUCCESS_LAST)
				{
					interpreter.undo(startUndoPosition);
				}
				return SUCCESS_LAST;
			}
			catch (RuntimeException rex)
			{
				PrologException.systemError(rex);
				return FAIL; // fake return
			}
		}
		catch (PrologException ex)
		{
			interpreter.popBacktrackInfoUntil(startBi);
			interpreter.undo(startUndoPosition);
			throw ex;
		}
	}

	/**
	 * Check that list is a valid Prolog list (including an uninstantiated
	 * variable)
	 * 
	 * @param list
	 *          the term to check to see if it is a list
	 * @throws PrologException
	 *           for the various errors when it is not a list.
	 */
	public static void checkList(Term list) throws PrologException
	{
		Term exArg = list;
		while (list != TermConstants.emptyListAtom)
		{
			if (list instanceof VariableTerm)
			{
				return;
			}
			if (!(list instanceof CompoundTerm))
			{
				PrologException.typeError(TermConstants.listAtom, exArg);
			}
			CompoundTerm ct = (CompoundTerm) list;
			if (ct.tag != TermConstants.listTag)
			{
				PrologException.typeError(TermConstants.listAtom, exArg);
			}
			list = ct.args[1].dereference();
		}
	}
}
