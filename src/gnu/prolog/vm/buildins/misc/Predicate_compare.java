/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA. The text ol license can be also found 
 * at http://www.gnu.org/copyleft/lgpl.html
 */
package gnu.prolog.vm.buildins.misc;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.TermComparator;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_compare implements PrologCode
{
	public static final AtomTerm EQ_ATOM = AtomTerm.get("=");
	public static final AtomTerm LT_ATOM = AtomTerm.get("<");
	public static final AtomTerm GT_ATOM = AtomTerm.get(">");

	protected TermComparator comp = new TermComparator();

	public Predicate_compare()
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#execute(gnu.prolog.vm.Interpreter, boolean,
	 * gnu.prolog.term.Term[])
	 */
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		if (!(args[0] instanceof VariableTerm))
		{
			//
		}
		int rc = comp.compare(args[1], args[2]);
		if (rc == 0)
		{
			return interpreter.unify(EQ_ATOM, args[0]);
		}
		else if (rc > 0)
		{
			return interpreter.unify(GT_ATOM, args[0]);
		}
		else if (rc < 0)
		{
			return interpreter.unify(LT_ATOM, args[0]);
		}
		return FAIL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#install(gnu.prolog.vm.Environment)
	 */
	public void install(Environment env)
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#uninstall(gnu.prolog.vm.Environment)
	 */
	public void uninstall(Environment env)
	{}

}
