/*
 * Groove Prolog Interface
 * Copyright (C) 2009 Michiel Hendriks, University of Twente
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package gnu.prolog.vm;

import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;

import java.util.Iterator;

/**
 * Generic collection iterator which can be used by PrologCode implementations.
 * 
 * @author Michiel Hendriks
 */
public class PrologCollectionIterator extends BacktrackInfo
{
	/**
	 * The iterator it will go through
	 */
	protected Iterator<?> iterator;

	/**
	 * The term to unify the value with
	 */
	protected Term destTerm;

	/**
	 * The start undo position
	 */
	protected int startUndoPosition;

	/**
	 * @param iterable
	 *          The collection to iterate over
	 * @param destination
	 *          The destination term
	 * @param undoPosition
	 *          the value of interpreter.getUndoPosition();
	 */
	public PrologCollectionIterator(Iterable<?> iterable, Term destination, int undoPosition)
	{
		this(iterable.iterator(), destination, undoPosition);
	}

	/**
	 * @param iterable
	 *          The collection to iterate over
	 * @param destination
	 *          The destination term
	 * @param undoPosition
	 *          the value of interpreter.getUndoPosition();
	 */
	public PrologCollectionIterator(Iterator<?> it, Term destination, int undoPosition)
	{
		super(-1, -1);
		iterator = it;
		destTerm = destination;
		startUndoPosition = undoPosition;
	}

	/**
	 * @return the startUndoPosition
	 */
	public int getUndoPosition()
	{
		return startUndoPosition;
	}

	/**
	 * Get the next value
	 * 
	 * @param interpreter
	 * @return
	 * @throws PrologException
	 */
	public int nextSolution(Interpreter interpreter) throws PrologException
	{
		while (iterator.hasNext())
		{
			Term term = new JavaObjectTerm(iterator.next());
			int rc = interpreter.unify(destTerm, term);
			if (rc == PrologCode.FAIL)
			{
				interpreter.undo(startUndoPosition);
				continue;
			}
			interpreter.pushBacktrackInfo(this);
			return PrologCode.SUCCESS;
		}
		return PrologCode.FAIL;
	}
}
