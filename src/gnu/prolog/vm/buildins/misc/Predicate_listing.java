/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA. The text ol license can be also found
 * at http://www.gnu.org/copyleft/lgpl.html
 */
package gnu.prolog.vm.buildins.misc;

import gnu.prolog.database.Predicate;
import gnu.prolog.io.WriteOptions;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.PrologStream;
import gnu.prolog.vm.buildins.debug.Predicate_spy;

/**
 * @author Michiel Hendriks
 */
public class Predicate_listing implements PrologCode
{
	public Predicate_listing()
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#execute(gnu.prolog.vm.Interpreter, boolean,
	 * gnu.prolog.term.Term[])
	 */
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		CompoundTermTag filter = null;
		if (args.length >= 1)
		{
			filter = Predicate_spy.getTag(args[0]);
		}
		WriteOptions options = new WriteOptions();
		options.declaredVariableNames = true;
		options.operatorSet = interpreter.environment.getOperatorSet();
		options.numbervars = true;
		options.quoted = true;
		PrologStream stream = interpreter.environment.getCurrentOutput();
		for (CompoundTermTag tag : interpreter.environment.getModule().getPredicateTags())
		{
			if (filter != null)
			{
				if (!tag.functor.equals(filter.functor))
				{
					continue;
				}
				if (filter.arity != -1)
				{
					if (tag.arity != filter.arity)
					{
						continue;
					}
				}
			}
			Predicate p = interpreter.environment.getModule().getDefinedPredicate(tag);
			if (p.getType() != Predicate.USER_DEFINED)
			{
				stream.putCodeSequence(null, interpreter, "% Foreign: ");
				stream.writeTerm(null, interpreter, options, tag.getPredicateIndicator());
				stream.putCode(null, interpreter, '\n');
				stream.putCode(null, interpreter, '\n');
			}
			else
			{
				for (Term t : p.getClauses())
				{
					stream.writeTerm(null, interpreter, options, t);
					stream.putCode(null, interpreter, '.');
					stream.putCode(null, interpreter, '\n');
				}
				stream.putCode(null, interpreter, '\n');
			}
		}
		stream.flushOutput(null);
		return SUCCESS_LAST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#install(gnu.prolog.vm.Environment)
	 */
	public void install(Environment env)
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#uninstall(gnu.prolog.vm.Environment)
	 */
	public void uninstall(Environment env)
	{}
}
