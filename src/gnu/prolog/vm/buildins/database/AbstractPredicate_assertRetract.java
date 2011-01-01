/* GNU Prolog for Java
 * Copyright (C) 2011       Daniel Thomas
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
package gnu.prolog.vm.buildins.database;

import gnu.prolog.database.Predicate;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * 
 * @author Daniel Thomas
 */
public abstract class AbstractPredicate_assertRetract extends ExecuteOnlyCode
{

	protected static PredicateTagHeadBody execute(Term clause, Interpreter interpreter) throws PrologException
	{
		Term head = null;
		Term body = null;
		if (clause instanceof VariableTerm)
		{
			PrologException.instantiationError(clause);
		}
		else if (clause instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) clause;
			if (ct.tag == TermConstants.clauseTag)
			{
				head = ct.args[0].dereference();
				body = ct.args[1].dereference();
			}
			else
			{
				head = ct;
				body = TermConstants.trueAtom;
			}
		}
		else if (clause instanceof AtomTerm)
		{
			head = clause;
			body = TermConstants.trueAtom;
		}
		else
		{
			PrologException.typeError(TermConstants.callableAtom, clause);
		}
		CompoundTermTag predTag = null;
		if (head instanceof VariableTerm)
		{
			PrologException.instantiationError(head);
		}
		else if (head instanceof CompoundTerm)
		{
			predTag = ((CompoundTerm) head).tag;
		}
		else if (head instanceof AtomTerm)
		{
			predTag = CompoundTermTag.get((AtomTerm) head, 0);
		}
		else
		{
			PrologException.typeError(TermConstants.callableAtom, head);
		}
		return new PredicateTagHeadBody(interpreter.getEnvironment().getModule().getDefinedPredicate(predTag), predTag,
				head, body);
	}

	protected static class PredicateTagHeadBody
	{
		public final Predicate predicate;
		public final CompoundTermTag tag;
		public final Term head;
		public final Term body;

		public PredicateTagHeadBody(Predicate predicate, CompoundTermTag predicateTag, Term head, Term body)
		{
			this.predicate = predicate;
			tag = predicateTag;
			this.head = head;
			this.body = body;
		}
	}
}
