/**
 * 
 */
package gnu.prolog.vm.interpreter;

/**
 * Listens for TracerEvents.
 * 
 * @author Michiel Hendriks
 */
public interface TracerEventListener
{
	/**
	 * Called in case a tracer event was accepted by the tracer.
	 * 
	 * @param event
	 */
	void tracerEvent(TracerEvent event);
}
