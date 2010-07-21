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
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * prolog code
 */
public class Predicate_sub_atom extends ExecuteOnlyCode
{

	/** private backtrack info */
	private static class SubAtomBacktrackInfo extends BacktrackInfo
	{
		SubAtomBacktrackInfo()
		{
			super(-1, -1);
		}

		int startUndoPosition;
		AtomTerm atom;
		boolean beforeFixed;
		int before;
		VariableTerm varBefore;
		boolean lengthFixed;
		int length;
		VariableTerm varLength;
		boolean afterFixed;
		int after;
		VariableTerm varAfter;
		boolean subAtomFixed;
		AtomTerm subAtom;
		VariableTerm varSubAtom;

		int currentPos;
		int currentLen;
		int atomLen;
	}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		if (backtrackMode)
		{
			SubAtomBacktrackInfo bi = (SubAtomBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			SubAtomBacktrackInfo bi = new SubAtomBacktrackInfo();
			bi.startUndoPosition = interpreter.getUndoPosition();

			Term tatom = args[0];
			Term tbefore = args[1];
			Term tlength = args[2];
			Term tafter = args[3];
			Term tsub_atom = args[4];

			if (tatom instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			else if (!(tatom instanceof AtomTerm))
			{
				PrologException.typeError(TermConstants.atomAtom, tatom);
			}
			bi.atom = (AtomTerm) tatom;
			bi.atomLen = bi.atom.value.length();
			bi.currentPos = 0;
			bi.currentLen = 0;
			if (tbefore instanceof VariableTerm)
			{
				bi.beforeFixed = false;
				bi.varBefore = (VariableTerm) tbefore;
			}
			else if (tbefore instanceof IntegerTerm)
			{
				bi.beforeFixed = true;
				bi.before = ((IntegerTerm) tbefore).value;
				if (bi.before < 0)
				{
					PrologException.domainError(TermConstants.notLessThanZeroAtom, tbefore);
				}
			}
			else
			{
				PrologException.typeError(TermConstants.integerAtom, tbefore);
			}
			if (tlength instanceof VariableTerm)
			{
				bi.lengthFixed = false;
				bi.varLength = (VariableTerm) tlength;
			}
			else if (tlength instanceof IntegerTerm)
			{
				bi.lengthFixed = true;
				bi.length = ((IntegerTerm) tlength).value;
				if (bi.length < 0)
				{
					PrologException.domainError(TermConstants.notLessThanZeroAtom, tlength);
				}
				if (bi.length > bi.atomLen)
				{
					return FAIL;
				}
			}
			else
			{
				PrologException.typeError(TermConstants.integerAtom, tlength);
			}
			if (tafter instanceof VariableTerm)
			{
				bi.afterFixed = false;
				bi.varAfter = (VariableTerm) tafter;
			}
			else if (tafter instanceof IntegerTerm)
			{
				bi.afterFixed = true;
				bi.after = ((IntegerTerm) tafter).value;
				if (bi.after < 0)
				{
					PrologException.domainError(TermConstants.notLessThanZeroAtom, tafter);
				}
			}
			else
			{
				PrologException.typeError(TermConstants.integerAtom, tafter);
			}

			if (tsub_atom instanceof VariableTerm)
			{
				bi.subAtomFixed = false;
				bi.varSubAtom = (VariableTerm) tsub_atom;
			}
			else if (tsub_atom instanceof AtomTerm)
			{
				AtomTerm a = (AtomTerm) tsub_atom;
				if (bi.lengthFixed)
				{
					if (bi.length != a.value.length())
					{
						return FAIL;
					}
				}
				else
				{
					bi.lengthFixed = true;
					bi.length = a.value.length();
					if (bi.length > bi.atomLen)
					{
						return FAIL;
					}
				}
				bi.subAtomFixed = true;
				bi.subAtom = a;
			}
			else
			{
				PrologException.typeError(TermConstants.atomAtom, tsub_atom);
			}
			return nextSolution(interpreter, bi);
		}
	}

	private static int nextSolution(Interpreter interpreter, SubAtomBacktrackInfo bi)
	{
		while (true)
		{
			if (bi.currentLen > bi.atomLen - bi.currentPos)
			{
				bi.currentLen = 0;
				bi.currentPos++;
				if (bi.currentPos > bi.atomLen)
				{
					return FAIL;
				}
			}

			int len = bi.currentLen;
			int pos = bi.currentPos;
			bi.currentLen++;

			if (bi.beforeFixed && pos != bi.before)
			{
				continue;
			}
			if (bi.lengthFixed && len != bi.length)
			{
				continue;
			}
			if (bi.afterFixed && bi.atomLen - (pos + len) != bi.after)
			{
				continue;
			}
			if (bi.subAtomFixed && !bi.atom.value.regionMatches(pos, bi.subAtom.value, 0, len))
			{
				continue;
			}
			// unify
			if (bi.varBefore != null)
			{
				interpreter.addVariableUndo(bi.varBefore);
				bi.varBefore.value = IntegerTerm.get(pos);
			}
			if (bi.varLength != null)
			{
				interpreter.addVariableUndo(bi.varLength);
				bi.varLength.value = IntegerTerm.get(len);
			}
			if (bi.varAfter != null)
			{
				interpreter.addVariableUndo(bi.varAfter);
				bi.varAfter.value = IntegerTerm.get(bi.atomLen - (pos + len));
			}
			if (bi.varSubAtom != null)
			{
				interpreter.addVariableUndo(bi.varSubAtom);
				bi.varSubAtom.value = AtomTerm.get(bi.atom.value.substring(pos, pos + len));
			}
			interpreter.pushBacktrackInfo(bi);
			return SUCCESS;
		}
	}
}
