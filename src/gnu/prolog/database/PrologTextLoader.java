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
package gnu.prolog.database;

import gnu.prolog.io.OperatorSet;
import gnu.prolog.io.ParseException;
import gnu.prolog.io.TermReader;
import gnu.prolog.io.Operator.SPECIFIER;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.TermConstants;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Stack;

/**
 * Processes prolog files adding the new predicates listed within them and
 * processing directives.
 * 
 * @see PrologTextLoaderState all the PrologTextLoader's for an
 *      {@link gnu.prolog.vm.Environment Environment} use the same instance of
 *      PrologTextLoaderState to store the collective state.
 */
public class PrologTextLoader
{
	/** root file */
	protected String rootFile;
	/** current file */
	protected String currentFile;
	/** current term reader */
	protected TermReader currentReader;
	/** stack of previous readers */
	protected Stack<TermReader> readerStack = new Stack<TermReader>();
	/** stack of previous files */
	protected Stack<String> fileStack = new Stack<String>();
	/** operator set */
	protected OperatorSet operatorSet = new OperatorSet();
	/** prolog text loader state */
	protected PrologTextLoaderState prologTextLoaderState;

	// tags used in loader
	public static final CompoundTermTag includeTag = CompoundTermTag.get("include", 1);
	public static final CompoundTermTag multifileTag = CompoundTermTag.get("multifile", 1);
	public static final CompoundTermTag dynamicTag = CompoundTermTag.get("dynamic", 1);
	public static final CompoundTermTag discontiguousTag = CompoundTermTag.get("discontiguous", 1);
	public static final CompoundTermTag opTag = CompoundTermTag.get("op", 3);
	public static final CompoundTermTag char_conversionTag = CompoundTermTag.get("char_conversion", 2);
	public static final CompoundTermTag initializationTag = CompoundTermTag.get("initialization", 1);
	public static final CompoundTermTag ensure_loadedTag = CompoundTermTag.get("ensure_loaded", 1);
	public static final CompoundTermTag set_prolog_flagTag = CompoundTermTag.get("set_prolog_flag", 2);

	// my extension directives
	public static final CompoundTermTag externalTag = CompoundTermTag.get("external", 2);
	public static final CompoundTermTag build_inTag = CompoundTermTag.get("build_in", 2);
	public static final CompoundTermTag controlTag = CompoundTermTag.get("control", 2);

	// include/ensure loaded argument terms
	public static final CompoundTermTag url1Tag = CompoundTermTag.get("url", 1);
	public static final CompoundTermTag resource1Tag = CompoundTermTag.get("resource", 1);
	public static final CompoundTermTag file1Tag = CompoundTermTag.get("file", 1);

	protected PrologTextLoader(PrologTextLoaderState prologTextLoaderState)
	{
		this.prologTextLoaderState = prologTextLoaderState;
	}

	public PrologTextLoader(PrologTextLoaderState prologTextLoaderState, Term root)
	{
		this(prologTextLoaderState);
		rootFile = prologTextLoaderState.getInputName(root);
		currentFile = rootFile;
		try
		{
			currentReader = new TermReader(new InputStreamReader(prologTextLoaderState.getInputStream(root)),
					prologTextLoaderState.getEnvironment());
		}
		catch (Exception ex)
		{
			logError("could not open file \'" + currentFile + "\': " + ex.getMessage());
			return;
		}
		processFile();
	}

	/**
	 * @param prologTextLoaderState
	 * @param stream
	 *          the input stream to read from.
	 */
	public PrologTextLoader(PrologTextLoaderState prologTextLoaderState, Reader stream)
	{
		this(prologTextLoaderState, stream, "input:");
	}

	/**
	 * @param prologTextLoaderState
	 * @param stream
	 * @param streamName
	 *          The stream name
	 */
	public PrologTextLoader(PrologTextLoaderState prologTextLoaderState, Reader stream, String streamName)
	{
		this(prologTextLoaderState);
		if (streamName == null || streamName.length() == 0)
		{
			streamName = "input:";
		}
		rootFile = streamName;
		currentFile = rootFile;
		try
		{
			currentReader = new TermReader(stream, prologTextLoaderState.getEnvironment());
		}
		catch (Exception ex)
		{
			logError("could not open stream \'" + currentFile + "\': " + ex.getMessage());
			return;
		}
		processFile();
	}

	public String getCurrentFile()
	{
		return currentFile;
	}

	public int getCurrentLine()
	{
		if (currentReader == null)
		{
			return 0;
		}
		return currentReader.getCurrentLine();
	}

	public int getCurrentColumn()
	{
		if (currentReader == null)
		{
			return 0;
		}
		return currentReader.getCurrentColumn();
	}

	/**
	 * Get a PrologTextLoaderError for the position the PrologTextLoader is at to
	 * use later if something goes wrong caused by an error here.
	 * 
	 * The message is null.
	 * 
	 * This is mainly intended for use during initialisation.
	 * 
	 * @see PrologTextLoaderState#addInitialization(PrologTextLoader,Term)
	 * 
	 * @return a partially filled in PrologTextLoaderError (missing a message)
	 */
	public PrologTextLoaderError getCurrentPartialLoaderError()
	{
		return new PrologTextLoaderError(getCurrentFile(), getCurrentLine(), getCurrentColumn(), null);
	}

	/**
	 * @return the prologTextLoaderState
	 */
	public PrologTextLoaderState getPrologTextLoaderState()
	{
		return prologTextLoaderState;
	}

	protected void processFile()
	{
		prologTextLoaderState.beforeProcessFile(this);
		while (currentReader != null)
		{
			Term term;
			try
			{
				term = currentReader.readTerm(operatorSet);
			}
			catch (ParseException ex)
			{
				// ex.printStackTrace();
				logError(ex);
				continue;
			}
			if (term == null) // if eof
			{
				processEof();
			}
			else if (term instanceof AtomTerm)
			{
				processClause(term);
			}
			else if (term instanceof CompoundTerm)
			{
				CompoundTerm cterm = (CompoundTerm) term;
				if (cterm.tag != TermConstants.directiveTag)
				{
					processClause(term);
				}
				else
				{
					if (!(cterm.args[0] instanceof CompoundTerm))
					{
						logError("invalid directive term");
						continue;
					}
					CompoundTerm dirTerm = (CompoundTerm) cterm.args[0];
					CompoundTermTag dirTag = dirTerm.tag;
					if (dirTag == includeTag)
					{
						processIncludeDirective(dirTerm.args[0]);
					}
					else if (dirTag == multifileTag)
					{
						processMultifileDirective(dirTerm.args[0]);
					}
					else if (dirTag == dynamicTag)
					{
						processDynamicDirective(dirTerm.args[0]);
					}
					else if (dirTag == discontiguousTag)
					{
						processDiscontiguousDirective(dirTerm.args[0]);
					}
					else if (dirTag == opTag)
					{
						processOpDirective(dirTerm.args[0], dirTerm.args[1], dirTerm.args[2]);
					}
					else if (dirTag == char_conversionTag)
					{
						processCharConversionDirective(dirTerm.args[0], dirTerm.args[1]);
					}
					else if (dirTag == initializationTag)
					{
						processInitializationDirective(dirTerm.args[0]);
					}
					else if (dirTag == ensure_loadedTag)
					{
						processEnsureLoadedDirective(dirTerm.args[0]);
					}
					else if (dirTag == set_prolog_flagTag)
					{
						processSetPrologFlagDirective(dirTerm.args[0], dirTerm.args[1]);
					}
					else if (dirTag == externalTag)
					{
						processExternalDirective(dirTerm.args[0], dirTerm.args[1]);
					}
					else if (dirTag == controlTag)
					{
						processControlDirective(dirTerm.args[0], dirTerm.args[1]);
					}
					else if (dirTag == build_inTag)
					{
						processBuildInDirective(dirTerm.args[0], dirTerm.args[1]);
					}
					else
					{// treat it as a goal to run at runtime not in ISO but common
						// (NONISO)
						processInitializationDirective(dirTerm);
					}
				}
			}
			else
			{
				logError("term is not a clause or directive");
			}
		}
		prologTextLoaderState.afterProcessFile(this);
	}

	protected void processSetPrologFlagDirective(Term arg0, Term arg1)
	{
		// logError("set_prolog_flag/2 directive was ignored");
		prologTextLoaderState.addInitialization(this, new CompoundTerm(set_prolog_flagTag, arg0, arg1));
	}

	protected void processBuildInDirective(Term pi, Term className)
	{
		if (!(className instanceof AtomTerm))
		{
			logError("class name should be atom term");
			return;
		}
		if (!(pi instanceof CompoundTerm))
		{
			logError("predicate indicator should be a compound term");
			return;
		}
		prologTextLoaderState.defineExternal(this, ((CompoundTerm) pi), ((AtomTerm) className).value,
				Predicate.TYPE.BUILD_IN);
	}

	protected void processControlDirective(Term pi, Term className)
	{
		if (!(className instanceof AtomTerm))
		{
			logError("class name should be atom term");
			return;
		}
		if (!(pi instanceof CompoundTerm))
		{
			logError("predicate indicator should be a compound term");
			return;
		}
		prologTextLoaderState.defineExternal(this, ((CompoundTerm) pi), ((AtomTerm) className).value,
				Predicate.TYPE.CONTROL);
	}

	protected void processExternalDirective(Term pi, Term className)
	{
		if (!(className instanceof AtomTerm))
		{
			logError("class name should be atom term");
			return;
		}
		if (!(pi instanceof CompoundTerm))
		{
			logError("predicate indicator should be a compound term");
			return;
		}
		prologTextLoaderState.defineExternal(this, ((CompoundTerm) pi), ((AtomTerm) className).value,
				Predicate.TYPE.EXTERNAL);
	}

	protected void processInitializationDirective(Term term)
	{
		prologTextLoaderState.addInitialization(this, term);
	}

	protected void processCharConversionDirective(Term from, Term to)
	{
		if (from instanceof AtomTerm)
		{
			if (((AtomTerm) from).value.length() != 1)
			{
				logError("Should be a single length atom");
				return;
			}
		}
		else if (from instanceof VariableTerm)
		{
			logError("instantiation error");
			return;
		}
		else
		{
			logError("representation error");
			return;
		}
		if (to instanceof AtomTerm)
		{
			if (((AtomTerm) to).value.length() != 1)
			{
				logError("Should be a single length atom");
				return;
			}
		}
		else if (to instanceof VariableTerm)
		{
			logError("instantiation error");
			return;
		}
		else
		{
			logError("representation error");
			return;
		}
		char cfrom = ((AtomTerm) from).value.charAt(0);
		char cto = ((AtomTerm) to).value.charAt(0);
		prologTextLoaderState.getConversionTable().setConversion(cfrom, cto);
	}

	protected void processOpDirective(Term priority, Term specifier, Term operatorAtom)
	{
		if (!(specifier instanceof AtomTerm))
		{
			logError("the specifier should be an atom term");
			return;
		}
		AtomTerm specifierAtom = (AtomTerm) specifier;
		if (!(priority instanceof IntegerTerm))
		{
			logError("the priority should be an integer term");
			return;
		}

		if (!(operatorAtom instanceof AtomTerm))
		{
			logError("the functor should be an atom term");
			return;
		}

		SPECIFIER spec = SPECIFIER.fromAtom(specifierAtom);
		if (spec == SPECIFIER.NONE)
		{
			logError("invalid operator specifier");
		}
		operatorSet.add(((IntegerTerm) priority).value, spec, ((AtomTerm) operatorAtom).value);
		prologTextLoaderState.addInitialization(this, new CompoundTerm(opTag, new Term[] { priority, specifier,
				operatorAtom }));
	}

	protected void processDiscontiguousDirective(Term pi)
	{
		if (!CompoundTermTag.isPredicateIndicator(pi))
		{
			logError("the predicate indicator is not valid.");
			return;
		}
		// pi is a CompoundTerm as isPredicateIndicator checks that
		CompoundTermTag tag = CompoundTermTag.get((CompoundTerm) pi);
		prologTextLoaderState.declareDiscontiguous(this, tag);
	}

	protected void processMultifileDirective(Term pi)
	{
		if (!CompoundTermTag.isPredicateIndicator(pi))
		{
			logError("the predicate indicator is not valid.");
			return;
		}
		// pi is a CompoundTerm as isPredicateIndicator checks that
		CompoundTermTag tag = CompoundTermTag.get((CompoundTerm) pi);
		prologTextLoaderState.declareMultifile(this, tag);
	}

	protected void processDynamicDirective(Term pi)
	{
		if (!CompoundTermTag.isPredicateIndicator(pi))
		{
			logError("the predicate indicator is not valid.");
			return;
		}
		// pi is a CompoundTerm as isPredicateIndicator checks that
		CompoundTermTag tag = CompoundTermTag.get((CompoundTerm) pi);
		prologTextLoaderState.declareDynamic(this, tag);
	}

	protected void processClause(Term argument)
	{
		prologTextLoaderState.addClause(this, argument);
	}

	protected void processIncludeDirective(Term argument)
	{
		try
		{
			prologTextLoaderState.beforeIncludeFile(this, argument);
			TermReader reader = new TermReader(new InputStreamReader(prologTextLoaderState.getInputStream(argument)),
					prologTextLoaderState.getEnvironment());
			readerStack.push(currentReader);
			fileStack.push(currentFile);
			currentReader = reader;
			currentFile = prologTextLoaderState.getInputName(argument);
		}
		catch (Exception ex)
		{
			logError("could not open datasource \'" + prologTextLoaderState.getInputName(argument) + "\': " + ex.getMessage());
			return;
		}
	}

	protected void processEnsureLoadedDirective(Term argument)
	{
		prologTextLoaderState.ensureLoaded(argument);
	}

	protected void processEof()
	{
		if (!fileStack.isEmpty())
		{
			prologTextLoaderState.afterIncludeFile(this);
			currentFile = null;
			try
			{
				currentReader.close();
			}
			catch (IOException ex)
			{
				logError("error during closing file: " + ex.getMessage());
			}
			currentFile = fileStack.pop();
			currentReader = readerStack.pop();
		}
		else
		{
			currentFile = null;
			currentReader = null;
		}
	}

	public void logError(String message)
	{
		prologTextLoaderState.logError(this, message);
	}

	public void logError(ParseException ex)
	{
		prologTextLoaderState.logError(this, ex);
	}
}
