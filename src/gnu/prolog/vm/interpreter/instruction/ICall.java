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

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologCodeListener;
import gnu.prolog.vm.PrologCodeUpdatedEvent;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.interpreter.CallBacktrackInfo;
import gnu.prolog.vm.interpreter.ExecutionState;

/**
 * call instruction.
 */
public class ICall extends Instruction implements PrologCodeListener
{
	/**
	 * a constructor
	 * 
	 * @param tag
	 */
	public ICall(CompoundTermTag tag)
	{
		this.tag = tag;
	}

	/** convert instruction to string */
	@Override
	public String toString()
	{
		return codePosition + ": call " + tag.functor.value + "/" + tag.arity;
	}

	/** tag of predicate to call */
	public CompoundTermTag tag;
	/** code to call, if predicate is dynamic it changes after each update */
	public transient PrologCode code;

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
		// System.err.print("calling: "+gnu.prolog.io.TermWriter.toString(tag.getPredicateIndicator()));
		/*
		 * cast current backtrack info, if it is not nill is is already popped from
		 * stack
		 */
		CallBacktrackInfo cbi = (CallBacktrackInfo) bi;
		boolean backtrack;
		Term args[];
		PrologCode code;
		if (cbi != null)
		{
			/* if redo just undo any changes up to this point */
			// cbi.undo(state.interpreter);
			backtrack = true;
			args = cbi.args;
			code = cbi.code; // take the same code as it was at first call
		}
		else
		{
			backtrack = false;
			int arity = tag.arity;
			args = new Term[arity];
			for (int i = arity - 1; i >= 0; i--)
			{
				args[i] = state.popPushDown().dereference();
				// System.err.print(" "+gnu.prolog.io.TermWriter.toString(args[i]));
			}
			// System.err.println();
			/* ensure that predicate is loaded from stack */
			ensureLoaded(state.interpreter.getEnvironment());
			code = this.code;
		}
		/* call code, with last backtrack info if applicable */
		int rc = code.execute(state.interpreter, backtrack, args);
		switch (rc)
		{
			case PrologCode.SUCCESS:
				/* push backtrack info */
				state.pushBacktrackInfo(state.getCallBacktrackInfo(codePosition, args, code, tag));
				rc = ExecutionState.NEXT; /* proceed to next instruction */
				// System.err.println("success: "+gnu.prolog.io.TermWriter.toString(tag.getPredicateIndicator()));
				break;
			case PrologCode.SUCCESS_LAST:
				rc = ExecutionState.NEXT; /* proceed to next instruction */
				// System.err.println("success last: "+gnu.prolog.io.TermWriter.toString(tag.getPredicateIndicator()));
				break;
			case PrologCode.FAIL:
				rc = ExecutionState.BACKTRACK; /* backtrack */
				// System.err.println("fail: "+gnu.prolog.io.TermWriter.toString(tag.getPredicateIndicator()));
				break;
		}
		return rc;
	}

	/** notify instruction that prolog code for predicate was updated */
	public void prologCodeUpdated(PrologCodeUpdatedEvent evt)
	{
		code = null; /* reset pointer */
	}

	/** install instruction to environment */
	@Override
	public void install(Environment env)
	{
		env.addPrologCodeListener(tag, this);
	}

	/** uninstall instruction from environment */
	@Override
	public void uninstall(Environment env)
	{
		env.removePrologCodeListener(tag, this);
	}

	/**
	 * ensure that code is loaded in predicate
	 * 
	 * @param env
	 * @throws PrologException
	 */
	protected void ensureLoaded(Environment env) throws PrologException
	{
		if (code == null)
		{
			code = env.getPrologCode(tag);
		}
	}
}
