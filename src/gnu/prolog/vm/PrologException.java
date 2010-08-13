/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
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

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;

/** a prolog exception */
public class PrologException extends Exception
{
	private static final long serialVersionUID = 946127094875894543L;

	/** term of the exception */
	protected Term term;
	/** message of exception */
	protected String msg;

	/**
	 *
	 */
	protected PrologException()
	{
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	protected PrologException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 */
	protected PrologException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	protected PrologException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * a constructor
	 * 
	 * @param term
	 * 
	 * @param inner
	 *          The cause of the exception.
	 */
	public PrologException(Term term, Throwable inner)
	{
		this(inner);
		this.term = term;
	}

	@Override
	public String getMessage()
	{
		if (msg == null)
		{
			msg = gnu.prolog.io.TermWriter.toString(term);
		}
		return msg;
	}

	/**
	 * get term of this exception
	 * 
	 * @return term of this exception
	 */
	public Term getTerm()
	{
		return term;
	}

	// public final static AtomTerm Atom = AtomTerm.get("");
	public final static AtomTerm instantiationErrorAtom = AtomTerm.get("instantiation_error");
	public final static AtomTerm systemErrorAtom = AtomTerm.get("system_error");
	public final static AtomTerm errorAtom = AtomTerm.get("error");
	// public final static CompoundTermTag Tag = CompoundTermTag.get("",);
	public final static CompoundTermTag errorTag = CompoundTermTag.get("error", 2);
	public final static CompoundTermTag typeErrorTag = CompoundTermTag.get("type_error", 2);
	public final static CompoundTermTag existenceErrorTag = CompoundTermTag.get("existence_error", 2);
	public final static CompoundTermTag domainErrorTag = CompoundTermTag.get("domain_error", 2);
	public final static CompoundTermTag representationErrorTag = CompoundTermTag.get("representation_error", 1);
	public final static CompoundTermTag syntaxErrorTag = CompoundTermTag.get("syntax_error", 1);
	public final static CompoundTermTag permissionErrorTag = CompoundTermTag.get("permission_error", 3);
	public final static CompoundTermTag evaluationErrorTag = CompoundTermTag.get("evaluation_error", 1);

	private static PrologException getError(Term term)
	{
		return getError(term, null);
	}

	private static PrologException getError(Term term, Throwable inner)
	{
		if (inner != null)
		{
			return new PrologException(new CompoundTerm(errorTag, term, AtomTerm.get(inner.toString())), inner);
		}
		else
		{
			return new PrologException(new CompoundTerm(errorTag, term, errorAtom), inner);
		}
	}

	public static void systemError() throws PrologException
	{
		systemError(null);
	}

	public static void systemError(Throwable inner) throws PrologException
	{
		throw getError(systemErrorAtom, inner);
	}

	public static void instantiationError() throws PrologException
	{
		throw getError(instantiationErrorAtom);
	}

	public static void typeError(AtomTerm errorType, Term errorTerm) throws PrologException
	{
		throw getError(new CompoundTerm(typeErrorTag, errorType, errorTerm));
	}

	public static void existenceError(AtomTerm errorType, Term errorTerm) throws PrologException
	{
		throw getError(new CompoundTerm(existenceErrorTag, errorType, errorTerm));
	}

	public static void domainError(AtomTerm errorType, Term errorTerm) throws PrologException
	{
		throw getError(new CompoundTerm(domainErrorTag, errorType, errorTerm));
	}

	public static void representationError(Term errorTerm) throws PrologException
	{
		throw getError(new CompoundTerm(representationErrorTag, errorTerm));
	}

	public static void syntaxError(AtomTerm term) throws PrologException
	{
		throw getError(new CompoundTerm(syntaxErrorTag, term));
	}

	public static void syntaxError(AtomTerm term, Throwable inner) throws PrologException
	{
		throw getError(new CompoundTerm(syntaxErrorTag, term), inner);
	}

	public static void syntaxError(gnu.prolog.io.ParseException ex) throws PrologException
	{
		syntaxError(AtomTerm.get(ex.getMessage()), ex);
	}

	public static void permissionError(AtomTerm operation, AtomTerm permissionType, Term culprit) throws PrologException
	{
		throw getError(new CompoundTerm(permissionErrorTag, operation, permissionType, culprit));
	}

	public static void evalutationError(AtomTerm error) throws PrologException
	{
		throw getError(new CompoundTerm(evaluationErrorTag, error));
	}
}
