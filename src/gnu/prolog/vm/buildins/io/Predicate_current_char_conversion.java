/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 *
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
package gnu.prolog.vm.buildins.io;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.Iterator;

/**
 *
 * @author Michiel Hendriks
 */
public class Predicate_current_char_conversion implements PrologCode
{
	private class CharConvBacktrackInfo extends BacktrackInfo
	{
		int startUndoPosition;
		Term arg0;
		Term arg1;
		Iterator<Character> charIt;
		char counter;

		CharConvBacktrackInfo()
		{
			super(-1, -1);
		}
	}

	public Predicate_current_char_conversion()
	{}

	/*
	 * (non-Javadoc)
	 *
	 * @see gnu.prolog.vm.PrologCode#execute(gnu.prolog.vm.Interpreter, boolean,
	 * gnu.prolog.term.Term[])
	 */
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		if (backtrackMode)
		{
			CharConvBacktrackInfo bi = (CharConvBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			if (args[0] instanceof AtomTerm)
			{
				if (((AtomTerm) args[0]).value.length() != 1)
				{
					PrologException.representationError(TermConstants.characterAtom);
				}
			}
			else if (!(args[0] instanceof VariableTerm))
			{
				PrologException.representationError(TermConstants.characterAtom);
			}
			if (args[1] instanceof AtomTerm)
			{
				if (((AtomTerm) args[1]).value.length() != 1)
				{
					PrologException.representationError(TermConstants.characterAtom);
				}
			}
			else if (!(args[1] instanceof VariableTerm))
			{
				PrologException.representationError(TermConstants.characterAtom);
			}

			if (args[0] instanceof VariableTerm && args[1] instanceof VariableTerm)
			{
				CharConvBacktrackInfo bi = new CharConvBacktrackInfo();
				bi.startUndoPosition = interpreter.getUndoPosition();
				bi.arg0 = args[0];
				bi.arg1 = args[1];
				return nextSolution(interpreter, bi);
			}
			else if (args[0] instanceof VariableTerm)
			{
				CharConvBacktrackInfo bi = new CharConvBacktrackInfo();
				bi.startUndoPosition = interpreter.getUndoPosition();
				bi.arg0 = args[0];
				bi.charIt = interpreter.environment.getConversionTable().convertsTo(((AtomTerm) args[1]).value.charAt(0))
						.iterator();
				return nextSolution(interpreter, bi);
			}
			else if (args[1] instanceof VariableTerm)
			{
				Term res = AtomTerm.get(Character.toString(interpreter.environment.getConversionTable().convert(
						((AtomTerm) args[0]).value.charAt(0))));
				return interpreter.unify(args[1], res);
			}
			// not possible
			PrologException.systemError();
			return FAIL;
		}
	}

	private int nextSolution(Interpreter interpreter, CharConvBacktrackInfo bi) throws PrologException
	{
		if (bi.charIt != null)
		{
			while (bi.charIt.hasNext())
			{
				Term res = AtomTerm.get(Character.toString(bi.charIt.next()));
				int rc = interpreter.unify(bi.arg0, res);
				if (rc == FAIL)
				{
					interpreter.undo(bi.startUndoPosition);
					continue;
				}
				interpreter.pushBacktrackInfo(bi);
				return SUCCESS;
			}
		}
		else
		{
			while (bi.counter < Character.MAX_CODE_POINT)
			{
				if (!Character.isDefined(bi.counter))
				{
					bi.counter++;
					continue;
				}
				Term res = AtomTerm.get(Character.toString(bi.counter));
				int rc = interpreter.unify(bi.arg0, res);
				if (rc == FAIL)
				{
					bi.counter++;
					interpreter.undo(bi.startUndoPosition);
					continue;
				}
				Term res2 = AtomTerm.get(Character.toString(interpreter.environment.getConversionTable().convert(bi.counter)));
				rc = interpreter.unify(bi.arg1, res2);
				if (rc == FAIL)
				{
					bi.counter++;
					interpreter.undo(bi.startUndoPosition);
					continue;
				}
				bi.counter++;
				interpreter.pushBacktrackInfo(bi);
				return SUCCESS;
			}
		}
		return FAIL;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gnu.prolog.vm.PrologCode#install(gnu.prolog.vm.Environment)
	 */
	public void install(Environment env)
	{}

	/*
	 * (non-Javadoc)
	 *
	 * @see gnu.prolog.vm.PrologCode#uninstall(gnu.prolog.vm.Environment)
	 */
	public void uninstall(Environment env)
	{}
}
