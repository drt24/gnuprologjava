/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
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

import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.PrologCode.RC;

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
	public PrologCollectionIterator(Iterator<?> iterable, Term destination, int undoPosition)
	{
		super(-1, -1);
		iterator = iterable;
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
	 * @return PrologCode return code
	 * @throws PrologException
	 */
	public RC nextSolution(Interpreter interpreter) throws PrologException
	{
		while (iterator.hasNext())
		{
			Term term = new JavaObjectTerm(iterator.next());
			RC rc = interpreter.unify(destTerm, term);
			if (rc == PrologCode.RC.FAIL)
			{
				interpreter.undo(startUndoPosition);
				continue;
			}
			interpreter.pushBacktrackInfo(this);
			return PrologCode.RC.SUCCESS;
		}
		return PrologCode.RC.FAIL;
	}
}
