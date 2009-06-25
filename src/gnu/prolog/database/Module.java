/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA. The text ol license can be also found
 * at http://www.gnu.org/copyleft/lgpl.html
 */
package gnu.prolog.database;

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Module in database
 * 
 * @author Contantine A Plotnikov
 */
public class Module
{
	/** map from tag to predicates */
	Map<CompoundTermTag, Predicate> tag2predicate = new HashMap<CompoundTermTag, Predicate>();

	/** initialization */
	List<Term> initialization = new ArrayList<Term>();

	/**
	 * create new predicate defined in this module
	 * 
	 * @param tag
	 *          tag of this predicate
	 * @return created predicate
	 * @throws IllegalStateException
	 *           when predicate already exists
	 */
	public synchronized Predicate createDefinedPredicate(CompoundTermTag tag)
	{
		if (tag2predicate.containsKey(tag))
		{
			throw new IllegalStateException("A predicate already exists.");
		}
		Predicate p = new Predicate(this, tag);
		tag2predicate.put(tag, p);
		predicateUpdated(tag);
		return p;
	}

	/**
	 * get predicate defined in this module
	 * 
	 * @param tag
	 *          tag of this predicate
	 * @return predicate defined in this module or null if predicate is not found
	 */
	public synchronized Predicate getDefinedPredicate(CompoundTermTag tag)
	{
		Predicate p = tag2predicate.get(tag);
		if (p == null)
		{
			return null;
		}
		return p;
	}

	public synchronized void removeDefinedPredicate(CompoundTermTag tag)
	{
		tag2predicate.remove(tag);
		predicateUpdated(tag);
	}

	/** add term to initialization list */
	public synchronized void addInitialization(Term term)
	{
		initialization.add(term);
	}

	/** get initaliztion */
	public synchronized List<Term> getInitialization()
	{
		return initialization;
	}

	/** get predicate tags */
	public synchronized Set<CompoundTermTag> getPredicateTags()
	{
		return tag2predicate.keySet();
	}

	List<PredicateListener> predicateListeners = new ArrayList<PredicateListener>();

	public synchronized void predicateUpdated(CompoundTermTag tag)
	{
		PredicateUpdatedEvent evt = new PredicateUpdatedEvent(this, tag);
		Iterator<PredicateListener> i = new ArrayList<PredicateListener>(predicateListeners).iterator();
		while (i.hasNext())
		{
			PredicateListener listener = i.next();
			listener.predicateUpdated(evt);
		}
	}

	public synchronized void addPredicateListener(PredicateListener listener)
	{
		predicateListeners.add(listener);
	}

	public synchronized void removePredicateListener(PredicateListener listener)
	{
		predicateListeners.remove(listener);
	}

}
