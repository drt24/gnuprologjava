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
package gnu.prolog.io.parser;

import gnu.prolog.io.Operator;
import gnu.prolog.io.OperatorSet;
import gnu.prolog.io.Operator.SPECIFIER;
import gnu.prolog.io.parser.gen.Token;

public class NameToken extends Token
{
	public Operator fxOp; // prefix operator
	public Operator xfOp; // postfix of infix operator
	public String value; // converted value

	public String getValue()
	{
		if (value == null)
		{
			if (image.charAt(0) == '\'') // if quoted string
			{
				value = TermParserUtils.convertQuotedString(image, '\'');
			}
			else
			{
				value = image;
			}
		}
		return value;
	}

	public final boolean isOperator(OperatorSet set)
	{
		getValue();
		if (fxOp == null)
		{
			fxOp = set.lookupFx(value);
		}
		if (xfOp == null)
		{
			xfOp = set.lookupXf(value);
		}
		return fxOp != Operator.nonOperator || xfOp != Operator.nonOperator;
	}

	public final boolean isNonOperator(OperatorSet set)
	{
		return !isOperator(set);
	}

	public final boolean isOperator(OperatorSet set, int priority, SPECIFIER specifier)
	{
		getValue();
		switch (specifier)
		{
			case FX:
			case FY:
				if (fxOp == null)
				{
					fxOp = set.lookupFx(value);
				}
				return fxOp != null && fxOp.priority == priority && fxOp.specifier == specifier;
			case XFX:
			case XFY:
			case YFX:
			case XF:
			case YF:
				if (xfOp == null)
				{
					xfOp = set.lookupXf(value);
				}
				return xfOp != null && xfOp.priority == priority && xfOp.specifier == specifier;
			default:
				throw new RuntimeException("invalid specifier");
		}
	}

	public final boolean isFxOperator(OperatorSet set, int priority)
	{
		return isOperator(set, priority, SPECIFIER.FX);
	}

	public final boolean isFyOperator(OperatorSet set, int priority)
	{
		return isOperator(set, priority, SPECIFIER.FY);
	}

	public final boolean isXfxOperator(OperatorSet set, int priority)
	{
		return isOperator(set, priority, SPECIFIER.XFX);
	}

	public final boolean isXfyOperator(OperatorSet set, int priority)
	{
		return isOperator(set, priority, SPECIFIER.XFY);
	}

	public final boolean isYfxOperator(OperatorSet set, int priority)
	{
		return isOperator(set, priority, SPECIFIER.YFX);
	}

	public final boolean isXfOperator(OperatorSet set, int priority)
	{
		return isOperator(set, priority, SPECIFIER.XF);
	}

	public final boolean isYfOperator(OperatorSet set, int priority)
	{
		return isOperator(set, priority, SPECIFIER.YF);
	}
}
