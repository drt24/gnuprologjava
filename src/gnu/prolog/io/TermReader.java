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

import gnu.prolog.io.parser.gen.TermParser;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;

import java.io.FilterReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * Reads {@link Term Terms} from strings and {@link Reader Readers}.
 * 
 */
public class TermReader extends FilterReader
{
	protected static final OperatorSet defaultOperatorSet = new OperatorSet();
	TermParser parser;

	public TermReader(Reader r, int line, int col, Environment environment)
	{
		super(r);
		parser = new TermParser(r, line, col, environment);
	}

	public TermReader(Reader r, Environment environment)
	{
		this(r, 1, 1, environment);
	}

	public Term readTerm(ReadOptions options) throws ParseException
	{
		try
		{
			return parser.readTerm(options);
		}
		catch (gnu.prolog.io.parser.gen.ParseException ex)
		{
			// ex.printStackTrace();
			throw new ParseException(ex);
		}
		catch (gnu.prolog.io.parser.gen.TokenMgrError ex)
		{
			throw new ParseException(ex);
		}
	}

	/**
	 * Parse the string into a Term in the specified environment using the
	 * specified options.
	 * 
	 * @see ReadOptions#variableNames which is a map from the strings of the
	 *      variable names to the generated {@link VariableTerm VariableTerms}.
	 * @param options
	 *          the options to parse with
	 * @param str
	 *          the string to convert to a term
	 * @param environment
	 *          to create the Term in
	 * @return the term representing the provided string
	 * @throws ParseException
	 *           if any errors occur while parsing
	 */
	public static Term stringToTerm(ReadOptions options, String str, Environment environment) throws ParseException
	{
		StringReader srd = new StringReader(str);
		TermReader trd = new TermReader(srd, environment);
		return trd.readTermEof(options);
	}

	public static Term stringToTerm(String str, Environment environment) throws ParseException
	{
		StringReader srd = new StringReader(str);
		TermReader trd = new TermReader(srd, environment);
		return trd.readTermEof();
	}

	public Term readTermEof(ReadOptions options) throws ParseException
	{
		try
		{
			return parser.readTermEof(options);
		}
		catch (gnu.prolog.io.parser.gen.ParseException ex)
		{
			throw new ParseException(ex);
		}
		catch (gnu.prolog.io.parser.gen.TokenMgrError ex)
		{
			throw new ParseException(ex);
		}
	}

	public Term readTerm(OperatorSet set) throws ParseException
	{
		ReadOptions options = new ReadOptions(set);
		return readTerm(options);
	}

	public Term readTermEof(OperatorSet set) throws ParseException
	{
		ReadOptions options = new ReadOptions(set);
		return readTermEof(options);
	}

	public Term readTerm() throws ParseException
	{
		return readTerm(defaultOperatorSet);
	}

	public Term readTermEof() throws ParseException
	{
		return readTermEof(defaultOperatorSet);
	}

	public int getCurrentLine()
	{
		return parser.getCurrentLine();
	}

	public int getCurrentColumn()
	{
		return parser.getCurrentColumn();
	}

}
