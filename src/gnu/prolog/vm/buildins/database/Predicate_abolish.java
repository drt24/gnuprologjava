/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
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
package gnu.prolog.vm.buildins.database;

import gnu.prolog.database.Predicate;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * prolog code
 */
public class Predicate_abolish implements PrologCode
{
	static final AtomTerm modifyAtom = AtomTerm.get("modify");
	static final CompoundTermTag divideTag = CompoundTermTag.get("/", 2);

	/**
	 * this method is used for execution of code
	 *
	 * @param interpreter
	 *          interpreter in which context code is executed
	 * @param backtrackMode
	 *          true if predicate is called on backtracking and false otherwise
	 * @param args
	 *          arguments of code
	 * @return either SUCCESS, SUCCESS_LAST, or FAIL.
	 */
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
			PrologException.domainError(TermConstants.notLessThenZeroAtom, ta);
		}
		CompoundTermTag tag = CompoundTermTag.get(n, a.value);
		Predicate p = interpreter.environment.getModule().getDefinedPredicate(tag);
		if (p != null)
		{
			if (p.getType() != Predicate.USER_DEFINED || !p.isDynamic())
			{
				PrologException.permissionError(modifyAtom, TermConstants.staticProcedureAtom, pi);
			}
			interpreter.environment.getModule().removeDefinedPredicate(tag);
		}
		return SUCCESS_LAST;
	}

	/**
	 * this method is called when code is installed to the environment code can be
	 * installed only for one environment.
	 *
	 * @param environment
	 *          environemnt to install the predicate
	 */
	public void install(Environment env)
	{

	}

	/**
	 * this method is called when code is uninstalled from the environment
	 *
	 * @param environment
	 *          environemnt to install the predicate
	 */
	public void uninstall(Environment env)
	{}

}
