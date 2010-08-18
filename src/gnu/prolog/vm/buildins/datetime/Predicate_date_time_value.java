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
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_date_time_value extends DateTimePrologCode
{
	public static final AtomTerm yearAtom = AtomTerm.get("year");
	public static final AtomTerm monthAtom = AtomTerm.get("month");
	public static final AtomTerm dayAtom = AtomTerm.get("day");
	public static final AtomTerm hourAtom = AtomTerm.get("hour");
	public static final AtomTerm minuteAtom = AtomTerm.get("minute");
	public static final AtomTerm secondAtom = AtomTerm.get("second");
	public static final AtomTerm utcOffsetAtom = AtomTerm.get("utc_offset");
	public static final AtomTerm timeZoneAtom = AtomTerm.get("time_zone");
	public static final AtomTerm daylightSavingAtom = AtomTerm.get("daylight_saving");

	// used for iterating
	public static final AtomTerm[] date9keys = new AtomTerm[] { yearAtom, monthAtom, dayAtom, hourAtom, minuteAtom,
			secondAtom, utcOffsetAtom, timeZoneAtom, daylightSavingAtom, dateAtom, timeAtom };

	static class Date9BacktrackInfo extends BacktrackInfo
	{
		Term key;
		CompoundTerm date9;
		Term value;
		int date9idx;
		int startUndoPosition;

		public Date9BacktrackInfo()
		{
			super(-1, -1);
		}
	}

	public Predicate_date_time_value()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		// date_time_value(?Key, +DateTime, ?Value)
		if (backtrackMode)
		{
			Date9BacktrackInfo bi = (Date9BacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}

		if (!(args[1] instanceof CompoundTerm))
		{
			PrologException.typeError(dateAtom, args[1]);
		}
		CompoundTerm date9 = (CompoundTerm) args[1];
		if (date9.tag != date9Tag)
		{
			PrologException.typeError(dateAtom, args[1]);
		}
		if (date9.args.length != 9)
		{
			PrologException.typeError(dateAtom, args[1]);
		}

		if (args[0] instanceof VariableTerm)
		{
			Date9BacktrackInfo bi = new Date9BacktrackInfo();
			bi.startUndoPosition = interpreter.getUndoPosition();
			bi.key = args[0];
			bi.value = args[2];
			bi.date9 = date9;
			return nextSolution(interpreter, bi);
		}
		else
		{
			Term res = getDate9Value(args[0], date9);
			if (res == null)
			{
				return FAIL;
			}
			return interpreter.unify(args[2], res);
		}
	}

	/**
	 * @param keyTerm
	 *          the term to use to select which part of date9 to return
	 * @param date9
	 * @return the Term representing the part of date9 selected by keyTerm
	 * @throws PrologException
	 */
	protected Term getDate9Value(Term keyTerm, CompoundTerm date9) throws PrologException
	{
		if (!(keyTerm instanceof AtomTerm))
		{
			PrologException.typeError(TermConstants.atomAtom, keyTerm);
		}
		AtomTerm key = (AtomTerm) keyTerm;
		if (key == yearAtom)
		{
			return date9.args[0];
		}
		else if (key == monthAtom)
		{
			return date9.args[1];
		}
		else if (key == dayAtom)
		{
			return date9.args[2];
		}
		else if (key == hourAtom)
		{
			return date9.args[3];
		}
		else if (key == minuteAtom)
		{
			return date9.args[4];
		}
		else if (key == secondAtom)
		{
			return date9.args[5];
		}
		else if (key == utcOffsetAtom)
		{
			return date9.args[6];
		}
		else if (key == timeZoneAtom)
		{
			if (date9.args[7] == AtomTerm.get("-"))
			{
				return null;
			}
			return date9.args[7];
		}
		else if (key == daylightSavingAtom)
		{
			if (date9.args[8] == AtomTerm.get("-"))
			{
				return null;
			}
			return date9.args[8];
		}
		else if (key == dateAtom)
		{
			return new CompoundTerm(date3Tag, new Term[] { date9.args[0], date9.args[1], date9.args[2] });
		}
		else if (key == timeAtom)
		{
			return new CompoundTerm(date3Tag, new Term[] { date9.args[3], date9.args[4], date9.args[5] });
		}
		return null;
	}

	/**
	 * @param interpreter
	 * @param bi
	 * @return PrologCode return code
	 * @throws PrologException
	 */
	protected int nextSolution(Interpreter interpreter, Date9BacktrackInfo bi) throws PrologException
	{
		while (bi.date9idx < date9keys.length)
		{
			Term key = date9keys[bi.date9idx];
			Term res = getDate9Value(key, bi.date9);
			bi.date9idx++;
			if (res == null)
			{
				continue;
			}
			if (interpreter.unify(bi.value, res) == FAIL)
			{
				interpreter.undo(bi.startUndoPosition);
				continue;
			}
			if (interpreter.unify(bi.key, key) == FAIL)
			{
				interpreter.undo(bi.startUndoPosition);
				continue;
			}
			interpreter.pushBacktrackInfo(bi);
			return SUCCESS;
		}
		return FAIL;
	}

}
