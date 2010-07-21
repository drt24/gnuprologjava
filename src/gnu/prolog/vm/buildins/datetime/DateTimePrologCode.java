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
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 * @author Michiel Hendriks
 */
public abstract class DateTimePrologCode extends ExecuteOnlyCode
{
	public static final AtomTerm dateAtom = AtomTerm.get("date");
	public static final AtomTerm timeAtom = AtomTerm.get("time");
	public static final CompoundTermTag date9Tag = CompoundTermTag.get(dateAtom, 9);
	public static final CompoundTermTag date3Tag = CompoundTermTag.get(dateAtom, 3);
	public static final CompoundTermTag timeTag = CompoundTermTag.get(timeAtom, 3);

	public static final Date getDate(Term term) throws PrologException
	{
		if (!(term instanceof CompoundTerm))
		{
			PrologException.typeError(dateAtom, term);
		}
		CompoundTerm cterm = (CompoundTerm) term;
		if (cterm.tag != date9Tag)
		{
			PrologException.typeError(dateAtom, term);
		}
		if (cterm.args.length != 9)
		{
			PrologException.typeError(dateAtom, term);
		}

		// type checking
		if (!(cterm.args[0] instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, cterm.args[0]);
		}
		if (!(cterm.args[1] instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, cterm.args[1]);
		}
		if (!(cterm.args[2] instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, cterm.args[2]);
		}
		if (!(cterm.args[3] instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, cterm.args[3]);
		}
		if (!(cterm.args[4] instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, cterm.args[4]);
		}
		if (!(cterm.args[5] instanceof FloatTerm || cterm.args[5] instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.floatAtom, cterm.args[5]);
		}
		if (!(cterm.args[6] instanceof IntegerTerm))
		{
			PrologException.typeError(TermConstants.integerAtom, cterm.args[6]);
		}
		if (!(cterm.args[7] instanceof AtomTerm))
		{
			PrologException.typeError(TermConstants.atomAtom, cterm.args[7]);
		}
		if (!(cterm.args[8] instanceof AtomTerm))
		{
			PrologException.typeError(TermConstants.atomAtom, cterm.args[8]);
		}

		// get the timezone
		TimeZone zone = TimeZone.getDefault();
		if (cterm.args[7] != AtomTerm.get("-"))
		{
			zone = TimeZone.getTimeZone(((AtomTerm) cterm.args[7]).value);
			// what to do if the zone ID doesn't exist?
		}
		else
		{
			String[] zones = TimeZone.getAvailableIDs(((IntegerTerm) cterm.args[6]).value);
			for (String zoneId : zones)
			{
				TimeZone z = TimeZone.getTimeZone(zoneId);
				if (cterm.args[8] == AtomTerm.get("-"))
				{
					zone = z;
					break;
				}
				else if (cterm.args[8] == TermConstants.trueAtom)
				{
					if (z.useDaylightTime())
					{
						zone = z;
						break;
					}
				}
				else if (cterm.args[8] == TermConstants.falseAtom)
				{
					if (!z.useDaylightTime())
					{
						zone = z;
						break;
					}
				}
				else
				{
					// TODO unknown term... error?
				}
			}
		}
		Calendar cal = Calendar.getInstance(zone);
		cal.clear();
		int sec = 0;
		int msec = 0;
		if (cterm.args[5] instanceof IntegerTerm)
		{
			sec = ((IntegerTerm) cterm.args[5]).value;
		}
		else
		{
			sec = (int) Math.floor(((FloatTerm) cterm.args[5]).value);
			msec = (int) Math.round((((FloatTerm) cterm.args[5]).value % 1.0) * 1000);
		}
		cal.set(((IntegerTerm) cterm.args[0]).value, ((IntegerTerm) cterm.args[1]).value - 1,
				((IntegerTerm) cterm.args[2]).value, ((IntegerTerm) cterm.args[3]).value, ((IntegerTerm) cterm.args[4]).value,
				sec);
		cal.set(Calendar.MILLISECOND, msec);
		return cal.getTime();
	}

	public DateTimePrologCode()
	{}
}
