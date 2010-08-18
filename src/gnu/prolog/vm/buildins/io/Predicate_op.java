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

import gnu.prolog.io.Operator;
import gnu.prolog.io.OperatorSet;
import gnu.prolog.io.Operator.SPECIFIER;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * prolog code
 */
public class Predicate_op extends ExecuteOnlyCode
{
	static final AtomTerm commaAtom = AtomTerm.get(",");

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term tpriority = args[0];
		Term topspec = args[1];
		Term tops = args[2];

		int priority = 0; // parsed priority
		SPECIFIER opspec = SPECIFIER.NONE; // parsed operator specifier
		Set<AtomTerm> ops = new HashSet<AtomTerm>(); // set of operators
		OperatorSet opSet = interpreter.getEnvironment().getOperatorSet();

		// parse arguments
		// priority
		if (tpriority instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		if (!(tpriority instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, tpriority);
		}
		priority = ((IntegerTerm) tpriority).value;
		if (priority < 0 || 1200 < priority)
		{
			PrologException.domainError(TermConstants.operatorPriorityAtom, tpriority);
		}
		// specifier
		if (topspec instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		if (!(topspec instanceof AtomTerm))
		{
			PrologException.typeError(TermConstants.atomAtom, topspec);
		}
		AtomTerm atomSpec = (AtomTerm) topspec;

		opspec = SPECIFIER.fromAtom(atomSpec);

		if (opspec == SPECIFIER.NONE)
		{
			PrologException.domainError(TermConstants.operatorSpecifierAtom, topspec);
		}
		// parse ops
		if (tops == TermConstants.emptyListAtom)
		{
			// do nothing
		}
		else if (tops instanceof AtomTerm)
		{
			validateOp(priority, opspec, (AtomTerm) tops, opSet);
			ops.add((AtomTerm) tops);
		}
		else if (tops instanceof CompoundTerm)
		{
			Term cur = tops;
			while (cur != TermConstants.emptyListAtom)
			{
				if (cur instanceof VariableTerm)
				{
					PrologException.instantiationError();
				}
				if (!(cur instanceof CompoundTerm))
				{
					PrologException.typeError(TermConstants.listAtom, tops);
				}
				CompoundTerm ct = (CompoundTerm) cur;
				if (ct.tag != TermConstants.listTag)
				{
					PrologException.typeError(TermConstants.listAtom, tops);
				}
				Term head = ct.args[0].dereference();
				cur = ct.args[1].dereference();
				if (head instanceof VariableTerm)
				{
					PrologException.instantiationError();
				}
				if (!(head instanceof AtomTerm))
				{
					PrologException.typeError(TermConstants.atomAtom, head);
				}
				validateOp(priority, opspec, (AtomTerm) head, opSet);
				ops.add((AtomTerm) head);
			}
		}
		else
		{
			PrologException.typeError(TermConstants.listAtom, tops);
		}
		if (priority == 0) // if remove requested
		{
			Iterator<AtomTerm> i = ops.iterator();
			while (i.hasNext())
			{
				AtomTerm op = i.next();
				opSet.remove(opspec, op.value);
			}
		}
		else
		{
			Iterator<AtomTerm> i = ops.iterator();
			while (i.hasNext())
			{
				AtomTerm op = i.next();
				opSet.add(priority, opspec, op.value);
			}
		}
		return SUCCESS_LAST;
	}

	private static void validateOp(int priority, SPECIFIER specifier, AtomTerm opAtom, OperatorSet opSet)
			throws PrologException
	{
		if (opAtom == commaAtom)
		{
			PrologException.permissionError(TermConstants.modifyAtom, TermConstants.operatorAtom, opAtom);
		}
		switch (specifier)
		{
			case FX:
			case FY:
				break;
			case XF:
			case YF:
			{
				Operator op = opSet.lookupXf(opAtom.value);
				if (op.specifier != SPECIFIER.YF && specifier != SPECIFIER.XF)
				{
					PrologException.permissionError(TermConstants.createAtom, TermConstants.operatorAtom, opAtom);
				}
				break;
			}
			case XFX:
			case XFY:
			case YFX:
			{
				Operator op = opSet.lookupXf(opAtom.value);
				if (op.specifier == SPECIFIER.YF || specifier == SPECIFIER.XF)
				{
					PrologException.permissionError(TermConstants.createAtom, TermConstants.operatorAtom, opAtom);
				}
				break;
			}
		}
	}
}
