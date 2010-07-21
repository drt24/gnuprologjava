/**
 * 
 */
package gnu.prolog.database;

/**
 * A pair of a {@link L} and a {@link R}
 * 
 * (Because java doesn't do tuples)
 * 
 * @author daniel
 * 
 */
public class Pair<L, R>
{

	/**
	 * The left part of the tuple
	 */
	public L left;
	/**
	 * The right part of the tuple
	 */
	public R right;

	/**
	 * Construct a tuple with a left and right part
	 * 
	 * @param left
	 *          the left part
	 * @param right
	 *          the right part
	 */
	public Pair(L left, R right)
	{
		this.left = left;
		this.right = right;
	}
}
