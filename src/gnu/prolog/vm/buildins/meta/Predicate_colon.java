/* GNU Prolog for Java
 * Copyright (C) 2016 Matt Lilley
 *
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

package gnu.prolog.vm.buildins.meta;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.TermConstants;
import gnu.prolog.vm.interpreter.Predicate_call;


/**
 * @author Matt Lilley
 * 
 */

public class Predicate_colon extends ExecuteOnlyCode
{
	@Override
	public RC execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[]) throws PrologException
	{
		Environment environment = interpreter.getEnvironment();
		if (!(args[0] instanceof AtomTerm))
		{
			PrologException.typeError(TermConstants.atomAtom, args[0]);
		}
		AtomTerm moduleName = (AtomTerm)args[0];
		RC rc;
		BacktrackInfo bi = null;
		if (backtrackMode)
		{
			bi = interpreter.popBacktrackInfo();
			bi.undo(interpreter);
		}
		// If this fails we want to let the exception bubble up. We DEFINITELY do not want to pop later unless we have pushed successfully!
		environment.pushModule(moduleName);
		try
		{
			rc = Predicate_call.staticExecute(interpreter, backtrackMode, args[1]);
			if (rc == RC.SUCCESS)
			{
				// Make a fake backtrack point here so we get a chance to restore the module context if backtracking
                                bi = new BacktrackInfo(interpreter.getUndoPosition(), -1);
				interpreter.pushBacktrackInfo(bi);
			}
		}
		finally
		{
			environment.popModule();
		}
		return rc;
	}
}
