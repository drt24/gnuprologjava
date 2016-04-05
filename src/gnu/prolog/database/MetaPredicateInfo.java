/* GNU Prolog for Java
 * Copyright (C) 2016       Matt Lilley
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

import gnu.prolog.vm.PrologException;
import gnu.prolog.term.Term;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.IntegerTerm;
import java.util.HashMap;

/**
 *  MetaPredicateInfo describes the meta-predicate information for a predicate
 *  It contains an array, args, of the same size as the arity of the predicate,
 *  containing MetaType enum values
 *   NORMAL   indicates that the argument is module-insensitive
 *   META     indicates that the argument is module-sensitive
 *   ONE-NINE indicate that the argument is module-sensitive and takes that many extra args when called
 *   COLON    indicates that the argument is module-sensitive but is not a goal
 *   EXISTS   indicates that the argument is module-sensitive and may be quantified with the ^ operator
 */

public class MetaPredicateInfo
{
	private static HashMap<Term, MetaType> map = new HashMap<Term, MetaType>();

	static
	{
		map.put(AtomTerm.get("+"), MetaType.NORMAL);
		map.put(AtomTerm.get("-"), MetaType.NORMAL);
		map.put(AtomTerm.get("*"), MetaType.NORMAL);
		map.put(AtomTerm.get("//"), MetaType.TWO);
		map.put(IntegerTerm.get(0), MetaType.META);
		map.put(IntegerTerm.get(1), MetaType.ONE);
		map.put(IntegerTerm.get(2), MetaType.TWO);
		map.put(IntegerTerm.get(3), MetaType.THREE);
		map.put(IntegerTerm.get(4), MetaType.FOUR);
		map.put(IntegerTerm.get(5), MetaType.FIVE);
		map.put(IntegerTerm.get(6), MetaType.SIX);
		map.put(IntegerTerm.get(7), MetaType.SEVEN);
		map.put(IntegerTerm.get(8), MetaType.EIGHT);
		map.put(IntegerTerm.get(9), MetaType.NINE);
		map.put(AtomTerm.get(":"), MetaType.COLON);
		map.put(AtomTerm.get("^"), MetaType.EXISTS);
	}

	public enum MetaType
	{
		NORMAL, META, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, COLON, EXISTS;
	}
	public MetaType[] args = null;
	public MetaPredicateInfo(MetaType[] args)
	{
		this.args = args;
	}

	/**
	 *  Given a term, retrieve the appropriate meta value for it
	 * @param t
	 *   The term to look up
	 * @throws PrologException
	 *   if t does not correspond to a meta type
	 */
	public static MetaType get(Term t) throws PrologException
	{
		if (map.containsKey(t))
			return map.get(t);
		PrologException.domainError(AtomTerm.get("meta_argument_specifier"), t);
		return MetaType.NORMAL; // Unreachable
	}
}
