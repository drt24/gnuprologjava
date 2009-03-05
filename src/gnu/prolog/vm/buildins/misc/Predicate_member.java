/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA. The text ol license can be also found 
 * at http://www.gnu.org/copyleft/lgpl.html
 */
package gnu.prolog.vm.buildins.misc;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;

/**
 * Does:
 * 
 * <pre>
 * member(X, [X|_]).
 * member(X, [_|Y]):-member(X,Y).
 * </pre>
 * 
 * But without recursion
 * 
 * @author Michiel Hendriks
 */
public class Predicate_member implements PrologCode
{
	protected class MemberBacktrackInfo extends BacktrackInfo
	{
		protected Term item;
		protected Term list;
		protected boolean listExpand;
		protected Term listDest;

		protected int startUndoPosition;

		protected MemberBacktrackInfo()
		{
			super(-1, -1);
		}
	}

	public Predicate_member()
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#execute(gnu.prolog.vm.Interpreter, boolean,
	 * gnu.prolog.term.Term[])
	 */
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		if (backtrackMode)
		{
			MemberBacktrackInfo bi = (MemberBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			MemberBacktrackInfo bi = new MemberBacktrackInfo();
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
	 * @return
	 * @throws PrologException
	 */
	protected int nextSolution(Interpreter interpreter, MemberBacktrackInfo bi) throws PrologException
	{
		while (!AtomTerm.emptyList.equals(bi.list))
		{
			if (bi.listExpand)
			{
				Term tmp = CompoundTerm.getList(bi.item, bi.list);
				interpreter.unify(bi.listDest, tmp);
				bi.item = new VariableTerm();
				bi.list = tmp;
			}
			Term head = ((CompoundTerm) bi.list).args[0].dereference();
			if (bi.listExpand)
			{
				bi.list = ((CompoundTerm) bi.list).args[1].dereference();
			}
			if (interpreter.unify(bi.item, head) == FAIL)
			{
				interpreter.undo(bi.startUndoPosition);
				continue;
			}
			if (bi.list instanceof VariableTerm)
			{
				bi.listDest = bi.list;
				bi.list = new VariableTerm();
				bi.listExpand = true;
			}
			else if (!CompoundTerm.isListPair(bi.list) && !AtomTerm.emptyList.equals(bi.list))
			{
				return FAIL;
			}
			interpreter.pushBacktrackInfo(bi);
			return SUCCESS;
		}
		return FAIL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#install(gnu.prolog.vm.Environment)
	 */
	public void install(Environment env)
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#uninstall(gnu.prolog.vm.Environment)
	 */
	public void uninstall(Environment env)
	{}

}
