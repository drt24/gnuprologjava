/**
 * 
 */
package gnu.prolog.vm.buildins.debug;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;
import gnu.prolog.vm.interpreter.Tracer.TraceLevel;

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
	public static String getTag(Term term) throws PrologException
	{
		String tag = "";
		if (term instanceof AtomTerm)
		{
			tag = ((AtomTerm) term).value;
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			if (!ct.tag.toString().endsWith("//2"))
			{
				PrologException.typeError(TermConstants.compoundAtom, term);
			}
			if (ct.args[0] instanceof AtomTerm)
			{
				tag = ((AtomTerm) ct.args[0]).value;
			}
			else
			{
				PrologException.typeError(TermConstants.atomAtom, ct.args[0]);
			}
			if (ct.args[1] instanceof IntegerTerm)
			{
				tag = tag + "/" + ((IntegerTerm) ct.args[1]).value;
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
		return tag;
	}

	public Predicate_spy()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#execute(gnu.prolog.vm.Interpreter, boolean,
	 * gnu.prolog.term.Term[])
	 */
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		String tag = getTag(args[0]);
		if (args[1] instanceof AtomTerm)
		{
			EnumSet<TraceLevel> lvl = getTraceLevel(args[1]);
			interpreter.getTracer().setTrace(tag, lvl);
		}
		else if (args[1] instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) args[1];
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
				PrologException.representationError(args[0]);
			}
		}
		return SUCCESS_LAST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#install(gnu.prolog.vm.Environment)
	 */
	public void install(Environment env)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#uninstall(gnu.prolog.vm.Environment)
	 */
	public void uninstall(Environment env)
	{
	}
}
