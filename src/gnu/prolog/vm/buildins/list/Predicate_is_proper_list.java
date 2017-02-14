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
package gnu.prolog.vm.buildins.list;

import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_is_proper_list extends ExecuteOnlyCode
{
	public Predicate_is_proper_list()
	{}

	@Override
	public RC execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		Term list = args[0];
		while (list != null)
		{
			if (TermConstants.emptyListAtom.equals(list))
			{
				return RC.SUCCESS_LAST;
			}
			if (!CompoundTerm.isListPair(list))
			{
				return RC.FAIL;
			}
			CompoundTerm ct = (CompoundTerm) list;
			if (ct.args.length != 2)
			{
				return RC.FAIL;
			}
                        list = ct.args[1].dereference();
		}
		return RC.FAIL;
	}
}
