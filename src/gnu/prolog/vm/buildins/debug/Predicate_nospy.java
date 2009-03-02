/**
 * 
 */
package gnu.prolog.vm.buildins.debug;

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;

import java.util.Collection;

/**
 * Remove a trace point
 * 
 * @author Michiel Hendriks
 */
public class Predicate_nospy implements PrologCode
{
	public Predicate_nospy()
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
		CompoundTermTag tag = Predicate_spy.getTag(args[0]);
		if (tag.arity == -1)
		{
			for (CompoundTermTag ptag : (Collection<CompoundTermTag>) interpreter.environment.getModule().getPredicateTags())
			{
				if (ptag.functor.equals(tag.functor))
				{
					interpreter.getTracer().removeTrace(ptag);
				}
			}
		}
		else
		{
			interpreter.getTracer().removeTrace(tag);
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
