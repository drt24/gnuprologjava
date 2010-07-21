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
package gnu.prolog.vm.buildins.uuid;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;

import java.util.UUID;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_uuid_compare extends Predicate_uuid
{
	public Predicate_uuid_compare()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		UUID uuid1 = getUUID(args[1]);
		UUID uuid2 = getUUID(args[2]);
		if (uuid1 == null || uuid2 == null)
		{
			return FAIL;
		}
		int cmp = uuid1.compareTo(uuid2);
		if (cmp > 0)
		{
			return interpreter.unify(args[0], AtomTerm.get(">"));
		}
		else if (cmp < 0)
		{
			return interpreter.unify(args[0], AtomTerm.get("<"));
		}
		return interpreter.unify(args[0], AtomTerm.get("="));
	}
}
