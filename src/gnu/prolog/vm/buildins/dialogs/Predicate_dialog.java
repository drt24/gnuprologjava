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
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * 
 * @author Michiel Hendriks
 */
public abstract class Predicate_dialog extends ExecuteOnlyCode
{
	public static final AtomTerm DIALOG_OPTION_ATOM = AtomTerm.get("dialog_option");

	// Buttons
	public static final AtomTerm OK_ATOM = AtomTerm.get("ok");
	public static final AtomTerm CANCEL_ATOM = AtomTerm.get("cancel");
	public static final AtomTerm YES_ATOM = AtomTerm.get("yes");
	public static final AtomTerm NO_ATOM = AtomTerm.get("no");
	public static final AtomTerm IGNORE_ATOM = AtomTerm.get("ignore");
	public static final AtomTerm ABORT_ATOM = AtomTerm.get("abort");
	public static final AtomTerm RETRY_ATOM = AtomTerm.get("retry");

	// Other options
	public static final CompoundTermTag TITLE_TAG = CompoundTermTag.get("title", 1);
	public static final CompoundTermTag MESSAGE_TAG = CompoundTermTag.get("message", 1);
	public static final CompoundTermTag SELECTION_TAG = CompoundTermTag.get("selection", 1);
	public static final CompoundTermTag FILEMASK_TAG = CompoundTermTag.get("filemask", 1);
	public static final CompoundTermTag TYPE_TAG = CompoundTermTag.get("type", 1);

	// Message dialog types
	public static final AtomTerm ERROR_ATOM = AtomTerm.get("error");
	public static final AtomTerm WARNING_ATOM = AtomTerm.get("warning");
	public static final AtomTerm INFO_ATOM = AtomTerm.get("info");
	public static final AtomTerm QUESTION_ATOM = AtomTerm.get("question");

	public static class DialogOptions
	{
		public static final int BUTTON_OK = 1;
		public static final int BUTTON_CANCEL = 2;
		public static final int BUTTON_YES = 4;
		public static final int BUTTON_NO = 8;
		public static final int BUTTON_IGNORE = 16;
		public static final int BUTTON_ABORT = 32;
		public static final int BUTTON_RETRY = 64;

		public String title;
		public String message;
		public String selection;
		public List<FileFilter> fileFilters;
		public int messageType = -1;
		public int buttons;
	}

	public Predicate_dialog()
	{}

	/**
	 * Process dialog options
	 * 
	 * @param optionsList
	 * @return the processed DialogOptions
	 * @throws PrologException
	 */
	protected DialogOptions processOptions(Term optionsList) throws PrologException
	{
		DialogOptions options = new DialogOptions();
		Term cur = optionsList;
		while (cur != TermConstants.emptyListAtom)
		{
			if (cur instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}
			if (!(cur instanceof CompoundTerm))
			{
				PrologException.typeError(TermConstants.listAtom, optionsList);
			}
			CompoundTerm ct = (CompoundTerm) cur;
			if (ct.tag != TermConstants.listTag)
			{
				PrologException.typeError(TermConstants.listAtom, optionsList);
			}
			Term head = ct.args[0].dereference();
			cur = ct.args[1].dereference();
			if (head instanceof VariableTerm)
			{
				PrologException.instantiationError();
			}

			if (head instanceof AtomTerm)
			{
				if (OK_ATOM.equals(head))
				{
					options.buttons |= DialogOptions.BUTTON_OK;
					continue;
				}
				if (CANCEL_ATOM.equals(head))
				{
					options.buttons |= DialogOptions.BUTTON_CANCEL;
					continue;
				}
				if (YES_ATOM.equals(head))
				{
					options.buttons |= DialogOptions.BUTTON_YES;
					continue;
				}
				if (NO_ATOM.equals(head))
				{
					options.buttons |= DialogOptions.BUTTON_NO;
					continue;
				}
				if (IGNORE_ATOM.equals(head))
				{
					options.buttons |= DialogOptions.BUTTON_IGNORE;
					continue;
				}
				if (ABORT_ATOM.equals(head))
				{
					options.buttons |= DialogOptions.BUTTON_ABORT;
					continue;
				}
				if (RETRY_ATOM.equals(head))
				{
					options.buttons |= DialogOptions.BUTTON_RETRY;
					continue;
				}
			}

			if (!(head instanceof CompoundTerm))
			{
				PrologException.domainError(DIALOG_OPTION_ATOM, head);
			}
			CompoundTerm op = (CompoundTerm) head;
			if (op.tag == TITLE_TAG)
			{
				Term val = op.args[0].dereference();
				if (!(val instanceof AtomTerm))
				{
					PrologException.domainError(DIALOG_OPTION_ATOM, op);
				}
				options.title = ((AtomTerm) val).value;
			}
			else if (op.tag == MESSAGE_TAG)
			{
				Term val = op.args[0].dereference();
				if (!(val instanceof AtomTerm))
				{
					PrologException.domainError(DIALOG_OPTION_ATOM, op);
				}
				options.message = ((AtomTerm) val).value;
			}
			else if (op.tag == SELECTION_TAG)
			{
				Term val = op.args[0].dereference();
				if (!(val instanceof AtomTerm))
				{
					PrologException.domainError(DIALOG_OPTION_ATOM, op);
				}
				options.selection = ((AtomTerm) val).value;
			}
			else if (op.tag == FILEMASK_TAG)
			{
				Term val = op.args[0].dereference();
				FileFilter filter = null;
				if (val instanceof AtomTerm || (val instanceof CompoundTerm && ((CompoundTerm) val).tag.arity == 1))
				{
					filter = new TermFileFilter(val);
				}
				else
				{
					PrologException.domainError(DIALOG_OPTION_ATOM, op);
				}
				if (options.fileFilters == null)
				{
					options.fileFilters = new ArrayList<FileFilter>();
				}
				options.fileFilters.add(filter);
			}
			else if (op.tag == TYPE_TAG)
			{
				Term val = op.args[0].dereference();
				if (ERROR_ATOM.equals(val))
				{
					options.messageType = JOptionPane.ERROR_MESSAGE;
				}
				else if (WARNING_ATOM.equals(val))
				{
					options.messageType = JOptionPane.WARNING_MESSAGE;
				}
				else if (INFO_ATOM.equals(val))
				{
					options.messageType = JOptionPane.INFORMATION_MESSAGE;
				}
				else if (QUESTION_ATOM.equals(val))
				{
					options.messageType = JOptionPane.QUESTION_MESSAGE;
				}
				else
				{
					PrologException.domainError(DIALOG_OPTION_ATOM, op);
				}
			}
			else
			{
				PrologException.domainError(DIALOG_OPTION_ATOM, op);
			}
		}
		return options;
	}
}
