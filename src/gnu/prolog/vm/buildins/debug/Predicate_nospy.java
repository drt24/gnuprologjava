/**
 * 
 */
package gnu.prolog.vm.buildins.debug;

import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;

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
		String tag = Predicate_spy.getTag(args[0]);
		interpreter.getTracer().removeTrace(tag);
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
