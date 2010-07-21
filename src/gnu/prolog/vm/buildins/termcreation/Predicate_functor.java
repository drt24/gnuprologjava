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
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * prolog code
 */
public class Predicate_functor extends ExecuteOnlyCode
{
	/** constant used in predicate */
	public static final IntegerTerm zero = IntegerTerm.get(0);

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		int undoPos = interpreter.getUndoPosition();
		Term term = args[0];
		Term name = args[1];
		Term arity = args[2];
		int rc;
		if (term instanceof AtomicTerm)
		{
			rc = interpreter.unify(term, name);
			if (rc == FAIL)
			{
				interpreter.undo(undoPos);
				return FAIL;
			}
			rc = interpreter.unify(arity, zero);
			if (rc == FAIL)
			{
				interpreter.undo(undoPos);
				return FAIL;
			}
			return SUCCESS_LAST;
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTermTag tag = ((CompoundTerm) term).tag;
			IntegerTerm tarity = IntegerTerm.get(tag.arity);
			rc = interpreter.unify(tag.functor, name);
			if (rc == FAIL)
			{
				interpreter.undo(undoPos);
				return FAIL;
			}
			rc = interpreter.unify(tarity, arity);
			if (rc == FAIL)
			{
				interpreter.undo(undoPos);
				return FAIL;
			}
			return SUCCESS_LAST;
		}
		else if (term instanceof VariableTerm)
		{
			if (arity instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (name instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (!(name instanceof AtomicTerm))
			{
				PrologException.typeError(TermConstants.atomicAtom, name);
			}
			if (!(arity instanceof IntegerTerm))
			{
				PrologException.typeError(TermConstants.integerAtom, arity);
			}
			IntegerTerm iarity = (IntegerTerm) arity;
			if (iarity.value > 0)
			{
				if (!(name instanceof AtomTerm))
				{
					PrologException.typeError(TermConstants.atomAtom, name);
				}
				AtomTerm functor = (AtomTerm) name;
				int n = iarity.value;
				// check that we can make something that big
				IntegerTerm maxArityTerm = (IntegerTerm) interpreter.getEnvironment().getPrologFlag(TermConstants.maxArityAtom);
				if (n > maxArityTerm.value)
				{
					PrologException.representationError(TermConstants.maxArityAtom);
				}

				Term targs[] = new Term[n];
				for (int i = 0; i < n; i++)
				{
					targs[i] = new VariableTerm();
				}
				rc = interpreter.unify(term, new CompoundTerm(functor, targs));
				if (rc == FAIL)
				{
					interpreter.undo(undoPos);
					return FAIL;
				}
				return SUCCESS_LAST;
			}
			if (iarity.value < 0)
			{
				PrologException.domainError(TermConstants.notLessThanZeroAtom, arity);
			}
			rc = interpreter.unify(term, name);
			if (rc == FAIL)
			{
				interpreter.undo(undoPos);
				return FAIL;
			}
			return SUCCESS_LAST;
		}
		return FAIL; // this point is unreachable
	}
}
