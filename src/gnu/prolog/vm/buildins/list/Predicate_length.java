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
package gnu.prolog.vm.buildins.list;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_length implements PrologCode
{
	public Predicate_length()
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gnu.prolog.vm.PrologCode#execute(gnu.prolog.vm.Interpreter, boolean,
	 * gnu.prolog.term.Term[])
	 */
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		if (CompoundTerm.isListPair(args[0]))
		{
			int length = 0;
			
			Term lst = args[0];
			while (lst != null)
			{
				if (AtomTerm.emptyList.equals(lst))
				{
					break;
				}
				if (!(CompoundTerm.isListPair(lst)))
				{
					return FAIL;
				}
				CompoundTerm ct = (CompoundTerm) lst;
				if (ct.args.length != 2)
				{
					return FAIL;
				}
				++length;
				lst = ct.args[1];
			}
			
			return interpreter.unify(args[1], IntegerTerm.get(length));
		}
		else if (args[0] instanceof VariableTerm)
		{
			if (!(args[1] instanceof IntegerTerm))
			{
				PrologException.typeError(TermConstants.integerAtom, args[1]);
			}
			List<Term> genList = new ArrayList<Term>();
			for (int i = 0; i < ((IntegerTerm) args[1]).value; i++)
			{
				genList.add(new VariableTerm());
			}
			Term term = CompoundTerm.getList(genList);
			return interpreter.unify(args[0], term);
		}
		else
		{
			PrologException.typeError(TermConstants.listAtom, args[0]);
		}
		return 0;
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
