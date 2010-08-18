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

import gnu.prolog.io.PrologStream;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_format_time extends DateTimePrologCode
{
	enum OutputFormat
	{
		OF_STREAM, OF_ATOM, OF_CHARS, OF_CODES
	}

	public Predicate_format_time()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		// format_time(+Out, +Format, +StampOrDateTime)
		// format_time(+Out, +Format, +StampOrDateTime, +Locale)

		// figure out the output
		OutputFormat outFormat = OutputFormat.OF_STREAM;
		PrologStream outstream = null;
		Term outterm = null;
		Term outtermTail = null;
		if (args[0] instanceof JavaObjectTerm)
		{
			JavaObjectTerm jt = (JavaObjectTerm) args[0];
			if (!(jt.value instanceof PrologStream))
			{
				PrologException.domainError(TermConstants.streamAtom, args[0]);
			}
		}
		else if (args[0] instanceof AtomTerm)
		{
			outstream = interpreter.getEnvironment().resolveStream(args[0]);
			if (outstream == null)
			{
				PrologException.domainError(TermConstants.streamAtom, args[0]);
			}
		}
		else if (args[0] instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) args[0];
			if (ct.tag.functor == TermConstants.atomAtom)
			{
				if (ct.tag.arity != 1)
				{
					PrologException.typeError(TermConstants.outputAtom, ct);
				}
				outterm = ct.args[0];
				outFormat = OutputFormat.OF_ATOM;
			}
			else if (ct.tag.functor == TermConstants.codesAtom)
			{
				if (ct.tag.arity > 2)
				{
					PrologException.typeError(TermConstants.outputAtom, ct);
				}
				outterm = ct.args[0];
				if (ct.args.length > 1)
				{
					outtermTail = ct.args[1];
				}
				outFormat = OutputFormat.OF_CODES;
			}
			else if (ct.tag.functor == TermConstants.charsAtom)
			{
				if (ct.tag.arity > 2)
				{
					PrologException.typeError(TermConstants.outputAtom, ct);
				}
				outterm = ct.args[0];
				if (ct.args.length > 1)
				{
					outtermTail = ct.args[1];
				}
				outFormat = OutputFormat.OF_CHARS;
			}
			else
			{
				PrologException.typeError(AtomTerm.get("atom_codes_chars"), args[0]);
			}
		}
		else
		{
			PrologException.typeError(TermConstants.outputAtom, args[0]);
		}
		// either `outstream' or `outterm*' is set

		if (!(args[1] instanceof AtomTerm))
		{
			PrologException.typeError(TermConstants.atomAtom, args[1]);
		}
		String format = ((AtomTerm) args[1]).value;

		Date date;
		if (args[2] instanceof FloatTerm)
		{
			date = new Date(Math.round(((FloatTerm) args[2]).value * 1000));
		}
		else
		{
			date = getDate(args[2]);
		}

		Locale locale = Locale.getDefault();
		if (args.length > 3)
		{
			if (args[3] instanceof AtomTerm)
			{
				String loc = ((AtomTerm) args[3]).value;
				int idx = loc.indexOf('_');
				if (idx > -1)
				{
					locale = new Locale(loc.substring(0, idx), loc.substring(idx + 1));
				}
				else
				{
					locale = new Locale(loc);
				}
			}
			else
			{
				PrologException.typeError(TermConstants.atomAtom, args[3]);
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		String result = sdf.format(date);
		if (outstream != null)
		{
			outstream.writeTerm(args[0], interpreter, null, AtomTerm.get(result));
			return SUCCESS_LAST;
		}
		else
		{
			Term res = TermConstants.emptyListAtom;
			if (outtermTail != null)
			{
				res = outtermTail;
			}
			switch (outFormat)
			{
				case OF_ATOM:
					res = AtomTerm.get(result);
					break;
				case OF_CHARS:
					for (int i = result.length() - 1; i >= 0; i--)
					{
						res = CompoundTerm.getList(AtomTerm.get(result.charAt(i)), res);
					}
					break;
				case OF_CODES:
					for (int i = result.length() - 1; i >= 0; i--)
					{
						res = CompoundTerm.getList(IntegerTerm.get(result.charAt(i)), res);
					}
					break;
				default:
					PrologException.systemError();
			}

			return interpreter.unify(outterm, res);
		}
	}
}
