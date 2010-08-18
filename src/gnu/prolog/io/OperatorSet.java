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
package gnu.prolog.io;

import gnu.prolog.io.Operator.SPECIFIER;
import gnu.prolog.term.CompoundTermTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores the current {@link Operator Operators}.
 * 
 */
final public class OperatorSet
{
	static class OperatorLevel
	{
		OperatorLevel(int p)
		{
			priority = p;
			// usage = 0;
		}

		int priority;
		// int usage;
	}

	List<OperatorLevel> priorityLevels = new ArrayList<OperatorLevel>();
	Map<String, Operator> xfOps = new HashMap<String, Operator>();
	Map<String, Operator> fxOps = new HashMap<String, Operator>();

	public synchronized Operator lookupXf(String value)
	{
		Operator op = xfOps.get(value);
		return op != null ? op : Operator.nonOperator;
	}

	public synchronized Operator lookupFx(String value)
	{
		Operator op = fxOps.get(value);
		return op != null ? op : Operator.nonOperator;
	}

	/**
	 * get all operators currently in the set
	 * 
	 * @return all the operators currently in the set
	 */
	public synchronized Set<Operator> getOperators()
	{
		Set<Operator> rc = new HashSet<Operator>();
		rc.addAll(fxOps.values());
		rc.addAll(xfOps.values());
		return rc;
	}

	/**
	 * remove operator from operator set
	 * 
	 * @param specifier
	 * @param name
	 */
	public synchronized void remove(SPECIFIER specifier, String name)
	{
		switch (specifier)
		{
			case FX:
			case FY:
				fxOps.remove(name);
				break;
			case XF:
			case YF:
			case XFX:
			case XFY:
			case YFX:
				xfOps.remove(name);
				break;
		}
	}

	/**
	 * add operator to operator set
	 * 
	 * @param priority
	 * @param specifier
	 * @param name
	 * @return the added {@link Operator}
	 */
	public synchronized Operator add(int priority, SPECIFIER specifier, String name)
	{
		int i, n = priorityLevels.size();
		// System.out.println(name+" prio:" + priority+ " priolvs:"+n);
		OperatorLevel ol = null;
		int nlv = 0;
		for (i = n - 1; i >= 0; i--)
		{
			ol = priorityLevels.get(i);
			if (ol.priority == priority)
			{
				nlv = i;
				break;
			}
			else if (ol.priority < priority)
			{
				nlv = i + 1;
				ol = new OperatorLevel(priority);
				if (nlv == n) // w/a
				{
					priorityLevels.add(ol);
				}
				else
				{
					priorityLevels.add(nlv, ol);
				}
				break;
			}
		}
		if (i < 0)
		{
			nlv = 0;
			ol = new OperatorLevel(priority);
			if (priorityLevels.size() == 0) // w/a
			{
				priorityLevels.add(ol);
			}
			else
			{
				priorityLevels.add(nlv, ol);
			}
		}
		// ol.usage++;

		Operator op = new Operator(name, specifier, priority);

		switch (specifier)
		{
			case FX:
			case FY:
				fxOps.put(name, op);
				break;
			case XF:
			case YF:
			case XFX:
			case XFY:
			case YFX:
				xfOps.put(name, op);
				break;
		}
		return op;
	}

	public synchronized int getNextLevel(int priority)
	{
		int i, n = priorityLevels.size();
		for (i = n - 1; i >= 0; i--)
		{
			int p = (priorityLevels.get(i)).priority;
			if (p <= priority)
			{
				return p;
			}
		}
		return 0;
	}

	public synchronized int getCommaLevel()
	{
		return 1000;
	}

	public synchronized int getMaxLevel()
	{
		return 1200;
	}

	/**
	 * 
	 * @param defaultSet
	 *          should this OperatorSet be initialized with the default
	 *          OperatorSet?
	 */
	public OperatorSet(boolean defaultSet)
	{
		if (defaultSet)
		{
			initDefault();
		}
	}

	public OperatorSet()
	{
		this(true);
	}

	protected void initDefault()
	{
		add(1200, SPECIFIER.XFX, "-->");
		add(1200, SPECIFIER.XFX, ":-");
		add(1200, SPECIFIER.FX, ":-");
		add(1200, SPECIFIER.FX, "?-");
		add(1100, SPECIFIER.XFY, ";");
		add(1050, SPECIFIER.XFY, "->");
		add(1000, SPECIFIER.XFY, ",");
		add(900, SPECIFIER.FX, "\\+");
		add(700, SPECIFIER.XFX, "=");
		add(700, SPECIFIER.XFX, "\\=");
		add(700, SPECIFIER.XFX, "==");
		add(700, SPECIFIER.XFX, "\\==");
		add(700, SPECIFIER.XFX, "@<");
		add(700, SPECIFIER.XFX, "@=<");
		add(700, SPECIFIER.XFX, "@>");
		add(700, SPECIFIER.XFX, "@>=");
		add(700, SPECIFIER.XFX, "=..");
		add(700, SPECIFIER.XFX, "is");
		add(700, SPECIFIER.XFX, "=:=");
		add(700, SPECIFIER.XFX, "=\\=");
		add(700, SPECIFIER.XFX, "<");
		add(700, SPECIFIER.XFX, "=<");
		add(700, SPECIFIER.XFX, ">");
		add(700, SPECIFIER.XFX, ">=");
		add(600, SPECIFIER.XFX, ":");
		add(500, SPECIFIER.YFX, "+");
		add(500, SPECIFIER.YFX, "-");
		add(500, SPECIFIER.YFX, "/\\");
		add(500, SPECIFIER.YFX, "\\/");
		add(400, SPECIFIER.YFX, "*");
		add(400, SPECIFIER.YFX, "/");
		add(400, SPECIFIER.YFX, "//");
		add(400, SPECIFIER.YFX, "rem");
		add(400, SPECIFIER.YFX, "mod");
		add(400, SPECIFIER.YFX, "<<");
		add(400, SPECIFIER.YFX, ">>");
		add(200, SPECIFIER.XFX, "**");
		add(200, SPECIFIER.XFY, "^");
		add(200, SPECIFIER.FY, "-");
		add(200, SPECIFIER.FY, "\\");
		add(100, SPECIFIER.XFX, "@");
	}

	public synchronized Operator getOperatorForTag(CompoundTermTag tag)
	{
		if (tag.arity == 1)
		{
			Operator op = lookupFx(tag.functor.value);
			if (op == Operator.nonOperator)
			{
				op = lookupXf(tag.functor.value);
			}
			if (op.tag != tag)
			{
				op = Operator.nonOperator;
			}
			return op;
		}
		if (tag.arity == 2)
		{
			Operator op = lookupXf(tag.functor.value);
			if (op.tag != tag)
			{
				op = Operator.nonOperator;
			}
			return op;
		}
		return Operator.nonOperator;
	}
}
