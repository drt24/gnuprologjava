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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_file_save extends Predicate_dialog
{
	public Predicate_file_save()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
	{
		JFileChooser choose = createFileDialog(args);
		while (choose.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			if (choose.getSelectedFile().exists())
			{
				if (JOptionPane.showConfirmDialog(null, String.format(
						"Are you sure you want to overwrite the existing file:\n%s ?", choose.getSelectedFile().toString()),
						"File Exists", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				{
					// ask for a new file
					continue;
				}
			}
			Term term = AtomTerm.get(choose.getSelectedFile().toString());
			return interpreter.unify(args[0], term);
		}
		return FAIL;
	}

	/**
	 * @param args
	 * @return the created JFileChooser
	 * @throws PrologException
	 */
	protected JFileChooser createFileDialog(Term[] args) throws PrologException
	{
		DialogOptions options;
		if (args.length >= 2)
		{
			options = processOptions(args[1]);
		}
		else
		{
			options = new DialogOptions();
		}

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		chooser.setMultiSelectionEnabled(false);
		if (options.title != null)
		{
			chooser.setDialogTitle(options.title);
		}
		if (options.fileFilters != null)
		{
			FileFilter selfilter = null;
			chooser.setAcceptAllFileFilterUsed(false);
			for (FileFilter filter : options.fileFilters)
			{
				chooser.addChoosableFileFilter(filter);
				if (selfilter == null)
				{
					selfilter = filter;
				}
			}
			chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
			if (selfilter != null)
			{
				chooser.setFileFilter(selfilter);
			}
		}
		if (options.selection != null)
		{
			chooser.setSelectedFile(new File(options.selection));
		}
		return chooser;
	}

}
