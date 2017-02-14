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
package gnu.prolog.vm.buildins.list;

import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.TermComparator;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * sort(+List, -Sorted)
 * 
 * @author Daniel Thomas
 */
public abstract class Abstract_sort extends ExecuteOnlyCode
{
	@Override
	public RC execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		Term listTerm = args[0];
		Term sortedTerm = args[1];

		if (TermConstants.emptyListAtom.equals(listTerm))
		{
			RC rc = interpreter.unify(listTerm, sortedTerm);
			if (rc == RC.FAIL)
			{
				Term[] arrayOfSorted = { sortedTerm };
				if (new Predicate_is_list().execute(interpreter, false, arrayOfSorted) == RC.FAIL)
				{
					PrologException.typeError(TermConstants.listAtom, sortedTerm);
				}
			}
			return rc;
		}
		else if (listTerm.dereference() instanceof VariableTerm)
		{
			PrologException.instantiationError(listTerm);
		}

		if (CompoundTerm.isListPair(listTerm))
		{
                        Term[] arrayOfList = { listTerm.dereference() };
			if (new Predicate_is_proper_list().execute(interpreter, false, arrayOfList) == RC.FAIL)
			{
				PrologException.instantiationError();
			}
			List<Term> list = makeList(listTerm);
			Collections.sort(list, getComparator());
			Term result = CompoundTerm.getList(list);
			return interpreter.unify(sortedTerm, result);
		}
		else
		{
			PrologException.typeError(TermConstants.listAtom, listTerm);
		}

		throw new IllegalStateException("Exection cannot reach this location");
	}

	protected abstract List<Term> makeList(Term listTerm);

	/**
	 * Get the comparator to use to do the sorting.
	 * 
	 * @return a Comparator for Terms
	 * 
	 * @see Predicate_predsort#getComparator()
	 */
	protected Comparator<? super Term> getComparator()
	{
		return new TermComparator();
	}
}
