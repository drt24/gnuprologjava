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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * 
 * @author Michiel Hendriks
 */
public class Predicate_confirm extends Predicate_dialog
{
	public static class TermOption
	{
		public String text;
		public Term result;

		public TermOption(String res)
		{
			this(res, AtomTerm.get(res));
		}

		public TermOption(String res, Term resTerm)
		{
			text = res;
			result = resTerm;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return text;
		}
	}

	public Predicate_confirm()
	{}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException
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
		if (options.title == null)
		{
			options.title = UIManager.getString("OptionPane.titleText");
		}
		if (options.buttons == 0)
		{
			options.buttons = DialogOptions.BUTTON_OK | DialogOptions.BUTTON_CANCEL;
		}

		Object initialValue = null;
		List<TermOption> opts = new ArrayList<TermOption>();
		if ((options.buttons & DialogOptions.BUTTON_OK) != 0)
		{
			opts.add(new TermOption(UIManager.getString("OptionPane.okButtonText", null), Predicate_dialog.OK_ATOM));
		}
		if ((options.buttons & DialogOptions.BUTTON_YES) != 0)
		{
			opts.add(new TermOption(UIManager.getString("OptionPane.yesButtonText", null), Predicate_dialog.YES_ATOM));
		}
		if ((options.buttons & DialogOptions.BUTTON_NO) != 0)
		{
			opts.add(new TermOption(UIManager.getString("OptionPane.noButtonText", null), Predicate_dialog.NO_ATOM));
		}
		if ((options.buttons & DialogOptions.BUTTON_ABORT) != 0)
		{
			opts.add(new TermOption("Abort", Predicate_dialog.ABORT_ATOM));
		}
		if ((options.buttons & DialogOptions.BUTTON_IGNORE) != 0)
		{
			opts.add(new TermOption("Ignore", Predicate_dialog.IGNORE_ATOM));
		}
		if ((options.buttons & DialogOptions.BUTTON_RETRY) != 0)
		{
			opts.add(new TermOption("Retry", Predicate_dialog.RETRY_ATOM));
		}
		if ((options.buttons & DialogOptions.BUTTON_CANCEL) != 0)
		{
			opts.add(new TermOption(UIManager.getString("OptionPane.cancelButtonText", null), Predicate_dialog.CANCEL_ATOM));
		}

		if (options.selection != null)
		{
			for (TermOption opt : opts)
			{
				if (opt.result instanceof AtomTerm)
				{
					if (((AtomTerm) opt.result).value.equals(options.selection))
					{
						initialValue = opt;
						break;
					}
				}
			}
		}

		Object result = JOptionPane.showOptionDialog(null, options.message, options.title, JOptionPane.DEFAULT_OPTION,
				options.messageType, null, opts.toArray(), initialValue);
		if (result instanceof Integer)
		{
			int res = (Integer) result;
			if (res >= 0 && res <= opts.size())
			{
				return interpreter.unify(args[0], opts.get(res).result);
			}
		}
		return FAIL;
	}
}
