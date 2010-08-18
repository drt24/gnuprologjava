/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
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
package gnu.prolog.vm;

import gnu.prolog.io.PrologStream;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.interpreter.Tracer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represent interpreter, it should be used only from one thread If
 * you need to use interpreter from two threads,
 * {@link Environment#createInterpreter() create new interpreter from
 * Environment}
 */

public final class Interpreter implements HasEnvironment
{
	/**
	 * The "slack" used in float comparison
	 */
	static final double FLOAT_EPSILON = 0.0000001d;

	/**
	 * Environment for this interpreter
	 */
	private Environment environment;

	/**
	 * Keeps track of prolog call/return traces
	 */
	private Tracer tracer;

	/**
	 * Contains an {@link PrologHalt} instance when the interpreter was halted in
	 * an {@link #execute(Goal)}.
	 */
	private PrologHalt haltExitCode;

	/**
	 * Used to store context information for the lifespan of a goal. The context
	 * is cleared every time a new goal is prepared. Can be used by predicates to
	 * store information based on goals.
	 */
	@Deprecated
	private Map<String, Object> context;

	/**
	 * this constructor should not be used by client programs
	 * 
	 * @param environment
	 */
	protected Interpreter(Environment environment)
	{
		this.environment = environment;
		PrologStream outstream = null;
		try
		{
			outstream = environment.getUserOutput();
		}
		catch (PrologException e)
		{
			System.err.println("Could not get an output stream:");
			e.printStackTrace();
		}
		tracer = new Tracer(outstream);
		context = new HashMap<String, Object>();
	}

	/** get environment */
	public Environment getEnvironment()
	{
		return environment;
	}

	public Tracer getTracer()
	{
		return tracer;
	}

	@Deprecated
	public Object putContext(String key, Object contextValue)
	{
		return context.put(key, contextValue);
	}

	@Deprecated
	public Object getContext(String key)
	{
		return context.get(key);
	}

	private static final int PAGESIZE = 4096;
	private static final int GROWSIZE = PAGESIZE;

	private BacktrackInfo backtrackInfoStack[] = new BacktrackInfo[PAGESIZE];
	private int backtrackInfoAmount = 0;
	private int backtrackInfoMax = backtrackInfoStack.length;

	/**
	 * push backtrack information
	 * 
	 * @param bi
	 */
	public void pushBacktrackInfo(BacktrackInfo bi)
	{
		if (backtrackInfoAmount == backtrackInfoMax)
		{
			BacktrackInfo tmp[] = new BacktrackInfo[backtrackInfoMax + GROWSIZE];
			System.arraycopy(backtrackInfoStack, 0, tmp, 0, backtrackInfoAmount);
			backtrackInfoStack = tmp;
			backtrackInfoMax += GROWSIZE;
		}
		backtrackInfoStack[backtrackInfoAmount++] = bi;
	}

	/**
	 * pop backtrack information
	 * 
	 * @return the popped top backtrack information
	 */
	public BacktrackInfo popBacktrackInfo()
	{
		BacktrackInfo rc = backtrackInfoStack[--backtrackInfoAmount];
		backtrackInfoStack[backtrackInfoAmount] = null;
		return rc;
	}

	public void popBacktrackInfoUntil(BacktrackInfo cutPoint)
	{
		int pos = backtrackInfoAmount - 1;
		while (pos >= 0 && cutPoint != backtrackInfoStack[pos])
		{
			pos--;
		}
		if (pos < 0)
		{
			throw new IllegalArgumentException("cutPoint not found");
		}
		for (int i = pos + 1; i < backtrackInfoAmount; i++)
		{
			backtrackInfoStack[i] = null;
		}
		backtrackInfoAmount = pos + 1;
	}

	/**
	 * peek top backtrack information
	 * 
	 * @return the top backtrack information
	 */
	public BacktrackInfo peekBacktrackInfo()
	{
		return backtrackInfoStack[backtrackInfoAmount - 1];
	}

	// Undo Stack methods
	/**
	 * get current undo position
	 * 
	 * @return the current undo position
	 */
	public int getUndoPosition()
	{
		undoPositionAsked = true;
		return undoDataAmount;
	}

	/**
	 * undo changes until this position
	 * 
	 * @param position
	 */
	public void undo(int position)
	{
		// System.err.println("undo "+position+" current="+undoData_amount);
		for (int i = undoDataAmount - 1; i >= position; i--)
		{
			undoData[i].undo();
			undoData[i] = null;
		}
		undoDataAmount = position;
		undoPositionAsked = true;
	}

	/**
	 * add variable undo
	 * 
	 * @param variable
	 */
	public void addVariableUndo(VariableTerm variable)
	{
		if (undoPositionAsked || !(undoData[undoDataAmount - 1] instanceof VariableUndoData))
		{
			addSpecialUndo(new VariableUndoData());
		}
		if (variablesAmount == variables.length)
		{
			VariableTerm tmp[] = new VariableTerm[GROWSIZE + variables.length];
			System.arraycopy(variables, 0, tmp, 0, variablesAmount);
			variables = tmp;
		}
		variables[variablesAmount] = variable;
		variablesAmount++;
	}

	/**
	 * add special undo
	 * 
	 * @param undoDatum
	 */
	public void addSpecialUndo(UndoData undoDatum)
	{
		if (undoDataAmount == undoData.length)
		{
			UndoData tmp[] = new UndoData[GROWSIZE + undoData.length];
			System.arraycopy(undoData, 0, tmp, 0, undoDataAmount);
			undoData = tmp;
		}
		undoData[undoDataAmount] = undoDatum;
		undoDataAmount++;
	}

	protected VariableTerm variables[] = new VariableTerm[PAGESIZE];
	protected int variablesAmount = 0;

	private UndoData undoData[] = new UndoData[PAGESIZE];
	private int undoDataAmount = 0;
	private boolean undoPositionAsked = true;

	private class VariableUndoData implements UndoData
	// maybe later this class will be pooled
	{
		private int startPosion;

		protected VariableUndoData()
		{
			startPosion = variablesAmount;
		}

		public void undo()
		{
			for (int i = variablesAmount - 1; i >= startPosion; i--)
			{
				variables[i].value = null;
				variables[i] = null;
			}
			variablesAmount = startPosion;
		}
	}

	/**
	 * unify two terms, no undo done
	 * 
	 * @param t1
	 * @param t2
	 * @return {@link PrologCode#SUCCESS_LAST} or {@link PrologCode#FAIL}
	 * @throws PrologException
	 */
	public int simpleUnify(Term t1, Term t2) throws PrologException
	{
		int rc = PrologCode.SUCCESS_LAST;
		if (t1 == t2)
		{
		}
		else if (t1 instanceof VariableTerm)
		{
			VariableTerm vt1 = (VariableTerm) t1;
			addVariableUndo(vt1);
			vt1.value = t2;
		}
		else if (t2 instanceof VariableTerm)
		{
			VariableTerm vt2 = (VariableTerm) t2;
			addVariableUndo(vt2);
			vt2.value = t1;
		}
		else if (t1.getClass() != t2.getClass())
		{
			rc = PrologCode.FAIL;
		}
		else if (t1 instanceof CompoundTerm/* && t2 instanceof CompoundTerm */)
		{
			CompoundTerm ct1 = (CompoundTerm) t1;
			CompoundTerm ct2 = (CompoundTerm) t2;
			if (ct1.tag != ct2.tag)
			{
				rc = PrologCode.FAIL;
			}
			else
			{
				Term args1[] = ct1.args;
				Term args2[] = ct2.args;
				// System.err.println("unify "+ct2.tag+" al1 = "+args1.length+" al2 = "+args2.length);
				unify_loop: for (int i = args2.length - 1; i >= 0; i--)
				{
					rc = simpleUnify(args1[i].dereference(), args2[i].dereference());
					if (rc == PrologCode.FAIL)
					{
						break unify_loop;
					}
				}
			}
		}
		else if (t1 instanceof FloatTerm/* && t2 instanceof FloatTerm */)
		{
			FloatTerm ct1 = (FloatTerm) t1;
			FloatTerm ct2 = (FloatTerm) t2;
			if (ct1.value != ct2.value && Math.abs(ct1.value - ct2.value) > FLOAT_EPSILON)
			{
				rc = PrologCode.FAIL;
			}
		}
		else if (t1 instanceof IntegerTerm /* && t2 instanceof IntegerTerm */)
		{
			IntegerTerm ct1 = (IntegerTerm) t1;
			IntegerTerm ct2 = (IntegerTerm) t2;
			if (ct1.value != ct2.value)
			{
				rc = PrologCode.FAIL;
			}
		}
		else if (t1 instanceof JavaObjectTerm /* && t2 instanceof JavaObjectTerm */)
		{
			JavaObjectTerm ct1 = (JavaObjectTerm) t1;
			JavaObjectTerm ct2 = (JavaObjectTerm) t2;
			if (ct1.value != ct2.value)
			{
				rc = PrologCode.FAIL;
			}
		}
		else
		{
			rc = PrologCode.FAIL;
		}
		return rc;
	}

	/**
	 * unify two terms
	 * 
	 * @param t1
	 * @param t2
	 * @return {@link PrologCode#SUCCESS_LAST} or {@link PrologCode#FAIL}
	 * @throws PrologException
	 */
	public int unify(Term t1, Term t2) throws PrologException
	{
		int undoPos = getUndoPosition();
		int rc = simpleUnify(t1, t2);
		if (rc == PrologCode.FAIL)
		{
			undo(undoPos);
		}
		return rc;
	}

	/** user level calls */
	public static final class Goal
	{
		private Term goal;
		protected boolean firstTime = true;
		protected boolean stopped = false;

		protected Goal(Term goal)
		{
			this.goal = goal;
		}

		protected Term getGoal()
		{
			return goal;
		}
	}

	/**
	 * The most recently {@link #prepareGoal(Term)}ed Goal.
	 */
	private Goal currentGoal;

	/**
	 * Used to store the current state so that we can support
	 * {@link gnu.prolog.vm.buildins.io.Predicate_ensure_loaded}
	 * 
	 * @see #prepareGoal(Term)
	 * @see #stop(Goal)
	 * 
	 *      WARNING: may result in obscure bugs, if so sorry.
	 * 
	 * @author Daniel Thomas
	 */
	static class ReturnPoint
	{
		public Map<String, Object> rContext;

		public BacktrackInfo rBacktrackInfoStack[];
		public int rBacktrackInfoAmount;
		public int rBacktrackInfoMax;

		public VariableTerm rVariables[];
		public int rVariablesAmount;

		public UndoData rUndoData[];
		public int rUndoDataAmount;
		public boolean rUndoPositionAsked;

		public Goal rCurrentGoal;
	}

	/**
	 * Map of Goals to ReturnPoints so that we can save the state in a return
	 * point if in {@link #prepareGoal(Term)} we discover that we need to execute
	 * another goal first before finishing executing the {@link #currentGoal}.
	 */
	private Map<Goal, ReturnPoint> returnPoints = new HashMap<Goal, ReturnPoint>();

	/**
	 * prepare goal for execution
	 * 
	 * If this is called before the Goal which was previously prepared but has not
	 * yet been stopped is stopped then we save that state so we can jump back to
	 * it when this goal has been stopped.
	 * 
	 * @param term
	 * @return the prepared Goal
	 */
	public Goal prepareGoal(Term term)
	{
		ReturnPoint rp = null;
		if (currentGoal != null)
		{// Take a copy of the current state into a Return point
			rp = new ReturnPoint();
			rp.rContext = context;
			rp.rBacktrackInfoStack = backtrackInfoStack.clone();
			rp.rBacktrackInfoAmount = backtrackInfoAmount;
			rp.rBacktrackInfoMax = backtrackInfoMax;
			rp.rVariables = variables.clone();
			rp.rVariablesAmount = variablesAmount;
			rp.rUndoData = undoData.clone();
			rp.rUndoDataAmount = undoDataAmount;
			rp.rUndoPositionAsked = undoPositionAsked;
			rp.rCurrentGoal = currentGoal;
		}
		currentGoal = new Goal(term);
		context.clear();
		if (rp != null)
		{// save the return point so that we can jump back later
			returnPoints.put(currentGoal, rp);
		}
		return currentGoal;
	}

	/**
	 * Execute the {@link Goal} and return the status code indicating how
	 * successful this was.
	 * 
	 * @param goal
	 *          the goal created using {@link #prepareGoal(Term)} which is to be
	 *          run.
	 * @return {@link PrologCode#SUCCESS SUCCESS}, {@link PrologCode#SUCCESS_LAST}
	 *         , {@link PrologCode#FAIL} (or {@link PrologCode#HALT})
	 * @throws PrologException
	 */
	public int execute(Goal goal) throws PrologException
	{
		haltExitCode = null;
		try
		{
			try
			{
				if (currentGoal == null)
				{
					throw new IllegalStateException("The goal is not prepared");
				}
				if (currentGoal != goal)
				{
					throw new IllegalArgumentException("The goal is not currently active");
				}
				if (goal.stopped)
				{
					throw new Stopped();
				}
				try
				{
					int rc = gnu.prolog.vm.interpreter.Predicate_call.staticExecute(this, !goal.firstTime, goal.getGoal());
					switch (rc)
					{
						case PrologCode.SUCCESS_LAST:
						case PrologCode.FAIL:
							goal.stopped = true;
							currentGoal = null;
							break;
						case PrologCode.SUCCESS:
							goal.firstTime = false;
							break;
					}
					return rc;
				}
				finally
				{
					environment.getUserOutput().flushOutput(null);
				}
			}
			catch (RuntimeException rex)
			{
				PrologException.systemError(rex);
				throw rex; // fake
			}
			catch (StackOverflowError se)
			{
				// too much recursion
				PrologException.systemError(se);
				throw se; // fake
			}
		}
		catch (PrologHalt ph)
		{
			stop(goal);
			haltExitCode = ph;
			return PrologCode.HALT;
		}
		catch (PrologException ex)
		{
			stop(goal);
			throw ex;
		}
	}

	/**
	 * Once the goal has been finished with and if the goal has not been stopped
	 * (as it will have been if SUCCESS_LAST or FAIL has been returned) then
	 * {@link #stop(Goal)} should be run.
	 * 
	 * 
	 * @param goal
	 *          the goal to stop.
	 */
	public void stop(Goal goal)
	{
		if (currentGoal != goal)
		{
			throw new IllegalArgumentException("The goal is not currently active");
		}
		if (goal.stopped)
		{
			throw new Stopped();
		}

		// This destroys information and means that this information is not
		// available after stop(Goal) has been called. Hence I (Daniel) have
		// commented it out. Hopefully this doesn't break anything (tests still pass
		// fine).
		// undo(0);

		for (int i = 0; i < backtrackInfoAmount; i++)
		{
			backtrackInfoStack[i] = null;
		}
		backtrackInfoAmount = 0;
		currentGoal = null;

		// We have just finished with a goal we originally forced another goal out
		// to do so pull that state back so that we can finish that goal
		ReturnPoint rp = returnPoints.get(goal);
		if (rp != null)
		{
			returnPoints.remove(rp.rCurrentGoal);// garbage collect
			context = rp.rContext;
			backtrackInfoStack = rp.rBacktrackInfoStack;
			backtrackInfoAmount = rp.rBacktrackInfoAmount;
			backtrackInfoMax = rp.rBacktrackInfoMax;
			variables = rp.rVariables;
			undoData = rp.rUndoData;
			undoDataAmount = rp.rUndoDataAmount;
			undoPositionAsked = rp.rUndoPositionAsked;
			currentGoal = rp.rCurrentGoal;
		}
	}

	/**
	 * Run the provided goalTerm once returning the value returned by
	 * {@link #execute(Goal)} and then stop the goal. This is thus an atomic
	 * operation on the Interpreter.
	 * 
	 * Runs {@link #prepareGoal(Term)} then {@link #execute(Goal)} then if
	 * necessary {@link #stop(Goal)}. Returns the return code from
	 * {@link #execute(Goal)}.
	 * 
	 * @param goalTerm
	 *          the term to be executed
	 * @return {@link PrologCode#SUCCESS}, {@link PrologCode#SUCCESS_LAST} or
	 *         {@link PrologCode#FAIL}
	 * @throws PrologException
	 */
	public int runOnce(Term goalTerm) throws PrologException
	{
		Goal goal = prepareGoal(goalTerm);
		try
		{
			int rc = execute(goal);
			return rc;
		}
		finally
		{
			if (!goal.stopped)
			{
				stop(goal);
			}
		}
	}

	/**
	 * Only call this method if you have had {@link PrologCode#HALT} returned by
	 * the most recent call to {@link #execute(Goal)}. Otherwise and
	 * {@link IllegalStateException} will be thrown.
	 * 
	 * @return The exit code when the prolog interpreter was halted
	 */
	public int getExitCode()
	{
		if (haltExitCode == null)
		{
			throw new IllegalStateException("Prolog Interpreter was not halted");
		}
		return haltExitCode.getExitCode();
	}

	/**
	 * Someone tried to do something with a {@link Goal} which had already been
	 * stopped.
	 * 
	 * @author Daniel Thomas
	 */
	private static class Stopped extends IllegalStateException
	{
		public Stopped()
		{
			super("The goal is already stopped");
		}

		private static final long serialVersionUID = 1L;
	}
}
