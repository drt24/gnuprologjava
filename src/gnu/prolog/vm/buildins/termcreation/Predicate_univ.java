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
package gnu.prolog.vm.buildins.termcreation;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.AtomicTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * prolog code
 */
public class Predicate_univ extends ExecuteOnlyCode
{
	public final static Term termArrayType[] = new Term[0];

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		int undoPos = interpreter.getUndoPosition();
		Term term = args[0];
		Term list = args[1];
		if (term instanceof AtomicTerm)
		{
			checkList(list, false);
			if (list instanceof VariableTerm)
			{
				VariableTerm lvar = (VariableTerm) list;
				interpreter.addVariableUndo(lvar);
				lvar.value = CompoundTerm.getList(term, TermConstants.emptyListAtom);
				return SUCCESS_LAST;
			}
			CompoundTerm ct = (CompoundTerm) list;
			Term head = ct.args[0].dereference();
			Term tail = ct.args[1].dereference();
			if (head instanceof CompoundTerm)
			{
				PrologException.typeError(TermConstants.atomicAtom, head);
			}
			Term t = CompoundTerm.getList(term, TermConstants.emptyListAtom);
			return interpreter.unify(t, list);
		}
		else if (term instanceof CompoundTerm)
		{
			checkList(list, false);
			CompoundTerm ct = (CompoundTerm) term;
			CompoundTermTag tag = ct.tag;
			AtomTerm functor = tag.functor;
			Term tmp = TermConstants.emptyListAtom;
			Term targs[] = ct.args;
			for (int i = tag.arity - 1; i >= 0; i--)
			{
				tmp = CompoundTerm.getList(targs[i].dereference(), tmp);
			}
			tmp = CompoundTerm.getList(functor, tmp);
			int rc = interpreter.unify(tmp, list);
			if (rc == FAIL)
			{
				interpreter.undo(undoPos);
			}
			return rc;
		}
		else if (term instanceof VariableTerm)
		{
			checkList(list, true);
			VariableTerm vt = (VariableTerm) term;
			if (list == TermConstants.emptyListAtom)
			{
				PrologException.domainError(TermConstants.nonEmptyListAtom, list);
			}
			CompoundTerm ct = (CompoundTerm) list;
			if (ct.tag != TermConstants.listTag)
			{
				PrologException.typeError(TermConstants.listAtom, list);
			}
			Term head = ct.args[0].dereference();
			Term tail = ct.args[1].dereference();
			if (head instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (tail == TermConstants.emptyListAtom)
			{
				interpreter.addVariableUndo(vt);
				vt.value = head;
				return SUCCESS_LAST;
			}
			if (!(head instanceof AtomTerm))
			{
				PrologException.typeError(TermConstants.atomAtom, head);
			}
			AtomTerm functor = (AtomTerm) head;
			List<Term> argList = new ArrayList<Term>();
			do
			{
				ct = (CompoundTerm) tail;
				head = ct.args[0].dereference();
				tail = ct.args[1].dereference();
				argList.add(head);
			} while (tail != TermConstants.emptyListAtom);
			Term targs[] = argList.toArray(termArrayType);
			interpreter.addVariableUndo(vt);
			vt.value = new CompoundTerm(functor, targs);
			return SUCCESS_LAST;
		}
		return FAIL;
	}

	private static void checkList(Term list, boolean nonPartial) throws PrologException
	{
		Term exArg = list;

		if (list == TermConstants.emptyListAtom)
		{
			return;
		}
		if (list instanceof VariableTerm)
		{
			if (nonPartial)
			{
				PrologException.instantiationError();
			}
			else
			{
				return;
			}
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
		Term head = ct.args[0].dereference();
		Term tail = ct.args[1].dereference();
		if (tail == TermConstants.emptyListAtom)
		{
			if (head instanceof CompoundTerm)
			{
				PrologException.typeError(TermConstants.atomicAtom, head);
			}
			return;
		}
		else
		{
			if (!(head instanceof VariableTerm) && !(head instanceof AtomicTerm))
			{
				PrologException.typeError(TermConstants.atomAtom, head);
			}
		}

		list = tail;
		while (true)
		{
			if (list == TermConstants.emptyListAtom)
			{
				return;
			}
			if (list instanceof VariableTerm)
			{
				if (nonPartial)
				{
					PrologException.instantiationError();
				}
				else
				{
					return;
				}
			}
			if (!(list instanceof CompoundTerm))
			{
				PrologException.typeError(TermConstants.listAtom, exArg);
			}
			ct = (CompoundTerm) list;
			if (ct.tag != TermConstants.listTag)
			{
				PrologException.typeError(TermConstants.listAtom, exArg);
			}
			// Term head = ct.args[1].dereference();
			list = ct.args[1].dereference();
		}
	}
}
