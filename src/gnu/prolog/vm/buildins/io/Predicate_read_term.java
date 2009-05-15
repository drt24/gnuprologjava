/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
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
package gnu.prolog.vm.buildins.io;

import gnu.prolog.io.ReadOptions;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.PrologStream;
import gnu.prolog.vm.TermConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * prolog code
 */
public class Predicate_read_term implements PrologCode
{
	final static CompoundTermTag variablesTag = CompoundTermTag.get("variables", 1);
	final static CompoundTermTag variableNamesTag = CompoundTermTag.get("variable_names", 1);
	final static CompoundTermTag singletonsTag = CompoundTermTag.get("singletons", 1);
	final static CompoundTermTag unifyTag = CompoundTermTag.get("=", 2);

	/**
	 * this method is used for execution of code
	 * 
	 * @param interpreter
	 *          interpreter in which context code is executed
	 * @param backtrackMode
	 *          true if predicate is called on backtracking and false otherwise
	 * @param args
	 *          arguments of code
	 * @return either SUCCESS, SUCCESS_LAST, or FAIL.
	 */
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		PrologStream stream = interpreter.environment.resolveStream(args[0]);
		Term optionsList = args[2];
		ReadOptions options = new ReadOptions();
		options.operatorSet = interpreter.environment.getOperatorSet();

		List<Term> singletons = new ArrayList<Term>();
		List<Term> variableLists = new ArrayList<Term>();
		List<Term> vnlists = new ArrayList<Term>();

		// parse and unify options
		Term cur = optionsList;
		while (cur != TermConstants.emptyListAtom)
		{
			if (cur instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (!(cur instanceof CompoundTerm))
			{
				PrologException.typeError(TermConstants.listAtom, optionsList);
			}
			CompoundTerm ct = (CompoundTerm) cur;
			if (ct.tag != TermConstants.listTag)
			{
				PrologException.typeError(TermConstants.listAtom, optionsList);
			}
			Term head = ct.args[0].dereference();
			cur = ct.args[1].dereference();
			if (head instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (!(head instanceof CompoundTerm))
			{
				PrologException.domainError(TermConstants.readOptionAtom, head);
			}
			CompoundTerm op = (CompoundTerm) head;
			if (op.tag == variablesTag)
			{
				variableLists.add(op.args[0]);
			}
			else if (op.tag == singletonsTag)
			{
				singletons.add(op.args[0]);
			}
			else if (op.tag == variableNamesTag)
			{
				vnlists.add(op.args[0]);
			}
			else
			{
				PrologException.domainError(TermConstants.readOptionAtom, head);
			}
		}

		Term readTerm = stream.readTerm(args[0], interpreter, options);
		int undoPos = interpreter.getUndoPosition();

		try
		{
			int rc = interpreter.simple_unify(args[1], readTerm);
			if (rc == FAIL)
			{
				interpreter.undo(undoPos);
				return FAIL;
			}
			Iterator<Term> i = singletons.iterator();
			if (i.hasNext())
			{
				Term singletonsList = mapToList(options.singletons);
				while (i.hasNext())
				{
					Term t = (Term) i.next();
					t = t.dereference();
					rc = interpreter.simple_unify(t, singletonsList);
					if (rc == FAIL)
					{
						interpreter.undo(undoPos);
						return FAIL;
					}
				}
			}
			i = vnlists.iterator();
			if (i.hasNext())
			{
				Term vnlist = mapToList(options.variableNames);
				while (i.hasNext())
				{
					Term t = (Term) i.next();
					t = t.dereference();
					rc = interpreter.simple_unify(t, vnlist);
					if (rc == FAIL)
					{
						interpreter.undo(undoPos);
						return FAIL;
					}
				}
			}
			i = variableLists.iterator();
			if (i.hasNext())
			{
				Term vnlist = CompoundTerm.getList(options.variables);
				while (i.hasNext())
				{
					Term t = (Term) i.next();
					t = t.dereference();
					rc = interpreter.simple_unify(t, vnlist);
					if (rc == FAIL)
					{
						interpreter.undo(undoPos);
						return FAIL;
					}
				}
			}
			return SUCCESS_LAST;
		}
		catch (PrologException ex)
		{
			interpreter.undo(undoPos);
			throw ex;
		}
	}

	/**
	 * this method is called when code is installed to the environment code can be
	 * installed only for one environment.
	 * 
	 * @param environment
	 *          environemnt to install the predicate
	 */
	public void install(Environment env)
	{

	}

	private static Term mapToList(Map<String, Term> map)
	{
		Iterator<String> i = map.keySet().iterator();
		Term rc = TermConstants.emptyListAtom;
		while (i.hasNext())
		{
			String key = (String) i.next();
			Term val = (Term) map.get(key);
			rc = CompoundTerm.getList(new CompoundTerm(unifyTag, AtomTerm.get(key), val), rc);
		}
		return rc;
	}

	/**
	 * this method is called when code is uninstalled from the environment
	 * 
	 * @param environment
	 *          environemnt to install the predicate
	 */
	public void uninstall(Environment env)
	{}

}
