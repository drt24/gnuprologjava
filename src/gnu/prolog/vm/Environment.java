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

import gnu.prolog.Version;
import gnu.prolog.database.Module;
import gnu.prolog.database.Pair;
import gnu.prolog.database.Predicate;
import gnu.prolog.database.PredicateListener;
import gnu.prolog.database.PredicateUpdatedEvent;
import gnu.prolog.database.PrologTextLoaderError;
import gnu.prolog.database.PrologTextLoaderState;
import gnu.prolog.io.BinaryPrologStream;
import gnu.prolog.io.CharConversionTable;
import gnu.prolog.io.OperatorSet;
import gnu.prolog.io.PrologStream;
import gnu.prolog.io.TextInputPrologStream;
import gnu.prolog.io.TextOutputPrologStream;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.interpreter.InterpretedCodeCompiler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * this class represent prolog processor.
 */
public class Environment implements PredicateListener
{

	protected OperatorSet opSet = new OperatorSet();
	/** current state of loaded database */
	protected PrologTextLoaderState prologTextLoaderState;
	/** predicate which used instead of real code when predicate is not defined */
	protected PrologCode undefinedPredicate;
	/** PredicateTag to code mapping */
	protected Map<CompoundTermTag, PrologCode> tag2code = new HashMap<CompoundTermTag, PrologCode>();

	// TODO move into TermConstants, possibly consider using enums.
	// flag atoms
	public final static AtomTerm boundedAtom = AtomTerm.get("bounded");
	public final static AtomTerm integerRoundingFunctionAtom = AtomTerm.get("integer_rounding_function");
	public final static AtomTerm downAtom = AtomTerm.get("down");
	public final static AtomTerm towardZeroAtom = AtomTerm.get("toward_zero");
	public final static AtomTerm charConversionAtom = AtomTerm.get("char_conversion");
	public final static AtomTerm onAtom = AtomTerm.get("on");
	public final static AtomTerm offAtom = AtomTerm.get("off");
	public final static AtomTerm debugAtom = AtomTerm.get("debug");
	public final static AtomTerm unknownAtom = AtomTerm.get("unknown");
	public final static AtomTerm errorAtom = AtomTerm.get("error");
	public final static AtomTerm warningAtom = AtomTerm.get("warning");
	public final static AtomTerm doubleQuotesAtom = AtomTerm.get("double_quotes");

	/**
	 * The possible values of the {@link #doubleQuotesAtom double_quotes flag}
	 * 
	 * @author Daniel Thomas
	 */
	public static enum DoubleQuotesValue implements HasAtom
	{
		DQ_CODES
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.codesAtom;
			}
		},
		DQ_CHARS
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.charsAtom;
			}
		},
		DQ_ATOM
		{
			@Override
			public AtomTerm getAtom()
			{
				return TermConstants.atomAtom;
			}
		};

		/**
		 * @return the AtomTerm for this value for the double_quotes flag.
		 */
		public abstract AtomTerm getAtom();

		/**
		 * @param value
		 *          the AtomTerm to be converted into a DoubleQuotesValue
		 * @return the DoubleQuotesValue for the value or null if it does not match.
		 */
		public static DoubleQuotesValue fromAtom(AtomTerm value)
		{
			if (TermConstants.codesAtom == value)
			{
				return DQ_CODES;
			}
			else if (TermConstants.charsAtom == value)
			{
				return DQ_CHARS;
			}
			else if (TermConstants.atomAtom == value)
			{
				return DQ_ATOM;
			}
			return null;
		}

		/**
		 * @return the default value for the {@link #doubleQuotesAtom double_quotes
		 *         flag}.
		 */
		public static DoubleQuotesValue getDefault()
		{
			return DQ_CODES;
		}
	}

	public final static AtomTerm dialectAtom = AtomTerm.get("dialect");
	public final static AtomTerm versionAtom = AtomTerm.get("version");
	// integer terms
	public final static IntegerTerm maxIntegerTerm = IntegerTerm.get(Integer.MAX_VALUE);
	public final static IntegerTerm minIntegerTerm = IntegerTerm.get(Integer.MIN_VALUE);
	// The identifier string for this prolog engine
	public final static AtomTerm dialectTerm = AtomTerm.get("gnuprologjava");
	// the version
	public final static IntegerTerm versionTerm = IntegerTerm.get(Version.intEncoded());

	public final static AtomTerm prologFlagAtom = AtomTerm.get("prolog_flag");
	public final static AtomTerm flagValueAtom = AtomTerm.get("flag_value");
	public final static AtomTerm modifyAtom = AtomTerm.get("modify");
	public final static CompoundTermTag plusTag = CompoundTermTag.get("+", 2);
	/** atom to flag */
	protected Map<AtomTerm, Term> atom2flag = new HashMap<AtomTerm, Term>();
	protected Set<AtomTerm> changableFlags = new HashSet<AtomTerm>();

	/** constructor of environment, it loads buildins to database at start. */
	public Environment()
	{
		this(null, null);
	}

	public Environment(InputStream stdin, OutputStream stdout)
	{
		createTextLoader();
		initEnvironment();
		initStreams(stdin, stdout);
	}

	/**
	 * Initialize the environment
	 */
	protected void initEnvironment()
	{
		// load builtins
		CompoundTerm term = new CompoundTerm(AtomTerm.get("resource"), new Term[] { AtomTerm
				.get("/gnu/prolog/vm/buildins/buildins.pro") });
		ensureLoaded(term);
		// set flags for environment
		createNewPrologFlag(boundedAtom, TermConstants.trueAtom, false);
		createNewPrologFlag(TermConstants.maxIntegerAtom, maxIntegerTerm, false);
		createNewPrologFlag(TermConstants.minIntegerAtom, minIntegerTerm, false);
		createNewPrologFlag(integerRoundingFunctionAtom, downAtom, false);
		createNewPrologFlag(charConversionAtom, offAtom, true);
		createNewPrologFlag(debugAtom, offAtom, true);
		// we can't have a Term with an arity higher than the available memory
		long maxMemory = Runtime.getRuntime().maxMemory();
		IntegerTerm maxArity = (maxMemory < maxIntegerTerm.value) ? IntegerTerm.get((int) maxMemory) : maxIntegerTerm;
		createNewPrologFlag(TermConstants.maxArityAtom, maxArity, false);
		createNewPrologFlag(unknownAtom, errorAtom, true);
		createNewPrologFlag(doubleQuotesAtom, DoubleQuotesValue.getDefault().getAtom(), true);
		createNewPrologFlag(dialectAtom, dialectTerm, false);
		createNewPrologFlag(versionAtom, versionTerm, false);

		EnvInitializer.runInitializers(this);
	}

	protected void createTextLoader()
	{
		prologTextLoaderState = new PrologTextLoaderState(this);
	}

	@Deprecated
	public PrologTextLoaderState getTextLoaderState()
	{
		return prologTextLoaderState;
	}

	/**
	 * true if the environment is currently initialized
	 * 
	 * @return if the environment is currently initialized
	 */
	public boolean isInitialized()
	{
		return getModule().getInitialization().size() == 0;
	}

	/**
	 * Run the initialization.
	 * 
	 * This executes any goals loaded into the initailization list by the
	 * :-initialization(Goal). directive or by the use of the NONISO abbreviation
	 * :- Goal.
	 * 
	 * This should be run after {@link #ensureLoaded(Term)} with the
	 * {@link Interpreter} obtained from {@link #createInterpreter()}.
	 * 
	 * @param interpreter
	 */
	public void runInitialization(Interpreter interpreter)
	{
		Module module = getModule();
		List<Pair<PrologTextLoaderError, Term>> initialization;
		synchronized (module)
		{// get the initialization list and then clear it so that it is no longer
			// referenced from module so that it will not be modified while we are
			// processing it
			initialization = module.getInitialization();
			module.clearInitialization();
		}
		for (Pair<PrologTextLoaderError, Term> loaderTerm : initialization)
		{
			Term term = loaderTerm.right;
			try
			{
				Interpreter.Goal goal = interpreter.prepareGoal(term);
				int rc = interpreter.execute(goal);
				if (rc == PrologCode.SUCCESS)
				{
					interpreter.stop(goal);
				}
				else if (rc != PrologCode.SUCCESS_LAST)
				{
					prologTextLoaderState.logError(loaderTerm.left, "Goal Failed: " + term);
				}
			}
			catch (PrologException ex)
			{
				prologTextLoaderState.logError(loaderTerm.left, ex.getMessage());
			}
		}
	}

	/**
	 * get copy of current state of flags for this environment
	 * 
	 * @return copy of current state of flags for this environment
	 */
	public synchronized Map<AtomTerm, Term> getPrologFlags()
	{
		return new HashMap<AtomTerm, Term>(atom2flag);
	}

	/**
	 * get flag for this environment
	 * 
	 * @param term
	 *          the flag to get the value of
	 * @return the value of the flag
	 */
	public synchronized Term getPrologFlag(AtomTerm term)
	{
		return atom2flag.get(term);
	}

	/**
	 * create a new flag for this environment
	 * 
	 * @param flag
	 *          the flag to add
	 * @param newValue
	 *          the value of the flag
	 * @param changable
	 *          whether the flag's value can be changed
	 */
	protected synchronized void createNewPrologFlag(AtomTerm flag, Term newValue, boolean changable)
	{
		atom2flag.put(flag, newValue);
		if (changable)
		{
			changableFlags.add(flag);
		}
	}

	public synchronized void setPrologFlag(AtomTerm flag, Term newValue) throws PrologException
	{
		Term value = atom2flag.get(flag);
		if (value == null)
		{
			PrologException.domainError(prologFlagAtom, flag);
		}
		if (flag == boundedAtom)
		{
			if (newValue != TermConstants.trueAtom && newValue != TermConstants.falseAtom)
			{
				PrologException.domainError(flagValueAtom, new CompoundTerm(plusTag, flag, newValue));
			}
		}
		else if (flag == TermConstants.maxIntegerAtom)
		{
			if (!(newValue instanceof IntegerTerm))
			{
				PrologException.domainError(prologFlagAtom, new CompoundTerm(plusTag, flag, newValue));
			}
		}
		else if (flag == TermConstants.minIntegerAtom)
		{
			if (!(newValue instanceof IntegerTerm))
			{
				PrologException.domainError(prologFlagAtom, new CompoundTerm(plusTag, flag, newValue));
			}
		}
		else if (flag == integerRoundingFunctionAtom)
		{
			if (newValue != downAtom && newValue != towardZeroAtom)
			{
				PrologException.domainError(flagValueAtom, new CompoundTerm(plusTag, flag, newValue));
			}
		}
		else if (flag == charConversionAtom)
		{
			if (newValue != onAtom && newValue != offAtom)
			{
				PrologException.domainError(flagValueAtom, new CompoundTerm(plusTag, flag, newValue));
			}
		}
		else if (flag == debugAtom)
		{
			if (newValue != onAtom && newValue != offAtom)
			{
				PrologException.domainError(flagValueAtom, new CompoundTerm(plusTag, flag, newValue));
			}
		}
		else if (flag == TermConstants.maxArityAtom)
		{
			if (!(newValue instanceof IntegerTerm))
			{
				PrologException.domainError(prologFlagAtom, new CompoundTerm(plusTag, flag, newValue));
			}
		}
		else if (flag == unknownAtom)
		{
			if (newValue != errorAtom && newValue != TermConstants.failAtom && newValue != warningAtom)
			{
				PrologException.domainError(flagValueAtom, new CompoundTerm(plusTag, flag, newValue));
			}
		}
		else if (flag == doubleQuotesAtom)
		{
			if (!(newValue instanceof AtomTerm) || ((DoubleQuotesValue.fromAtom((AtomTerm) newValue)) == null))
			{
				PrologException.domainError(flagValueAtom, new CompoundTerm(plusTag, flag, newValue));
			}
		}
		if (!changableFlags.contains(flag))
		{
			PrologException.permissionError(modifyAtom, TermConstants.flagAtom, flag);
		}
		atom2flag.put(flag, newValue);
	}

	public List<PrologTextLoaderError> getLoadingErrors()
	{
		return prologTextLoaderState.getErrors();
	}

	public PrologTextLoaderState getPrologTextLoaderState()
	{
		return prologTextLoaderState;
	}

	/**
	 * Ensure that prolog text designated by term is loaded
	 * 
	 * You must use {@link #runInitialization(Interpreter)} after using this and
	 * before expecting answers.
	 * 
	 * @param term
	 * 
	 * @see gnu.prolog.vm.buildins.io.Predicate_ensure_loaded
	 * */
	public synchronized void ensureLoaded(Term term)
	{
		prologTextLoaderState.ensureLoaded(term);
	}

	/**
	 * create interpreter for this environment
	 * 
	 * Use this to create different {@link Interpreter Interpreters} for different
	 * threads.
	 * 
	 * @return an interpreter for this environment.
	 */
	public Interpreter createInterpreter()
	{
		return new Interpreter(this);
	}

	public Module getModule()
	{
		return prologTextLoaderState.getModule();
	}

	/**
	 * load code for prolog
	 * 
	 * @param tag
	 *          the tag of the {@link PrologCode} to load
	 * @return the loaded PrologCode
	 * @throws PrologException
	 */
	public synchronized PrologCode loadPrologCode(CompoundTermTag tag) throws PrologException
	{
		// simple variant, later I will need to add compilation.
		Predicate p = prologTextLoaderState.getModule().getDefinedPredicate(tag);
		if (p == null) // case of undefined predicate
		{
			return getUndefinedPredicateCode(tag);
		}
		switch (p.getType())
		{
			case CONTROL:
				// really only call should be loaded in this way
			case BUILD_IN:
			case EXTERNAL:
			{
				try
				{
					Class<?> cls = Class.forName(p.getJavaClassName());
					PrologCode code = (PrologCode) cls.newInstance();
					code.install(this);
					return code;
				}
				catch (/* ClassNotFound */Exception ex)
				// Maybe it will be needed to separate different cases later
				{
					ex.printStackTrace();
					return getUndefinedPredicateCode(tag);
				}
			}
			case USER_DEFINED:
			{
				PrologCode code = InterpretedCodeCompiler.compile(p.getClauses());
				code.install(this);
				return code;
			}
			default:
				return getUndefinedPredicateCode(tag);
		}
	}

	/**
	 * get undefined predicate code
	 * 
	 * @param tag
	 * @return undefined predicate code for the tag
	 */
	public PrologCode getUndefinedPredicateCode(CompoundTermTag tag)
	{
		return new UndefinedPredicateCode(tag);
	}

	/**
	 * get prolog code
	 * 
	 * @param tag
	 * @return the {@link PrologCode} for the tag
	 * @throws PrologException
	 */
	public synchronized PrologCode getPrologCode(CompoundTermTag tag) throws PrologException
	{
		PrologCode code = tag2code.get(tag);
		if (code == null)
		{
			code = loadPrologCode(tag);
			tag2code.put(tag, code);
		}
		return code;
	}

	protected Map<CompoundTermTag, List<PrologCodeListenerRef>> tag2listeners = new HashMap<CompoundTermTag, List<PrologCodeListenerRef>>();
	protected ReferenceQueue<? super PrologCodeListener> prologCodeListenerReferenceQueue = new ReferenceQueue<PrologCodeListener>();

	protected void pollPrologCodeListeners()
	{
		PrologCodeListenerRef ref;
		while (null != (ref = (PrologCodeListenerRef) prologCodeListenerReferenceQueue.poll()))
		{
			List<PrologCodeListenerRef> list = tag2listeners.get(ref.tag);
			list.remove(ref);
		}
	}

	/**
	 * A {@link WeakReference} to a {@link PrologCodeListener}
	 * 
	 */
	private static class PrologCodeListenerRef extends WeakReference<PrologCodeListener>
	{
		PrologCodeListenerRef(ReferenceQueue<? super PrologCodeListener> queue, PrologCodeListener listener,
				CompoundTermTag tag)
		{
			super(listener, queue);
			this.tag = tag;
		}

		CompoundTermTag tag;
	}

	// this functionality will be needed later, but I need to think more ;-)
	/**
	 * add prolog code listener
	 * 
	 * @param tag
	 * @param listener
	 */
	public synchronized void addPrologCodeListener(CompoundTermTag tag, PrologCodeListener listener)
	{
		pollPrologCodeListeners();
		List<PrologCodeListenerRef> list = tag2listeners.get(tag);
		if (list == null)
		{
			list = new ArrayList<PrologCodeListenerRef>();
			tag2listeners.put(tag, list);
		}
		list.add(new PrologCodeListenerRef(prologCodeListenerReferenceQueue, listener, tag));
	}

	/**
	 * remove prolog code listener
	 * 
	 * @param tag
	 * @param listener
	 */
	public synchronized void removePrologCodeListener(CompoundTermTag tag, PrologCodeListener listener)
	{
		pollPrologCodeListeners();
		List<PrologCodeListenerRef> list = tag2listeners.get(tag);
		if (list != null)
		{
			ListIterator<PrologCodeListenerRef> i = list.listIterator();
			while (i.hasNext())
			{
				PrologCodeListenerRef ref = i.next();
				PrologCodeListener lst = ref.get();
				if (lst == null)
				{
					i.remove();
				}
				else if (lst == listener)
				{
					i.remove();
					return;
				}
			}
		}
	}

	public synchronized void predicateUpdated(PredicateUpdatedEvent evt)
	{
		Object code = tag2code.remove(evt.getTag());
		pollPrologCodeListeners();
		if (code == null) // if code was not loaded yet
		{
			return;
		}
		CompoundTermTag tag = evt.getTag();
		List<PrologCodeListenerRef> list = tag2listeners.get(tag);
		if (list != null)
		{
			PrologCodeUpdatedEvent uevt = new PrologCodeUpdatedEvent(this, tag);
			ListIterator<PrologCodeListenerRef> i = list.listIterator();
			while (i.hasNext())
			{
				PrologCodeListenerRef ref = i.next();
				PrologCodeListener lst = ref.get();
				if (lst == null)
				{
					i.remove();
				}
				else
				{
					lst.prologCodeUpdated(uevt);
					return;
				}
			}
		}
	}

	private static InputStream defaultInputStream;
	private static OutputStream defaultOutputStream;

	/**
	 * @return the defaultInputStream
	 */
	public static InputStream getDefaultInputStream()
	{
		return defaultInputStream == null ? System.in : defaultInputStream;
	}

	/**
	 * @param defaultInputStream
	 *          the defaultInputStream to set
	 */
	public static void setDefaultInputStream(InputStream defaultInputStream)
	{
		Environment.defaultInputStream = defaultInputStream;
	}

	/**
	 * @return the defaultOutputStream
	 */
	public static OutputStream getDefaultOutputStream()
	{
		return defaultOutputStream == null ? System.out : defaultOutputStream;
	}

	/**
	 * @param defaultOutputStream
	 *          the defaultOutputStream to set
	 */
	public static void setDefaultOutputStream(OutputStream defaultOutputStream)
	{
		Environment.defaultOutputStream = defaultOutputStream;
	}

	// IO support
	protected PrologStream userInput;
	protected PrologStream userOutput;
	protected PrologStream currentInput;
	protected PrologStream currentOutput;

	protected List<PrologStream> openStreams = new ArrayList<PrologStream>();
	protected Map<AtomTerm, PrologStream> alias2stream = new HashMap<AtomTerm, PrologStream>();

	public OperatorSet getOperatorSet()
	{
		return opSet;
	}

	protected void initStreams(InputStream stdin, OutputStream stdout)
	{
		try
		{
			PrologStream.OpenOptions inops = new PrologStream.OpenOptions(PrologStream.userInputAtom, PrologStream.readAtom,
					this);
			PrologStream.OpenOptions outops = new PrologStream.OpenOptions(PrologStream.userOutputAtom,
					PrologStream.appendAtom, this);
			inops.aliases.add(PrologStream.userInputAtom);
			inops.eofAction = PrologStream.resetAtom;
			inops.reposition = TermConstants.falseAtom;
			inops.type = PrologStream.textAtom;
			outops.aliases.add(PrologStream.userOutputAtom);
			outops.eofAction = PrologStream.resetAtom;
			outops.reposition = TermConstants.falseAtom;
			outops.type = PrologStream.textAtom;
			userInput = new TextInputPrologStream(inops, new InputStreamReader(stdin == null ? getDefaultInputStream()
					: stdin));
			userOutput = new TextOutputPrologStream(outops, new OutputStreamWriter(stdout == null ? getDefaultOutputStream()
					: stdout));
			setCurrentInput(getUserInput());
			setCurrentOutput(getUserOutput());
			alias2stream.put(PrologStream.userOutputAtom, userOutput);
			alias2stream.put(PrologStream.userInputAtom, userInput);
			openStreams.add(userInput);
			openStreams.add(userOutput);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new IllegalStateException("unable to intialize standard streams");
		}
	}

	public synchronized PrologStream getUserInput() throws PrologException
	{
		return userInput;
	}

	public synchronized PrologStream getUserOutput() throws PrologException
	{
		return userOutput;
	}

	public synchronized PrologStream getCurrentInput() throws PrologException
	{
		return currentInput;
	}

	public synchronized PrologStream getCurrentOutput() throws PrologException
	{
		return currentOutput;
	}

	public synchronized void setCurrentInput(PrologStream stream) throws PrologException
	{
		currentInput = stream;
	}

	public synchronized void setCurrentOutput(PrologStream stream) throws PrologException
	{
		currentOutput = stream;
	}

	public synchronized Map<PrologStream, List<Term>> getStreamProperties() throws PrologException
	{
		Map<PrologStream, List<Term>> map = new HashMap<PrologStream, List<Term>>();
		Iterator<PrologStream> srt = openStreams.iterator();
		while (srt.hasNext())
		{
			PrologStream stream = srt.next();
			List<Term> list = new ArrayList<Term>();
			stream.getProperties(list);
			map.put(stream, list);
		}
		return map;
	}

	public synchronized PrologStream resolveStream(Term stream_or_alias) throws PrologException
	{
		stream_or_alias = stream_or_alias.dereference();
		if (stream_or_alias instanceof VariableTerm)
		{
			PrologException.instantiationError();
		}
		else if (stream_or_alias instanceof AtomTerm)
		{
			PrologStream stream = alias2stream.get(stream_or_alias);
			if (stream == null)
			{
				PrologException.existenceError(PrologStream.streamAtom, stream_or_alias);
			}
			else
			{
				stream.checkExists();
			}
			return stream;
		}
		else if (stream_or_alias instanceof JavaObjectTerm)
		{
			JavaObjectTerm jt = (JavaObjectTerm) stream_or_alias;
			if (!(jt.value instanceof PrologStream))
			{
				PrologException.domainError(PrologStream.streamOrAliasAtom, stream_or_alias);
			}
			PrologStream stream = (PrologStream) jt.value;
			if (stream.isClosed())
			{
				// TODO: put this into a proper debugging framework
				// String info = stream.filename.value + ":" + stream.getCurrentLine() +
				// ":" + stream.getCurrentColumn()
				// + ": Stream closed";
				// System.err.println(info);

				PrologException.existenceError(PrologStream.streamAtom, stream_or_alias);
			}
			return stream;
		}
		else
		{
			PrologException.domainError(PrologStream.streamOrAliasAtom, stream_or_alias);
		}
		return null;
	}

	public synchronized Term open(AtomTerm source_sink, AtomTerm mode, PrologStream.OpenOptions options)
			throws PrologException
	{

		Iterator<AtomTerm> ia = options.aliases.iterator();
		while (ia.hasNext())
		{
			AtomTerm a = ia.next();
			if (alias2stream.get(a) != null)
			{
				PrologException.permissionError(PrologStream.openAtom, PrologStream.sourceSinkAtom, new CompoundTerm(
						PrologStream.aliasTag, a));

			}
		}
		PrologStream stream = null;
		if (options.type == PrologStream.binaryAtom)
		{
			stream = new BinaryPrologStream(source_sink, mode, options);
			// PrologException.systemError(); // add handling of this later
		}
		else if (options.type == PrologStream.textAtom)
		{
			boolean randAccess = options.reposition == TermConstants.trueAtom;
			// if (options.reposition == PrologStream.TermConstants.trueAtom)
			// {
			// PrologException.permissionError(PrologStream.openAtom,
			// PrologStream.sourceSinkAtom, new CompoundTerm(
			// PrologStream.repositionTag, PrologStream.TermConstants.trueAtom));
			// }
			if (options.mode == PrologStream.readAtom)
			{
				if (!new File(source_sink.value).exists())
				{
					PrologException.existenceError(PrologStream.sourceSinkAtom, source_sink);
				}
				try
				{
					if (randAccess)
					{
						RandomAccessFile raf = new RandomAccessFile(source_sink.value, "r");
						stream = new TextInputPrologStream(options, raf);
					}
					else
					{
						Reader rd = new FileReader(source_sink.value);
						stream = new TextInputPrologStream(options, rd);
					}
				}
				catch (IOException ex)
				{
					PrologException.permissionError(PrologStream.openAtom, PrologStream.sourceSinkAtom, source_sink);
				}
			}
			else
			{
				boolean append = options.mode == PrologStream.appendAtom;
				try
				{
					if (randAccess)
					{
						RandomAccessFile raf = new RandomAccessFile(source_sink.value, "rw");
						stream = new TextOutputPrologStream(options, raf);
					}
					else
					{
						Writer wr = new FileWriter(source_sink.value, append);
						stream = new TextOutputPrologStream(options, wr);
					}
				}
				catch (IOException ex)
				{
					PrologException.permissionError(PrologStream.openAtom, PrologStream.sourceSinkAtom, source_sink);
				}
			}
		}
		else
		{
			PrologException.domainError(AtomTerm.get("invalid_stream_type"), options.type);
		}

		ia = options.aliases.iterator();
		while (ia.hasNext())
		{
			AtomTerm a = ia.next();
			alias2stream.put(a, stream);
		}
		openStreams.add(stream);
		return stream.getStreamTerm();
	}

	/**
	 * 
	 * @param stream
	 * @return true if we closed and false if we did not close.
	 * @throws PrologException
	 */
	public synchronized boolean close(PrologStream stream) throws PrologException
	{
		if (stream == userInput)
		{
			return false;
		}
		if (stream == userOutput)
		{
			userOutput.flushOutput(null);
			return false;
		}
		for (AtomTerm alias : stream.getAliases())
		{
			alias2stream.remove(alias);
		}
		openStreams.remove(stream);
		if (currentInput == stream)
		{
			currentInput = userInput;
		}
		if (currentOutput == stream)
		{
			currentOutput = userOutput;
		}
		return true;
	}

	/**
	 * Closes all open streams
	 */
	public void closeStreams()
	{
		for (PrologStream stream : new ArrayList<PrologStream>(openStreams))
		{
			try
			{
				close(stream);
			}
			catch (PrologException e)
			{
				e.printStackTrace();// TODO do something more useful with this.
			}
		}
	}

	/**
	 * @return the convTable
	 */
	public CharConversionTable getConversionTable()
	{
		return prologTextLoaderState.getConversionTable();
	}
}
