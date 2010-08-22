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
package gnu.prolog.io;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A conversion map, used by
 * {@link gnu.prolog.vm.buildins.io.Predicate_char_conversion char_conversion}
 * and {@link gnu.prolog.vm.buildins.io.Predicate_current_char_conversion
 * current_char_conversion} and
 * {@link TextInputPrologStream#readTerm(Term,Interpreter,ReadOptions)} to map
 * one character to another.
 * 
 * @author Michiel Hendriks
 */
public class CharConversionTable
{
	protected Map<Character, Character> conv;

	public CharConversionTable()
	{
		conv = new HashMap<Character, Character>();
	}

	/**
	 * @return True if there are conversions (other than c==c)
	 */
	public boolean hasConversions()
	{
		return !conv.isEmpty();
	}

	/**
	 * Set the conversion of a character
	 * 
	 * @param from
	 * @param to
	 */
	public void setConversion(char from, char to)
	{
		if (from == to)
		{
			conv.remove(from);
		}
		else
		{
			conv.put(from, to);
		}
	}

	/**
	 * Convert a character.
	 * 
	 * @param input
	 * @return the converted character
	 */
	public char convert(char input)
	{
		if (conv.containsKey(input))
		{
			return conv.get(input);
		}
		return input;
	}

	/**
	 * Returns the set of characters which are converted to this character
	 * 
	 * @param toChar
	 * @return the set of characters which are converted to this character
	 */
	public Set<Character> convertsTo(char toChar)
	{
		HashSet<Character> res = new HashSet<Character>();
		if (!conv.containsKey(toChar))
		{
			// in case it is not translated
			res.add(toChar);
		}
		for (Entry<Character, Character> entry : conv.entrySet())
		{
			if (entry.getValue() == toChar)
			{
				res.add(entry.getKey());
			}
		}
		return res;
	}

	/**
	 * Apply {@link CharConversionTable} to term if this should happen.
	 * 
	 * @see gnu.prolog.io.TextInputPrologStream#readTerm(Term,Interpreter,ReadOptions)
	 * 
	 * @param term
	 *          the term to apply the conversion to
	 * @param environment
	 *          the environment this is happening in (provides the flag value)
	 * @return the converted term.
	 */
	public Term charConvert(Term term, Environment environment)
	{
		Term status = environment.getPrologFlag(Environment.charConversionAtom);
		// skip if should not do it.
		// TODO we should also skip "quoted character (6.4.2.1)" (but I don't have
		// chapter 6)
		if (status == Environment.onAtom)
		{
			term = term.dereference();
			if (term instanceof AtomTerm)
			{
				AtomTerm aTerm = (AtomTerm) term;
				term = AtomTerm.get(applyConversion(aTerm.value));
			}
			else if (term instanceof CompoundTerm)
			{
				CompoundTerm cTerm = (CompoundTerm) term;

				AtomTerm convertedFunctor = (AtomTerm) charConvert(cTerm.tag.functor, environment);

				Term[] convertedArgs = new Term[cTerm.args.length];
				for (int i = 0; i < cTerm.args.length; ++i)
				{
					convertedArgs[i] = charConvert(cTerm.args[i], environment);
				}
				term = new CompoundTerm(convertedFunctor, convertedArgs);
			}// else we don't need to do anything
		}
		return term;
	}

	/**
	 * Apply this {@link CharConversionTable} to fromString
	 * 
	 * @param fromString
	 *          the string to apply the conversion to
	 * @return the converted string
	 */
	public String applyConversion(String fromString)
	{
		char[] fromCharacters = fromString.toCharArray();
		char[] toCharacters = new char[fromCharacters.length];
		for (int i = 0; i < fromCharacters.length; ++i)
		{
			toCharacters[i] = convert(fromCharacters[i]);
		}
		return new String(toCharacters);
	}
}
