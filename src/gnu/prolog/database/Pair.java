/**
 * 
 */
package gnu.prolog.database;

/**
 * A pair of a LeftType (L) and a RightType (R)
 * 
 * (Because java doesn't do tuples)
 * 
 * @author Daniel Thomas
 * @param <L>
 * @param <R>
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
