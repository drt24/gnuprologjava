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
package gnu.prolog.database;

import gnu.prolog.term.Term;

/**
 * A listener for certain prolog text loader events
 * 
 * @author Michiel Hendriks
 */
public interface PrologTextLoaderListener
{
	/**
	 * Called right before the file is being processed
	 * 
	 * @param loader
	 */
	void beforeProcessFile(PrologTextLoader loader);

	/**
	 * Called right after the file has been processed
	 * 
	 * @param loader
	 */
	void afterProcessFile(PrologTextLoader loader);

	/**
	 * Called before an include file is being processed. The loader's currentFile
	 * fields have not been updated yet.
	 * 
	 * @param loader
	 * @param argument
	 *          The argument passed to the include directive
	 */
	void beforeIncludeFile(PrologTextLoader loader, Term argument);

	/**
	 * Called right after an include file has been processed and before the loader
	 * returns to the previous file.
	 * 
	 * @param loader
	 */
	void afterIncludeFile(PrologTextLoader loader);
}
