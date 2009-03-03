/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA. The text ol license can be also found 
 * at http://www.gnu.org/copyleft/lgpl.html
 */
package gnu.prolog.io;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A conversion map, used by char_conversion and current_char_conversion
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
	 * @return
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
	 * @param from
	 * @return
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
}
