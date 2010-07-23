/* GNU Prolog for Java
 * Copyright (C) 2010  Daniel Thomas
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
package gnu.prolog.term;

import gnu.prolog.vm.Environment;

/**
 * Terms for which the value varies depending on prolog flags extend this class.
 * They pick which to use for value on creation and at {@link #dereference()}.
 * 
 * @author Daniel Thomas
 */
public abstract class ChangeableTerm extends VariableTerm
{
	private static final long serialVersionUID = 3298672894800988968L;

	/**
	 * The Environment we will use to get the flags from.
	 */
	protected Environment environment;

	protected Term getPrologFlag(AtomTerm term)
	{
		return environment.getPrologFlag(term);
	}

	protected ChangeableTerm(Environment env)
	{
		if (env != null)
		{
			environment = env;
		}
		else
		{
			throw new IllegalArgumentException("Environment cannot be null");
		}
	}

	/**
	 * The value may also be altered by this method in child classes.
	 */
	@Override
	public abstract Term dereference();
}
