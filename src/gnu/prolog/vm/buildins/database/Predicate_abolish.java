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
public class Predicate_abolish extends ExecuteOnlyCode
{
	static final CompoundTermTag divideTag = CompoundTermTag.get("/", 2);

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term tpi = args[0];
		if (tpi instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		if (!(tpi instanceof CompoundTerm))
		{
			PrologException.typeError(TermConstants.predicateIndicatorAtom, tpi);
		}
		CompoundTerm pi = (CompoundTerm) tpi;
		if (pi.tag != divideTag)
		{
			PrologException.typeError(TermConstants.predicateIndicatorAtom, pi);
		}
		Term tn = pi.args[0].dereference();
		Term ta = pi.args[1].dereference();
		if (tn instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		else if (!(tn instanceof AtomTerm))
		{
			PrologException.typeError(TermConstants.atomAtom, tn);
		}
		AtomTerm n = (AtomTerm) tn;
		if (ta instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		else if (!(ta instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, ta);
		}
		IntegerTerm a = (IntegerTerm) ta;
		if (a.value < 0)
		{
			PrologException.domainError(TermConstants.notLessThanZeroAtom, ta);
		}
		// check that something that big can exist
		IntegerTerm maxArityTerm = (IntegerTerm) interpreter.getEnvironment().getPrologFlag(TermConstants.maxArityAtom);
		if (a.value > maxArityTerm.value)
		{
			PrologException.representationError(TermConstants.maxArityAtom);
		}
		CompoundTermTag tag = CompoundTermTag.get(n, a.value);
		Predicate p = interpreter.getEnvironment().getModule().getDefinedPredicate(tag);
		if (p != null)
		{
			if (p.getType() != Predicate.TYPE.USER_DEFINED || !p.isDynamic())
			{
				PrologException.permissionError(TermConstants.modifyAtom, TermConstants.staticProcedureAtom, pi);
			}
			interpreter.getEnvironment().getModule().removeDefinedPredicate(tag);
		}
		return SUCCESS_LAST;
	}
}
