/* GNU Prolog for Java - Mental Arithmetic demo
 * Copyright (C) 2010 Daniel Thomas
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
/**
 * 
 */
package gnu.prolog.demo.mentalarithmetic;

import gnu.prolog.database.Pair;
import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.io.TermWriter;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This is the main class of the mentalarithemtic program. It is runnable as a
 * stand alone program.
 * 
 * @author Daniel Thomas
 * 
 */
public class MentalArithmetic
{

	private static final int DEFAULTLIMIT = 500;
	private static final int DEFAULTLENGTH = 5;
	public static final String USAGE = "Either no arguments or {Limit, Length}.\n"
			+ " Where Limit is the largest number to use and Length is the number of " + "operations to use.\n"
			+ "--help|-h displays this usage text.";

	private static boolean issetup = false;

	private static Environment env;
	private static Interpreter interpreter;

	/**
	 * @param args
	 * @see #USAGE
	 */
	public static void main(String[] args)
	{
		int limit, length;
		if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0])))
		{
			System.out.println(USAGE);
		}
		if (args.length == 2)
		{
			try
			{
				limit = Integer.parseInt(args[0]);
				length = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException e)
			{
				System.err.println(String.format("Not a number (%s) or (%s)", args[0], args[1]));
				System.out.println(USAGE);
				return;
			}
		}
		else
		{
			limit = DEFAULTLIMIT;
			length = DEFAULTLENGTH;
		}
		try
		{
			Pair<String, Integer> question = generateQuestion(limit, length);
			System.out.print(question.left + ": ");

			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			String answer = reader.readLine();
			try
			{
				int value = Integer.parseInt(answer);
				if (question.right == value)
				{
					System.out.print("Correct.");
				}
			}
			catch (NumberFormatException e)
			{
				System.err.println(String.format("Not a number: (%s)", answer));
			}
		}
		catch (PrologException e)
		{
			e.printStackTrace();
			// TODO Auto-generated catch block
		}
		catch (NoAnswerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Pair<String, Integer> generateQuestion(int limit, int length) throws PrologException, NoAnswerException
	{

		setup();

		VariableTerm listTerm = new VariableTerm("List");
		VariableTerm answerTerm = new VariableTerm("Answer");
		Term[] args = { new IntegerTerm(limit), new IntegerTerm(length), listTerm, answerTerm };
		CompoundTerm goalTerm = new CompoundTerm(AtomTerm.get("arithmetic"), args);

		Interpreter.Goal goal = interpreter.prepareGoal(goalTerm);

		debug();

		int rc = interpreter.execute(goal);

		if (rc == PrologCode.SUCCESS || rc == PrologCode.SUCCESS_LAST)
		{
			interpreter.stop(goal);

			Pair<String, Integer> answer = new Pair<String, Integer>(null, 0);

			Term list = listTerm.dereference();
			Term value = answerTerm.dereference();
			if (list != null)
			{
				if (list instanceof CompoundTerm)
				{
					CompoundTerm cList = (CompoundTerm) list;
					if (cList.tag == TermConstants.listTag)// it is a list
					{
						answer.left = TermWriter.toString(list);
					}
					else
					{
						throw new NoAnswerException("List is not a list but is a CompoundTerm: " + list);
					}
				}
				else
				{
					throw new NoAnswerException("List is not a list: " + list);
				}
			}
			else
			{
				throw new NoAnswerException("List null when it should not be null");
			}
			if (value != null)
			{
				if (value instanceof IntegerTerm)
				{
					answer.right = ((IntegerTerm) value).value;
				}
				else
				{
					throw new NoAnswerException("Answer is not an integer: (" + value + ") but List is:" + list);
				}
			}
			else
			{
				throw new NoAnswerException("Answer null when it should not be null");
			}

			return answer;
		}
		else
		{
			throw new NoAnswerException("Goal failed");
		}

	}

	/**
	 * Ensure that we have an environment and have loaded the prolog code and have
	 * an interpreter to use.
	 */
	private synchronized static void setup()
	{
		if (issetup)
		{
			return;// don't setup more than once
		}

		env = new Environment();

		// get the filename relative to the class file
		env.ensureLoaded(AtomTerm.get(MentalArithmetic.class.getResource("mentalarithmetic.pro").getFile()));

		interpreter = env.createInterpreter();
		env.runIntialization(interpreter);

		issetup = true;
	}

	/**
	 * Collect debugging information if something has gone wrong in particular get
	 * any {@link PrologTextLoaderError PrologTextLoaderErrors} which were created
	 * during loading.
	 */
	private static void debug()
	{
		List<PrologTextLoaderError> errors = env.getLoadingErrors();
		for (PrologTextLoaderError error : errors)
		{
			error.printStackTrace();
		}

		/*
		 * Map<AtomTerm, Term> atom2flag = env.getPrologFlags(); Set<AtomTerm> atoms
		 * = atom2flag.keySet(); for (AtomTerm a : atoms) {
		 * System.out.println(a.toString() + " => " + atom2flag.get(a)); }
		 */
	}
}
