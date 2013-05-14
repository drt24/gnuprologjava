/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
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
package gnu.prolog.database;

import gnu.prolog.term.CompoundTermTag;

/** event notifying about event with predicates */
public class PredicateUpdatedEvent extends java.util.EventObject
{
	private static final long serialVersionUID = -7290433520091984961L;

	protected CompoundTermTag tag;

	protected PredicateUpdatedEvent(Module module, CompoundTermTag tag)
	{
		super(module);
		this.tag = tag;
	}

	/**
	 * get tag of changed predicate
	 * 
	 * @return the tag of the changed predicate
	 */
	public CompoundTermTag getTag()
	{
		return tag;
	}

	/**
	 * get module of changed predicate
	 * 
	 * @return the module of the changed predicate
	 */
	public Module getModule()
	{
		return (Module) getSource();
	}

	@Override
	public String toString()
	{
		return "(" + tag + ", " + getModule() + ")";
	}
}
