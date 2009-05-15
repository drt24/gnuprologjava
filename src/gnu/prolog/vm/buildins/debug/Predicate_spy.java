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
package gnu.prolog.vm.buildins.debug;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;
import gnu.prolog.vm.interpreter.Tracer.TraceLevel;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Set a trace point
 * 
 * @author Michiel Hendriks
 */
public class Predicate_spy implements PrologCode
{
	/**
	 * @param term
	 * @throws PrologException
	 */
	public static EnumSet<TraceLevel> getTraceLevel(Term term) throws PrologException
	{
		if (term instanceof AtomTerm)
		{
			return TraceLevel.fromString(((AtomTerm) term).value);
		}
		else
		{
			PrologException.typeError(TermConstants.atomAtom, term);
		}
		return EnumSet.noneOf(TraceLevel.class);
	}

	/**
	 * @param term
	 * @return
	 * @throws PrologException
	 */
	public static CompoundTermTag getTag(Term term) throws PrologException
	{
		String functor = "";
		int arity = -1;
		if (term instanceof AtomTerm)
		{
			functor = ((AtomTerm) term).value;
			int idx = functor.indexOf('/');
			if (idx > -1)
			{
				try
				{
					arity = Integer.parseInt(functor.substring(idx + 1));
					functor = functor.substring(0, idx);
				}
				catch (NumberFormatException e)
				{
				}
			}
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			if (!ct.tag.toString().equals("//2"))
			{
				PrologException.typeError(TermConstants.compoundAtom, term);
			}
			if (ct.args[0] instanceof AtomTerm)
			{
				functor = ((AtomTerm) ct.args[0]).value;
			}
			else
			{
				PrologException.typeError(TermConstants.atomAtom, ct.args[0]);
			}
			if (ct.args[1] instanceof IntegerTerm)
			{
				arity = ((IntegerTerm) ct.args[1]).value;
			}
			else
			{
				PrologException.typeError(TermConstants.atomAtom, ct.args[1]);
			}
		}
		else
		{
			PrologException.typeError(TermConstants.compoundAtom, term);
		}
		return CompoundTermTag.get(functor, arity);
	}

	public Predicate_spy()
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#execute(gnu.prolog.vm.Interpreter, boolean,
	 * gnu.prolog.term.Term[])
	 */
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		CompoundTermTag tag = getTag(args[0]);
		if (tag.arity == -1)
		{
			for (CompoundTermTag ptag : (Collection<CompoundTermTag>) interpreter.environment.getModule().getPredicateTags())
			{
				if (ptag.functor.equals(tag.functor))
				{
					setSpyPoint(interpreter, ptag, args[1]);
				}
			}
		}
		else
		{
			setSpyPoint(interpreter, tag, args[1]);
		}
		return SUCCESS_LAST;
	}

	protected void setSpyPoint(Interpreter interpreter, CompoundTermTag tag, Term arg) throws PrologException
	{
		if (arg instanceof AtomTerm)
		{
			EnumSet<TraceLevel> lvl = getTraceLevel(arg);
			interpreter.getTracer().setTrace(tag, lvl);
		}
		else if (arg instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) arg;
			EnumSet<TraceLevel> lvl = getTraceLevel(ct.args[0]);
			if (ct.tag.toString().equals("+/1"))
			{
				interpreter.getTracer().addTrace(tag, lvl);
			}
			else if (ct.tag.toString().equals("-/1"))
			{
				interpreter.getTracer().removeTrace(tag, lvl);
			}
			else
			{
				PrologException.representationError(arg);
			}
		}
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
