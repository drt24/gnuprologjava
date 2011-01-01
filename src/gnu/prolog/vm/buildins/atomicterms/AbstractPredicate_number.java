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
package gnu.prolog.vm.buildins.atomicterms;

import gnu.prolog.database.Pair;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * 
 * @author Daniel Thomas
 */
public abstract class AbstractPredicate_number extends ExecuteOnlyCode
{

	/**
	 * @see CompoundTerm#getInstantiatedHeadBody
	 * @param term
	 * @param numberIsVariable
	 * @return a (head,body) Pair or may be null if numberIsVariable is false
	 * @throws PrologException
	 *           in the event of type or instantiation errors
	 */
	protected static Pair<Term, Term> getInstantiatedHeadBody(Term term, boolean numberIsVariable) throws PrologException
	{
		if (term instanceof VariableTerm)
		{
			if (numberIsVariable)
			{
				PrologException.instantiationError(term);
			}
			else
			{
				return null;
			}
		}
		if (!CompoundTerm.isListPair(term))
		{
			PrologException.typeError(TermConstants.listAtom, term);
		}
		CompoundTerm ct = (CompoundTerm) term;
		Term head = ct.args[0].dereference();
		term = ct.args[1].dereference();
		if (head instanceof VariableTerm)
		{
			if (numberIsVariable)
			{
				PrologException.instantiationError(head);
			}
			else
			{
				return null;
			}
		}
		return new Pair<Term, Term>(head, term);
	}
}
