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

import gnu.prolog.io.ParseException;
import gnu.prolog.io.TermReader;
import gnu.prolog.io.TermWriter;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.FloatTerm;
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
public class Predicate_number_chars extends ExecuteOnlyCode
{
	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term number = args[0];
		Term list = args[1];
		if (!(number instanceof VariableTerm || number instanceof IntegerTerm || number instanceof FloatTerm))
		{
			PrologException.typeError(TermConstants.numberAtom, number);
		}

		String numStr = getNumberString(list, (number instanceof VariableTerm));
		if (numStr != null)
		{
			Term res = null;
			try
			{
				res = TermReader.stringToTerm(numStr, interpreter.getEnvironment());
			}
			catch (ParseException ex)
			{// TODO there is useful debug information here which we are discarding
				PrologException.syntaxError(ex);
			}
			if (!(res instanceof IntegerTerm || res instanceof FloatTerm))
			{
				PrologException.syntaxError(TermConstants.numberExpectedAtom);
			}
			return interpreter.unify(res, number);
		}
		else
		{
			numStr = TermWriter.toString(number);
			Term res = TermConstants.emptyListAtom;
			for (int i = numStr.length() - 1; i >= 0; i--)
			{
				res = CompoundTerm.getList(AtomTerm.get(numStr.charAt(i)), res);
			}
			return interpreter.unify(list, res);
		}
	}

	/** returns null if illegal chracter sequenca */
	private static String getNumberString(Term list, boolean numberIsVariable) throws PrologException
	{
		StringBuffer bu = new StringBuffer();
		Term cur = list;
		while (cur != TermConstants.emptyListAtom)
		{
			if (cur instanceof VariableTerm)
			{
				if (numberIsVariable)
				{
					PrologException.instantiationError();
				}
				else
				{
					return null;
				}
			}
			if (!CompoundTerm.isListPair(cur))
			{
				PrologException.typeError(TermConstants.listAtom, list);
			}
			CompoundTerm ct = (CompoundTerm) cur;
			Term head = ct.args[0].dereference();
			cur = ct.args[1].dereference();
			if (head instanceof VariableTerm)
			{
				if (numberIsVariable)
				{
					PrologException.instantiationError();
				}
				else
				{
					return null;
				}
			}
			if (!(head instanceof AtomTerm))
			{
				PrologException.typeError(TermConstants.characterAtom, head);
			}
			AtomTerm ch = (AtomTerm) head;
			if (ch.value.length() != 1)
			{
				PrologException.typeError(TermConstants.characterAtom, head);
			}
			bu.append(ch.value.charAt(0));
		}
		return bu.toString();
	}
}
