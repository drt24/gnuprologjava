/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 *
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
package gnu.prolog.vm.buildins.misc;

import gnu.prolog.database.Predicate;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_current_functor extends ExecuteOnlyCode
{
	private static class CurrentPredicateBacktrackInfo extends BacktrackInfo
	{
		CurrentPredicateBacktrackInfo()
		{
			super(-1, -1);
		}

		int startUndoPosition;
		Iterator<CompoundTermTag> tagsIterator;
		Term functor;
		Term arity;
	}

	public Predicate_current_functor()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		if (backtrackMode)
		{
			CurrentPredicateBacktrackInfo bi = (CurrentPredicateBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			Term functor = args[0];
			Term arity = args[1];
			if (!(functor instanceof VariableTerm || functor instanceof AtomTerm))
			{
				PrologException.typeError(TermConstants.atomAtom, functor);
			}
			if (!(arity instanceof VariableTerm || arity instanceof IntegerTerm))
			{
				PrologException.typeError(TermConstants.integerAtom, arity);
			}
			Set<CompoundTermTag> tagSet = new HashSet<CompoundTermTag>(interpreter.getEnvironment().getModule().getPredicateTags());
			CurrentPredicateBacktrackInfo bi = new CurrentPredicateBacktrackInfo();
			bi.startUndoPosition = interpreter.getUndoPosition();
			bi.functor = functor;
			bi.arity = arity;
			bi.tagsIterator = tagSet.iterator();
			return nextSolution(interpreter, bi);
		}
	}

	private static int nextSolution(Interpreter interpreter, CurrentPredicateBacktrackInfo bi) throws PrologException
	{
		while (bi.tagsIterator.hasNext())
		{
			CompoundTermTag tag = bi.tagsIterator.next();
			Predicate p = interpreter.getEnvironment().getModule().getDefinedPredicate(tag);
			if (p == null) // if was destroyed
			{
				continue;
			}
			int rc = interpreter.unify(bi.functor, tag.functor);
			if (rc == FAIL)
			{
				interpreter.undo(bi.startUndoPosition);
				continue;
			}
			rc = interpreter.unify(bi.arity, IntegerTerm.get(tag.arity));
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
