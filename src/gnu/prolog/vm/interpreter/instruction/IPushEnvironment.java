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

import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.interpreter.ExecutionState;

/**
 * push term from environment
 */
public class IPushEnvironment extends Instruction
{
	/**
	 * push ter from environment
	 * 
	 * @param environmentPosition
	 */
	public IPushEnvironment(int environmentPosition)
	{
		this.environmentPosition = environmentPosition;
	}

	/** position of term in environment */
	public int environmentPosition;

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
		state.pushPushDown(state.getEnvironment(environmentPosition));
		return ExecutionState.NEXT;
	}

	/** convert instruction to string */
	@Override
	public String toString()
	{
		return codePosition + ": push_environment " + environmentPosition;
	}

}
