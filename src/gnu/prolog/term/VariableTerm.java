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
/** variable term.
 * @author Constantine Plotnikov
 * @version 0.0.1
 */
package gnu.prolog.term;

/**
 * Represents Prolog variables such as X or A.
 * 
 * @see Term#dereference()
 */
public class VariableTerm extends Term
{
	private static final long serialVersionUID = -8440602532721728373L;

	/** create new unnamed variable term */
	public VariableTerm()
	{}

	/**
	 * Create a new named variable term
	 * 
	 * @param name
	 *          the name of the term.
	 * @see #name
	 */
	public VariableTerm(String name)
	{
		this.name = name;
	}

	/**
	 * a constructor
	 * 
	 * @param name
	 *          name of term
	 */
	/** value of variable term */
	public Term value = null;

	/**
	 * Name of the variable when it was declared
	 * 
	 * Used for display purposes
	 */
	public String name = null;

	/**
	 * clone the object using clone context
	 * 
	 * @param context
	 *          clone context
	 * @return cloned term
	 */
	@Override
	public Term clone(TermCloneContext context)
	{
		if (value == null)
		{
			VariableTerm term = (VariableTerm) context.getTerm(this);
			if (term == null)
			{
				term = new VariableTerm(name);
				context.putTerm(this, term);
			}
			return term;
		}
		else
		{
			return value.clone(context);
		}
	}

	/**
	 * dereference term.
	 * 
	 * @return dereferenced term
	 */
	@Override
	public Term dereference()
	{
		VariableTerm variable = this;
		do
		{
			Term val = variable.value;
			if (val == null)
			{
				return variable;
			}
			else if (!(val instanceof VariableTerm))
			{
				return val.dereference();
			}
			else
			{
				variable = (VariableTerm) val;
			}
		} while (true);
		/*
		 * if(value == null) { return this; } return value.dereference();
		 */
	}

	/**
	 * get type of term
	 * 
	 * @return type of term
	 */
	@Override
	public int getTermType()
	{
		return VARIABLE;
	}
}
