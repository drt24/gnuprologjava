/**
 * 
 */
package gnu.prolog.demo.mentalarithmetic;

/**
 * Exception thrown when we have no answer to give.
 * @author daniel
 *
 */
public class NoAnswerException extends Exception
{
    private static final long serialVersionUID = 2808738995798796114L;

    /**
     * @param message
     */
    public NoAnswerException(String message)
    {
        super(message);
    }

}
