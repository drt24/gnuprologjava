/* GNU Prolog for Java
 * Copyright (C) 2010       Daniel Thomas
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
package gnu.prolog.io;

/**
 * 
 * @author Daniel Thomas
 */
abstract class AbstractOptions
{
	/**
	 * Current operator set to use in write term. User need to set this variable
	 * if ignore ops is true.
	 */
	public OperatorSet operatorSet;

	/**
	 * 
	 * @param opSet
	 *          the OperatorSet to use for these Options
	 */
	protected AbstractOptions(OperatorSet opSet)
	{
		operatorSet = opSet;
	}
}
