/* GNU Prolog for Java
 * Copyright (C) 2010,2012,2013       Daniel Thomas
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

import gnu.prolog.term.AtomTerm;

/**
 * The possible values of the {@link Environment#doubleQuotesAtom double_quotes
 * flag}
 * 
 * @author Daniel Thomas
 */
public enum DoubleQuotesValue implements HasAtom
{
	DQ_CODES
	{
		@Override
		public AtomTerm getAtom()
		{
			return TermConstants.codesAtom;
		}
	},
	DQ_CHARS
	{
		@Override
		public AtomTerm getAtom()
		{
			return TermConstants.charsAtom;
		}
	},
	DQ_ATOM
	{
		@Override
		public AtomTerm getAtom()
		{
			return TermConstants.atomAtom;
		}
	};

	/**
	 * @return the AtomTerm for this value for the double_quotes flag.
	 */
	public abstract AtomTerm getAtom();

	/**
	 * @param value
	 *          the AtomTerm to be converted into a DoubleQuotesValue
	 * @return the DoubleQuotesValue for the value or null if it does not match.
	 */
	public static DoubleQuotesValue fromAtom(AtomTerm value)
	{
		if (TermConstants.codesAtom == value)
		{
			return DQ_CODES;
		}
		else if (TermConstants.charsAtom == value)
		{
			return DQ_CHARS;
		}
		else if (TermConstants.atomAtom == value)
		{
			return DQ_ATOM;
		}
		return null;
	}

	/**
	 * @return the default value for the {@link Environment#doubleQuotesAtom
	 *         double_quotes flag}.
	 */
	public static DoubleQuotesValue getDefault()
	{
		return DQ_CODES;
	}
}
