/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2010       Daniel Thomas
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

import gnu.prolog.io.CharConversionTable;
import gnu.prolog.io.ParseException;
import gnu.prolog.io.TermWriter;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.HasEnvironment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Stores the state of all the {@link PrologTextLoader PrologTextLoaders} for
 * the {@link Environment} instance it is for.
 * 
 */
public class PrologTextLoaderState implements PrologTextLoaderListener, HasEnvironment
{
	protected Module module = null;
	protected final Map<Predicate, Map<String, Set<PrologTextLoader>>> predicate2options2loaders = new HashMap<Predicate, Map<String, Set<PrologTextLoader>>>();
	protected Predicate currentPredicate = null;
	private final Object currentPredicateLock = new Object();
	protected final List<PrologTextLoaderError> errorList = new ArrayList<PrologTextLoaderError>();// @Guarded-By
																																																	// errorList
	protected final Set<String> loadedFiles = new HashSet<String>();
	protected final CharConversionTable convTable = new CharConversionTable();
	protected final List<PrologTextLoaderListener> listeners = new ArrayList<PrologTextLoaderListener>();
	private final Environment environment;

	// arguments of ensure_loaded/1 and include/2 directive
	protected final static CompoundTermTag resourceTag = CompoundTermTag.get("resource", 1);
	protected final static CompoundTermTag urlTag = CompoundTermTag.get("url", 1);
	protected final static CompoundTermTag fileTag = CompoundTermTag.get("file", 1);
	public PrologTextLoaderState(Environment env, Module module)
	{
		environment = env;
		this.module = module;
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public List<PrologTextLoaderError> getErrors()
	{
		final ArrayList<PrologTextLoaderError> duplicate = new ArrayList<PrologTextLoaderError>();
		synchronized (errorList)
		{
			for (PrologTextLoaderError dup : errorList)
			{
				duplicate.add(dup);
			}
		}
		return duplicate;
	}

	public Module getModule()
	{
		return module;
	}

	/**
	 * @return the convTable
	 */
	public CharConversionTable getConversionTable()
	{
		return convTable;
	}

	protected boolean testOption(PrologTextLoader loader, Predicate p, String option)
	{
		synchronized (predicate2options2loaders)
		{
			Map<String, Set<PrologTextLoader>> options2loaders = predicate2options2loaders.get(p);
			if (options2loaders == null)
			{
				return false;
			}

			Set<PrologTextLoader> loaders = options2loaders.get(option);
			if (loaders == null)
			{
				return false;
			}
			if (loader != null && !loaders.contains(loader))
			{
				return false;
			}
			return true;
		}
	}

	protected void defineOption(PrologTextLoader loader, Predicate p, String option)
	{
		synchronized (predicate2options2loaders)
		{
			Map<String, Set<PrologTextLoader>> options2loaders = predicate2options2loaders.get(p);
			if (options2loaders == null)
			{
				options2loaders = new HashMap<String, Set<PrologTextLoader>>();
				predicate2options2loaders.put(p, options2loaders);
			}
			Set<PrologTextLoader> loaders = options2loaders.get(option);
			if (loaders == null)
			{
				loaders = new HashSet<PrologTextLoader>();
				options2loaders.put(option, loaders);
			}
			if (!loaders.contains(loader))
			{
				loaders.add(loader);
			}
		}
	}

	protected void defineOptionAndDeclare(PrologTextLoader loader, Predicate p, String option)
	{
		synchronized (predicate2options2loaders)
		{
			defineOption(loader, p, option);
			defineOption(loader, p, "declared");
		}
	}

	protected boolean isDeclaredInOtherLoaders(PrologTextLoader loader, Predicate p)
	{
		synchronized (predicate2options2loaders)
		{
			Map<String, Set<PrologTextLoader>> options2loaders = predicate2options2loaders.get(p);
			if (options2loaders == null)
			{
				return false;
			}

			Set<PrologTextLoader> loaders = options2loaders.get("declared");
			if (loaders == null || loaders.isEmpty())
			{
				return false;
			}

			Iterator<PrologTextLoader> i = loaders.iterator();
			while (i.hasNext())
			{
				if (loader != i.next())
				{
					return true;
				}
			}
			return false;
		}
	}

	public boolean declareDynamic(PrologTextLoader loader, CompoundTermTag tag)
	{
		Predicate p = module.getOrCreateDefinedPredicate(tag);
		if (testOption(loader, p, "dynamic"))
		{
			return true;
		}
		if (isDeclaredInOtherLoaders(loader, p))
		{
			if (!testOption(loader, p, "multifile"))
			{
				logError(loader, "non multifile predicate could not be changed in other prolog text.");
				return false;
			}
			if (!testOption(null, p, "dynamic"))
			{
				logError(loader,
						"predicate was not declared dynamic in other texts, dynamic option should be the same in each prolog text.");
				return false;
			}
		}
		else
		{
			if (testOption(loader, p, "defined"))
			{
				logError(loader, "predicate was already defined and could not be declared dynamic.");
				return false;
			}
		}
		if (p.getType() == Predicate.TYPE.UNDEFINED)
		{
			p.setType(Predicate.TYPE.USER_DEFINED);
		}
		p.setDynamic();
		defineOptionAndDeclare(loader, p, "dynamic");
		return true;
	}

	public boolean declareMeta(PrologTextLoader loader, CompoundTermTag tag, MetaPredicateInfo info)
	{
		Predicate p = module.getOrCreateDefinedPredicate(tag);
		if (p.getType() == Predicate.TYPE.UNDEFINED)
		{
			p.setType(Predicate.TYPE.USER_DEFINED);
		}
		p.setMeta(info);
		return true;
	}

	public void declareMultifile(PrologTextLoader loader, CompoundTermTag tag)
	{
		Predicate p = module.getOrCreateDefinedPredicate(tag);
		if (testOption(loader, p, "multifile"))
		{
			return;
		}
		if (isDeclaredInOtherLoaders(loader, p))
		{
			if (!testOption(null, p, "multifile"))
			{
				logError(loader, "non multifile predicate could not be changed in other prolog text.");
				return;
			}
		}
		else
		{
			if (testOption(loader, p, "defined"))
			{
				logError(loader, "predicate was already defined and could not be declared multifile.");
				return;
			}
		}
		if (p.getType() == Predicate.TYPE.UNDEFINED)
		{
			p.setType(Predicate.TYPE.USER_DEFINED);
		}
		defineOptionAndDeclare(loader, p, "multifile");
	}

	public void declareDiscontiguous(PrologTextLoader loader, CompoundTermTag tag)
	{
		Predicate p = module.getOrCreateDefinedPredicate(tag);
		if (testOption(loader, p, "discontiguous"))
		{
			return;
		}
		if (isDeclaredInOtherLoaders(loader, p))
		{
			if (!testOption(null, p, "multifile"))
			{
				logError(loader, "non multifile predicate could not be changed in other prolog text.");
				return;
			}
		}
		if (testOption(loader, p, "defined"))
		{
			logError(loader, "predicate was already defined and could not be declared discontiguous.");
			return;
		}
		if (p.getType() == Predicate.TYPE.UNDEFINED)
		{
			p.setType(Predicate.TYPE.USER_DEFINED);
		}
		defineOptionAndDeclare(loader, p, "discontiguous");
	}

	public void addClause(PrologTextLoader loader, Term term)
	{
		Term head = term;
		CompoundTermTag headTag;
		if (term instanceof CompoundTerm && ((CompoundTerm) term).tag == TermConstants.clauseTag)
		{
			head = ((CompoundTerm) term).args[0];
		}
		if (head instanceof AtomTerm)
		{
			headTag = CompoundTermTag.get((AtomTerm) head, 0);
		}
		else if (head instanceof CompoundTerm)
		{
			headTag = ((CompoundTerm) head).tag;
		}
		else
		{
			logError(loader, "predicate head is not a callable term.");
			return;
		}

		synchronized (currentPredicateLock)
		{
			if (currentPredicate == null || headTag != currentPredicate.getTag())
			{
				currentPredicate = null;
				Predicate p = module.getOrCreateDefinedPredicate(headTag);
				if (testOption(loader, p, "defined") && !testOption(loader, p, "discontiguous"))
				{
					logError(loader, "predicate is not discontiguous.");
					return;
				}
				if (!testOption(loader, p, "declared") && testOption(null, p, "declared")
						&& !testOption(loader, p, "multifile"))
				{
					logError(loader, "predicate is not multifile: " + p.getTag());
					return;
				}
				if (!testOption(loader, p, "dynamic") && testOption(null, p, "dynamic"))
				{
					logError(loader, "predicate is not declared dynamic in this prolog text.");
					return;
				}
				currentPredicate = p;
				if (!testOption(loader, p, "defined"))
				{
					if (p.getType() == Predicate.TYPE.UNDEFINED)
					{
						p.setType(Predicate.TYPE.USER_DEFINED);
					}
					defineOptionAndDeclare(loader, p, "defined");
				}
			}
			try
			{
				currentPredicate.addClauseLast(Predicate.prepareClause(term));
			}
			catch (PrologException ex)
			{
				logError(loader, ex.getMessage());
			}
		}

	}

	public void defineExternal(PrologTextLoader loader, CompoundTerm pi, String javaClassName, Predicate.TYPE type)
	{
		if (!CompoundTermTag.isPredicateIndicator(pi))
		{
			logError(loader, "predicate indicator is not valid.");
			return;
		}
		CompoundTermTag tag = CompoundTermTag.get(pi);
		Predicate p = module.getOrCreateDefinedPredicate(tag);
		if (p.getType() != Predicate.TYPE.UNDEFINED)
		{
			logError(loader, "predicate type could not be changed.");
			return;
		}
		p.setType(type);
		p.setJavaClassName(javaClassName);
		defineOptionAndDeclare(loader, p, "defined");
	}

	public void logError(PrologTextLoader loader, ParseException ex)
	{
		synchronized (errorList)
		{
			errorList.add(new PrologTextLoaderError(loader, ex));
		}
	}

	public void logError(PrologTextLoader loader, String message)
	{
		synchronized (errorList)
		{
			errorList.add(new PrologTextLoaderError(loader, message));
		}
	}

	/**
	 * To be used for errors during initialisation
	 * 
	 * @see #logError(PrologTextLoader,String)
	 * @see Environment#runInitialization(Interpreter)
	 * 
	 * @param partialError
	 *          the partially filled in error (missing message)
	 * @param message
	 *          the message to add
	 */
	public void logError(PrologTextLoaderError partialError, String message)
	{
		synchronized (errorList)
		{
			errorList.add(new PrologTextLoaderError(partialError, message));
		}
	}

	public void startNewModule(PrologTextLoader loader, AtomTerm moduleName, Term list) throws PrologException
	{
		List<CompoundTermTag> exports = new ArrayList<CompoundTermTag>();
		while ((list = list.dereference()) instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm)list;
			if (ct.tag != TermConstants.listTag)
			{
				PrologException.typeError(TermConstants.listAtom, ct);
			}
			else
			{
				Term head = ct.args[0];
				if (head instanceof CompoundTerm && ((CompoundTerm)head).tag == CompoundTermTag.get("/", 2))
				{
					CompoundTerm predicateIndicator = (CompoundTerm)head;
					if (predicateIndicator.args[0] instanceof AtomTerm && predicateIndicator.args[1] instanceof IntegerTerm)
					{
						exports.add(CompoundTermTag.get(predicateIndicator));
					}
					else
					{
						PrologException.typeError(TermConstants.predicateIndicatorAtom, predicateIndicator);
					}
				}
			}
			list = ct.args[1];
		}
		if (!TermConstants.emptyListAtom.equals(list))
		{
			PrologException.typeError(TermConstants.listAtom, list);
		}
		module = environment.startNewModule(moduleName, exports);
		currentPredicate = null;
	}

	public void addInitialization(PrologTextLoader loader, Term term)
	{
		module.addInitialization(loader.getCurrentPartialLoaderError(), term);
	}

	public void ensureLoaded(Term term)
	{
		String inputName = getInputName(term);
		synchronized (loadedFiles)
		{
			if (!loadedFiles.contains(inputName))
			{
				loadedFiles.add(inputName);
				// Instead of this I suppose we could have a module-stack-stack?
				// When we load a new file we push the current stack and pop the stack from the stack-stack afterwards...
				Stack<AtomTerm> currentModuleStack = environment.cloneModuleStack();
				try
				{
					new PrologTextLoader(this, term);
				}
				finally
				{
					environment.restoreModuleStack(currentModuleStack);
					module = environment.getModule();
				}
			}
		}
	}

	/**
	 * Resolve the input filename. Will add a .pl or .pro when needed.
	 * 
	 * @param filename
	 * @return the file object resolved from the filename
	 */
	protected File resolveInputFile(String filename)
	{
		File fl = new File(filename);
		if (fl.exists())
		{
			return fl;
		}
		if (!(filename.endsWith(".pl") || filename.endsWith(".pro")))
		{
			fl = new File(filename + ".pro");
			if (fl.exists())
			{
				return fl;
			}
			fl = new File(filename + ".pl");
			if (fl.exists())
			{
				return fl;
			}
		}
		return new File(filename);// reset here as we might have added a .pl
	}

	protected String getInputName(Term term)
	{
		if (term instanceof AtomTerm) // argument is an atom, which is an filename
		{
			return resolveInputFile(((AtomTerm) term).value).toString();
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			if (ct.tag == fileTag)
			{
				if (ct.args[0] instanceof AtomTerm)
				{
					return resolveInputFile(getInputName(ct.args[0])).toString();
				}
			}
			else if (ct.tag == urlTag || ct.tag == resourceTag)
			{
				if (ct.args[0] instanceof AtomTerm)
				{
					AtomTerm arg = (AtomTerm) ct.args[0];
					if (ct.tag == urlTag)
					{
						return "url:" + arg.value;
					}
					else
					// resource tag
					{
						return "resource:" + arg.value;
					}
				}
			}
		}
		return "bad_input(" + TermWriter.toString(term) + ")";
	}

	protected InputStream getInputStream(Term term) throws IOException
	{
		if (term instanceof AtomTerm) // argument is an atom, which is an filename
		{
			return new FileInputStream(resolveInputFile(((AtomTerm) term).value));
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			if (ct.tag == fileTag)
			{
				if (!(ct.args[0] instanceof AtomTerm))
				{
					throw new IOException("unknown type of datasource");
				}
				return getInputStream(ct.args[0]);
			}
			else if (ct.tag == urlTag || ct.tag == resourceTag)
			{
				URL url;
				if (!(ct.args[0] instanceof AtomTerm))
				{
					throw new IOException("unknown type of datasource");
				}
				AtomTerm arg = (AtomTerm) ct.args[0];
				if (ct.tag == urlTag)
				{
					url = new URL(arg.value);
				}
				else
				// resource tag
				{
					url = getClass().getResource(arg.value);
					if (url == null)
					{
						throw new IOException("resource not found");
					}
				}
				return url.openStream();
			}
		}
		throw new IOException("unknown type of datasource");
	}

	public boolean addPrologTextLoaderListener(PrologTextLoaderListener listener)
	{
		if (listener == null || listener == this)
		{
			return false;
		}
		synchronized (listeners)
		{
			return listeners.add(listener);
		}
	}

	public boolean removePrologTextLoaderListener(PrologTextLoaderListener listener)
	{
		synchronized (listeners)
		{
			return listeners.remove(listener);
		}
	}

	public void afterIncludeFile(PrologTextLoader loader)
	{
		synchronized (listeners)
		{
			for (PrologTextLoaderListener listener : listeners)
			{
				listener.afterIncludeFile(loader);
			}
		}
	}

	public void afterProcessFile(PrologTextLoader loader)
	{
		synchronized (listeners)
		{
			for (PrologTextLoaderListener listener : listeners)
			{
				listener.afterProcessFile(loader);
			}
		}
	}

	public void beforeIncludeFile(PrologTextLoader loader, Term argument)
	{
		synchronized (listeners)
		{
			for (PrologTextLoaderListener listener : listeners)
			{
				listener.beforeIncludeFile(loader, argument);
			}
		}
	}

	public void beforeProcessFile(PrologTextLoader loader)
	{
		synchronized (listeners)
		{
			for (PrologTextLoaderListener listener : listeners)
			{
				listener.beforeProcessFile(loader);
			}
		}
	}

}
