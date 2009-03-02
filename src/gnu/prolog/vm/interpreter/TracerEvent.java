/**
 * 
 */
package gnu.prolog.vm.interpreter;

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.interpreter.Tracer.TraceLevel;

/**
 * The event object send to TracerEventListeners
 * 
 * @author Michiel Hendriks
 */
public class TracerEvent
{
	protected Tracer tracer;
	protected TraceLevel level;
	protected CompoundTermTag tag;
	protected Term[] args;

	public TracerEvent(Tracer tracer, TraceLevel level, CompoundTermTag tag, Term[] args)
	{
		this.tracer = tracer;
		this.level = level;
		this.tag = tag;
		this.args = args;
	}

	public Term[] getArgs()
	{
		return args;
	}

	public TraceLevel getLevel()
	{
		return level;
	}

	public CompoundTermTag getTag()
	{
		return tag;
	}

	public Tracer getTracer()
	{
		return tracer;
	}
}
