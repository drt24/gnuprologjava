/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
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
 * Boston, MA  02111-1307, USA. The text ol license can be also found 
 * at http://www.gnu.org/copyleft/lgpl.html
 */
package gnu.prolog.test;
import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.io.OperatorSet;
import gnu.prolog.io.ReadOptions;
import gnu.prolog.io.TermReader;
import gnu.prolog.io.TermWriter;
import gnu.prolog.io.WriteOptions;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Iterator;
public class GoalRunner
{
  private static void usage()
  {
    System.out.println("usage: java gnu.prolog.test.GoalRunner [-once] <text to load> <goal to run>");
    System.out.println("example: java gnu.prolog.test.GoalRunner append.pro append([a,b],[c,d],R)");
    System.exit(-1);
  }

  public static void main (String args[]) 
  {
    try
    {
      System.out.println("GNU Prolog for Java Goal runner (c) Constantine Plotnikov, 1997-1999.");
      if (args.length < 2)
      {
        usage();
      }
      boolean once;
      String textToLoad;
      String goalToRun;
      if("-once".equals(args[0]))
      {
        if (args.length < 3)
        {
          usage();
        }
        once = true;
        textToLoad = args[1];
        goalToRun = args[2];
      }
      else
      {
        once = false;
        textToLoad = args[0];
        goalToRun = args[1];
      }
      Environment env = new Environment();
      env.ensureLoaded(AtomTerm.get(textToLoad));
      Interpreter interpreter = env.createInterpreter();
      env.runIntialization(interpreter);
      for (Iterator ierr = env.getLoadingErrors().iterator();ierr.hasNext();)
      {
        PrologTextLoaderError err = (PrologTextLoaderError)ierr.next();
        System.err.println(err);
        //err.printStackTrace();
      }
      LineNumberReader kin = new LineNumberReader(new InputStreamReader(System.in));
      StringReader rd = new StringReader(goalToRun);
      TermReader trd = new TermReader(rd);
      TermWriter out = new TermWriter(new OutputStreamWriter(System.out));
      ReadOptions rd_ops = new ReadOptions();
      rd_ops.operatorSet = env.getOperatorSet();
      WriteOptions wr_ops = new WriteOptions();
      wr_ops.operatorSet = env.getOperatorSet();
      Term goalTerm = trd.readTermEof(rd_ops);
      // temp
      //PrologCode code = env.getPrologCode(CompoundTermTag.get("append",3));
      //System.err.println(code);
      // end temp

      Interpreter.Goal goal = interpreter.prepareGoal(goalTerm);
      String response;
      loop: do
      {
        long startTime = System.currentTimeMillis();
        int rc = interpreter.execute(goal);
        long stopTime = System.currentTimeMillis();
        env.getUserOutput().flushOutput(null);
        System.out.println("time = "+(stopTime-startTime)+"ms");
        response = "n";
        switch (rc)
        {
        case PrologCode.SUCCESS:
          {
            WriteOptions options = new WriteOptions();
            options.operatorSet = new OperatorSet();
            Iterator ivars2 = rd_ops.variableNames.keySet().iterator();
            Iterator ivars = rd_ops.variableNames.keySet().iterator();
            while (ivars.hasNext())
            {
              String name = (String)ivars.next();
              out.print(name+" = ");
              out.print(options,((Term)rd_ops.variableNames.get(name)).dereference());
              out.print("; ");
            }
            out.println();
            if(once)
            {
              out.print("SUCCESS. redo suppressed by command line option \"-once\"");
              return;
            }
            out.print("SUCCESS. redo (y/n/a)?");
            out.flush();
            response = kin.readLine();
  
            if ("a".equals(response))
            {
              interpreter.stop(goal);
              goal = interpreter.prepareGoal(goalTerm);
            }
  
            if ("n".equals(response))
            {
              return;
            }
            break;
          }
        case PrologCode.SUCCESS_LAST:
          {
            WriteOptions options = new WriteOptions();
            options.operatorSet = new OperatorSet();
            Iterator ivars2 = rd_ops.variableNames.keySet().iterator();
            while (ivars2.hasNext())
            {
              String name = (String)ivars2.next();
              out.print(name+" = ");
              out.print(options, ((Term)rd_ops.variableNames.get(name)).dereference());
              out.print("; ");
            }
            out.println();
            out.println("SUCCESS LAST");
            out.flush();
            return;
          }
        case PrologCode.FAIL:
          out.println("FAIL");
          out.flush();
          return;
        case PrologCode.HALT:
          env.closeStreams();
          out.println("HALT");
          out.flush();
          System.exit(interpreter.getExitCode());
          return;
        }
      } while (true);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
