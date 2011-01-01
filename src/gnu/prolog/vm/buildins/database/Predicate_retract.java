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
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * prolog code
 */
public class Predicate_retract extends AbstractPredicate_assertRetract
{
	private static class RetractBacktrackInfo extends BacktrackInfo
	{
		RetractBacktrackInfo()
		{
			super(-1, -1);
		}

		Iterator<Term> iclauses;
		Map<Term, Term> clauseMap;
		int startUndoPosition;
		Term clause;
		Predicate pred;
	}

	@Override
	public RC execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[]) throws PrologException
	{
		if (backtrackMode)
		{
			RetractBacktrackInfo bi = (RetractBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			Term clause = args[0];

			PredicateTagHeadBody predicateTagHeadBody = execute(clause, interpreter);

			Predicate p = predicateTagHeadBody.predicate;
			Term head = predicateTagHeadBody.head;
			Term body = predicateTagHeadBody.body;
			CompoundTermTag predTag = predicateTagHeadBody.tag;

			if (p == null)
			{
				return RC.FAIL;
			}
			else if (p.getType() == Predicate.TYPE.USER_DEFINED)
			{
				if (!p.isDynamic())
				{
					PrologException.permissionError(TermConstants.modifyAtom, TermConstants.staticProcedureAtom, predTag
							.getPredicateIndicator());
				}
			}
			else
			{
				PrologException.permissionError(TermConstants.modifyAtom, TermConstants.staticProcedureAtom, predTag
						.getPredicateIndicator());
			}
			Map<Term, Term> map = new HashMap<Term, Term>();
			RetractBacktrackInfo bi = new RetractBacktrackInfo();
			synchronized (p)
			{
				List<Term> clauses = p.getClauses();
				List<Term> list = new ArrayList<Term>(clauses.size());
				for (Term term : clauses)
				{
					Term cl = term;
					Term cp = (Term) cl.clone();
					map.put(cp, cl);
					list.add(cp);
				}

				bi.iclauses = list.iterator();
				bi.clauseMap = map;
				bi.startUndoPosition = interpreter.getUndoPosition();
				bi.clause = new CompoundTerm(TermConstants.clauseTag, head, body);
				bi.pred = p;
			}
			return nextSolution(interpreter, bi);

		}
	}

	private static RC nextSolution(Interpreter interpreter, RetractBacktrackInfo bi) throws PrologException
	{
		while (bi.iclauses.hasNext())
		{
			Term term = bi.iclauses.next();
			RC rc = interpreter.unify(bi.clause, term);
			if (rc == RC.SUCCESS_LAST)
			{
				bi.pred.removeClause(bi.clauseMap.get(term));
				interpreter.pushBacktrackInfo(bi);
				return RC.SUCCESS;
			}
		}
		return RC.FAIL;
	}
}
