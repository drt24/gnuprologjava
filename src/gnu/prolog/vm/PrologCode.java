/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2010       Daniel Thomas
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
package gnu.prolog.vm;

/**
 * Implementing classes can be executed and return a return code of
 * {@link RC#SUCCESS}, {@link RC#SUCCESS_LAST}, or {@link RC#FAIL}.
 * 
 * {@link gnu.prolog.vm.buildins.imphooks.Predicate_halt Predicate_halt} can
 * also return {@link RC#HALT}
 */
public interface PrologCode extends Installable
{
	public static enum RC
	{
		/**
		 * predicate was returned with success, backtrack info was created, and
		 * re-execute is possible.
		 */
		SUCCESS,
		/** predicate was returned with success, backtrack info was not created */
		SUCCESS_LAST,
		/** predicate failed */
		FAIL,
		/**
		 * returned by the interpreter when it was halted, should never be returned
		 * by prolog code
		 */
		HALT
	}

	/**
	 * this method is used for execution of code
	 * 
	 * @param interpreter
	 *          interpreter in which context code is executed
	 * @param backtrackMode
	 *          true if predicate is called on backtracking and false otherwise
	 * @param args
	 *          arguments of code
	 * @return either RC.SUCCESS, RC.SUCCESS_LAST, or RC.FAIL.
	 * @throws PrologException
	 */
	public RC execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[]) throws PrologException;

}
