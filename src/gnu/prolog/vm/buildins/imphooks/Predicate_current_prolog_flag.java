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
package gnu.prolog.vm.buildins.imphooks;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.Iterator;
import java.util.Map;

/**
 * prolog code
 */
public class Predicate_current_prolog_flag extends ExecuteOnlyCode
{
	private static class CurrentPrologFlagBacktrackInfo extends BacktrackInfo
	{
		CurrentPrologFlagBacktrackInfo()
		{
			super(-1, -1);
		}

		Map<AtomTerm, Term> map;
		Iterator<AtomTerm> keys;
		int startUndoPosition;
		Term flag;
		Term value;
	}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		if (backtrackMode)
		{
			CurrentPrologFlagBacktrackInfo bi = (CurrentPrologFlagBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			Term flag = args[0];
			Term value = args[1];
			if (flag instanceof AtomTerm)
			{
				Term val = interpreter.getEnvironment().getPrologFlag((AtomTerm) flag);
				if (val == null)
				{
					PrologException.domainError(TermConstants.prologFlagAtom, flag);
					return FAIL;// fake return.
				}
				return interpreter.unify(value, val.dereference());
			}
			else if (!(flag instanceof VariableTerm))
			{
				PrologException.typeError(TermConstants.atomAtom, flag);
			}
			CurrentPrologFlagBacktrackInfo bi = new CurrentPrologFlagBacktrackInfo();
			bi.map = interpreter.getEnvironment().getPrologFlags();
			bi.keys = bi.map.keySet().iterator();
			bi.startUndoPosition = interpreter.getUndoPosition();
			bi.flag = flag;
			bi.value = value;
			return nextSolution(interpreter, bi);
		}
	}

	private int nextSolution(Interpreter interpreter, CurrentPrologFlagBacktrackInfo bi) throws PrologException
	{
		while (bi.keys.hasNext())
		{
			AtomTerm f = bi.keys.next();
			Term v = bi.map.get(f);
			int rc = interpreter.simpleUnify(f, bi.flag);
			if (rc == FAIL)
			{
				interpreter.undo(bi.startUndoPosition);
				continue;
			}
			rc = interpreter.simpleUnify(v, bi.value);
			if (rc == FAIL)
			{
				interpreter.undo(bi.startUndoPosition);
				continue;
			}
			interpreter.pushBacktrackInfo(bi);
			return SUCCESS;
		}
		return FAIL;
	}
}
