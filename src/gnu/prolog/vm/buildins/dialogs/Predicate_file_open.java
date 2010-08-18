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
package gnu.prolog.vm.buildins.dialogs;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;

import javax.swing.JFileChooser;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_file_open extends Predicate_file_save
{
	public Predicate_file_open()
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gnu.prolog.vm.buildins.dialogs.Predicate_file_save#execute(gnu.prolog.vm
	 * .Interpreter, boolean, gnu.prolog.term.Term[])
	 */
	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		JFileChooser choose = createFileDialog(args);
		if (choose.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			Term term = AtomTerm.get(choose.getSelectedFile().toString());
			return interpreter.unify(args[0], term);
		}
		return FAIL;
	}
}
