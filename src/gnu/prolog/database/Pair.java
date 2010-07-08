/**
 * 
 */
package gnu.prolog.database;

/**
 * A pair of a <L> and a <R>
 * 
 * (Because java doesn't do tuples)
 * 
 * @author daniel
 * 
 */
public class Pair<L, R>
{

	public L left;
	public R right;

	public Pair(L left, R right)
	{
		this.left = left;
		this.right = right;
	}
}
