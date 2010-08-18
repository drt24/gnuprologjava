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
package gnu.prolog.vm.buildins.list;

import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * TODO
 * 
 * Does:
 * 
 * <pre>
 * %append(?HeadList, ?TailList, ?List)
 * append([],X,X).
 * append([X|Xs],Y,[X|Z]) :- append(Xs,Y,Z).
 * </pre>
 * 
 * But without recursion
 * 
 * @author Daniel Thomas based on Predicate_member by Michiel Hendriks
 */
public class Predicate_append extends ExecuteOnlyCode
{
	protected static class AppendBacktrackInfo extends BacktrackInfo
	{
		protected Term item;
		protected Term list;
		protected boolean listExpand;
		protected Term listDest;

		protected int startUndoPosition;

		protected AppendBacktrackInfo()
		{
			super(-1, -1);
		}
	}

	public Predicate_append()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		assert (args.length == 3);
		Term headList = args[0];
		Term tailList = args[1];
		Term list = args[2];
		if (list instanceof VariableTerm)
		{
			// Unify the list with the appending of the head onto the tail.
			return interpreter.unify(CompoundTerm.getList(headList, tailList), list);
		}
		if (backtrackMode)
		{
			AppendBacktrackInfo bi = (AppendBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			AppendBacktrackInfo bi = new AppendBacktrackInfo();
			bi.startUndoPosition = interpreter.getUndoPosition();
			bi.item = args[0];
			if (args[1] instanceof VariableTerm)
			{
				bi.list = new VariableTerm();
				bi.listExpand = true;
				bi.listDest = args[1];
			}
			else
			{
				bi.list = args[1];
			}
			return nextSolution(interpreter, bi);
		}
	}

	/*
	 * member(1,N) -> N = [1|_] -> N = [_,1|_] -> N = [_,_,1|_]
	 */

	/**
	 * @param interpreter
	 * @param bi
	 * @return PrologCode return code
	 * @throws PrologException
	 */
	protected int nextSolution(Interpreter interpreter, AppendBacktrackInfo bi) throws PrologException
	{
		while (!TermConstants.emptyListAtom.equals(bi.list))
		{
			if (bi.listExpand)
			{
				Term tmp = CompoundTerm.getList(bi.item, bi.list);
				interpreter.unify(bi.listDest, tmp);
				bi.item = new VariableTerm();
				bi.list = tmp;
			}
			Term head = ((CompoundTerm) bi.list).args[0].dereference();
			if (!bi.listExpand)
			{
				bi.list = ((CompoundTerm) bi.list).args[1].dereference();
			}
			if (bi.list instanceof VariableTerm)
			{
				bi.listDest = bi.list;
				bi.list = new VariableTerm();
				bi.listExpand = true;
			}
			else if (!CompoundTerm.isListPair(bi.list) && !TermConstants.emptyListAtom.equals(bi.list))
			{
				return FAIL;
			}
			if (interpreter.unify(bi.item, head) == FAIL)
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
