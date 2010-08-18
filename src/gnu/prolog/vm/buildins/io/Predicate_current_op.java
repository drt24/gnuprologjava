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
package gnu.prolog.vm.buildins.io;

import gnu.prolog.io.Operator;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * prolog code
 */
public class Predicate_current_op extends ExecuteOnlyCode
{

	private static class CurrentOpBacktrackInfo extends BacktrackInfo
	{
		CurrentOpBacktrackInfo()
		{
			super(-1, -1);
		}

		int startUndoPosition;

		Iterator<AtomTerm> ops;
		Iterator<AtomTerm> specifiers;
		Iterator<IntegerTerm> priorities;

		Term op;
		Term specifier;
		Term priority;
	}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		if (backtrackMode)
		{
			CurrentOpBacktrackInfo bi = (CurrentOpBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			Term op = args[2];
			Term specifier = args[1];
			Term priority = args[0];
			// validate args
			if (!(op instanceof AtomTerm || op instanceof VariableTerm))
			{
				PrologException.typeError(TermConstants.atomAtom, op);
			}
			if (!(specifier instanceof VariableTerm || specifier == TermConstants.xfxAtom
					|| specifier == TermConstants.xfyAtom || specifier == TermConstants.yfxAtom
					|| specifier == TermConstants.fxAtom || specifier == TermConstants.fyAtom
					|| specifier == TermConstants.xfAtom || specifier == TermConstants.yfAtom))
			{
				PrologException.domainError(TermConstants.operatorSpecifierAtom, specifier);
			}
			if (priority instanceof VariableTerm)
			{
			}
			else if (priority instanceof IntegerTerm)
			{
				IntegerTerm tt = (IntegerTerm) priority;
				if (tt.value <= 0 || 1200 < tt.value)
				{
					PrologException.domainError(TermConstants.operatorPriorityAtom, priority);
				}
			}
			else
			{
				PrologException.domainError(TermConstants.operatorPriorityAtom, priority);
			}

			// prepare and exec
			List<AtomTerm> ops = new ArrayList<AtomTerm>();
			List<AtomTerm> specifiers = new ArrayList<AtomTerm>();
			List<IntegerTerm> priorities = new ArrayList<IntegerTerm>();

			Iterator<Operator> i = interpreter.getEnvironment().getOperatorSet().getOperators().iterator();
			while (i.hasNext())
			{
				Operator o = i.next();
				ops.add(o.tag.functor);
				priorities.add(IntegerTerm.get(o.priority));
				AtomTerm a = o.specifier.getAtom();
				specifiers.add(a);
			}
			CurrentOpBacktrackInfo bi = new CurrentOpBacktrackInfo();
			bi.startUndoPosition = interpreter.getUndoPosition();
			bi.ops = ops.iterator();
			bi.specifiers = specifiers.iterator();
			bi.priorities = priorities.iterator();
			bi.op = op;
			bi.specifier = specifier;
			bi.priority = priority;
			return nextSolution(interpreter, bi);
		}
	}

	private static int nextSolution(Interpreter interpreter, CurrentOpBacktrackInfo bi) throws PrologException
	{
		try
		{
			while (bi.ops.hasNext())
			{
				Term op = bi.ops.next();
				Term specifier = bi.specifiers.next();
				Term priority = bi.priorities.next();
				if (interpreter.simpleUnify(op, bi.op) == SUCCESS_LAST
						&& interpreter.simpleUnify(specifier, bi.specifier) == SUCCESS_LAST
						&& interpreter.simpleUnify(priority, bi.priority) == SUCCESS_LAST)
				{
					interpreter.pushBacktrackInfo(bi);
					return SUCCESS;
				}
				interpreter.undo(bi.startUndoPosition);
			}
			return FAIL;
		}
		catch (PrologException ex)
		{
			interpreter.undo(bi.startUndoPosition);
			throw ex;
		}
	}
}
