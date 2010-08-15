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
package gnu.prolog.vm.interpreter.instruction;

import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.interpreter.ExecutionState;

/**
 * create compound term
 */
public class ICreateCompoundTerm extends Instruction
{
	/** position of term in environment */
	public CompoundTermTag tag;

	/**
	 * a constructor
	 * 
	 * @param tag
	 */
	public ICreateCompoundTerm(CompoundTermTag tag)
	{
		this.tag = tag;
	}

	/** convert instruction to string */
	@Override
	public String toString()
	{
		return codePosition + ": create_struct " + tag.functor.value + "/" + tag.arity;
	}

	/**
	 * execute call instruction within specified sate
	 * 
	 * @param state
	 *          state within which instruction will be executed
	 * @return instruction to caller how to execute next instruction
	 * @throws PrologException if code is throwing prolog exception
	 */
	@Override
	public int execute(ExecutionState state, BacktrackInfo bi) throws PrologException
	{
		int arity = tag.arity;
		Term args[] = new Term[arity];
		for (int i = arity - 1; i >= 0; i--)
		{
			args[i] = state.popPushDown().dereference();
		}
		state.pushPushDown(new CompoundTerm(tag, args));
		return ExecutionState.NEXT;
	}
}
