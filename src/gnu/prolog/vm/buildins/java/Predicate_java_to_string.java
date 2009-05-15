/**
 *
 */
package gnu.prolog.vm.buildins.java;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;

/**
 * @author Michiel Hendriks
 *
 */
public class Predicate_java_to_string implements PrologCode
{
	public static final AtomTerm javaObjectAtom = AtomTerm.get("java_object");

	/**
	 *
	 */
	public Predicate_java_to_string()
	{}

	/*
	 * (non-Javadoc)
	 *
	 * @see gnu.prolog.vm.PrologCode#execute(gnu.prolog.vm.Interpreter, boolean,
	 * gnu.prolog.term.Term[])
	 */
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		Object obj = null;
		if (args[0] instanceof JavaObjectTerm)
		{
			obj = ((JavaObjectTerm) args[0]).value;
		}
		else
		{
			PrologException.typeError(javaObjectAtom, args[0]);
		}
		Term val = AtomTerm.get(obj != null ? obj.toString() : "null");
		return interpreter.unify(args[1], val);
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
