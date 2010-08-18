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

/**
 * Errors which occur when parsing text as Prolog code, stores a line and column
 * number.
 * 
 */
public class ParseException extends java.io.IOException
{
	private static final long serialVersionUID = -7824584186874732911L;

	protected ParseException(gnu.prolog.io.parser.gen.ParseException ex)
	{
		super(ex.getMessage());
		line = ex.currentToken.next.beginLine;
		column = ex.currentToken.next.beginColumn;
		// line = ex.currentToken.endLine;
		// column = ex.currentToken.endColumn;
	}

	protected ParseException(gnu.prolog.io.parser.gen.TokenMgrError ex)
	{
		super(ex.getMessage());
	}

	protected int line;
	protected int column;
	protected String expectedTokens[];

	public int getLine()
	{
		return line;
	}

	public int getColumn()
	{
		return column;
	}

}
