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
package gnu.prolog.vm;

/**
 * For Predicates which do not need to be installed or uninstalled. So that they
 * don't all need to have empty methods.
 * 
 * @author Daniel Thomas
 */
public abstract class ExecuteOnlyCode implements PrologCode
{

	public abstract int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException;

	/**
	 * Just an empty method as we don't need to do anything here.
	 * 
	 * @see gnu.prolog.vm.Installable#install(gnu.prolog.vm.Environment)
	 */
	public void install(Environment env)
	{}

	/**
	 * Just an empty method as we don't need to do anything here.
	 * 
	 * @see gnu.prolog.vm.Installable#uninstall(gnu.prolog.vm.Environment)
	 */
	public void uninstall(Environment env)
	{}
}
