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
package gnu.prolog.term;

import gnu.prolog.database.Pair;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.Collection;
import java.util.List;

/**
 * Compound terms are the basic method for combining terms. In
 * <code>foo(a,b) foo/2</code> is the compound term while <code>a</code> and
 * <code>b</code> are {@link AtomTerm AtomTerms}
 * 
 * @author Constantine Plotnilkov
 * @version 0.0.1
 */
public class CompoundTerm extends Term
{
	private static final long serialVersionUID = -8207470525318790957L;

	public static boolean isListPair(Term term)
	{
		if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			return ct.tag == TermConstants.listTag;
		}
		else
		{
			return false;
		}
	}

	/**
	 * get list pair
	 * 
	 * @param head
	 *          head term
	 * @param tail
	 *          tail term
	 * @return '.'(head, tail) term
	 */
	public static CompoundTerm getList(Term head, Term tail)
	{
		return new CompoundTerm(TermConstants.listTag, head, tail);
	}

	/**
	 * get prolog list by java array
	 * 
	 * @param list
	 * @return a Term representation of the list
	 */
	public static Term getList(Term[] list)
	{
		Term tlist = TermConstants.emptyListAtom;
		for (int i = list.length - 1; i >= 0; i--)
		{
			tlist = getList(list[i], tlist);
		}
		return tlist;
	}

	/**
	 * get prolog list by java list
	 * 
	 * @param list
	 * @return a Term representation of the list
	 */
	public static Term getList(List<Term> list)
	{
		Term tlist = TermConstants.emptyListAtom;
		for (int i = list.size() - 1; i >= 0; i--)
		{
			tlist = getList(list.get(i), tlist);
		}
		return tlist;
	}

	/**
	 * Adds all the non-compound Terms in term to the collection col by
	 * recursively running over the arguments of all the compound terms.
	 * 
	 * @param term
	 *          the Term to extract all the Term elements from to produce the
	 *          collection
	 * @param col
	 *          the collection to add the terms to
	 * @return whether we successfully formed a collection. If false then the
	 *         state of col is undefined.
	 */
	public static boolean toCollection(Term term, Collection<Term> col)
	{
		while ((term = term.dereference()) instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			if (ct.tag != TermConstants.listTag)
			{
				return false;
			}
			else
			{
				col.add(ct.args[0]);
				term = ct.args[1];
			}
		}
		return term instanceof AtomTerm && ((AtomTerm) term) == TermConstants.listAtom;
	}

	/**
	 * get conjunction term
	 * 
	 * @param head
	 *          head term
	 * @param tail
	 *          tail term
	 * @return ','(head, tail) term
	 */
	public static CompoundTerm getConjunction(Term head, Term tail)
	{
		return new CompoundTerm(TermConstants.conjunctionTag, head, tail);
	}

	/**
	 * get disjunction term
	 * 
	 * @param head
	 *          head term
	 * @param tail
	 *          tail term
	 * @return ';'(head, tail) term
	 */
	public static CompoundTerm getDisjunction(Term head, Term tail)
	{
		return new CompoundTerm(TermConstants.disjunctionTag, head, tail);
	}

	/**
	 * Check that the term is a listPair where the head is instantiated and throw
	 * the relevant PrologException if not.
	 * 
	 * @param term
	 * @return a (head,body) Pair
	 * @throws PrologException
	 */
	public static Pair<Term, Term> getInstantiatedHeadBody(Term term) throws PrologException
	{
		if (term instanceof VariableTerm)
		{
			PrologException.instantiationError(term);
		}
		if (!CompoundTerm.isListPair(term))
		{
			PrologException.typeError(TermConstants.listAtom, term);
		}
		CompoundTerm ct = (CompoundTerm) term;
		Term head = ct.args[0].dereference();
		term = ct.args[1].dereference();
		if (head instanceof VariableTerm)
		{
			PrologException.instantiationError(head);
		}
		return new Pair<Term, Term>(head, term);
	}

	/**
	 * get term with specified term tag and arguments.
	 * 
	 * @param tg
	 *          tag of new term
	 * @param arg1
	 *          1st argument of term
	 */
	public CompoundTerm(CompoundTermTag tg, Term arg1)
	{
		this(tg, new Term[] { arg1 });
	}

	/**
	 * get term with specified term tag and arguments.
	 * 
	 * @param tg
	 *          tag of new term
	 * @param arg1
	 *          1st argument of term
	 * @param arg2
	 *          2nd argument of term
	 */
	public CompoundTerm(CompoundTermTag tg, Term arg1, Term arg2)
	{
		this(tg, new Term[] { arg1, arg2 });
	}

	/**
	 * get term with specified term tag and arguments.
	 * 
	 * @param tg
	 *          tag of new term
	 * @param arg1
	 *          1st argument of term
	 * @param arg2
	 *          2nd argument of term
	 * @param arg3
	 *          3rd argument of term
	 */
	public CompoundTerm(CompoundTermTag tg, Term arg1, Term arg2, Term arg3)
	{
		this(tg, new Term[] { arg1, arg2, arg3 });
	}

	/**
	 * get term with specified functor and arity
	 * 
	 * @param functor
	 *          a functor of new term
	 * @param arity
	 *          arity of new term
	 */
	public CompoundTerm(String functor, int arity)
	{
		this(AtomTerm.get(functor), arity);
	}

	/**
	 * get term with specified functor and arity
	 * 
	 * @param functor
	 *          a functor of new term
	 * @param arity
	 *          arity of new term
	 */
	public CompoundTerm(AtomTerm functor, int arity)
	{
		this(CompoundTermTag.get(functor, arity));
	}

	/**
	 * get term with specified term functor and arguments.
	 * 
	 * @param functor
	 *          a functor of new term
	 * @param args
	 *          arguments of term, this array is directly assigned to term and any
	 *          changes that are done to array change term.
	 */
	public CompoundTerm(AtomTerm functor, Term args[])
	{
		this(CompoundTermTag.get(functor, args.length), args);
	}

	/**
	 * get term with specified term functor and arguments.
	 * 
	 * @param functor
	 *          a functor of new term
	 * @param args
	 *          arguments of term, this array is directly assigned to term and any
	 *          changes that are done to array change term.
	 */
	public CompoundTerm(String functor, Term args[])
	{
		this(CompoundTermTag.get(functor, args.length), args);
	}

	/** term tag */
	public final CompoundTermTag tag;
	/** term argumets */
	public final Term args[];

	/**
	 * a contructor
	 * 
	 * @param tag
	 *          tag of term
	 */
	public CompoundTerm(CompoundTermTag tag)
	{
		this.tag = tag;
		args = new Term[tag.arity];
	}

	/**
	 * a constructor
	 * 
	 * @param tag
	 *          tag of term
	 * @param args
	 *          arguments of term
	 */
	public CompoundTerm(CompoundTermTag tag, Term args[])
	{
		this.tag = tag;
		this.args = args.clone();
	}

	/**
	 * clone the object using clone context
	 * 
	 * @param context
	 *          clone context
	 * @return cloned term
	 */
	@Override
	public Term clone(TermCloneContext context)
	{
		CompoundTerm term = (CompoundTerm) context.getTerm(this);
		if (term == null)
		{
			term = new CompoundTerm(tag);
			context.putTerm(this, term);
			for (int i = 0; i < args.length; i++)
			{
				if (args[i] != null)
				{
					term.args[i] = args[i].clone(context);
				}
			}
		}
		return term;
	}

	/**
	 * get type of term
	 * 
	 * @return type of term
	 */
	@Override
	public int getTermType()
	{
		return COMPOUND;
	}
}
