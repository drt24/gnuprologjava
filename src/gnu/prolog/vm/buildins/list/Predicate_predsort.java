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

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.TermComparator;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;
import gnu.prolog.vm.interpreter.Predicate_call;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_predsort extends ExecuteOnlyCode
{
	static class ComparatorException extends RuntimeException
	{
		private static final long serialVersionUID = 7709083338614572022L;

		PrologException thrown;

		ComparatorException(PrologException thrownElm)
		{
			thrown = thrownElm;
		}
	}

	/**
	 * 
	 * @author Michiel Hendriks
	 */
	public static class CallPredComparator implements Comparator<Term>
	{
		Interpreter interpreter;
		CompoundTerm callMe;

		/**
		 * @param interp
		 * @param call
		 */
		public CallPredComparator(Interpreter interp, CompoundTerm call)
		{
			interpreter = interp;
			callMe = call;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Term o1, Term o2)
		{
			Term result = new VariableTerm();
			callMe.args[0] = result;
			callMe.args[1] = o1;
			callMe.args[2] = o2;

			int startUndoPosition = interpreter.getUndoPosition();
			BacktrackInfo startBi = interpreter.peekBacktrackInfo();
			try
			{
				try
				{
					boolean callBacktrackMode = false;
					int rc = Predicate_call.staticExecute(interpreter, callBacktrackMode, callMe);
					callBacktrackMode = true;
					if (rc == FAIL)
					{
						throw new ComparatorException(null);
					}
					result = result.dereference();
					String retval = null;
					if (result instanceof AtomTerm)
					{
						retval = ((AtomTerm) result).value;
					}
					interpreter.undo(startUndoPosition);
					if ("=".equals(retval))
					{
						return 0;
					}
					else if (">".equals(retval))
					{
						return 1;
					}
					else if ("<".equals(retval))
					{
						return -1;
					}
					throw new ComparatorException(null);
				}
				catch (RuntimeException rex)
				{
					if (rex instanceof ComparatorException)
					{
						throw rex;
					}
					PrologException.systemError(rex);
					return 0; // fake return
				}
			}
			catch (PrologException ex)
			{
				interpreter.popBacktrackInfoUntil(startBi);
				interpreter.undo(startUndoPosition);
				throw new ComparatorException(ex);
			}
		}
	}

	public static final CompoundTermTag COMPARE_TAG = CompoundTermTag.get("compare", 3);

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		if (!CompoundTerm.isListPair(args[1]))
		{
			PrologException.typeError(TermConstants.listAtom, args[1]);
		}
		Set<Term> set = new HashSet<Term>();
		CompoundTerm.toCollection(args[1], set);
		List<Term> list = new ArrayList<Term>(set);
		try
		{
			Collections.sort(list, getComparator(interpreter, args[0]));
		}
		catch (ComparatorException e)
		{
			if (e.thrown == null)
			{
				return FAIL;
			}
			throw e.thrown;
		}
		Term result = CompoundTerm.getList(list);
		return interpreter.unify(args[2], result);
	}

	/**
	 * @param interpreter
	 * @param sorter
	 * @return a Comparator for Terms
	 * @throws PrologException
	 */
	protected Comparator<? super Term> getComparator(Interpreter interpreter, Term sorter) throws PrologException
	{
		CompoundTermTag tag;
		CompoundTerm call;
		Term[] args;
		if (sorter instanceof AtomTerm)
		{
			tag = CompoundTermTag.get(((AtomTerm) sorter).value, 3);
			args = new Term[3];
		}
		else if (sorter instanceof CompoundTerm)
		{
			CompoundTerm ct = ((CompoundTerm) sorter);
			tag = CompoundTermTag.get(ct.tag.functor, ct.tag.arity + 3);
			args = new Term[tag.arity];
			System.arraycopy(ct.args, 0, args, 3, ct.args.length);
		}
		else
		{
			PrologException.typeError(TermConstants.atomAtom, sorter);
			return null;
		}
		call = new CompoundTerm(tag, args);
		if (call.tag == COMPARE_TAG)
		{
			return new TermComparator();
		}
		return new CallPredComparator(interpreter, call);
	}
}
