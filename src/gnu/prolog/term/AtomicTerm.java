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
package gnu.prolog.term;

import gnu.prolog.io.TermWriter;
import gnu.prolog.io.WriteOptions;

/**
 * base class for all constant terms
 * 
 * @author Constantine Plotnikov
 * @version 0.0.1
 */
public abstract class AtomicTerm extends Term
{
	private static final long serialVersionUID = -3966209611457278787L;

	@Override
	public Term clone(TermCloneContext context)
	{
		return this;
	}

	@Override
	public Object clone()
	{
		return this;
	}

	/**
	 * You can override this to control printing of terms
	 * that derive from AtomicTerm that are not otherwise built-in
	 * If the default implementation below is used, a term will be
	 * printed that cannot be read back. This is by design, to
	 * highlight the problem that the method is not implemented in
	 * the subclass
	 * @param options
	 *          WriteOptions to use
	 * @param writer
	 *          TermWriter to write the term to
	 */
	public void displayTerm(WriteOptions options, TermWriter writer)
	{
		writer.print("<unprintable>");
	}

}
