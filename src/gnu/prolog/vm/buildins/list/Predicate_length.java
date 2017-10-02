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
import gnu.prolog.term.IntegerTerm;
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
 * 
 * @author Michiel Hendriks
 */
public class Predicate_length extends ExecuteOnlyCode
{
	public Predicate_length()
	{}

	protected static class LengthBacktrackInfo extends BacktrackInfo
	{
		protected int length;
		protected int startUndoPosition;
		public LengthBacktrackInfo(int l, int s)
		{
			super(-1, -1);
			startUndoPosition = s;
			length = l;
		}

	}

	@Override
	public RC execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		Term listTerm = args[0];
		Term lengthTerm = args[1];
		LengthBacktrackInfo bi = null;
		if (backtrackMode)
		{
			bi = (LengthBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			interpreter.unify(lengthTerm, IntegerTerm.get(++bi.length));
		}
		if (!(lengthTerm instanceof VariableTerm | lengthTerm instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, lengthTerm);
		}
		if (CompoundTerm.isListPair(listTerm) || TermConstants.emptyListAtom.equals(listTerm))
		{
			int length = 0;
			Term lst = listTerm;
			while (lst != null)
			{
				if (TermConstants.emptyListAtom.equals(lst))
				{
					break;
				}
				if ((lst instanceof VariableTerm))
				{
					((VariableTerm) lst).value = TermConstants.emptyListAtom;
					// TODO on backtracking we need to unify with [_] etc.
					break;
				}
				if (!CompoundTerm.isListPair(lst))
				{
					return RC.FAIL;
				}
				CompoundTerm ct = (CompoundTerm) lst;
				if (ct.args.length != 2)
				{
					return RC.FAIL;
				}
				++length;
				lst = ct.args[1].dereference();
			}

			return interpreter.unify(lengthTerm, IntegerTerm.get(length));
		}
		else if (listTerm instanceof VariableTerm)
		{
			if ((lengthTerm.dereference() instanceof VariableTerm))
			{
				bi = new LengthBacktrackInfo(0, interpreter.getUndoPosition());
				((VariableTerm) lengthTerm).value = IntegerTerm.get(bi.length);

			}
			List<Term> genList = new ArrayList<Term>();
			int length = ((IntegerTerm) lengthTerm.dereference()).value;
			if (length < 0)
			{
				return RC.FAIL;
			}
			for (int i = 0; i < length; i++)
			{
				genList.add(new VariableTerm());
			}
			Term term = CompoundTerm.getList(genList);
			RC rc = interpreter.unify(listTerm, term);
			if (rc == RC.SUCCESS_LAST && bi != null)
			{
				interpreter.pushBacktrackInfo(bi);
				return RC.SUCCESS;
			}
			return rc;
		}
		else
		{
			PrologException.typeError(TermConstants.listAtom, listTerm);
		}
		return RC.SUCCESS;
	}
}
