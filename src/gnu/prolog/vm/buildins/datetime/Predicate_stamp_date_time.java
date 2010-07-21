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
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_stamp_date_time extends DateTimePrologCode
{
	public Predicate_stamp_date_time()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		// stamp_date_time(+TimeStamp, -DateTime, +TimeZone)
		if (!(args[0] instanceof FloatTerm))
		{
			PrologException.typeError(TermConstants.floatAtom, args[0]);
		}
		double ts = ((FloatTerm) args[0]).value;
		if (!(args[1] instanceof VariableTerm))
		{
			PrologException.typeError(TermConstants.variableAtom, args[1]);
		}

		TimeZone tz = null;
		if (args[2] instanceof IntegerTerm)
		{
			tz = new SimpleTimeZone(((IntegerTerm) args[2]).value * 1000, "-");
		}
		else if (args[2] instanceof AtomTerm)
		{
			String tzString = ((AtomTerm) args[2]).value;
			tz = TimeZone.getTimeZone(tzString);

		}
		else
		{
			PrologException.typeError(TermConstants.atomAtom, args[2]);
		}

		Calendar cal = Calendar.getInstance(tz);
		cal.setTimeInMillis(Math.round(ts * 1000));
		Term[] dateTime = new Term[9];
		dateTime[0] = IntegerTerm.get(cal.get(Calendar.YEAR));
		dateTime[1] = IntegerTerm.get(cal.get(Calendar.MONTH) + 1);
		dateTime[2] = IntegerTerm.get(cal.get(Calendar.DAY_OF_MONTH));
		dateTime[3] = IntegerTerm.get(cal.get(Calendar.HOUR_OF_DAY));
		dateTime[4] = IntegerTerm.get(cal.get(Calendar.MINUTE));
		dateTime[5] = new FloatTerm(cal.get(Calendar.SECOND) + (cal.get(Calendar.MILLISECOND) / 1000.0));
		dateTime[6] = IntegerTerm.get(cal.get(Calendar.ZONE_OFFSET) / 1000);
		if (tz != null)
		{
			dateTime[7] = AtomTerm.get(tz.getID());
			if (tz.useDaylightTime())
			{
				if (tz.inDaylightTime(cal.getTime()))
				{
					dateTime[8] = TermConstants.trueAtom;
				}
				else
				{
					dateTime[8] = TermConstants.falseAtom;
				}
			}
			else
			{
				dateTime[8] = AtomTerm.get("-");
			}
		}
		else
		{
			dateTime[7] = AtomTerm.get("-");
			dateTime[8] = AtomTerm.get("-");
		}
		Term res = new CompoundTerm(date9Tag, dateTime);
		return interpreter.unify(args[1], res);
	}
}
