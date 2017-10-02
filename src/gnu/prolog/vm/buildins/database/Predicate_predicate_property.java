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
package gnu.prolog.vm.buildins.database;

import gnu.prolog.database.Predicate;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * predicate_property(Head, Property) is true iff the procedure associated with
 * the argument Head has predicate property Property.
 * TBD: Only static/0, dynamic/0, built_in/0 and meta_predicate/1 are implemented
 *      None of these are mentioned in ISO-13211/1 which is the only ISO Prolog standard
 *      available to me. Someone can perhaps implement more (and/or fix) the ones here
 * 
 */
public class Predicate_predicate_property extends ExecuteOnlyCode
{
	CompoundTermTag divideTag = CompoundTermTag.get("/", 2);

	private static class PredicatePropertyBacktrackInfo extends BacktrackInfo
	{
		int startUndoPosition;
		Iterator<CompoundTermTag> tagsIterator = null;
		Iterator<PredicateProperty> propertyIterator = null;
		Predicate predicate = null;
		CompoundTermTag pi = null;
		boolean mustUnify;
		PredicatePropertyBacktrackInfo()
		{
			super(-1, -1);
		}
	}

	private static HashSet<PredicateProperty> properties = new HashSet<PredicateProperty>();
	private interface PredicateProperty
	{
		public boolean isPropertyOf(Predicate p);
		public Term getTerm(Predicate p);
	}

	static
	{
		properties.add(new PredicateProperty()
		{
			public boolean isPropertyOf(Predicate p)
			{
				return !p.isDynamic();
			}
			public Term getTerm(Predicate p)
			{
				return AtomTerm.get("static");
			}
		});

		properties.add(new PredicateProperty()
		{
			public boolean isPropertyOf(Predicate p)
			{
				return p.isDynamic();
			}
			public Term getTerm(Predicate p)
			{
				return AtomTerm.get("dynamic");
			}
		});

		properties.add(new PredicateProperty()
		{
			public boolean isPropertyOf(Predicate p)
			{
				return p.getType() == Predicate.TYPE.BUILD_IN;
			}
			public Term getTerm(Predicate p)
			{
				return AtomTerm.get("built_in");
			}
		});

		properties.add(new PredicateProperty()
		{
			public boolean isPropertyOf(Predicate p)
			{
				return p.getMetaPredicateInfo() != null;
			}
			public Term getTerm(Predicate p)
			{
				return new CompoundTerm(CompoundTermTag.get("meta_predicate", 1), new CompoundTerm(p.getTag(), p.getMetaPredicateInfo().asArgs()));
			}
		});


	}

	@Override
	public RC execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		PredicatePropertyBacktrackInfo bi = null;
		if (backtrackMode)
		{
			bi = (PredicatePropertyBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
		}
		else
		{
			Term a0 = args[0];
			if (a0 instanceof VariableTerm)
			{
				// In this case, we must enumerate all predicates unfortunately
				Set<CompoundTermTag> tagSet = new HashSet<CompoundTermTag>(interpreter.getEnvironment().getModule().getPredicateTags());
				bi = new PredicatePropertyBacktrackInfo();
				bi.startUndoPosition = interpreter.getUndoPosition();
				bi.tagsIterator = tagSet.iterator();
				bi.mustUnify = true;
			}
			else if (a0 instanceof CompoundTerm && ((CompoundTerm)a0).tag == divideTag && ((((CompoundTerm)a0).args[0] instanceof VariableTerm) ||
												       (((CompoundTerm)a0).args[1] instanceof VariableTerm)))
			{
				// In this case, we must enumerate all predicates as well
				Set<CompoundTermTag> tagSet = new HashSet<CompoundTermTag>(interpreter.getEnvironment().getModule().getPredicateTags());
				bi = new PredicatePropertyBacktrackInfo();
				bi.startUndoPosition = interpreter.getUndoPosition();
				bi.tagsIterator = tagSet.iterator();
				bi.mustUnify = true;
			}
			else
			{
				// in this case the first argument is well-defined and we can just get the predicate they are asking for
				CompoundTermTag pi = null;
				if (a0 instanceof CompoundTerm)
				{
					CompoundTerm ct = (CompoundTerm) a0;
					if (ct.tag != divideTag)
					{
						pi = ct.tag;
					}
					else
					{
						Term n = ct.args[0].dereference();
						Term a = ct.args[1].dereference();
						if (!(n instanceof AtomTerm))
						{
							PrologException.typeError(TermConstants.predicateIndicatorAtom, a0);
						}
						if (!(a instanceof IntegerTerm))
						{
							PrologException.typeError(TermConstants.predicateIndicatorAtom, a0);
						}
						pi = CompoundTermTag.get((AtomTerm)n, ((IntegerTerm)a).value);
					}
				}
				else if (a0 instanceof AtomTerm)
				{
					pi = CompoundTermTag.get((AtomTerm)a0, 0);
				}
				else
				{
					PrologException.typeError(TermConstants.predicateIndicatorAtom, a0);
				}
				HashSet<CompoundTermTag> tagSet = new HashSet<CompoundTermTag>();
				tagSet.add(pi);
				bi = new PredicatePropertyBacktrackInfo();
				bi.startUndoPosition = interpreter.getUndoPosition();
				bi.mustUnify = false;
				bi.tagsIterator = tagSet.iterator();
			}
		}
		return nextSolution(interpreter, bi, args[0], args[1]);

	}

	private static RC nextSolution(Interpreter interpreter, PredicatePropertyBacktrackInfo bi, Term pi, Term property) throws PrologException
	{
		while (true)
		{
			while (bi.predicate != null && bi.propertyIterator.hasNext())
			{
				// Get the next property
				PredicateProperty pp = bi.propertyIterator.next();
				if (pp.isPropertyOf(bi.predicate))
				{
					if (interpreter.unify(pp.getTerm(bi.predicate), property) == RC.FAIL)
					{
						// Cannot unify this property. Keep looking
						interpreter.undo(bi.startUndoPosition);
						continue;
					}
					else
					{
						interpreter.pushBacktrackInfo(bi);
						return RC.SUCCESS;
					}
				}

			}
			// We get here immediately on entry, but also if we have run out of properties in an old predicate
			// and need to look for another one. To make the logic the same for both cases, we set bi.predicate
			// to null here:
			bi.predicate = null;

			// Get the next predicate
			while (bi.predicate == null)
			{
				if (bi.tagsIterator.hasNext())
				{
					CompoundTermTag tag = bi.tagsIterator.next();
					bi.predicate = interpreter.getEnvironment().getModule().getDefinedPredicate(tag);
					if (bi.predicate == null) // if was destroyed
					{
						continue;
					}
					// Check that we can unify this if the first argument is suitably shaped
					if (bi.mustUnify)
					{
						RC rc = interpreter.unify(pi, tag.getPredicateIndicator());
						if (rc == RC.SUCCESS_LAST)
						{
							// OK good. Now that bi.predicate is set, we can break out of here and go back to
							// looking at the possible properties
							bi.propertyIterator = properties.iterator();
							break;
						}
						else
						{
							// No, try the next predicate
							interpreter.undo(bi.startUndoPosition);
							bi.predicate = null;
						}
					}
					else
					{
						// If mustUnify is false it means the first argument is either a head (not a PI)
						// or was ground, so unification isnt needed. In this case, we are done
						bi.propertyIterator = properties.iterator();
						break;
					}
				}
				else
				{
					// We have run out of predicates. Give up.
					return RC.FAIL;
				}
			}
		}
	}
}
