/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 * Copyright (C) 2010       Daniel Thomas
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
package gnu.prolog.vm.buildins.io;

import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.database.PrologTextLoaderState;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;

/**
 * NONISO but in SWI-Prolog, not in GNU Prolog (even in its ISO usage).
 * 
 * @author Daniel Thomas
 */
public class Predicate_ensure_loaded extends ExecuteOnlyCode
{
	public Predicate_ensure_loaded()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		Environment environment = interpreter.getEnvironment();
		PrologTextLoaderState state = environment.getPrologTextLoaderState();

		state.ensureLoaded(args[0]);// actually do the loading
		// ensure that any initializations are run.
		environment.runInitialization(interpreter);

		// loader errors could be generated and need to be displayed somewhere.
		for (PrologTextLoaderError error : state.getErrors())
		{
			System.err.println(error.toString());
		}
		return SUCCESS_LAST;
	}
}
