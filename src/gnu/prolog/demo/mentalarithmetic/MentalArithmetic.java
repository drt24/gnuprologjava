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

import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.io.ParseException;
import gnu.prolog.io.ReadOptions;
import gnu.prolog.io.TermReader;
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

import java.io.Console;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the main class of the mentalarithemtic program. It is runnable as a
 * stand alone
 * 
 * @author daniel
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
	private static ReadOptions rd_ops;
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

			Console console = System.console();
			String answer = console.readLine();
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
		catch (ParseException e)
		{
			e.printStackTrace();
			// TODO
		}
		catch (PrologException e)
		{
			e.printStackTrace();
			// TODO
		}
		catch (NoAnswerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Pair<String, Integer> generateQuestion(int limit, int length) throws ParseException, PrologException,
			NoAnswerException
	{
		if (!issetup)
		{
			setup();
			issetup = true;
		}
		String question = String.format("arithmetic(%d, %d, List, Answer)", limit, length);

		Term goalTerm = TermReader.stringToTerm(rd_ops, question);

		Interpreter.Goal goal = interpreter.prepareGoal(goalTerm);

		debug();

		int rc = interpreter.execute(goal);

		if (rc == PrologCode.SUCCESS || rc == PrologCode.SUCCESS_LAST)
		{
			interpreter.stop(goal);

			Pair<String, Integer> answer = new Pair<String, Integer>(null, 0);

			VariableTerm list = rd_ops.variableNames.get("List");
			VariableTerm value = rd_ops.variableNames.get("Answer");
			if (list != null)
			{
				if (list.value instanceof CompoundTerm)
				{
					CompoundTerm cList = (CompoundTerm) list.value;
					if (cList.tag == TermConstants.listTag)// it is a list
					{
						answer.left = list.value.toString();
					}
					else
					{
						throw new NoAnswerException("List is not a list but is a CompoundTerm: " + list.value);
					}
				}
				else
				{
					throw new NoAnswerException("List is not a list: " + list.value);
				}
			}
			else
			{
				throw new NoAnswerException("List null when it should not be null");
			}
			if (value != null)
			{
				if (value.value instanceof IntegerTerm)
				{
					answer.right = ((IntegerTerm) value.value).value;
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
			throw new PrologException(PrologException.errorAtom, null);
		}

	}

	private synchronized static void setup() throws PrologException
	{
		if (issetup)
		{
			return;// don't setup more than once
		}

		env = new Environment();

		// get the filename relative to the class file
		env.ensureLoaded(AtomTerm.get((new MentalArithmetic()).getClass().getResource("mentalarithmetic.pro").getFile()));

		interpreter = env.createInterpreter();
		env.runIntialization(interpreter);

		rd_ops = new ReadOptions();
		rd_ops.operatorSet = env.getOperatorSet();

	}

	private static void debug()
	{
		List<PrologTextLoaderError> errors = env.getLoadingErrors();
		for (PrologTextLoaderError error : errors)
		{
			error.printStackTrace();
		}

		Map<AtomTerm, Term> atom2flag = env.getPrologFlags();
		Set<AtomTerm> atoms = atom2flag.keySet();
		for (AtomTerm a : atoms)
		{
			System.out.println(a.toString() + " => " + atom2flag.get(a));
		}

	}
}
