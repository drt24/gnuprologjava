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
package gnu.prolog.vm.interpreter;

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;

import java.util.ArrayList;
import java.util.List;

/** execution state class */
public class ExecutionState
{
	// TODO: use an enum here.
	/** instruction return code indicating need for backtracking */
	public final static int BACKTRACK = -1;
	/**
	 * instruction return code indicating success. backtrack information may or
	 * not be created
	 */
	public final static int NEXT = 0;

	/**
	 * instruction return code indicating success. jump to position in instruction
	 * pointer needed
	 */
	public final static int JUMP = 1;

	/**
	 * instruction return code indicating success. return from function needed.
	 */
	public final static int RETURN = 2;

	/** current interpreter */
	public Interpreter interpreter;
	/** current backtrack info, only active on backtracking */
	public BacktrackInfo startBacktrackInfo;
	/** current environment */
	public Term environment[];
	/** pushdown stack, used for unification and calls */
	public List<Term> pushdown = new ArrayList<Term>();
	/** current position */
	public int jumpPosition;
	/** arguments */
	public Term args[];

	/**
	 * pop backtrack info from stack
	 * 
	 * @return the popped backtrack info from the stack
	 */
	public BacktrackInfo popBacktrackInfo()
	{
		return interpreter.popBacktrackInfo();
	}

	/**
	 * peek backtrack info w/o popping it
	 * 
	 * @return the top of the backtrack info stack without popping
	 */
	public BacktrackInfo peekBacktrackInfo()
	{
		return interpreter.peekBacktrackInfo();
	}

	/**
	 * push backtrack info to stack
	 * 
	 * @param cbi
	 */
	public void pushBacktrackInfo(BacktrackInfo cbi)
	{
		interpreter.pushBacktrackInfo(cbi);
	}

	/**
	 * get BacktrackInfo for call instruction.
	 * 
	 * @param codePosition
	 * @param args
	 * @param code
	 * @param tag
	 * @return BacktrackInfo for call instruction
	 */
	public CallBacktrackInfo getCallBacktrackInfo(int codePosition, Term args[], PrologCode code, CompoundTermTag tag)
	{
		return new CallBacktrackInfo(interpreter.getUndoPosition(), codePosition, args, code, tag);
	}

	/**
	 * get BacktrackInfo for try family instructions.
	 * 
	 * @param retryPosition
	 * @return BacktrackInfo for try family instructions
	 */
	public RetryBacktrackInfo getRetryBacktrackInfo(int retryPosition)
	{
		return new RetryBacktrackInfo(interpreter.getUndoPosition(), retryPosition);
	}

	/**
	 * pop term from pushdown stack
	 * 
	 * @return term removed from pushdown stack
	 */
	public Term popPushDown()
	{
		return pushdown.remove(pushdown.size() - 1);
	}

	/**
	 * push term to pushdown stack
	 * 
	 * @param term
	 */
	public void pushPushDown(Term term)
	{
		pushdown.add(term);
	}

	/**
	 * get term from environment
	 * 
	 * @param environmentIndex
	 * @return term from environment
	 */
	public Term getEnvironment(int environmentIndex)
	{
		return environment[environmentIndex];
	}

	/**
	 * put term to environment
	 * 
	 * @param environmentIndex
	 * @param term
	 */
	public void putEnvironment(int environmentIndex, Term term)
	{
		environment[environmentIndex] = term;
	}

	/**
	 * get leave backtrack info
	 * 
	 * @return the leave backtrack info
	 */
	public LeaveBacktrackInfo getLeaveBacktrackInfo()
	{
		return new LeaveBacktrackInfo(interpreter.getUndoPosition(), this);
	}

	/**
	 * get enter backtrack info
	 * 
	 * @return the enter backtrack info
	 */
	public EnterBacktrackInfo getEnterBacktrackInfo()
	{
		return new EnterBacktrackInfo(interpreter.getUndoPosition());
	}

}
