/* GNU Prolog for Java
 * Copyright (C) 2010  Daniel Thomas
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
package gnu.prolog.term;

import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Environment.DoubleQuotesValue;

/**
 * Term for storing a double quoted string as what this resolves to varies
 * depending on the value of the double_quotes flag.
 * 
 * @author Daniel Thomas
 */
public class DoubleQuotesTerm extends ChangeableTerm
{
	private static final long serialVersionUID = 4760147509009342072L;

	private Term codesValue;
	private Term charsValue;
	private AtomTerm atomValue;

	/**
	 * Construct a DoubleQuotesTerm
	 * 
	 * @param environment
	 *          the environment to be used to get the prolog flags from
	 * @param codes
	 *          the char codes representation e.g. [232,134] or []
	 * @param chars
	 *          the chars representation e.g. [a,b] or []
	 * @param atom
	 *          the atom representation e.g. ab or ''
	 */
	public DoubleQuotesTerm(Environment environment, Term codes, Term chars, AtomTerm atom)
	{
		super(environment);

		codesValue = codes;
		charsValue = chars;
		atomValue = atom;

		dereference();// sets value as a side effect.
	}

	@Override
	public Term dereference()
	{
		DoubleQuotesValue status = DoubleQuotesValue.fromAtom((AtomTerm) getPrologFlag(Environment.doubleQuotesAtom));
		switch (status)
		{
			case DQ_ATOM:
				value = atomValue;
				break;
			case DQ_CHARS:
				value = charsValue;
				break;
			case DQ_CODES:
				value = codesValue;
				break;
		}
		return value;
	}

}
