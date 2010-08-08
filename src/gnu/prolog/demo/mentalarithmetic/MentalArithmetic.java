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
 * Should be thread safe.
 * 
 * @author Daniel Thomas
 * 
 */
public class MentalArithmetic
{

	private MentalArithmetic()
	{}

	/**
	 * @see #main(String[])
	 */
	private static final int DEFAULTLIMIT = 500;
	/**
	 * @see #main(String[])
	 */
	private static final int DEFAULTLENGTH = 5;
	/**
	 * The USAGE of this program as a standalone program.
	 */
	public static final String USAGE = "Either no arguments or {Limit, Length}.\n"
			+ " Where Limit is the largest number to use and Length is the number of " + "operations to use.\n"
			+ "--help|-h displays this usage text.";

	/**
	 * Whether we have done {@link #setup()} or not.
	 */
	private static boolean issetup = false;

	/**
	 * The {@link Environment} we are using.
	 * 
	 * @see #setup() for creation
	 * @see #generateQuestion(int,int) for usage
	 */
	private static Environment env;
	/**
	 * The {@link Interpreter} we are using.
	 * 
	 * @see #setup() for creation
	 * @see #generateQuestion(int,int) for usage
	 */
	private static Interpreter interpreter;

	/**
	 * Standalone entry point for the program
	 * 
	 * @param args
	 *          as detailed in {@link #USAGE}
	 * @see #USAGE
	 */
	public static void main(String[] args)
	{
		int limit, length;// @see #USAGE

		// Check if we need to display usage information
		if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0])))
		{
			System.out.println(USAGE);
		}
		// If we are not displaying usage information then we expect 2 arguments
		if (args.length == 2)
		{
			try
			{// The arguments should be integers.
				limit = Integer.parseInt(args[0]);
				length = Integer.parseInt(args[1]);
			}// if they are not print something useful.
			catch (NumberFormatException e)
			{
				System.err.println(String.format("Not a number (%s) or (%s)", args[0], args[1]));
				System.out.println(USAGE);
				return;
			}
		}
		else
		{// no arguments so use defaults.
			limit = DEFAULTLIMIT;
			length = DEFAULTLENGTH;
		}
		try
		{
			// Get the question to ask
			Pair<String, Integer> question = generateQuestion(limit, length);
			// Print out the question
			System.out.print(question.left + ": ");

			// Get a reader to read in the answer
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			// Read in the answer
			String answer = reader.readLine();
			try
			{// Try to parse the number as an integer
				int value = Integer.parseInt(answer);
				if (question.right == value)
				{
					System.out.println("Correct.");
				}
				else
				{
					System.out.println("Wrong. Answer was: " + question.right);
				}
			}// Tell the user what went wrong if it doesn't work
			catch (NumberFormatException e)
			{
				System.err.println(String.format("Not a number: (%s)", answer));
			}
		}// Something went wrong: tell the user.
		catch (PrologException e)
		{
			System.err.println(e.toString());
		}
		catch (NoAnswerException e)
		{
			System.err.println(e.toString());
		}
		catch (IOException e)
		{
			System.err.println(e.toString());
		}
	}

	public static Pair<String, Integer> generateQuestion(int limit, int length) throws PrologException, NoAnswerException
	{
		setup();

		// // Construct the question.
		// Create variable terms so that we can pull the answers out later
		VariableTerm listTerm = new VariableTerm("List");
		VariableTerm answerTerm = new VariableTerm("Answer");
		// Create the arguments to the compound term which is the question
		Term[] args = { new IntegerTerm(limit), new IntegerTerm(length), listTerm, answerTerm };
		// Construct the question
		CompoundTerm goalTerm = new CompoundTerm(AtomTerm.get("arithmetic"), args);

		synchronized (interpreter)// so that this class is thread safe.
		{
			// Print out any errors
			debug();

			// Execute the goal and return the return code.
			int rc = interpreter.runOnce(goalTerm);

			// If it succeeded.
			if (rc == PrologCode.SUCCESS || rc == PrologCode.SUCCESS_LAST)
			{
				// Create the answer
				Pair<String, Integer> answer = new Pair<String, Integer>(null, 0);

				// Get hold of the actual Terms which the variable terms point to
				Term list = listTerm.dereference();
				Term value = answerTerm.dereference();
				// Check it is valid
				if (list != null)
				{
					if (list instanceof CompoundTerm)
					{
						CompoundTerm cList = (CompoundTerm) list;
						if (cList.tag == TermConstants.listTag)// it is a list
						{// Turn it into a string to use.
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

		// Construct the environment
		env = new Environment();

		// get the filename relative to the class file
		env.ensureLoaded(AtomTerm.get(MentalArithmetic.class.getResource("mentalarithmetic.pro").getFile()));

		// Get the interpreter.
		interpreter = env.createInterpreter();
		// Run the initialization
		env.runInitialization(interpreter);

		// So that we don't repeat ourselves
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
