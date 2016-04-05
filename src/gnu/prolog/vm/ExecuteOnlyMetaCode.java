/* GNU Prolog for Java
 * Copyright (C) 2010       Daniel Thomas
 * Copyright (C) 2016       Matt Lilley
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
package gnu.prolog.vm;

import gnu.prolog.database.MetaPredicateInfo;

/**
 * For built-in predicates with meta-arg-information
 * 
 * @author Matt Lilley
 */
public abstract class ExecuteOnlyMetaCode extends ExecuteOnlyCode
{

	public abstract RC execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException;


	public abstract MetaPredicateInfo getMetaPredicateInfo();
}
