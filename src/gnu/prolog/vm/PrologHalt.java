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
 * Thrown by the halt/1 prolog predicate to stop the interpreter
 *
 * @author Michiel Hendriks
 */
public class PrologHalt extends Error
{
	private static final long serialVersionUID = -8799959644073589092L;

	protected int exitCode;

	public PrologHalt(int exit)
	{
		super("Halt prolog interpreter");
		exitCode = exit;
	}

	/**
	 * @return the exitCode
	 */
	public int getExitCode()
	{
		return exitCode;
	}
}
