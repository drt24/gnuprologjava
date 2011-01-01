/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2010       Daniel Thomas
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
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.vm.HasAtom;
import gnu.prolog.vm.TermConstants;

/**
 * Represents a Prolog operator with a name, {@link SPECIFIER} and priority.
 * 
 */
final public class Operator
{

	// 6.3.4 Operator notation
	/**
	 * Specifier Class Associativity
	 * 
	 * @author Daniel Thomas
	 */
	public static enum SPECIFIER implements HasAtom
	{
		/**
		 * prefix non-associative
		 */
		FX
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.fxAtom;
			}
		},
		/**
		 * prefix right-associative
		 */
		FY
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.fyAtom;
			}
		},
		/**
		 * infix non-associative
		 */
		XFX
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.xfxAtom;
			}
		},
		/**
		 * infix right-associative
		 */
		XFY
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.xfyAtom;
			}
		},
		/**
		 * infix left-associative
		 */
		YFX
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.yfxAtom;
			}
		},
		/**
		 * postfix non-associative
		 */
		XF
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.xfAtom;
			}
		},
		/**
		 * postfix left-associative
		 */
		YF
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.yfAtom;
			}
		},
		/**
		 * non operator
		 */
		NONE
		{
			@Override
			public AtomTerm getAtom()
			{
				return null;
			}
		};

		/**
		 * @return the AtomTerm representation for this value for the specifier.
		 */
		public abstract AtomTerm getAtom();

		/**
		 * Return the SPECIFIER represented by the AtomTerm specifier or
		 * {@link #NONE} if it does not match one.
		 * 
		 * @param specifier
		 *          the AtomTerm specifier to convert
		 * @return the SPECIFIER represented by the AtomTerm specifier or
		 *         {@link #NONE} if it does not match one.
		 */
		public static SPECIFIER fromAtom(AtomTerm specifier)
		{
			if (specifier == TermConstants.fxAtom)
			{
				return SPECIFIER.FX;
			}
			else if (specifier == TermConstants.fyAtom)
			{
				return SPECIFIER.FY;
			}
			else if (specifier == TermConstants.xfxAtom)
			{
				return SPECIFIER.XFX;
			}
			else if (specifier == TermConstants.xfyAtom)
			{
				return SPECIFIER.XFY;
			}
			else if (specifier == TermConstants.yfxAtom)
			{
				return SPECIFIER.YFX;
			}
			else if (specifier == TermConstants.xfAtom)
			{
				return SPECIFIER.XF;
			}
			else if (specifier == TermConstants.yfAtom)
			{
				return SPECIFIER.YF;
			}
			return SPECIFIER.NONE;
		}
	}

	public final static int MAX_PRIORITY = 1200;
	public final static int MIN_PRIORITY = 1;

	public static final Operator nonOperator = new Operator("", SPECIFIER.NONE, -1);

	public final String name;
	public final SPECIFIER specifier;
	public final int priority;
	public final CompoundTermTag tag;

	protected Operator(String name, SPECIFIER specifier, int priority)
	{
		this.name = name;
		this.specifier = specifier;
		this.priority = priority;
		switch (specifier)
		{
			case FX:
			case FY:
			case XF:
			case YF:
				tag = CompoundTermTag.get(name, 1);
				break;
			case XFX:
			case XFY:
			case YFX:
				tag = CompoundTermTag.get(name, 2);
				break;
			case NONE:
				tag = null;
				break;
			default:
				tag = null;
				throw new IllegalArgumentException("invalid specifier = " + specifier);
		}
	}

	@Override
	public String toString()
	{
		return "Opearator[name='" + name + "';specifier='" + specifier + ";priority=" + priority + "]";
	}
}
