/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
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
package gnu.prolog.database;

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
	protected Map<CompoundTermTag, Predicate> tag2predicate = new HashMap<CompoundTermTag, Predicate>();

	/** initialization */
	protected List<Pair<PrologTextLoaderError, Term>> initialization = Collections
			.synchronizedList(new ArrayList<Pair<PrologTextLoaderError, Term>>());

	protected AtomTerm name = null;
	protected List<CompoundTermTag> exports = null;
	public static final CompoundTermTag moduleTag = CompoundTermTag.get("module", 2);

	public Module(AtomTerm name, List<CompoundTermTag> exports)
	{
		this.name = name;
		this.exports = exports;
	}

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
		return tag2predicate.get(tag);
	}

	public synchronized void removeDefinedPredicate(CompoundTermTag tag)
	{
		tag2predicate.remove(tag);
		predicateUpdated(tag);
	}

	/**
	 * If a Predicate for the tag exists then get it else create it Does this in
	 * one synchronized operation - otherwise thread safety issues could occur.
	 * 
	 * @param tag
	 * @return the Predicate retrieved or if it didn't exist created.
	 * @see #getDefinedPredicate(CompoundTermTag)
	 * @see #createDefinedPredicate(CompoundTermTag)
	 */
	public synchronized Predicate getOrCreateDefinedPredicate(CompoundTermTag tag)
	{
		Predicate p = getDefinedPredicate(tag);
		if (p == null)
		{
			p = createDefinedPredicate(tag);
		}
		return p;
	}

	/**
	 * add term to initialization list
	 * 
	 * @param prologTextLoaderError
	 *          the partial error to be used if this term throws an error
	 * @param term
	 *          the goal to execute at initialization
	 */
	public synchronized void addInitialization(PrologTextLoaderError prologTextLoaderError, Term term)
	{
		initialization.add(new Pair<PrologTextLoaderError, Term>(prologTextLoaderError, term));
	}

	/**
	 * get initaliztion
	 * 
	 * @return the list of the goals with their corresponding partial
	 *         {@link PrologTextLoaderError}s to be used if they throw an error.
	 * */
	public synchronized List<Pair<PrologTextLoaderError, Term>> getInitialization()
	{
		return initialization;
	}

	/**
	 * Intended to be run from {@link Environment#runInitialization(Interpreter)}
	 * and from nowhere else.
	 * 
	 * Resets the initialization list to the empty list so that they can be
	 * iterated through again later.
	 * 
	 * Should be called in a synchronized block which read out the initiaization
	 * list using {@link #getInitialization()}
	 */
	public synchronized void clearInitialization()
	{
		initialization = Collections.synchronizedList(new ArrayList<Pair<PrologTextLoaderError, Term>>());
	}

	/**
	 * get predicate tags
	 * 
	 * @return the set of tags for {@link Predicate}s.
	 * */
	public synchronized Set<CompoundTermTag> getPredicateTags()
	{
		return tag2predicate.keySet();
	}

	protected final List<PredicateListener> predicateListeners = new ArrayList<PredicateListener>();

	public void predicateUpdated(CompoundTermTag tag)
	{
		// We need to synchronize on predicateListeners rather than Module as
		// otherwise we end up locking module before environment and get deadlock
		synchronized (predicateListeners)
		{
			PredicateUpdatedEvent evt = new PredicateUpdatedEvent(this, tag);
			for (PredicateListener listener : predicateListeners)
			{
				listener.predicateUpdated(evt);
			}
		}
	}

	public void addPredicateListener(PredicateListener listener)
	{
		synchronized (predicateListeners)
		{
			predicateListeners.add(listener);
		}
	}

	public void removePredicateListener(PredicateListener listener)
	{
		synchronized (predicateListeners)
		{
			predicateListeners.remove(listener);
		}
	}

}
