/* GNU Prolog for Java
 * Copyright (C) 2010       Daniel Thomas
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
