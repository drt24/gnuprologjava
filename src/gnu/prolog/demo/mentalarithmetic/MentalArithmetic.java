/* GNU Prolog for Java - Mental Arithmetic demo
 * Copyright (C) 2010 Daniel Thomas
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
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

import java.io.Console;
import java.io.StringReader;

import gnu.prolog.io.ParseException;
import gnu.prolog.io.ReadOptions;
import gnu.prolog.io.TermReader;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;

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
            + " Where Limit is the largest number to use and Length is the number of "
            + "operations to use.\n" + "--help|-h displays this usage text.";

    private static boolean issetup = false;

    private static Environment env;
    private static ReadOptions rd_ops;
    private static Interpreter interpreter;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        int limit, length;
        if (args.length == 1
                && ("--help".equals(args[0]) || "-h".equals(args[0])))
        {
            System.out.println(USAGE);
        }
        if (args.length == 2)
        {
            try
            {
                limit = Integer.parseInt(args[0]);
                length = Integer.parseInt(args[1]);
            } catch (NumberFormatException e)
            {
                System.err.println(String.format("Not a number (%s) or (%s)",
                        args[0], args[1]));
                System.out.println(USAGE);
                return;
            }
        } else
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
            } catch (NumberFormatException e)
            {
                System.err.println(String.format("Not a number: (%s)", answer));
            }
        } catch (ParseException e)
        {
            // TODO
        } catch (PrologException e)
        {
            // TODO
        }

    }

    public static Pair<String, Integer> generateQuestion(int limit, int length)
            throws ParseException, PrologException
    {
        if (!issetup)
        {
            setup();
        }
        String question = String.format("arithmetic(%d, %d, List, Answer)",
                limit, length);

        TermReader trd = new TermReader(new StringReader(question));

        Term goalTerm = trd.readTermEof(rd_ops);

        Interpreter.Goal goal = interpreter.prepareGoal(goalTerm);

        int rc = interpreter.execute(goal);

        if (rc == PrologCode.SUCCESS)
        {
            interpreter.stop(goal);

            Pair<String, Integer> answer = new Pair<String, Integer>(null, 0);

            for (String name : rd_ops.variableNames.keySet())
            {
                Term res = (rd_ops.variableNames.get(name));
                System.out.println(res);
                if ("List".equals(name))
                    answer.left = res.toString();
                else if ("Answer".equals(name))
                    answer.right = Integer.parseInt(res.toString());
            }
            return answer;
        } else
        {
            throw new PrologException(PrologException.errorAtom);
        }

    }

    private synchronized static void setup()
    {
        env = new Environment();

        env.ensureLoaded(AtomTerm.get("mentalarithmetic.pro"));

        interpreter = env.createInterpreter();
        env.runIntialization(interpreter);

        rd_ops = new ReadOptions();
        rd_ops.operatorSet = env.getOperatorSet();

    }
}
