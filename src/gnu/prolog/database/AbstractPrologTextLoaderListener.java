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
 * Default implementation that doesn't do anything
 * 
 * @author Michiel Hendriks
 */
public class AbstractPrologTextLoaderListener implements PrologTextLoaderListener
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gnu.prolog.database.PrologTextLoaderListener#afterIncludeFile(gnu.prolog
	 * .database.PrologTextLoader)
	 */
	public void afterIncludeFile(PrologTextLoader loader)
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gnu.prolog.database.PrologTextLoaderListener#afterProcessFile(gnu.prolog
	 * .database.PrologTextLoader)
	 */
	public void afterProcessFile(PrologTextLoader loader)
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gnu.prolog.database.PrologTextLoaderListener#beforeIncludeFile(gnu.prolog
	 * .database.PrologTextLoader, gnu.prolog.term.Term)
	 */
	public void beforeIncludeFile(PrologTextLoader loader, Term argument)
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gnu.prolog.database.PrologTextLoaderListener#beforeProcessFile(gnu.prolog
	 * .database.PrologTextLoader)
	 */
	public void beforeProcessFile(PrologTextLoader loader)
	{}
}
