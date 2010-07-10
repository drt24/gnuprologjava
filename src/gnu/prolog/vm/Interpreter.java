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
 * you need to use interpreter from two threads, create new interpreter from
 * Environment
 */

public final class Interpreter
{
	/**
	 * The "slack" used in float comparison
	 */
	static final double FLOAT_EPSILON = 0.0000001d;

	/** environment for this interpreter */
	public Environment environment;

	/**
	 * Keeps track of prolog call/return traces
	 */
	protected Tracer tracer;

	/**
	 * Contains an {@link PrologHalt} instance when the interpreter was halted.
	 */
	protected PrologHalt haltExitCode;

	/**
	 * Used to store context information for the lifespan of a goal. The context
	 * is cleared every time a new goal is prepared. Can be used by predicates to
	 * store information based on goals.
	 */
	protected Map<String, Object> context;

	/**
	 * this constructor should not be used by client programs
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
			// TODO Auto-generated catch block
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

	public Object putContext(String key, Object contextValue)
	{
		return context.put(key, contextValue);
	}

	public Object getContext(String key)
	{
		return context.get(key);
	}

	// backtrack menthods
	// /** stack that contains backtrack info */
	// ArrayList backtrackInfoStack = new ArrayList(4096,4096);
	// int backtrackInfoStackSize = 0;

	// /** push backtrack information */
	// public void pushBacktrackInfo(BacktrackInfo bi)
	// {
	// backtrackInfoStack.add(bi);
	// }

	// /** pop backtrack information */
	// public BacktrackInfo popBacktrackInfo()
	// {
	// return
	// (BacktrackInfo)backtrackInfoStack.remove(backtrackInfoStack.size()-1);
	// }

	// public void popBacktrackInfoUntil(BacktrackInfo cutPoint)
	// {
	// int pos = backtrackInfoStack.lastIndexOf(cutPoint);
	// if (pos >= 0)
	// {
	// backtrackInfoStack.removeRange(pos, backtrackInfoStack.size()-1);
	// }
	// else
	// {
	// System.err.println("pop until failed ");
	// }
	// }

	// /** peek top backtrack information */
	// public BacktrackInfo peekBacktrackInfo()
	// {
	// return (BacktrackInfo)backtrackInfoStack.get(backtrackInfoStack.size()-1);
	// }

	protected BacktrackInfo backtrackInfoStack[] = new BacktrackInfo[4096];
	protected int backtrackInfoAmount = 0;
	protected int backtrackInfoMax = 4096;
	protected int backtrackInfoGrow = 4096;

	/** push backtrack information */
	public void pushBacktrackInfo(BacktrackInfo bi)
	{
		if (backtrackInfoAmount == backtrackInfoMax)
		{
			BacktrackInfo tmp[] = new BacktrackInfo[backtrackInfoMax + backtrackInfoGrow];
			System.arraycopy(backtrackInfoStack, 0, tmp, 0, backtrackInfoAmount);
			backtrackInfoStack = tmp;
			backtrackInfoMax += backtrackInfoGrow;
		}
		backtrackInfoStack[backtrackInfoAmount++] = bi;
	}

	/** pop backtrack information */
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

	/** peek top backtrack information */
	public BacktrackInfo peekBacktrackInfo()
	{
		return backtrackInfoStack[backtrackInfoAmount - 1];
	}

	// Undo Stack methods
	/** get current undo position */
	public int getUndoPosition()
	{
		undoPositionAsked = true;
		return undoDataAmount;
	}

	/** undo changes until this position */
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

	/** add variable undo */
	public void addVariableUndo(VariableTerm variable)
	{
		if (undoPositionAsked || !(undoData[undoDataAmount - 1] instanceof VariableUndoData))
		{
			addSpecialUndo(new VariableUndoData());
		}
		if (variablesAmount == variables.length)
		{
			VariableTerm tmp[] = new VariableTerm[growSize + variables.length];
			System.arraycopy(variables, 0, tmp, 0, variablesAmount);
			variables = tmp;
		}
		variables[variablesAmount] = variable;
		variablesAmount++;
	}

	/** add special undo */
	public void addSpecialUndo(UndoData undoDatum)
	{
		if (undoDataAmount == undoData.length)
		{
			UndoData tmp[] = new UndoData[growSize + undoData.length];
			System.arraycopy(undoData, 0, tmp, 0, undoDataAmount);
			undoData = tmp;
		}
		undoData[undoDataAmount] = undoDatum;
		undoDataAmount++;
	}

	// final static int maxStackSize = 0x1000000; // is not used
	protected final static int pageSize = 0x4096;
	protected final static int growSize = 0x4096;

	protected VariableTerm variables[] = new VariableTerm[4096];
	protected int variablesAmount = 0;

	protected UndoData undoData[] = new UndoData[4096];
	protected int undoDataAmount = 0;
	protected boolean undoPositionAsked = true;

	protected class VariableUndoData implements UndoData
	// maybe later this class will be pooled
	{
		protected int startPosion;

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

	/** unify two terms, no undo done */
	public int simple_unify(Term t1, Term t2) throws PrologException
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
					rc = simple_unify(args1[i].dereference(), args2[i].dereference());
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

	/** unify two terms */
	public int unify(Term t1, Term t2) throws PrologException
	{
		int undoPos = getUndoPosition();
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
					rc = simple_unify(args1[i].dereference(), args2[i].dereference());
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
		if (rc == PrologCode.FAIL)
		{
			undo(undoPos);
		}
		return rc;
	}

	/** user level calls */
	public static class Goal
	{
		Term goal = null;
		boolean firstTime = true;
		boolean stopped = false;
		Term args[] = null;
	}

	protected Goal currentGoal;

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
	protected class ReturnPoint
	{
		public Map<String, Object> context;

		public BacktrackInfo backtrackInfoStack[];
		public int backtrackInfoAmount;
		public int backtrackInfoMax;
		public int backtrackInfoGrow;

		public VariableTerm variables[];
		public int variablesAmount;

		public UndoData undoData[];
		public int undoDataAmount;
		public boolean undoPositionAsked;

		public Goal currentGoal;
	}

	/**
	 * Map of Goals to ReturnPoints so that we can save the state in a return
	 * point if in {@link #prepareGoal(Term)} we discover that we need to execute
	 * another goal first before finishing executing the {@link #currentGoal}.
	 */
	private Map<Goal, ReturnPoint> returnPoints = new HashMap<Goal, ReturnPoint>();

	/** prepare goal for execution */
	public Goal prepareGoal(Term term) throws PrologException
	{
		ReturnPoint rp = null;
		if (currentGoal != null)
		{// Take a copy of the current state into a Return point
			rp = new ReturnPoint();
			rp.context = context;
			rp.backtrackInfoStack = backtrackInfoStack.clone();
			rp.backtrackInfoAmount = backtrackInfoAmount;
			rp.backtrackInfoMax = backtrackInfoMax;
			rp.variables = variables.clone();
			rp.variablesAmount = variablesAmount;
			rp.undoData = undoData.clone();
			rp.undoDataAmount = undoDataAmount;
			rp.undoPositionAsked = undoPositionAsked;
			rp.currentGoal = currentGoal;
		}
		currentGoal = new Goal();
		currentGoal.goal = term;
		currentGoal.args = new Term[] { term };
		context.clear();
		if (rp != null)
		{// save the return point so that we can jump back later
			returnPoints.put(currentGoal, rp);
		}
		return currentGoal;
	}

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
					throw new IllegalStateException("The goal is already stopped");
				}
				try
				{
					int rc = gnu.prolog.vm.interpreter.Predicate_call.staticExecute(this, !goal.firstTime, goal.goal);
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

	public void stop(Goal goal)
	{
		if (currentGoal != goal)
		{
			throw new IllegalArgumentException("The goal is not currently active");
		}
		if (goal.stopped)
		{
			throw new IllegalStateException("The goal is already stopped");
		}
		undo(0);
		// backtrackInfoStack.removeRange(0, backtrackInfoStack.size()-1);
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
			context = rp.context;
			backtrackInfoStack = rp.backtrackInfoStack;
			backtrackInfoAmount = rp.backtrackInfoAmount;
			backtrackInfoMax = rp.backtrackInfoMax;
			backtrackInfoGrow = rp.backtrackInfoGrow;
			variables = rp.variables;
			undoData = rp.undoData;
			undoDataAmount = rp.undoDataAmount;
			undoPositionAsked = rp.undoPositionAsked;
			currentGoal = rp.currentGoal;
		}
	}

	/**
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
}
