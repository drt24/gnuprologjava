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
package gnu.prolog.vm.buildins.allsolutions;

import gnu.prolog.term.Term;
import gnu.prolog.term.TermComparator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * prolog code
 */
public class Predicate_setof extends Predicate_bagof
{

	@Override
	protected void processList(List<Term> curTList)
	{
		TermComparator tc = new TermComparator();
		Collections.sort(curTList, tc);
		// remove duplicates
		Iterator<Term> it = curTList.iterator();
		Term prev = null; // initially there is no "previous element"
		while (it.hasNext())
		{
			Term cur = it.next();
			if (prev != null && tc.compare(prev, cur) == 0)
			{
				it.remove(); // only safe way to remove list element while iterating
			}
			else
			{
				prev = cur;
			}
		}
	}

}
