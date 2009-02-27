/**
 * 
 */
package gnu.prolog.vm.interpreter;

import gnu.prolog.io.TermWriter;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologStream;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		 * @return
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
	
	public static final Set<String> UNTRACEABLE = new HashSet<String>();
	
	static {
		UNTRACEABLE.add("trace/0");
		UNTRACEABLE.add("untrace/0");
		UNTRACEABLE.add("tracing/0");
	}

	/**
	 * If tracing is active
	 */
	protected boolean tracingActive = true;

	/**
	 * Current active trace points
	 */
	protected Map<String, EnumSet<TraceLevel>> tracePoints;

	protected PrologStream output;

	/**
	 * 
	 */
	public Tracer(PrologStream stdout)
	{
		tracePoints = new HashMap<String, EnumSet<TraceLevel>>();
		output = stdout;
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
	public void setTrace(String pred, EnumSet<TraceLevel> levels)
	{
		tracePoints.put(pred, EnumSet.copyOf(levels));
		println(String.format("%% Tracing %s for %s", pred, levels));
	}

	/**
	 * Set a trace point
	 * 
	 * @param pred
	 * @param level
	 */
	public void addTrace(String pred, EnumSet<TraceLevel> levels)
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
	 * @param levels
	 */
	public void addTrace(String pred, TraceLevel level)
	{
		addTrace(pred, EnumSet.of(level));
	}

	/**
	 * Remove a trace point
	 * 
	 * @param pred
	 */
	public void removeTrace(String pred)
	{
		tracePoints.remove(pred);
		println(String.format("%% Not tracing", pred));
	}

	/**
	 * Remove a given level
	 * 
	 * @param pred
	 * @param level
	 */
	public void removeTrace(String pred, EnumSet<TraceLevel> levels)
	{
		EnumSet<TraceLevel> set = tracePoints.get(pred);
		if (set != null)
		{
			set.removeAll(levels);
			if (set.isEmpty())
			{
				tracePoints.remove(pred);
				println(String.format("%% Not tracing", pred));
			}
			else
			{
				println(String.format("%% Tracing %s for %s", pred, set));
			}
		}
	}

	/**
	 * @param pred
	 * @param levels
	 */
	public void removeTrace(String pred, TraceLevel level)
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
		if (!tracingActive)
		{
			return;
		}
		EnumSet<TraceLevel> lvlset = tracePoints.get(tag.toString());
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
			println(String.format("%7s: %s(%s)", level.toString(), tag.functor.value, sb.toString()));
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

}
