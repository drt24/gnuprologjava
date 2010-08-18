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
import gnu.prolog.io.WriteOptions;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * prolog code
 */
public class Predicate_write_term extends ExecuteOnlyCode
{
	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		PrologStream stream = interpreter.getEnvironment().resolveStream(args[0]);
		Term optionsList = args[2];
		WriteOptions options = new WriteOptions(interpreter.getEnvironment().getOperatorSet());

		// parse options
		Term cur = optionsList;
		while (cur != TermConstants.emptyListAtom)
		{
			if (cur instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (!(cur instanceof CompoundTerm))
			{
				PrologException.typeError(TermConstants.listAtom, optionsList);
			}
			CompoundTerm ct = (CompoundTerm) cur;
			if (ct.tag != TermConstants.listTag)
			{
				PrologException.typeError(TermConstants.listAtom, optionsList);
			}
			Term head = ct.args[0].dereference();
			cur = ct.args[1].dereference();
			if (head instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (!(head instanceof CompoundTerm))
			{
				PrologException.domainError(TermConstants.readOptionAtom, head);
			}
			CompoundTerm op = (CompoundTerm) head;
			if (op.tag == TermConstants.quotedTag)
			{
				Term val = op.args[0].dereference();
				if (val != TermConstants.trueAtom && val != TermConstants.falseAtom)
				{
					PrologException.domainError(TermConstants.readOptionAtom, head);
				}
				options.quoted = val == TermConstants.trueAtom;
			}
			else if (op.tag == TermConstants.ignoreOpsTag)
			{
				Term val = op.args[0].dereference();
				if (val != TermConstants.trueAtom && val != TermConstants.falseAtom)
				{
					PrologException.domainError(TermConstants.readOptionAtom, head);
				}
				options.ignoreOps = val == TermConstants.trueAtom;
			}
			else if (op.tag == TermConstants.numbervarsTag)
			{
				Term val = op.args[0].dereference();
				if (val != TermConstants.trueAtom && val != TermConstants.falseAtom)
				{
					PrologException.domainError(TermConstants.readOptionAtom, head);
				}
				options.numbervars = val == TermConstants.trueAtom;
			}
			else
			{
				PrologException.domainError(TermConstants.writeOptionAtom, head);
			}
		}
		stream.writeTerm(args[0], interpreter, options, args[1]);
		return SUCCESS_LAST;
	}
}
