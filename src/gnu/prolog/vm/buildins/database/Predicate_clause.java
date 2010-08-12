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
package gnu.prolog.vm.buildins.database;

import gnu.prolog.database.Predicate;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * prolog code
 */
public class Predicate_clause extends ExecuteOnlyCode
{
	private static class ClauseBacktrackInfo extends BacktrackInfo
	{
		ClauseBacktrackInfo()
		{
			super(-1, -1);
		}

		List<Term> clauses;
		int position;
		int startUndoPosition;
		Term clause;

	}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		if (backtrackMode)
		{
			ClauseBacktrackInfo bi = (ClauseBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			Term head = args[0];
			Term body = args[1];
			if (head instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			CompoundTermTag tag = null;
			if (head instanceof AtomTerm)
			{
				tag = CompoundTermTag.get((AtomTerm) head, 0);
			}
			else if (head instanceof CompoundTerm)
			{
				tag = ((CompoundTerm) head).tag;
			}
			else
			{
				PrologException.typeError(TermConstants.callableAtom, head);
			}

			if (!isCallable(body))
			{
				PrologException.typeError(TermConstants.callableAtom, body);
			}

			Predicate p = interpreter.getEnvironment().getModule().getDefinedPredicate(tag);
			if (p == null) // if predicate not found
			{
				return FAIL;
			}
			// System.err.println("p type = "+p.getType());
			// System.err.println("p dyn = "+p.isDynamic());
			if (p.getType() != Predicate.TYPE.USER_DEFINED || !p.isDynamic())
			{
				PrologException.permissionError(TermConstants.accessAtom, TermConstants.privateProcedureAtom, tag
						.getPredicateIndicator());
			}

			List<Term> clauses = new ArrayList<Term>();
			ClauseBacktrackInfo bi = new ClauseBacktrackInfo();
			synchronized (p)
			{
				for (Term term : p.getClauses())
				{
					clauses.add((Term) (term).clone());
				}
				if (clauses.size() == 0)
				{
					return FAIL;
				}
				else
				{
					bi.startUndoPosition = interpreter.getUndoPosition();
					bi.position = 0;
					bi.clauses = clauses;
					bi.clause = new CompoundTerm(TermConstants.clauseTag, head, body);
				}
			}
			return nextSolution(interpreter, bi);

		}
	}

	private int nextSolution(Interpreter interpreter, ClauseBacktrackInfo bi) throws PrologException
	{
		while (bi.position < bi.clauses.size())
		{
			int rc = interpreter.unify(bi.clauses.get(bi.position++), bi.clause);
			if (rc == SUCCESS_LAST)
			{
				interpreter.pushBacktrackInfo(bi);
				return SUCCESS;
			}
		}
		return FAIL;
	}

	public static boolean isCallable(Term body)
	{
		if (body instanceof VariableTerm)
		{
			return true;
		}
		else if (body instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) body;
			if (ct.tag == TermConstants.conjunctionTag || ct.tag == TermConstants.disjunctionTag
					|| ct.tag == TermConstants.ifTag)
			{
				return isCallable(ct.args[0].dereference()) && isCallable(ct.args[1].dereference());
			}
			return true;// TODO: this is not necessarily true.
		}
		else if (body instanceof AtomTerm)
		{
			return true;
		}
		return false;
	}
}
