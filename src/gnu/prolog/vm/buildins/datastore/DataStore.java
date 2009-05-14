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
package gnu.prolog.vm.buildins.datastore;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Michiel Hendriks
 */
public class DataStore implements Iterable<DataStore.Entry>
{
	public static class Entry
	{
		protected AtomTerm key;
		protected List<Term> value;

		public Entry(AtomTerm inKey, List<Term> inValue)
		{
			key = inKey;
			value = new ArrayList<Term>(inValue);
		}

		/**
		 * @return the key
		 */
		public AtomTerm getKey()
		{
			return key;
		}

		/**
		 * @return the value
		 */
		public List<Term> getValue()
		{
			return value;
		}
	}

	protected Map<String, Entry> keyLookup;

	/**
	 * The entries, stored in reversed order (first item is the last item added)
	 */
	protected LinkedList<Entry> entries;

	/**
	 * 
	 */
	public DataStore()
	{
		keyLookup = new HashMap<String, Entry>();
		entries = new LinkedList<Entry>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Entry> iterator()
	{
		return entries.iterator();
	}

	/**
	 * @return
	 */
	public Entry get()
	{
		return get(null);
	}

	/**
	 * @param key
	 * @return
	 */
	public Entry get(AtomTerm key)
	{
		if (key == null)
		{
			return entries.getFirst();
		}
		return keyLookup.get(key.value);
	}

	/**
	 * @param values
	 * @return
	 */
	public Entry put(List<Term> values)
	{
		return put(null, values);
	}

	/**
	 * @param key
	 * @param values
	 * @return
	 */
	public Entry put(AtomTerm key, List<Term> values)
	{
		Entry entry = new Entry(key, values);
		entries.addFirst(entry);
		if (key != null)
		{
			return keyLookup.put(key.value, entry);
		}
		return null;
	}

	/**
	 * @param key
	 * @return
	 */
	public Entry remove(AtomTerm key)
	{
		return keyLookup.remove(key.value);
	}
}
