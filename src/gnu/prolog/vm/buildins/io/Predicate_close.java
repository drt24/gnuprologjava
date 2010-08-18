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
package gnu.prolog.vm.buildins.io;

import gnu.prolog.io.PrologStream;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * prolog code
 */
public class Predicate_close extends ExecuteOnlyCode
{

	CompoundTermTag forceTag = CompoundTermTag.get("force", 1);

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term cur = args[1];
		Term force = TermConstants.falseAtom;
		while (cur != TermConstants.emptyListAtom)
		{
			if (cur instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (!(cur instanceof CompoundTerm))
			{
				PrologException.typeError(TermConstants.listAtom, args[1]);
			}
			CompoundTerm ct = (CompoundTerm) cur;
			if (ct.tag != TermConstants.listTag)
			{
				PrologException.typeError(TermConstants.listAtom, args[1]);
			}
			Term head = ct.args[0].dereference();
			cur = ct.args[1].dereference();

			if (head instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (!(head instanceof CompoundTerm))
			{
				PrologException.domainError(TermConstants.closeOptionAtom, head);
			}
			CompoundTerm e = (CompoundTerm) head;
			if (e.tag != forceTag || e.args[0] != TermConstants.trueAtom && e.args[0] != TermConstants.falseAtom)
			{
				PrologException.domainError(TermConstants.closeOptionAtom, head);
			}
			force = e.args[0];
		}

		PrologStream stream = interpreter.getEnvironment().resolveStream(args[0]);
		if (stream == interpreter.getEnvironment().getUserInput())
		{
			return SUCCESS_LAST;
		}
		if (stream == interpreter.getEnvironment().getUserOutput())
		{
			return SUCCESS_LAST;
		}
		stream.close(force == TermConstants.trueAtom);
		return SUCCESS_LAST;
	}
}
