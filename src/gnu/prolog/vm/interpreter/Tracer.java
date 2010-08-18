/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 *
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
package gnu.prolog.vm.interpreter;

import gnu.prolog.io.PrologStream;
import gnu.prolog.io.TermWriter;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Interpreter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author Michiel Hendriks
 * 
 */
public class Tracer
{
	/**
	 * Different trace levels
	 * 
	 * @author Michiel Hendriks
	 */
	public enum TraceLevel
	{
		CALL
		{
			@Override
			public String toString()
			{
				return "Call";
			}
		},
		REDO
		{
			@Override
			public String toString()
			{
				return "Redo";
			}
		},
		EXIT
		{
			@Override
			public String toString()
			{
				return "Exit";
			}
		},
		FAIL
		{
			@Override
			public String toString()
			{
				return "Fail";
			}
		};

		/**
		 * Convert a string to an enum set
		 * 
		 * @param lvl
		 * @return the EnumSet(TraceLevel) for the string lvl
		 */
		public static EnumSet<TraceLevel> fromString(String lvl)
		{
			if (lvl.equalsIgnoreCase("all"))
			{
				return EnumSet.allOf(TraceLevel.class);
			}
			else if (lvl.equalsIgnoreCase(CALL.toString()))
			{
				return EnumSet.of(CALL);
			}
			else if (lvl.equalsIgnoreCase(REDO.toString()))
			{
				return EnumSet.of(REDO);
			}
			else if (lvl.equalsIgnoreCase(EXIT.toString()))
			{
				return EnumSet.of(EXIT);
			}
			else if (lvl.equalsIgnoreCase(FAIL.toString()))
			{
				return EnumSet.of(FAIL);
			}
			return EnumSet.noneOf(TraceLevel.class);
		}
	}

	public static final Set<CompoundTermTag> UNTRACEABLE = new HashSet<CompoundTermTag>();

	static
	{
		UNTRACEABLE.add(CompoundTermTag.get("trace", 0));
		UNTRACEABLE.add(CompoundTermTag.get("untrace", 0));
		UNTRACEABLE.add(CompoundTermTag.get("tracing", 0));
	}

	/**
	 * If tracing is active
	 */
	protected boolean tracingActive;

	/**
	 * Current active trace points
	 */
	protected Map<CompoundTermTag, EnumSet<TraceLevel>> tracePoints;

	protected PrologStream output;

	protected Set<TracerEventListener> listeners;

	protected static final int callStackGrow = 4096;
	protected CompoundTermTag[] callStack = new CompoundTermTag[callStackGrow];
	protected int callStackPointer = 0;
	protected int callStackMax = callStackGrow;

	/**
	 * @param stdout
	 * 
	 */
	public Tracer(PrologStream stdout)
	{
		listeners = new HashSet<TracerEventListener>();
		tracePoints = new HashMap<CompoundTermTag, EnumSet<TraceLevel>>();
		output = stdout;
	}

	public void addTracerEventListener(TracerEventListener listener)
	{
		listeners.add(listener);
	}

	public void removeTracerEventListener(TracerEventListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Enable/disable tracing
	 * 
	 * @param tracingActive
	 */
	public void setActive(boolean tracingActive)
	{
		this.tracingActive = tracingActive;
	}

	/**
	 * @return True if tracing is active
	 */
	public boolean isActive()
	{
		return tracingActive;
	}

	/**
	 * @param pred
	 * @param levels
	 */
	public void setTrace(CompoundTermTag pred, EnumSet<TraceLevel> levels)
	{
		tracePoints.put(pred, EnumSet.copyOf(levels));
		println(String.format("%% Tracing %s for %s", pred, levels));
	}

	/**
	 * Set a trace point
	 * 
	 * @param pred
	 * @param levels
	 */
	public void addTrace(CompoundTermTag pred, EnumSet<TraceLevel> levels)
	{
		if (UNTRACEABLE.contains(pred))
		{
			return;
		}
		EnumSet<TraceLevel> set = tracePoints.get(pred);
		if (set == null)
		{
			set = EnumSet.copyOf(levels);
			tracePoints.put(pred, set);
		}
		else
		{
			set.addAll(levels);
		}
		println(String.format("%% Tracing %s for %s", pred, set));
	}

	/**
	 * @param pred
	 * @param level
	 */
	public void addTrace(CompoundTermTag pred, TraceLevel level)
	{
		addTrace(pred, EnumSet.of(level));
	}

	/**
	 * Remove a trace point
	 * 
	 * @param pred
	 */
	public void removeTrace(CompoundTermTag pred)
	{
		tracePoints.remove(pred);
		println(String.format("%% Not tracing %s", pred));
	}

	/**
	 * Remove a given level
	 * 
	 * @param pred
	 * @param levels
	 */
	public void removeTrace(CompoundTermTag pred, EnumSet<TraceLevel> levels)
	{
		EnumSet<TraceLevel> set = tracePoints.get(pred);
		if (set != null)
		{
			set.removeAll(levels);
			if (set.isEmpty())
			{
				tracePoints.remove(pred);
				println(String.format("%% Not tracing %s", pred));
			}
			else
			{
				println(String.format("%% Tracing %s for %s", pred, set));
			}
		}
	}

	/**
	 * @param pred
	 * @param level
	 */
	public void removeTrace(CompoundTermTag pred, TraceLevel level)
	{
		removeTrace(pred, EnumSet.of(level));
	}

	/**
	 * Remove all trace points
	 */
	public void removeAllTraces()
	{
		tracePoints.clear();
	}

	public CompoundTermTag[] getCallStack()
	{
		CompoundTermTag[] res = new CompoundTermTag[callStackPointer];
		System.arraycopy(callStack, 0, res, 0, callStackPointer);
		return res;
	}

	public void decreaseDepth()
	{
		if (callStackPointer >= 0)
		{
			callStack[--callStackPointer] = null;
		}
		// else invalid stack
	}

	/**
	 * A trace event
	 * 
	 * @param level
	 * @param interpreter
	 * @param tag
	 * @param args
	 */
	public void traceEvent(TraceLevel level, Interpreter interpreter, CompoundTermTag tag, Term args[])
	{
		int execDepth = callStackPointer;
		// update call stack
		switch (level)
		{
			case CALL:
			case REDO:
				if (callStackPointer == callStackMax)
				{
					// increase stack
					CompoundTermTag tmp[] = new CompoundTermTag[callStackMax + callStackGrow];
					System.arraycopy(callStack, 0, tmp, 0, callStackPointer);
					callStack = tmp;
					callStackMax += callStackGrow;
				}
				callStack[callStackPointer++] = tag;
				break;
			case EXIT:
			case FAIL:
			default:
		}

		if (!tracingActive)
		{
			return;
		}
		EnumSet<TraceLevel> lvlset = tracePoints.get(tag);
		if (lvlset == null)
		{
			// not tracing this tag
			return;
		}
		if (lvlset.contains(level))
		{
			StringBuilder sb = new StringBuilder();
			for (Term arg : args)
			{
				if (sb.length() > 0)
				{
					sb.append(", ");
				}
				sb.append(TermWriter.toString(arg));
			}
			println(String.format("%7s: (%d) %s(%s)", level.toString(), execDepth, tag.functor.value, sb.toString()));
			if (!listeners.isEmpty())
			{
				sendEvent(level, interpreter, tag, args);
			}
		}
	}

	/**
	 * Notify the listeners
	 * 
	 * @param level
	 * @param interpreter
	 * @param tag
	 * @param args
	 */
	protected void sendEvent(TraceLevel level, Interpreter interpreter, CompoundTermTag tag, Term args[])
	{
		TracerEvent event = new TracerEvent(this, level, tag, args);
		for (TracerEventListener listener : listeners)
		{
			listener.tracerEvent(event);
		}
	}

	protected void println(String string)
	{
		if (output != null)
		{
			try
			{
				output.putCodeSequence(null, null, string + "\n");
				output.flushOutput(null);
			}
			catch (Exception e)
			{
			}
		}
	}

	public void reportStatus()
	{
		println(String.format("%% Tracing enabled: %s", tracingActive));
		for (Entry<CompoundTermTag, EnumSet<TraceLevel>> entry : tracePoints.entrySet())
		{
			println(String.format("%% Trace point: %s = %s", entry.getKey(), entry.getValue()));
		}
	}

}
