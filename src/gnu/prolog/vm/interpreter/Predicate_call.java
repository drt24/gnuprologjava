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
package gnu.prolog.vm.interpreter;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * prolog code
 */
public class Predicate_call extends ExecuteOnlyCode
{
	/** head functor, it is completly unimportant what it is */
	public static final AtomTerm headFunctor = AtomTerm.get("$$$call$$$");
	/** term arry constant */
	public static final Term termArrayType[] = new Term[0];

	/** call term backtrack info */
	public static class CallTermBacktrackInfo extends BacktrackInfo
	{
		public CallTermBacktrackInfo(Interpreter in, PrologCode code, Term args[], Term callTerm)
		{
			super(in.getUndoPosition(), -1);
			this.code = code;
			this.args = args.clone();
			this.callTerm = callTerm;
		}

		/** prolog code being tried */
		PrologCode code;
		/** argument of prolog code */
		Term args[];
		/** Term passed as parameter */
		Term callTerm;
		/** environment */
		Environment environment;

		@Override
		protected void finalize() throws Throwable
		{
			if (code != null)
			{
				code.uninstall(environment);
			}
			super.finalize();
		}
	}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		return staticExecute(interpreter, backtrackMode, args[0]);
	}

	/**
	 * this method is used for execution of code
	 * 
	 * @param interpreter
	 *          interpreter in which context code is executed
	 * @param backtrackMode
	 *          true if predicate is called on backtracking and false otherwise
	 * @param arg
	 *          argument of code
	 * @return either SUCCESS, SUCCESS_LAST, or FAIL.
	 * @throws PrologException
	 */
	public static int staticExecute(Interpreter interpreter, boolean backtrackMode, Term arg) throws PrologException
	{
		CallTermBacktrackInfo cbi = backtrackMode ? (CallTermBacktrackInfo) interpreter.popBacktrackInfo() : null;
		PrologCode code; // code to call
		Term args[]; // arguments of code
		Term callTerm; // term being called
		if (cbi == null)
		{
			callTerm = arg;
			if (callTerm instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			// This was originally done using two Lists by keeping their sizes in sync
			// but I (Daniel) refactored this to a map. (This may have broken
			// something).
			Map<Term, VariableTerm> argumentsToArgumentVariables = new HashMap<Term, VariableTerm>();
			Term body;
			try
			{
				body = getClause(callTerm, argumentsToArgumentVariables);
			}
			catch (IllegalArgumentException ex) // term not callable
			{
				PrologException.typeError(TermConstants.callableAtom, callTerm);
				return FAIL; // fake return
			}
			Term headArgs[] = argumentsToArgumentVariables.values().toArray(termArrayType);
			Term head = new CompoundTerm(headFunctor, headArgs);
			Term clause = new CompoundTerm(TermConstants.clauseTag, head, body);
			args = argumentsToArgumentVariables.keySet().toArray(termArrayType);
			List<Term> clauses = new ArrayList<Term>(1);
			clauses.add(clause);
			code = InterpretedCodeCompiler.compile(clauses);
			code.install(interpreter.getEnvironment());
			// System.err.println("converted clause");
			// System.err.println(gnu.prolog.io.TermWriter.toString(clause));
			// System.err.println("converted code");
			// System.err.print(code);
		}
		else
		{
			cbi.undo(interpreter);
			args = cbi.args;
			code = cbi.code;
			callTerm = cbi.callTerm;
		}
		int rc = code.execute(interpreter, backtrackMode, args);
		if (rc == SUCCESS) // redo is possible
		{
			cbi = new CallTermBacktrackInfo(interpreter, code, args, callTerm);
			cbi.environment = interpreter.getEnvironment();
			interpreter.pushBacktrackInfo(cbi);
		}
		else
		{
			code.uninstall(interpreter.getEnvironment());
			if (cbi != null)
			{
				cbi.code = null;
			}
		}
		return rc;
	}

	/**
	 * convert callable term to clause
	 * 
	 * @param term
	 * @param argumentsToArgumentVariables
	 * @return
	 */
	public static Term getClause(Term term, Map<Term, VariableTerm> argumentsToArgumentVariables)
	{
		if (term instanceof AtomTerm)
		{
			return term;
		}
		else if (term instanceof VariableTerm)
		{
			if (!argumentsToArgumentVariables.containsKey(term))
			{
				VariableTerm var1 = new VariableTerm();
				argumentsToArgumentVariables.put(term, var1);
				return var1;
			}
			return argumentsToArgumentVariables.get(term);
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			if (ct.tag == TermConstants.ifTag || ct.tag == TermConstants.conjunctionTag
					|| ct.tag == TermConstants.disjunctionTag)
			{
				return new CompoundTerm(ct.tag, getClause(ct.args[0].dereference(), argumentsToArgumentVariables), getClause(
						ct.args[1].dereference(), argumentsToArgumentVariables));
			}
			Term newArgs[] = new Term[ct.tag.arity];
			for (int i = 0; i < newArgs.length; i++)
			{
				Term arg = ct.args[i].dereference();
				if (!argumentsToArgumentVariables.containsKey(arg))
				{
					newArgs[i] = new VariableTerm();
					argumentsToArgumentVariables.put(arg, (VariableTerm) newArgs[i]);
				}
				else
				{
					newArgs[i] = argumentsToArgumentVariables.get(arg);
				}
			}
			return new CompoundTerm(ct.tag, newArgs);
		}
		else
		{
			throw new IllegalArgumentException("the term is not callable");
		}
	}
}
