/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
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
package gnu.prolog.vm.buildins.debug;

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;

/**
 * Remove a trace point
 * 
 * @author Michiel Hendriks
 */
public class Predicate_nospy extends ExecuteOnlyCode
{
	public Predicate_nospy()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		CompoundTermTag tag = Predicate_spy.getTag(args[0]);
		if (tag.arity == -1)
		{
			for (CompoundTermTag ptag : interpreter.getEnvironment().getModule().getPredicateTags())
			{
				if (ptag.functor.equals(tag.functor))
				{
					interpreter.getTracer().removeTrace(ptag);
				}
			}
		}
		else
		{
			interpreter.getTracer().removeTrace(tag);
		}
		return SUCCESS_LAST;
	}
}
