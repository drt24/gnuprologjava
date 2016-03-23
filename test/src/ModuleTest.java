package gnu.prolog.test;

import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;

import org.junit.Test;
import org.junit.Ignore;
import java.io.ByteArrayOutputStream;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class ModuleTest
{
	@Test
	public void testNoModules() throws PrologException
	{
		Environment env = new Environment();
		Interpreter interpreter = env.createInterpreter();
		env.ensureLoaded(AtomTerm.get("test_no_modules.pl"));
		env.runInitialization(interpreter);
		for (PrologTextLoaderError error : env.getLoadingErrors())
		{
			System.err.println("Prolog compile error: " + error);
		}
		assertTrue(env.getLoadingErrors().size() == 0);
		Term goalTerm = AtomTerm.get("test");
		Interpreter.Goal g = interpreter.prepareGoal(goalTerm);
		PrologCode.RC rc = interpreter.execute(g);
		assertTrue(rc == PrologCode.RC.SUCCESS_LAST);
	}

	@Test
	public void testWithModules() throws PrologException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Environment env = new Environment(System.in, bos);
		Interpreter interpreter = env.createInterpreter();
		env.ensureLoaded(AtomTerm.get("test_with_modules.pl"));
		env.runInitialization(interpreter);
		for (PrologTextLoaderError error : env.getLoadingErrors())
		{
			System.err.println("Prolog compile error: " + error);
		}
		assertTrue(env.getLoadingErrors().size() == 0);
		Term goalTerm = AtomTerm.get("test");
		Interpreter.Goal g = interpreter.prepareGoal(goalTerm);
		PrologCode.RC rc = interpreter.execute(g);
		assertTrue(rc == PrologCode.RC.SUCCESS);
		assertEquals(bos.toString(), ">predicate_in_user\n>a:predicate_exported_from_a\n>a:local_predicate\n>b:predicate_exported_from_b\n>a:predicate_exported_from_a\n>a:local_predicate\n>c:local_predicate\n");
	}

}
