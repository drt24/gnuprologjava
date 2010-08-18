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
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

/**
 * 
 * @author Michiel Hendriks
 */
public class TermFileFilter extends FileFilter
{
	protected String description;

	protected Set<String> extension;

	public TermFileFilter(Term term) throws PrologException
	{
		extension = new HashSet<String>();
		String pattern = null;
		if (term instanceof AtomTerm)
		{
			pattern = ((AtomTerm) term).value;
			if ("prolog".equals(pattern))
			{
				pattern = "*.pro;*.pl";
				description = "Prolog files";
			}
		}
		else if (term instanceof CompoundTerm && ((CompoundTerm) term).tag.arity == 1)
		{
			pattern = ((CompoundTerm) term).tag.functor.value;
			term = ((CompoundTerm) term).args[0].dereference();
			if (term instanceof AtomTerm)
			{
				description = ((AtomTerm) term).value;
			}
			else
			{
				PrologException.typeError(TermConstants.atomAtom, term);
			}
		}
		else
		{
			PrologException.domainError(Predicate_dialog.DIALOG_OPTION_ATOM, term);
		}
		Matcher m = Pattern.compile("(\\*(\\.[^;]*);?)", Pattern.CASE_INSENSITIVE).matcher(pattern);
		while (m.find())
		{
			extension.add(m.group(2));
		}
		if (extension.isEmpty())
		{
			PrologException.domainError(Predicate_dialog.FILEMASK_TAG.functor, term);
		}
		if (description == null || description.length() == 0)
		{
			description = pattern;
		}
		else
		{
			description = String.format("%s (%s)", description, pattern);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f)
	{
		if (f.isDirectory())
		{
			return true;
		}
		for (String pat : extension)
		{
			if (f.getName().toLowerCase().endsWith(pat))
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return description;
	}
}
