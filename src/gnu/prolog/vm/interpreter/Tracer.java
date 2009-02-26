/**
 * 
 */
package gnu.prolog.vm.interpreter;

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Interpreter;

import java.util.HashMap;
import java.util.Map;

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
		ALL
		{
			@Override
			public String toString()
			{
				return "all";
			}
		},
		CALL
		{
			@Override
			public String toString()
			{
				return "call";
			}
		},
		REDO
		{
			@Override
			public String toString()
			{
				return "redo";
			}
		},
		EXIT
		{
			@Override
			public String toString()
			{
				return "exit";
			}
		},
		FAIL
		{
			@Override
			public String toString()
			{
				return "fail";
			}
		}
	}

	/**
	 * If tracing is active
	 */
	protected boolean tracingActive = true;

	/**
	 * Current active trace points
	 */
	protected Map<String, TraceLevel> tracePoints = new HashMap<String, TraceLevel>();

	/**
	 * 
	 */
	public Tracer()
	{
		tracePoints = new HashMap<String, TraceLevel>();
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
	 * Set a trace point
	 * 
	 * @param pred
	 * @param level
	 */
	public void addTrace(String pred, TraceLevel level)
	{
		tracePoints.put(pred, level);
	}

	/**
	 * Remove a trace point
	 * 
	 * @param pred
	 * @param level
	 */
	public void removeTrace(String pred, TraceLevel level)
	{
		tracePoints.put(pred, level);
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
		TraceLevel setlvl = tracePoints.get(tag);
		if (setlvl == null)
		{
			// not tracing this tag
			return;
		}
		if (setlvl == TraceLevel.ALL || setlvl == level)
		{
			System.err.println(String.format("%7s: %s(...)", level.toString(), tag));
		}
	}
}
