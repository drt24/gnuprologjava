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

import gnu.prolog.term.CompoundTerm;
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
public class Predicate_arg extends ExecuteOnlyCode
{
	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term n = args[0];
		Term term = args[1];
		Term arg = args[2];
		if (n instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		if (term instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		if (!(n instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, n);
		}
		IntegerTerm in = (IntegerTerm) n;
		if (in.value < 0)
		{
			PrologException.domainError(TermConstants.notLessThanZeroAtom, in);
		}
		if (!(term instanceof CompoundTerm))
		{
			PrologException.typeError(TermConstants.compoundAtom, term);
		}
		CompoundTerm ct = (CompoundTerm) term;
		if (ct.tag.arity < in.value)
		{
			return FAIL;
		}
		if (in.value == 0)
		{
			return FAIL;
		}
		int undoPos = interpreter.getUndoPosition();
		int rc = interpreter.unify(ct.args[in.value - 1], arg);
		if (rc == FAIL)
		{
			interpreter.undo(undoPos);
			return FAIL;
		}
		return SUCCESS_LAST;
	}
}
