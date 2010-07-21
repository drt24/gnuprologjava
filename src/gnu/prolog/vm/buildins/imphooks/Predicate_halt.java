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
package gnu.prolog.vm.buildins.imphooks;

import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.PrologHalt;
import gnu.prolog.vm.TermConstants;

/**
 * prolog code
 */
public class Predicate_halt extends ExecuteOnlyCode
{
	/**
	 * this method is used for execution of code
	 * 
	 * Halts execution by throwing {@link PrologHalt} DO NOT catch this.
	 * 
	 * @param interpreter
	 *          interpreter in which context code is executed
	 * @param backtrackMode
	 *          true if predicate is called on backtracking and false otherwise
	 * @param args
	 *          arguments of code
	 * 
	 */
	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term tcode = args[0];
		if (tcode instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		if (!(tcode instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, tcode);
		}
		IntegerTerm code = (IntegerTerm) tcode;
		throw new PrologHalt(code.value);
	}
}
