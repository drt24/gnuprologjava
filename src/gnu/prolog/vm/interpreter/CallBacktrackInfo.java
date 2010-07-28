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
package gnu.prolog.vm.interpreter;

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.PrologCode;

/** call backtrack info */
public class CallBacktrackInfo extends BacktrackInfo
{
	/** argument list save for call */
	public Term args[];
	/**
	 * code at moment of first call. It is saved in order to shield code from
	 * predicate changes. as result on backtracking exactly same code will be
	 * used.
	 */
	public PrologCode code;

	public CompoundTermTag tag;

	/**
	 * a constructor
	 * 
	 * @param undoPosition
	 * @param codePosition
	 * @param args
	 * @param code
	 * @param tag
	 */
	public CallBacktrackInfo(int undoPosition, int codePosition, Term args[], PrologCode code, CompoundTermTag tag)
	{
		super(undoPosition, codePosition);
		this.args = args.clone();
		this.code = code;
		this.tag = tag;
	}

}
