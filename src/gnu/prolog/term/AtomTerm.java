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

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Atom term. The objects of this class represent prolog atoms. This
 * encapsulates Strings and chars.
 * 
 * @author Constantin Plotnikov
 * @version 0.0.1
 */
public class AtomTerm extends AtomicTerm
{
	private static final long serialVersionUID = -7013961090908432585L;

	/** a map from string to atom */
	private final static Map<String, AtomTerm> string2atom = new WeakHashMap<String, AtomTerm>();

	/**
	 * get atom term
	 * 
	 * @param s
	 *          string representation of atom.
	 * @return the AtomTerm for the String
	 */
	public static AtomTerm get(String s)
	{
		synchronized (string2atom)
		{
			AtomTerm atom = string2atom.get(s);
			if (atom == null)
			{
				atom = new AtomTerm(s);
				string2atom.put(s, atom);
			}
			return atom;
		}
	}

	private static StringBuffer chbu = new StringBuffer(1);

	/**
	 * get atom term
	 * 
	 * @param ch
	 *          string representation of atom.
	 * @return the atom term for the character
	 */
	public static final AtomTerm get(char ch)
	{
		synchronized (chbu)
		{
			chbu.setLength(0);
			chbu.append(ch);
			return get(chbu.toString());
		}
	}

	/**
	 * Return an object to replace the object extracted from the stream. The
	 * object will be used in the graph in place of the original.
	 * 
	 * @return resolved object
	 * @see java.io.Resolvable
	 */
	public Object readResolve()
	{
		return get(value);
	}

	/** value of atom */
	final public String value;

	/**
	 * a constructor.
	 * 
	 * @param value
	 *          value of atom
	 */
	protected AtomTerm(String value) // constructor is private to package
	{
		this.value = value;
	}

	/**
	 * get type of term
	 * 
	 * @return type of term
	 */
	@Override
	public int getTermType()
	{
		return ATOM;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		AtomTerm other = (AtomTerm) obj;
		if (value == null)
		{
			if (other.value != null)
			{
				return false;
			}
		}
		else if (!value.equals(other.value))
		{
			return false;
		}
		return true;
	}
}
