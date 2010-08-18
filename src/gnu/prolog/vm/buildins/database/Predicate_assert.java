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
 * prolog code
 */
public abstract class Predicate_assert extends ExecuteOnlyCode
{
	/**
	 * assert a clause
	 * 
	 * @param p
	 * @param clause
	 */
	protected abstract void assertPred(Predicate p, CompoundTerm clause);

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		Term clause = args[0];
		Term head = null;
		Term body = null;
		if (clause instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		else if (clause instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) clause;
			if (ct.tag == TermConstants.clauseTag)
			{
				head = ct.args[0].dereference();
				body = ct.args[1].dereference();
				body = prepareBody(body, body);
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
			PrologException.instantiationError();
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
		Predicate p = interpreter.getEnvironment().getModule().getDefinedPredicate(predTag);
		if (p == null)
		{
			p = interpreter.getEnvironment().getModule().createDefinedPredicate(predTag);
			p.setType(Predicate.TYPE.USER_DEFINED);
			p.setDynamic();
		}
		else if (p.getType() == Predicate.TYPE.USER_DEFINED)
		{
			if (!p.isDynamic())
			{
				PrologException.permissionError(TermConstants.modifyAtom, TermConstants.staticProcedureAtom, predTag
						.getPredicateIndicator());
			}
		}
		else
		{
			PrologException.permissionError(TermConstants.modifyAtom, TermConstants.staticProcedureAtom, predTag
					.getPredicateIndicator());
		}
		assertPred(p, (CompoundTerm) new CompoundTerm(TermConstants.clauseTag, head, body).clone());
		return SUCCESS_LAST;
	}

	public static Term prepareBody(Term body, Term term) throws PrologException
	{
		if (body instanceof VariableTerm)
		{
			return new CompoundTerm(TermConstants.callTag, body);
		}
		else if (body instanceof AtomTerm)
		{
			return body;
		}
		else if (body instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) body;
			if (ct.tag == TermConstants.conjunctionTag || ct.tag == TermConstants.disjunctionTag
					|| ct.tag == TermConstants.ifTag)
			{
				return new CompoundTerm(ct.tag, prepareBody(ct.args[0].dereference(), term), prepareBody(ct.args[1]
						.dereference(), term));
			}
			return body;
		}
		else
		{
			PrologException.typeError(TermConstants.callableAtom, term);
			return null;
		}
	}

}
