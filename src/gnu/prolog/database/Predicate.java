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

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.TermConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Predicate in database
 * 
 * @author Contantine A Plotnikov
 */
public class Predicate
{
	/**
	 * The possible types of Predicate
	 * 
	 * @author Daniel Thomas
	 */
	public static enum TYPE
	{
		/** type of predicate is not yet set */
		UNDEFINED,
		/**
		 * predicate is a control construct. This type of predicate could be created
		 * only during initialization.
		 */
		CONTROL,
		/**
		 * predicate is a build in. This type of predicate could be created only
		 * during initialization.
		 */
		BUILD_IN,
		/** predicate is a user defined predicate. */
		USER_DEFINED,
		/** predicate is a user defined predicate defined in Java Class. */
		EXTERNAL
	}

	/**
	 * type of predicate. It should be either UNDEFINED, CONTROL ,BUILD_IN,
	 * USER_DEFINED or EXTERNAL
	 */
	protected TYPE type = TYPE.UNDEFINED;
	/** a tag of predicate head */
	protected CompoundTermTag tag;
	/** list of clauses for this predicate */
	protected List<Term> clauses = Collections.synchronizedList(new ArrayList<Term>());
	/** flag which indicate that clauses was added for this predicate */
	protected boolean propertiesLocked = false;
	/** dynamic property of predicate */
	protected boolean dynamicFlag = false;
	/** class name for external predicate */
	protected String javaClassName;
	/** set files where this predicate is defined */
	protected Set<String> files = new HashSet<String>();
	/** current module */
	protected Module module;

	/**
	 * constructor of predicate
	 * 
	 * @param module
	 * @param tag
	 */
	public Predicate(Module module, CompoundTermTag tag)
	{
		this.tag = tag;
		this.module = module;
	}

	/**
	 * get clauses of predicate
	 * 
	 * You must synchronize on this class when iterating through this list as
	 * although it is a synchronized list you are getting a unmodifiable view of
	 * that list.
	 * 
	 * @return an unmodifiable list of the clauses of the {@link Predicate}
	 * */
	public synchronized List<Term> getClauses()
	{
		return Collections.unmodifiableList(clauses);
	}

	/**
	 * get type of predicate
	 * 
	 * @return type of predicate
	 */
	public synchronized TYPE getType()
	{
		return type;
	}

	/**
	 * set type of predicate
	 * 
	 * @param type
	 *          type of predicate
	 * @throws IllegalStateException
	 *           if predicate type is already set
	 */
	public synchronized void setType(TYPE type)
	{
		if (this.type != TYPE.UNDEFINED)
		{
			throw new IllegalStateException("Type of predicate is already set.");
		}
		this.type = type;
	}

	/**
	 * Get name of Java class that defines this predicate.
	 * 
	 * @return true if predicate is external, false otherwise.
	 */
	public synchronized String getJavaClassName()
	{
		return javaClassName;
	}

	/**
	 * set java class name of the predicate.
	 * 
	 * @param javaClassName
	 *          the class name to set
	 */
	public synchronized void setJavaClassName(String javaClassName)
	{
		if (this.javaClassName != null)
		{
			throw new IllegalStateException("Java class name could not be changed after it was set.");
		}
		switch (type)
		{
			case CONTROL:
			case BUILD_IN:
			case EXTERNAL:
				break;
			default:
				throw new IllegalStateException(
						"Java class name could be only for control construct, build in  or external predicate.");
		}
		this.javaClassName = javaClassName;
		propertiesLocked = true;
	}

	/**
	 * get functor of predicate
	 * 
	 * @return the functor for the predicate. e.g. in foo(X,Y) the functor is
	 *         'foo'
	 * */
	public synchronized AtomTerm getFunctor()
	{
		return tag.functor;
	}

	/**
	 * get arity of predicate
	 * 
	 * @return the arity of the {@link Predicate}
	 */
	public synchronized int getArity()
	{
		return tag.arity;
	}

	/**
	 * get tag of predicate
	 * 
	 * @return the tag of the predicate
	 */
	public synchronized CompoundTermTag getTag()
	{
		return tag;
	}

	/**
	 * Add clause for predicate at the end. This method simply add clause to
	 * predicate. No modification to term is done. It even is not copied.
	 * 
	 * @param clause
	 *          a clause to add
	 */
	public synchronized void addClauseLast(Term clause)
	{
		if (type != TYPE.USER_DEFINED)
		{
			throw new IllegalStateException("clauses could be added only to user defined predicate");
		}
		propertiesLocked = true;
		clauses.add(clause);
		module.predicateUpdated(tag);
	}

	/**
	 * Add clause for predicate at the beginning. This method simply add clause to
	 * predicate. No modification to term is done. It even is not copied.
	 * 
	 * @param clause
	 *          a clause to add
	 */
	public synchronized void addClauseFirst(Term clause)
	{
		if (type != TYPE.USER_DEFINED)
		{
			throw new IllegalStateException("clauses could be added only to user defined predicate");
		}
		propertiesLocked = true;

		if (clauses.size() == 0) // bug workaround
		{
			clauses.add(clause);
		}
		else
		{
			clauses.add(0, clause);
		}
		module.predicateUpdated(tag);
	}

	/**
	 * Remove clause for predicate. This method remove first clause which is
	 * identical to clause.
	 * 
	 * @param clause
	 *          a clause to remove
	 */
	public synchronized void removeClause(Term clause)
	{
		clauses.remove(clause);
		module.predicateUpdated(tag);
	}

	/**
	 * Check if properties of predicate could be changed at this moment
	 * 
	 * @return true if properties of predicate could be changed at this moment
	 */
	public synchronized boolean arePropertiesLocked()
	{
		return propertiesLocked;
	}

	/**
	 * Check if predicate is dynamic.
	 * 
	 * @return true if predicate is dynamic, false otherwise.
	 */
	public synchronized boolean isDynamic()
	{
		return dynamicFlag;
	}

	/**
	 * set "dynamic" property of predicate to true. This method should be called
	 * first time before any clause was added.
	 * 
	 * @throws IllegalStateException
	 *           if there were clauses added to predicate and dynamic flag was not
	 *           set before. See 7.4.2.1 clause of ISO Prolog.
	 */
	public synchronized void setDynamic()
	{
		if (type != TYPE.USER_DEFINED)
		{
			throw new IllegalStateException("only user defined predicate may be declared dynamic");
		}
		if (dynamicFlag)
		{
			// ignore if flag already set
			return;
		}
		if (propertiesLocked)
		{
			throw new IllegalStateException("dynamic property of predicate could not be changed");
		}
		dynamicFlag = true;
	}

	public static Term prepareClause(Term clause)
	{
		clause = clause.dereference();
		Term head;
		Term body;
		if (clause instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) clause;
			if (ct.tag == TermConstants.clauseTag)
			{
				head = prepareHead(ct.args[0].dereference());
				body = prepareBody(ct.args[1].dereference());
			}
			else
			{
				head = prepareHead(clause);
				body = TermConstants.trueAtom;
			}
		}
		else if (clause instanceof AtomTerm)
		{
			head = prepareHead(clause);
			body = TermConstants.trueAtom;
		}
		else
		{
			throw new IllegalArgumentException("not callable");
		}
		return new CompoundTerm(TermConstants.clauseTag, head, body);
	}

	public static Term prepareHead(Term head)
	{
		if (head instanceof VariableTerm)
		{
			throw new IllegalArgumentException("head cannot be a variable");
		}
		else if (head instanceof AtomTerm)
		{
			return head;
		}
		else if (head instanceof CompoundTerm)
		{
			return head;
		}
		else
		{
			throw new IllegalArgumentException("head cannot be converted to predication");
		}
	}

	public static Term prepareBody(Term body)
	{
		if (body instanceof VariableTerm)
		{
			return new CompoundTerm(TermConstants.callTag, body);
		}
		else if (body instanceof AtomTerm)
		{
			return body;
		}
		else if (body instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) body;
			if (ct.tag == TermConstants.conjunctionTag || ct.tag == TermConstants.disjunctionTag
					|| ct.tag == TermConstants.ifTag)
			{
				return new CompoundTerm(ct.tag, prepareBody(ct.args[0].dereference()), prepareBody(ct.args[1].dereference()));
			}
			return body;
		}
		else
		{
			throw new IllegalArgumentException("body cannot be converted to goal");
		}
	}

}
