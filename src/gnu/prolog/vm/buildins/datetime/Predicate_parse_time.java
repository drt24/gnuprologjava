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
package gnu.prolog.vm.buildins.datetime;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_parse_time extends DateTimePrologCode
{
	public Predicate_parse_time()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		// % parse_time(+Text, -Stamp)
		// % parse_time(+Text, -Stamp, +Format)
		String text;
		if (!(args[0] instanceof AtomTerm))
		{
			PrologException.typeError(TermConstants.atomAtom, args[0]);
		}
		text = ((AtomTerm) args[0]).value;

		String format = "EEE, dd MMM yyyy HH:mm:ss zzz"; // RFC 1123
		if (args.length == 3)
		{
			if (!(args[2] instanceof AtomTerm))
			{
				PrologException.typeError(TermConstants.atomAtom, args[2]);
			}
			format = ((AtomTerm) args[2]).value;
		}
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		Date date;
		try
		{
			date = fmt.parse(text);
		}
		catch (ParseException e)
		{
			return FAIL;
		}
		return interpreter.unify(args[1], new FloatTerm(date.getTime() / 1000.0));
	}

}
