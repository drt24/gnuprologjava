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
package gnu.prolog.vm.buildins.io;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_char_conversion extends ExecuteOnlyCode
{
	public Predicate_char_conversion()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		Term from = args[0];
		Term to = args[1];
		if (from instanceof AtomTerm)
		{
			if (((AtomTerm) from).value.length() != 1)
			{
				PrologException.representationError(from);
			}
		}
		else if (from instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		else
		{
			PrologException.representationError(from);
		}
		if (to instanceof AtomTerm)
		{
			if (((AtomTerm) to).value.length() != 1)
			{
				PrologException.representationError(to);
			}
		}
		else if (to instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		else
		{
			PrologException.representationError(to);
		}
		char cfrom = ((AtomTerm) from).value.charAt(0);
		char cto = ((AtomTerm) to).value.charAt(0);
		interpreter.getEnvironment().getConversionTable().setConversion(cfrom, cto);
		return SUCCESS_LAST;
	}
}
