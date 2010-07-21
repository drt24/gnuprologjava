/**
 *
 */
package gnu.prolog.vm.buildins.java;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * @author Michiel Hendriks
 * 
 */
public class Predicate_java_to_string extends ExecuteOnlyCode
{
	public Predicate_java_to_string()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		Object obj = null;
		if (args[0] instanceof JavaObjectTerm)
		{
			obj = ((JavaObjectTerm) args[0]).value;
		}
		else
		{
			PrologException.typeError(TermConstants.javaObjectAtom, args[0]);
		}
		Term val = AtomTerm.get(obj != null ? obj.toString() : "null");
		return interpreter.unify(args[1], val);
	}
}
