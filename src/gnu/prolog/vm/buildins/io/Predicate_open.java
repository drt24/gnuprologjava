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
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * prolog code for open/4
 */
public class Predicate_open extends ExecuteOnlyCode
{
	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term tsource_sink = args[0];
		Term tmode = args[1];
		Term tstream = args[2];
		Term optionsList = args[3];

		AtomTerm source_sink = null;
		AtomTerm mode = null;
		VariableTerm vstream = null;
		PrologStream.OpenOptions options = new PrologStream.OpenOptions(source_sink, mode, interpreter.getEnvironment());

		// check source/sink
		if (tsource_sink instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		if (!(tsource_sink instanceof AtomTerm))
		{
			PrologException.domainError(TermConstants.sourceSinkAtom, tsource_sink);
		}
		source_sink = (AtomTerm) tsource_sink;
		// check mode
		if (tmode instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		if (!(tmode instanceof AtomTerm))
		{
			PrologException.typeError(TermConstants.atomAtom, tmode);
		}
		if (tmode != PrologStream.readAtom && tmode != PrologStream.writeAtom && tmode != PrologStream.appendAtom)
		{
			PrologException.domainError(TermConstants.ioModeAtom, tmode);
		}
		mode = (AtomTerm) tmode;
		// check stream
		if (!(tstream instanceof VariableTerm))
		{
			PrologException.typeError(TermConstants.variableAtom, tstream);
		}
		vstream = (VariableTerm) tstream;
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
				PrologException.domainError(TermConstants.streamOptionAtom, head);
			}
			CompoundTerm op = (CompoundTerm) head;
			if (op.tag == PrologStream.typeTag)
			{
				Term val = op.args[0].dereference();
				if (val != PrologStream.textAtom && val != PrologStream.binaryAtom)
				{
					PrologException.domainError(TermConstants.streamOptionAtom, op);
				}
				options.type = (AtomTerm) val;
			}
			else if (op.tag == PrologStream.repositionTag)
			{
				Term val = op.args[0].dereference();
				if (val != TermConstants.trueAtom && val != TermConstants.falseAtom)
				{
					PrologException.domainError(TermConstants.streamOptionAtom, op);
				}
				options.reposition = (AtomTerm) val;
			}
			else if (op.tag == PrologStream.aliasTag)
			{
				Term val = op.args[0].dereference();
				if (!(val instanceof AtomTerm))
				{
					PrologException.domainError(TermConstants.streamOptionAtom, op);
				}
				options.aliases.add((AtomTerm) val);
			}
			else if (op.tag == PrologStream.eofActionTag)
			{
				Term val = op.args[0].dereference();
				if (val != PrologStream.errorAtom && val != PrologStream.eofCodeAtom && val != PrologStream.resetAtom)
				{
					PrologException.domainError(TermConstants.streamOptionAtom, op);
				}
				options.reposition = (AtomTerm) val;
			}
			else
			{
				PrologException.domainError(TermConstants.streamOptionAtom, op);
			}
		}
		options.filename = source_sink;
		options.mode = mode;
		vstream.value = interpreter.getEnvironment().open(source_sink, mode, options);
		interpreter.addVariableUndo(vstream);
		return SUCCESS_LAST;
	}
}
