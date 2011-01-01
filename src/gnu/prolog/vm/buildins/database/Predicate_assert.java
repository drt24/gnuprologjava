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
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * prolog code
 */
public abstract class Predicate_assert extends AbstractPredicate_assertRetract
{
	/**
	 * assert a clause
	 * 
	 * @param p
	 * @param clause
	 */
	protected abstract void assertPred(Predicate p, CompoundTerm clause);

	@Override
	public RC execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[]) throws PrologException
	{
		Term clause = args[0];

		PredicateTagHeadBody predicateTagHeadBody = execute(clause, interpreter);

		Predicate p = predicateTagHeadBody.predicate;
		CompoundTermTag predTag = predicateTagHeadBody.tag;
		Term head = predicateTagHeadBody.head;
		Term body = predicateTagHeadBody.body;

		body = Predicate.prepareBody(body);

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

		return RC.SUCCESS_LAST;
	}
}
