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
package gnu.prolog.vm.buildins.atomicterms;

import gnu.prolog.term.AtomTerm;
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
public class Predicate_char_code extends ExecuteOnlyCode
{

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term tchar = args[0];
		Term tcode = args[1];
		if (tchar instanceof VariableTerm)
		{
			VariableTerm vchar = (VariableTerm) tchar;
			if (tcode instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			else if (!(tcode instanceof IntegerTerm))
			{
				PrologException.typeError(TermConstants.integerAtom, tcode);
			}
			IntegerTerm icode = (IntegerTerm) tcode;
			if (icode.value < 0 || 0xffff < icode.value)
			{
				PrologException.representationError(TermConstants.characterCodeAtom);
			}
			StringBuffer bu = new StringBuffer(1);
			bu.append((char) icode.value);
			interpreter.addVariableUndo(vchar);
			vchar.value = AtomTerm.get(bu.toString());
			return SUCCESS_LAST;
		}
		else if (tchar instanceof AtomTerm)
		{
			AtomTerm achar = (AtomTerm) tchar;
			if (achar.value.length() != 1)
			{
				PrologException.typeError(TermConstants.characterAtom, achar);
			}
			IntegerTerm code = IntegerTerm.get(achar.value.charAt(0));

			if (!(tcode instanceof IntegerTerm | tcode instanceof VariableTerm))
			{
				PrologException.typeError(TermConstants.integerAtom, tcode);
			}

			return interpreter.unify(code, tcode);
		}
		else
		{
			PrologException.typeError(TermConstants.characterAtom, tchar);
		}
		return FAIL; // fake return
	}
}
