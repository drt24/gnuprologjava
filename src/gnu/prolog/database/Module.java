/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2010       Daniel Thomas
 * Copyright (C) 2016       Matt Lilley
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
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologCodeUpdatedEvent;
import gnu.prolog.vm.PrologCodeListener;
import gnu.prolog.vm.PrologException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;


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
	public final static AtomTerm userAtom = AtomTerm.get("user");

	/** PredicateTag to code mapping */
	protected Map<CompoundTermTag, PrologCode> tag2code = new HashMap<CompoundTermTag, PrologCode>();
	protected final Map<CompoundTermTag, List<PrologCodeListenerRef>> tag2listeners = new HashMap<CompoundTermTag, List<PrologCodeListenerRef>>();


	public Module(AtomTerm name, List<CompoundTermTag> exports)
	{
		this.name = name;
		this.exports = exports;
	}

	public void importPredicates(Environment environment, AtomTerm exportingModule, List<CompoundTermTag> exports) throws PrologException
	{
	   for (CompoundTermTag export: exports)
	   {
	      importPredicate(environment, exportingModule, export);
	   }
	}

	public Predicate importPredicate(Environment environment, AtomTerm exportingModuleName, CompoundTermTag export) throws PrologException
	{
		Predicate p = null;
		try
		{
			p = createDefinedPredicate(export);
		}
		catch(IllegalStateException e)
		{
			PrologException.permissionError(AtomTerm.get("redefine"), AtomTerm.get("predicate"), export.getPredicateIndicator());
		}
                Term[] headArgs = new Term[export.arity];
		Term[] bodyArgs = new Term[export.arity];
		Term body = null;
		Module exportingModule = environment.getModule(exportingModuleName);
		MetaPredicateInfo metaPredicateInfo = exportingModule.getMetaPredicateInfo(environment, export);
                for (int i = 0; i < export.arity; i++)
                {
			headArgs[i] = new VariableTerm();
			if (metaPredicateInfo != null)
			{
				if (metaPredicateInfo.args[i] == MetaPredicateInfo.MetaType.EXISTS)
				{
					// This means that it could be called with ^(Quantifiers, X) or X
					// We want to substitute that for ^(Quantifiers, Module:X) or (Module:X), respectively
					// Add this to body:
					// ;(->(=(InArg, ^(Q, Goal)), =(OutArg, ^(Q, Module:Goal))), =(OutArg, Module:Goal)) which is
					// (  InArg = ^(Q,Goal)
					// -> X = ^(Q, Module:Goal)
					// ;  X = Module:InArg
					// )
					VariableTerm quantifier = new VariableTerm("Q");
					VariableTerm goal = new VariableTerm("Goal");
					VariableTerm outArg = new VariableTerm("OutArg");
					bodyArgs[i] = outArg;
					Term determinant = new CompoundTerm(AtomTerm.get(";"), new Term[]{new CompoundTerm(AtomTerm.get("->"), new Term[]{new CompoundTerm(AtomTerm.get("="), new Term[]{headArgs[i], new CompoundTerm(AtomTerm.get("^"), new Term[]{quantifier, goal})}),
																			  new CompoundTerm(AtomTerm.get("="), new Term[]{outArg, new CompoundTerm(AtomTerm.get("^"), new Term[]{quantifier, new CompoundTerm(AtomTerm.get(":"), new Term[]{name, goal})})})}),
													  new CompoundTerm(AtomTerm.get("="), new Term[]{outArg,
																			 new CompoundTerm(AtomTerm.get(":"), new Term[]{name, headArgs[i]})})});
					if (body == null)
					{
						body = determinant;
					}
					else
					{
						body = new CompoundTerm(AtomTerm.get(","), new Term[]{determinant, body});
					}
				}
				else if (metaPredicateInfo.args[i] != MetaPredicateInfo.MetaType.NORMAL)
				{
					bodyArgs[i] = new CompoundTerm(AtomTerm.get(":"), new Term[]{name, headArgs[i]});
				}
				else
				{
					bodyArgs[i] = headArgs[i];
				}
                        }
                        else
                        {
                                bodyArgs[i] = headArgs[i];
                        }
		}
                Term head = new CompoundTerm(export, headArgs);
		if (body == null)
		{
			body = crossModuleCall(exportingModuleName.value, new CompoundTerm(export, bodyArgs));
		}
		else
		{
			body = new CompoundTerm(AtomTerm.get(","), new Term[]{body, crossModuleCall(exportingModuleName.value, new CompoundTerm(export, bodyArgs))});
		}

		Term linkClause = new CompoundTerm(CompoundTermTag.get(":-", 2), new Term[]{head, body});
		p.setType(Predicate.TYPE.USER_DEFINED);
		p.addClauseLast(linkClause);
		p.setSourceModule(exportingModule);
		environment.pushModule(name);
		try
		{
			environment.loadPrologCode(export);
			return p;
		}
		finally
		{
			environment.popModule();
		}
        }

	/**
	 * convenience method for getting a Module:Goal term
	 *
	 * @param targetModule
	 *         Module to exeucte Goal in
	 * @param goal
	 *         Goal to execute
	 * @return cross-module goal
	 */
	public static Term crossModuleCall(String targetModule, Term goal)
	{
		return new CompoundTerm(AtomTerm.get(":"), new Term[]{AtomTerm.get(targetModule), goal});
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


	protected final ReferenceQueue<? super PrologCodeListener> prologCodeListenerReferenceQueue = new ReferenceQueue<PrologCodeListener>();

	/**
	 * A {@link WeakReference} to a {@link PrologCodeListener}
	 * 
	 */
	private static class PrologCodeListenerRef extends WeakReference<PrologCodeListener>
	{
		PrologCodeListenerRef(ReferenceQueue<? super PrologCodeListener> queue, PrologCodeListener listener,
				CompoundTermTag tag)
		{
			super(listener, queue);
			this.tag = tag;
		}

		CompoundTermTag tag;
	}


	/**
	 * add prolog code listener
	 * 
	 * @param tag
	 * @param listener
	 */
	public void addPrologCodeListener(Environment env, CompoundTermTag tag, PrologCodeListener listener)
	{
		synchronized (tag2listeners)
		{
			pollPrologCodeListeners();
			List<PrologCodeListenerRef> list = tag2listeners.get(tag);
			if (list == null)
			{
				list = new ArrayList<PrologCodeListenerRef>();
				tag2listeners.put(tag, list);
			}
			list.add(new PrologCodeListenerRef(prologCodeListenerReferenceQueue, listener, tag));
		}
	}

	/**
	 * remove prolog code listener
	 *
	 * @param env
	 * @param tag
	 * @param listener
	 */
	public void removePrologCodeListener(Environment env, CompoundTermTag tag, PrologCodeListener listener)
	{
		synchronized (tag2listeners)
		{
			pollPrologCodeListeners();
			List<PrologCodeListenerRef> list = tag2listeners.get(tag);
			if (list != null)
			{
				ListIterator<PrologCodeListenerRef> i = list.listIterator();
				while (i.hasNext())
				{
					PrologCodeListenerRef ref = i.next();
					PrologCodeListener lst = ref.get();
					if (lst == null)
					{
						i.remove();
					}
					else if (lst == listener)
					{
						i.remove();
						return;
					}
				}
			}
		}
	}

	public void predicateUpdated(Environment env, PredicateUpdatedEvent evt)
	{
		PrologCode code = tag2code.remove(evt.getTag());
		pollPrologCodeListeners();
		if (code == null) // if code was not loaded yet
		{
			return;
		}
		CompoundTermTag tag = evt.getTag();
		synchronized (tag2listeners)
		{
			List<PrologCodeListenerRef> list = tag2listeners.get(tag);
			if (list != null)
			{
				PrologCodeUpdatedEvent uevt = new PrologCodeUpdatedEvent(env, tag);
				ListIterator<PrologCodeListenerRef> i = list.listIterator();
				while (i.hasNext())
				{
					PrologCodeListenerRef ref = i.next();
					PrologCodeListener lst = ref.get();
					if (lst == null)
					{
						i.remove();
					}
					else
					{
						lst.prologCodeUpdated(uevt);
						return;
					}
				}
			}
		}
	}

	/**
	 * Retrieve prolog code for a given tag
	 *   If the code is not already loaded, load it here by invoking loadPrologCode on the environment
	 *
	 * @param env
	 *   Environment to use for loading the code if not already compiled
	 * @param tag
	 *   Tag of the predicate to load
	 * @return the {@link PrologCode} for the tag
	 * @throws PrologException
	 */
	public synchronized PrologCode getPrologCode(Environment env, CompoundTermTag tag) throws PrologException
	{
		PrologCode code = tag2code.get(tag);
		if (code == null)
		{
			code = env.loadPrologCode(tag);
			tag2code.put(tag, code);
		}
		return code;

	}

	protected void pollPrologCodeListeners()
	{
		PrologCodeListenerRef ref;
		synchronized (tag2listeners)
		{
			while (null != (ref = (PrologCodeListenerRef) prologCodeListenerReferenceQueue.poll()))
			{
				List<PrologCodeListenerRef> list = tag2listeners.get(ref.tag);
				list.remove(ref);
			}
		}
	}

	@Override
	public String toString()
	{
		return name.toString();
	}

	public AtomTerm getName()
	{
		return name;
	}

	public MetaPredicateInfo getMetaPredicateInfo(Environment env, CompoundTermTag tag) throws PrologException
	{
		Predicate p = tag2predicate.get(tag);
		if (p == null)
		{
			return null;
		}
		// We have to also ensure the code is loaded. Foreign predicates that define their
		// own meta-args will only do so once resolved. We are going to have to load them
		// very soon anyway if we are about to call them
		if (p.getType() == Predicate.TYPE.BUILD_IN)
		{
			env.pushModule(name);
			try
			{
				getPrologCode(env, tag);
			}
			finally
			{
				env.popModule();
			}
		}
		return p.getMetaPredicateInfo();
	}
}
