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
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Installable;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.interpreter.ExecutionState;

/** base call for instruction */
public abstract class Instruction implements Installable
{
	/** code position of instruction */
	public int codePosition;

	/**
	 * execute instruction
	 * 
	 * @param state
	 *          state within which instruction will be executed
	 * @param bi
	 * @return ExecutionState.{BACKTRACK,NEXT,JUMP,RETURN}: instruction to caller
	 *         how to execute next instruction
	 * @throws PrologException
	 *           if code is throwing prolog exception
	 */
	public abstract int execute(ExecutionState state, BacktrackInfo bi) throws PrologException;

	/** install instruction to environment */
	public void install(Environment env)
	{}

	/** uninstall instruction from environment */
	public void uninstall(Environment env)
	{}
}
