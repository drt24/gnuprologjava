/* GNU Prolog for Java
 * Copyright (C) 2016       Matt Lilley
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

package gnu.prolog.test;

import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.vm.PrologCode.RC;

import java.util.Collection;
import gnu.prolog.vm.TermConstants;

import org.junit.Test;
import org.junit.Ignore;
import java.io.ByteArrayOutputStream;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.IsNull.nullValue;

import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;


public class ModuleTest
{

	public Term callPredicate(String source, String name, Term... args) throws PrologException
        {
                VariableTerm result = new VariableTerm("Result");
                Term[] goalArgs = new Term[args.length+1];
                System.arraycopy(args, 0, goalArgs, 0, args.length);
                goalArgs[args.length] = result;
                Term goalTerm = new CompoundTerm(name, goalArgs);
		RC rc = createInterpreter(source).runOnce(goalTerm);
                assertEquals(RC.SUCCESS_LAST, rc);
                return result.dereference();
        }

	public Interpreter createInterpreter(String source)
        {
		Environment env = new Environment();
		Interpreter interpreter = env.createInterpreter();
		env.ensureLoaded(AtomTerm.get(source));
		env.runInitialization(interpreter);
		for (PrologTextLoaderError error : env.getLoadingErrors())
		{
                        System.err.println("Prolog compile error: " + error);
                }
                assertEquals(0, env.getLoadingErrors().size());
                return interpreter;
        }

	@Test
	public void testMetaPredicateBefore() throws PrologException
	{
		List<Term> collection = new LinkedList<Term>();
		Term result = callPredicate("test_metapredicate_before.pl", "test");
		assertThat(result, instanceOf(CompoundTerm.class));
		assertThat(CompoundTerm.toCollection(result, collection), is(true));
		assertThat(collection, hasItems(new Term[]{AtomTerm.get("w"), AtomTerm.get("x"), AtomTerm.get("y")}));
	}

	@Test
	public void testMetaPredicateAfter() throws PrologException
	{
		List<Term> collection = new LinkedList<Term>();
		Term result = callPredicate("test_metapredicate_after.pl", "test");
		assertThat(result, instanceOf(CompoundTerm.class));
		assertThat(CompoundTerm.toCollection(result, collection), is(true));
		assertThat(collection, hasItems(new Term[]{AtomTerm.get("w"), AtomTerm.get("x"), AtomTerm.get("y")}));
	}

	@Test
	public void testMetaPredicateMissing() throws PrologException
	{
		PrologException expected = null;
		try
		{
			callPredicate("test_metapredicate_missing.pl", "test");
		}
		catch(PrologException exception)
		{
			expected = exception;
		}
		assertThat(expected, not(is(nullValue())));
		assertThat(expected.getTerm().toString(), is("error(existence_error(procedure,goal / 1),error)"));
	}

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
                assertEquals(0, env.getLoadingErrors().size());
		Term goalTerm = AtomTerm.get("test");
		Interpreter.Goal g = interpreter.prepareGoal(goalTerm);
                PrologCode.RC rc = interpreter.execute(g);
                assertEquals(PrologCode.RC.SUCCESS_LAST, rc);
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
                assertEquals(0, env.getLoadingErrors().size());
		Term goalTerm = AtomTerm.get("test");
		Interpreter.Goal g = interpreter.prepareGoal(goalTerm);
		PrologCode.RC rc = interpreter.execute(g);
		assertTrue(rc == PrologCode.RC.SUCCESS);
                assertEquals(">predicate_in_user\n>a:predicate_exported_from_a\n>a:local_predicate\n>b:predicate_exported_from_b\n>a:predicate_exported_from_a\n>a:local_predicate\n>c:local_predicate\n", bos.toString());
	}

}
