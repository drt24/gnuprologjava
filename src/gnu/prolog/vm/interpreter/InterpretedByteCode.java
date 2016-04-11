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
package gnu.prolog.vm.interpreter;

import gnu.prolog.database.Pair;
import gnu.prolog.io.TermWriter;
import gnu.prolog.term.AtomicTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologCodeListener;
import gnu.prolog.vm.PrologCodeUpdatedEvent;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.interpreter.Tracer.TraceLevel;
import gnu.prolog.vm.interpreter.instruction.IAllocate;
import gnu.prolog.vm.interpreter.instruction.ICall;
import gnu.prolog.vm.interpreter.instruction.ICreateCompoundTerm;
import gnu.prolog.vm.interpreter.instruction.ICreateVariable;
import gnu.prolog.vm.interpreter.instruction.ICut;
import gnu.prolog.vm.interpreter.instruction.IDup;
import gnu.prolog.vm.interpreter.instruction.IFail;
import gnu.prolog.vm.interpreter.instruction.IJump;
import gnu.prolog.vm.interpreter.instruction.IPop;
import gnu.prolog.vm.interpreter.instruction.IPushArgument;
import gnu.prolog.vm.interpreter.instruction.IPushConstant;
import gnu.prolog.vm.interpreter.instruction.IPushEnvironment;
import gnu.prolog.vm.interpreter.instruction.IRetryMeElse;
import gnu.prolog.vm.interpreter.instruction.IReturn;
import gnu.prolog.vm.interpreter.instruction.ISaveCut;
import gnu.prolog.vm.interpreter.instruction.IStoreEnvironment;
import gnu.prolog.vm.interpreter.instruction.IThrow;
import gnu.prolog.vm.interpreter.instruction.ITrue;
import gnu.prolog.vm.interpreter.instruction.ITrustMe;
import gnu.prolog.vm.interpreter.instruction.ITryMeElse;
import gnu.prolog.vm.interpreter.instruction.IUnify;
import gnu.prolog.vm.interpreter.instruction.Instruction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Interpreted Code. This class is used by call_term instruction and
 * InterpretedCode
 */
public class InterpretedByteCode implements PrologCode, PrologCodeListener
{
	/** tag of this code */
	protected CompoundTermTag codeTag;
	/** tags used by code */
	protected CompoundTermTag tags[];
	/** constants used by code */
	protected AtomicTerm constants[];
	/** predicate codes used by code */
	protected PrologCode predicateCodes[];
	/** set of instructions */
	protected byte instructions[];
	/** set of exception handlers */
	protected ExceptionHandlerInfo exceptionHandlers[];

	/**
	 * constructor of code
	 * 
	 * @param codeTag
	 * @param isrc
	 * @param ehs
	 */
	protected InterpretedByteCode(CompoundTermTag codeTag, Instruction isrc[], ExceptionHandlerInfo ehs[])
	{
		this.codeTag = codeTag;
		int ipos[] = new int[isrc.length];
		Map<CompoundTermTag, Integer> tag2idx = new HashMap<CompoundTermTag, Integer>();
		Map<AtomicTerm, Integer> constant2idx = new HashMap<AtomicTerm, Integer>();
		pass1(isrc, ipos, tag2idx, constant2idx);
		exceptionHandlers = new ExceptionHandlerInfo[ehs.length];
		int i, n = ehs.length;
		for (i = 0; i < n; i++)
		{
			exceptionHandlers[i] = new ExceptionHandlerInfo();
			exceptionHandlers[i].startPosition = ipos[ehs[i].startPosition];
			exceptionHandlers[i].endPosition = ipos[ehs[i].endPosition];
			exceptionHandlers[i].handlerPosition = ipos[ehs[i].handlerPosition];
		}
		pass2(isrc, ipos, tag2idx, constant2idx);
	}

	protected void pass1(Instruction isrc[], int ipos[], Map<CompoundTermTag, Integer> tag2idx,
			Map<AtomicTerm, Integer> constant2idx)
	{
		Set<CompoundTermTag> callTags = new HashSet<CompoundTermTag>();
		Set<CompoundTermTag> createCompoundTermTags = new HashSet<CompoundTermTag>();
		Set<AtomicTerm> constantSet = new HashSet<AtomicTerm>();

		int bytes = 0;
		int n = isrc.length;
		for (int i = 0; i < n; i++)
		{
			if (isrc[i] instanceof IAllocate)
			{
				ipos[i] = bytes;
				bytes += 5;
			}
			else if (isrc[i] instanceof ICall)
			{
				ICall ii = (ICall) isrc[i];
				callTags.add(ii.tag);
				ipos[i] = bytes;
				bytes += 3;
			}
			else if (isrc[i] instanceof ICreateCompoundTerm)
			{
				ICreateCompoundTerm ii = (ICreateCompoundTerm) isrc[i];
				createCompoundTermTags.add(ii.tag);
				ipos[i] = bytes;
				bytes += 3;
			}
			else if (isrc[i] instanceof ICreateVariable)
			{
				ipos[i] = bytes;
				bytes++;
			}
			else if (isrc[i] instanceof ICut)
			{
				ipos[i] = bytes;
				bytes += 3;
			}
			else if (isrc[i] instanceof IDup)
			{
				ipos[i] = bytes;
				bytes++;
			}
			else if (isrc[i] instanceof IFail)
			{
				ipos[i] = bytes;
				bytes++;
			}
			else if (isrc[i] instanceof IJump)
			{
				ipos[i] = bytes;
				bytes += 5;
			}
			else if (isrc[i] instanceof IPop)
			{
				ipos[i] = bytes;
				bytes++;
			}
			else if (isrc[i] instanceof IPushArgument)
			{
				ipos[i] = bytes;
				bytes += 3;
			}
			else if (isrc[i] instanceof IPushConstant)
			{
				IPushConstant ii = (IPushConstant) isrc[i];
				constantSet.add(ii.term);
				ipos[i] = bytes;
				bytes += 3;
			}
			else if (isrc[i] instanceof IPushEnvironment)
			{
				ipos[i] = bytes;
				bytes += 3;
			}
			else if (isrc[i] instanceof IRetryMeElse)
			{
				ipos[i] = bytes;
				bytes += 5;
			}
			else if (isrc[i] instanceof IReturn)
			{
				ipos[i] = bytes;
				bytes++;
			}
			else if (isrc[i] instanceof ISaveCut)
			{
				ipos[i] = bytes;
				bytes += 3;
			}
			else if (isrc[i] instanceof IStoreEnvironment)
			{
				ipos[i] = bytes;
				bytes += 3;
			}
			else if (isrc[i] instanceof IThrow)
			{
				ipos[i] = bytes;
				bytes++;
			}
			else if (isrc[i] instanceof ITrue)
			{
				ipos[i] = bytes;
				bytes++;
			}
			else if (isrc[i] instanceof ITrustMe)
			{
				ipos[i] = bytes;
				bytes++;
			}
			else if (isrc[i] instanceof ITryMeElse)
			{
				ipos[i] = bytes;
				bytes += 5;
			}
			else if (isrc[i] instanceof IUnify)
			{
				ipos[i] = bytes;
				bytes++;
			}
		}
		createCompoundTermTags.removeAll(callTags);
		tags = new CompoundTermTag[createCompoundTermTags.size() + callTags.size()];
		predicateCodes = new PrologCode[callTags.size()];
		Iterator<CompoundTermTag> j = callTags.iterator();
		int k = 0;
		for (; j.hasNext(); k++)
		{
			CompoundTermTag tag = j.next();
			tags[k] = tag;
			tag2idx.put(tag, Integer.valueOf(k));
		}
		j = createCompoundTermTags.iterator();
		for (; j.hasNext(); k++)
		{
			CompoundTermTag tag = j.next();
			tags[k] = tag;
			tag2idx.put(tag, Integer.valueOf(k));
		}
		constants = new AtomicTerm[constantSet.size()];
		Iterator<AtomicTerm> j2 = constantSet.iterator();
		for (int i = 0; j2.hasNext(); i++)
		{
			AtomicTerm term = j2.next();
			constants[i] = term;
			constant2idx.put(term, Integer.valueOf(i));
		}
		instructions = new byte[bytes];
	}

	protected void pass2(Instruction isrc[], int ipos[], Map<CompoundTermTag, Integer> tag2idx,
			Map<AtomicTerm, Integer> constant2idx)
	{
		int bytes = 0;
		int i, n = isrc.length;
		for (i = 0; i < n; i++)
		{
			if (isrc[i] instanceof IAllocate)
			{
				IAllocate ii = (IAllocate) isrc[i];
				instructions[bytes++] = (byte) IALLOCATE;
				instructions[bytes++] = (byte) (ii.environmentSize >> 8 & 255);
				instructions[bytes++] = (byte) (ii.environmentSize & 255);
				instructions[bytes++] = (byte) (ii.reserved >> 8 & 255);
				instructions[bytes++] = (byte) (ii.reserved & 255);
			}
			else if (isrc[i] instanceof ICall)
			{
				ICall ii = (ICall) isrc[i];
				int idx = (tag2idx.get(ii.tag)).intValue();
				instructions[bytes++] = (byte) ICALL;
				instructions[bytes++] = (byte) (idx >> 8 & 255);
				instructions[bytes++] = (byte) (idx & 255);
			}
			else if (isrc[i] instanceof ICreateCompoundTerm)
			{
				ICreateCompoundTerm ii = (ICreateCompoundTerm) isrc[i];
				int idx = (tag2idx.get(ii.tag)).intValue();
				instructions[bytes++] = (byte) ICREATE_COMPOUND;
				instructions[bytes++] = (byte) (idx >> 8 & 255);
				instructions[bytes++] = (byte) (idx & 255);
			}
			else if (isrc[i] instanceof ICreateVariable)
			{
				instructions[bytes++] = (byte) ICREATE_VARIABLE;
			}
			else if (isrc[i] instanceof ICut)
			{
				ICut ii = (ICut) isrc[i];
				instructions[bytes++] = (byte) ICUT;
				instructions[bytes++] = (byte) (ii.environmentIndex >> 8 & 255);
				instructions[bytes++] = (byte) (ii.environmentIndex & 255);
			}
			else if (isrc[i] instanceof IDup)
			{
				instructions[bytes++] = (byte) IDUP;
			}
			else if (isrc[i] instanceof IFail)
			{
				instructions[bytes++] = (byte) IFAIL;
			}
			else if (isrc[i] instanceof IJump)
			{
				IJump ii = (IJump) isrc[i];
				instructions[bytes++] = (byte) IJUMP;
				instructions[bytes++] = (byte) (ipos[ii.jumpPosition] >> 24 & 255);
				instructions[bytes++] = (byte) (ipos[ii.jumpPosition] >> 16 & 255);
				instructions[bytes++] = (byte) (ipos[ii.jumpPosition] >> 8 & 255);
				instructions[bytes++] = (byte) (ipos[ii.jumpPosition] & 255);
			}
			else if (isrc[i] instanceof IPop)
			{
				instructions[bytes++] = (byte) IPOP;
			}
			else if (isrc[i] instanceof IPushArgument)
			{
				IPushArgument ii = (IPushArgument) isrc[i];
				instructions[bytes++] = (byte) IPUSH_ARGUMENT;
				instructions[bytes++] = (byte) (ii.argumentPosition >> 8 & 255);
				instructions[bytes++] = (byte) (ii.argumentPosition & 255);
			}
			else if (isrc[i] instanceof IPushConstant)
			{
				IPushConstant ii = (IPushConstant) isrc[i];
				int idx = (constant2idx.get(ii.term)).intValue();
				instructions[bytes++] = (byte) IPUSH_CONSTANT;
				instructions[bytes++] = (byte) (idx >> 8 & 255);
				instructions[bytes++] = (byte) (idx & 255);
			}
			else if (isrc[i] instanceof IPushEnvironment)
			{
				IPushEnvironment ii = (IPushEnvironment) isrc[i];
				instructions[bytes++] = (byte) IPUSH_ENVIRONMENT;
				instructions[bytes++] = (byte) (ii.environmentPosition >> 8 & 255);
				instructions[bytes++] = (byte) (ii.environmentPosition & 255);
			}
			else if (isrc[i] instanceof IRetryMeElse)
			{
				IRetryMeElse ii = (IRetryMeElse) isrc[i];
				instructions[bytes++] = (byte) IRETRY_ME_ELSE;
				instructions[bytes++] = (byte) (ipos[ii.retryPosition] >> 24 & 255);
				instructions[bytes++] = (byte) (ipos[ii.retryPosition] >> 16 & 255);
				instructions[bytes++] = (byte) (ipos[ii.retryPosition] >> 8 & 255);
				instructions[bytes++] = (byte) (ipos[ii.retryPosition] & 255);
			}
			else if (isrc[i] instanceof IReturn)
			{
				instructions[bytes++] = (byte) IRETURN;
			}
			else if (isrc[i] instanceof ISaveCut)
			{
				ISaveCut ii = (ISaveCut) isrc[i];
				instructions[bytes++] = (byte) ISAVE_CUT;
				instructions[bytes++] = (byte) (ii.environmentIndex >> 8 & 255);
				instructions[bytes++] = (byte) (ii.environmentIndex & 255);
			}
			else if (isrc[i] instanceof IStoreEnvironment)
			{
				IStoreEnvironment ii = (IStoreEnvironment) isrc[i];
				instructions[bytes++] = (byte) ISTORE_ENVIRONMENT;
				instructions[bytes++] = (byte) (ii.environmentIndex >> 8 & 255);
				instructions[bytes++] = (byte) (ii.environmentIndex & 255);
			}
			else if (isrc[i] instanceof IThrow)
			{
				instructions[bytes++] = (byte) ITHROW;
			}
			else if (isrc[i] instanceof ITrue)
			{
				instructions[bytes++] = (byte) ITRUE;
			}
			else if (isrc[i] instanceof ITrustMe)
			{
				instructions[bytes++] = (byte) ITRUST_ME;
			}
			else if (isrc[i] instanceof ITryMeElse)
			{
				ITryMeElse ii = (ITryMeElse) isrc[i];
				instructions[bytes++] = (byte) ITRY_ME_ELSE;
				instructions[bytes++] = (byte) (ipos[ii.retryPosition] >> 24 & 255);
				instructions[bytes++] = (byte) (ipos[ii.retryPosition] >> 16 & 255);
				instructions[bytes++] = (byte) (ipos[ii.retryPosition] >> 8 & 255);
				instructions[bytes++] = (byte) (ipos[ii.retryPosition] & 255);
			}
			else if (isrc[i] instanceof IUnify)
			{
				instructions[bytes++] = (byte) IUNIFY;
			}
		}
		// System.out.print("ssz = "+isrc.length+" sz = "+instructions.length+" code = ");
		// for (i=0;i<instructions.length;i++)
		// {
		// System.out.print(i+" ");
		// }
		// System.out.println();
		// System.out.flush();
	}

	/**
	 * this method is used for execution of code
	 * 
	 * @param interpreter
	 *          interpreter in which context code is executed
	 * @param backtrackMode
	 *          true if predicate is called on backtracking and false otherwise
	 * @param args
	 *          arguments of code
	 * @return either RC.SUCCESS, RC.SUCCESS_LAST, or RC.FAIL.
	 */
	public RC execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[]) throws PrologException
	{

		Term environment[] = null;
		BacktrackInfo currentBacktrackInfo = null;
		BacktrackInfo startBacktrackInfo = null;
		int currentPosition;
		BacktrackInfo bi = backtrackMode ? interpreter.popBacktrackInfo() : null;
		BacktrackInfo cur_bi = null;
		Term pds[] = new Term[16];
		int pdsSize = 0;
		int pdsMaxSize = pds.length;
		final int pdsInc = 16;

		if (bi != null) // if redo, restore execution state
		{
			if (!(bi instanceof LeaveByteCodeBacktrackInfo))
			{
				PrologException.systemError();
			}
			LeaveByteCodeBacktrackInfo lbi = (LeaveByteCodeBacktrackInfo) bi;
			environment = lbi.environment;
			startBacktrackInfo = lbi.startBacktrackInfo;
			currentPosition = 0; // fake assignment in order to fool compiler
		}
		else
		// else create new execution state
		{
			startBacktrackInfo = new EnterBacktrackInfo(interpreter.getUndoPosition());
			interpreter.pushBacktrackInfo(startBacktrackInfo);
			currentPosition = 0;
		}

		interpreter_loop: while (true) // interpreter loop
		{
			try
			{
				try
				{
					if (backtrackMode)
					{
						cur_bi = interpreter.popBacktrackInfo();
						cur_bi.undo(interpreter);
						if (cur_bi instanceof EnterBacktrackInfo)
						{
							return RC.FAIL;
						}
						else
						{
							currentPosition = cur_bi.codePosition;
						}
					}

					int instr = instructions[currentPosition] & 255;
					// System.err.println("code = "+codeTag+" bmode = "+backtrackMode+" instr = "+getIntruction(currentPosition));
					switch (instr)
					{
						case IALLOCATE:
						{
							int sz = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
							int rs = ((instructions[currentPosition + 3] & 255) << 8) + (instructions[currentPosition + 4] & 255);
							environment = new Term[sz];
							for (int i = rs; i < sz; i++)
							{
								environment[i] = new VariableTerm();
							}
							currentPosition += 5;
							continue interpreter_loop;
						}
						case ICALL:
						{
							PrologCode code;
							Term cargs[];
							CompoundTermTag tag;

							if (backtrackMode)
							{
								CallBacktrackInfo cbi = (CallBacktrackInfo) cur_bi;
								code = cbi.code;
								cargs = cbi.args;
								tag = cbi.tag;
							}
							else
							{
								int cd = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
								tag = tags[cd];
								int arity = tag.arity;
								cargs = new Term[arity];
								for (int i = arity - 1; i >= 0; i--)
								{
									cargs[i] = pds[--pdsSize].dereference(); // pop argument from
									// stack
									pds[pdsSize] = null;
								}
								code = predicateCodes[cd];
								if (code == null)
								{
									code = interpreter.getEnvironment().getPrologCode(tag);
									predicateCodes[cd] = code;
								}
							}
							interpreter.getTracer().traceEvent(backtrackMode ? TraceLevel.REDO : TraceLevel.CALL, interpreter, tag,
									cargs);
							RC rc;
							try
							{
								rc = code.execute(interpreter, backtrackMode, cargs);
							}
							finally
							{
								interpreter.getTracer().decreaseDepth();
							}
							switch (rc)
							{
								case SUCCESS_LAST:
									interpreter.getTracer().traceEvent(TraceLevel.EXIT, interpreter, tag, cargs);
									backtrackMode = false;
									break;
								case SUCCESS:
									interpreter.getTracer().traceEvent(TraceLevel.EXIT, interpreter, tag, cargs);
									if (backtrackMode)
									{
										cur_bi.undoPosition = interpreter.getUndoPosition();
										interpreter.pushBacktrackInfo(cur_bi);
									}
									else
									{
										interpreter.pushBacktrackInfo(new CallBacktrackInfo(interpreter.getUndoPosition(), currentPosition,
												cargs, code, tag));
									}
									backtrackMode = false;
									break;
								case FAIL:
									interpreter.getTracer().traceEvent(TraceLevel.FAIL, interpreter, tag, cargs);
									backtrackMode = true;
							}
							currentPosition += 3;
							continue interpreter_loop;
						}
						case ICREATE_COMPOUND:
						{
							int tg = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
							CompoundTermTag tag = tags[tg];
							int arity = tag.arity;
							Term targs[] = new Term[arity];
							for (int i = arity - 1; i >= 0; i--)
							{
								targs[i] = pds[--pdsSize].dereference(); // pop argument from
								// stack
								pds[pdsSize] = null;
							}
							pds[pdsSize++] = new CompoundTerm(tag, targs);
							currentPosition += 3;
							continue interpreter_loop;
						}
						case ICREATE_VARIABLE:
						{
							if (pdsSize == pdsMaxSize)
							{
								Term tmp[] = new Term[pdsSize + pdsInc];
								System.arraycopy(pds, 0, tmp, 0, pdsMaxSize);
								pdsMaxSize += pdsInc;
								pds = tmp;
							}
							pds[pdsSize++] = new VariableTerm();
							currentPosition++;
							continue interpreter_loop;
						}
						case ICUT:
						{
							int ep = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
							JavaObjectTerm t = (JavaObjectTerm) environment[ep];
							BacktrackInfo cutPoint = (BacktrackInfo) t.value;
							interpreter.popBacktrackInfoUntil(cutPoint);
							currentPosition += 3;
							continue interpreter_loop;
						}
						case IDUP:
						{
							if (pdsSize == pdsMaxSize)
							{
								Term tmp[] = new Term[pdsSize + pdsInc];
								System.arraycopy(pds, 0, tmp, 0, pdsMaxSize);
								pdsMaxSize += pdsInc;
							}
							Term tmp1 = pds[pdsSize - 1];
							pds[pdsSize++] = tmp1;
							currentPosition++;
							continue interpreter_loop;
						}
						case IFAIL:
						{
							backtrackMode = true;
							continue interpreter_loop;
						}
						case IJUMP:
						{
                                                        int jp = ((instructions[currentPosition + 1] & 255) << 24) + ((instructions[currentPosition + 2] & 255) << 16) + ((instructions[currentPosition + 3] & 255) << 8) + (instructions[currentPosition + 4] & 255);
							currentPosition = jp;
							continue interpreter_loop;
						}
						case IPOP:
						{
							pds[--pdsSize] = null;
							currentPosition++;
							continue interpreter_loop;
						}
						case IPUSH_ARGUMENT:
						{
							if (pdsSize == pdsMaxSize)
							{
								Term tmp[] = new Term[pdsSize + pdsInc];
								System.arraycopy(pds, 0, tmp, 0, pdsMaxSize);
								pdsMaxSize += pdsInc;
								pds = tmp;
							}
							int ar = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
							pds[pdsSize++] = args[ar];
							currentPosition += 3;
							continue interpreter_loop;
						}
						case IPUSH_CONSTANT:
						{
							if (pdsSize == pdsMaxSize)
							{
								Term tmp[] = new Term[pdsMaxSize + pdsInc];
								System.arraycopy(pds, 0, tmp, 0, pdsMaxSize);
								pdsMaxSize += pdsInc;
								pds = tmp;
							}
							int cp = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
							pds[pdsSize++] = constants[cp];
							currentPosition += 3;
							continue interpreter_loop;
						}
						case IPUSH_ENVIRONMENT:
						{
							if (pdsSize == pdsMaxSize)
							{
								Term tmp[] = new Term[pdsSize + pdsInc];
								System.arraycopy(pds, 0, tmp, 0, pdsMaxSize);
								pdsMaxSize += pdsInc;
								pds = tmp;
							}
							int ep = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
							pds[pdsSize++] = environment[ep];
							currentPosition += 3;
							continue interpreter_loop;
						}
						case IRETRY_ME_ELSE:
						{
							int rp = ((instructions[currentPosition + 1] & 255) << 24) + ((instructions[currentPosition + 2] & 255) << 16) + ((instructions[currentPosition + 3] & 255) << 8) + (instructions[currentPosition + 4] & 255);
							cur_bi.codePosition = rp;
							interpreter.pushBacktrackInfo(cur_bi);
							currentPosition += 5;
							backtrackMode = false;
							continue interpreter_loop;
						}
						case IRETURN:
						{
							if (startBacktrackInfo == interpreter.peekBacktrackInfo())
							{
								// code cannot be rexecuted
								interpreter.popBacktrackInfo();
								return RC.SUCCESS_LAST;
							}
							else
							{
								LeaveByteCodeBacktrackInfo lbi;
								if (bi != null)
								{
									lbi = (LeaveByteCodeBacktrackInfo) bi;
								}
								else
								{
									lbi = new LeaveByteCodeBacktrackInfo(environment, startBacktrackInfo);
								}
								interpreter.pushBacktrackInfo(lbi);
								return RC.SUCCESS;
							}
						}
						case ISAVE_CUT:
						{
							int ep = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
							JavaObjectTerm t = new JavaObjectTerm(interpreter.peekBacktrackInfo());
							environment[ep] = t;
							currentPosition += 3;
							continue interpreter_loop;
						}
						case ISTORE_ENVIRONMENT:
						{
							int ep = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
							Term t = pds[--pdsSize]; // pop argument from stack
							pds[pdsSize] = null;
							environment[ep] = t;
							currentPosition += 3;
							continue interpreter_loop;
						}
						case ITHROW:
						{
							Term t = pds[--pdsSize].dereference(); // pop argument from stack
							pds[pdsSize] = null;
							if (t instanceof VariableTerm)
							{
								PrologException.instantiationError(t);
							}
							throw new PrologException(t, null);
						}
						case ITRUE:
						{
							currentPosition++;
							continue interpreter_loop;
						}
						case ITRUST_ME:
						{
							currentPosition++;
							backtrackMode = false;
							continue interpreter_loop;
						}
						case ITRY_ME_ELSE:
						{
                                                        int rp = ((instructions[currentPosition + 1] & 255) << 24) + ((instructions[currentPosition + 2] & 255) << 16) + ((instructions[currentPosition + 3] & 255) << 8) + (instructions[currentPosition + 4] & 255);
							cur_bi = new BacktrackInfo(interpreter.getUndoPosition(), rp);
							interpreter.pushBacktrackInfo(cur_bi);
							currentPosition += 5;
							cur_bi = null;
							continue interpreter_loop;
						}
						case IUNIFY:
						{
							Term t1 = pds[--pdsSize].dereference(); // pop argument from stack
							pds[pdsSize] = null;
							Term t0 = pds[--pdsSize].dereference(); // pop argument from stack
							pds[pdsSize] = null;
							RC rc = interpreter.simpleUnify(t0, t1);
							if (rc == RC.FAIL)
							{
								backtrackMode = true;
							}
							else
							{
								currentPosition++;
							}
							continue interpreter_loop;
						}
						default:
							PrologException.systemError();
					}
				}
				catch (RuntimeException ex)
				{
					// unchecked exception behaves as system_error
					ex.printStackTrace();// TODO incorporate this into a proper debugging
					// framework
					PrologException.systemError(ex);
				}
			}
			catch (PrologException ex)
			{
				int ei, en = exceptionHandlers.length;
				backtrackMode = false;
				for (ei = 0; ei < en; ei++)
				{
					ExceptionHandlerInfo cur = exceptionHandlers[ei];
					if (cur.startPosition < currentPosition && currentPosition < cur.endPosition)
					{
						if (pdsSize == pdsMaxSize)
						{
							Term tmp[] = new Term[pdsSize + pdsInc];
							System.arraycopy(pds, 0, tmp, 0, pdsMaxSize);
							pdsMaxSize += pdsInc;
						}
						pds[pdsSize++] = ex.getTerm();
						currentPosition = cur.handlerPosition;
						continue interpreter_loop;
					}
				}
				// no handler was found
				interpreter.popBacktrackInfoUntil(startBacktrackInfo);
				cur_bi = interpreter.popBacktrackInfo();
				cur_bi.undo(interpreter);
				throw ex;
			}
		}
	}

	/** this method is called when prolog code was updated */
	public void prologCodeUpdated(PrologCodeUpdatedEvent evt)
	{
		CompoundTermTag tag = evt.getPredicateTag();
		for (int i = predicateCodes.length - 1; i >= 0; i--)
		{
			if (tags[i] == tag)
			{
				predicateCodes[i] = null;
				return;
			}
		}
	}

	/**
	 * this method is called when code is installed to the environment code can be
	 * installed only for one environment.
	 * 
	 * @param env
	 *          Environment to install the predicate
	 */
	public void install(Environment env)
	{
		for (int i = predicateCodes.length - 1; i >= 0; i--)
		{
			env.addPrologCodeListener(tags[i], this);
		}
	}

	/**
	 * this method is called when code is uninstalled from the environment
	 * 
	 * @param env
	 *          Environment to install the predicate
	 */
	public void uninstall(Environment env)
	{
		for (int i = predicateCodes.length - 1; i >= 0; i--)
		{
			env.removePrologCodeListener(tags[i], this);
		}
	}

	/** convert code to string */
	@Override
	public String toString()
	{
		int currentPosition = 0;
		String rc = "interpreted code\n";
		while (currentPosition < instructions.length)
		{
			int instr = instructions[currentPosition] & 255;

			Pair<String, Integer> answer = instructionToString(instr, currentPosition);

			rc += answer.left + "\n";
			currentPosition = answer.right;
		}
		int i, n;
		rc += "exceptions\n";
		n = exceptionHandlers.length;
		for (i = 0; i < n; i++)
		{
			rc += exceptionHandlers[i] + "\n";
		}
		rc += "end interpreted code\n";
		return rc;
	}

	private Pair<String, Integer> instructionToString(int instr, int currentPosition)
	{
		String rc = "";
		switch (instr)
		{
			case IALLOCATE:
			{
				int sz = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
				int rs = ((instructions[currentPosition + 3] & 255) << 8) + (instructions[currentPosition + 4] & 255);
				rc += currentPosition + ": allocate " + sz + ", " + rs;
				currentPosition += 5;
				break;
			}
			case ICALL:
			{
				int cd = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
				CompoundTermTag tag = tags[cd];
				rc += currentPosition + ": call " + tag;
				currentPosition += 3;
				break;
			}
			case ICREATE_COMPOUND:
			{
				int tg = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
				CompoundTermTag tag = tags[tg];
				rc += currentPosition + ": create_compound " + tag;
				currentPosition += 3;
				break;
			}
			case ICREATE_VARIABLE:
			{
				rc += currentPosition + ": create_variable";
				currentPosition++;
				break;
			}
			case ICUT:
			{
				int ep = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
				rc += currentPosition + ": cut " + ep;
				currentPosition += 3;
				break;
			}
			case IDUP:
			{
				rc += currentPosition + ": dup";
				currentPosition++;
				break;
			}
			case IFAIL:
			{
				rc += currentPosition + ": fail";
				currentPosition++;
				break;
			}
			case IJUMP:
			{
                                int jp = ((instructions[currentPosition + 1] & 255) << 24) + ((instructions[currentPosition + 2] & 255) << 16) + ((instructions[currentPosition + 3] & 255) << 8) + (instructions[currentPosition + 4] & 255);
				rc += currentPosition + ": jump " + jp;
				currentPosition += 5;
				break;
			}
			case IPOP:
			{
				rc += currentPosition + ": pop";
				currentPosition++;
				break;
			}
			case IPUSH_ARGUMENT:
			{
				int ar = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
				rc += currentPosition + ": push_argument " + ar;
				currentPosition += 3;
				break;
			}
			case IPUSH_CONSTANT:
			{
				int cp = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
				rc += currentPosition + ": push_constant " + TermWriter.toString(constants[cp]);
				currentPosition += 3;
				break;
			}
			case IPUSH_ENVIRONMENT:
			{
				int ep = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
				rc += currentPosition + ": push_environment " + ep;
				currentPosition += 3;
				break;
			}
			case IRETRY_ME_ELSE:
			{
				int rp = ((instructions[currentPosition + 1] & 255) << 24) + ((instructions[currentPosition + 2] & 255) << 16) + ((instructions[currentPosition + 3] & 255) << 8) + (instructions[currentPosition + 4] & 255);
				rc += currentPosition + ": retry_me_else " + rp;
				currentPosition += 5;
				break;
			}
			case IRETURN:
			{
				rc += currentPosition + ": return";
				currentPosition++;
				break;
			}
			case ISAVE_CUT:
			{
				int ep = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
				rc += currentPosition + ": save_cut " + ep;
				currentPosition += 3;
				break;
			}
			case ISTORE_ENVIRONMENT:
			{
				int ep = ((instructions[currentPosition + 1] & 255) << 8) + (instructions[currentPosition + 2] & 255);
				rc += currentPosition + ": store_environment " + ep;
				currentPosition += 3;
				break;
			}
			case ITHROW:
			{
				rc += currentPosition + ": throw";
				currentPosition += 1;
				break;
			}
			case ITRUE:
			{
				rc += currentPosition + ": true";
				currentPosition++;
				break;
			}
			case ITRUST_ME:
			{
				rc += currentPosition + ": trust_me";
				currentPosition++;
				break;
			}
			case ITRY_ME_ELSE:
			{
				int rp = ((instructions[currentPosition + 1] & 255) << 24) + ((instructions[currentPosition + 2] & 255) << 16) + ((instructions[currentPosition + 3] & 255) << 8) + (instructions[currentPosition + 4] & 255);
				rc += currentPosition + ": try_me_else " + rp;
				currentPosition += 5;
				break;
			}
			case IUNIFY:
			{
				rc += currentPosition + ": unify ";
				currentPosition++;
				break;
			}
			default:
				rc += currentPosition + ": unknown";
				currentPosition++;
				break;
		}
		return new Pair<String, Integer>(rc, currentPosition);
	}

	/**
	 * convert code to string
	 * 
	 * @param currentPosition
	 * @return a string representation of the instruction at the currentPosition
	 */
	public String getIntruction(int currentPosition)
	{
		int instr = instructions[currentPosition] & 255;

		return instructionToString(instr, currentPosition).left;
	}

	public final static int IALLOCATE = 0;
	public final static int ICALL = 1;
	public final static int ICREATE_COMPOUND = 2;
	public final static int ICREATE_VARIABLE = 3;
	public final static int ICUT = 4;
	public final static int IDUP = 5;
	public final static int IFAIL = 6;
	public final static int IJUMP = 7;
	public final static int IPOP = 8;
	public final static int IPUSH_ARGUMENT = 9;
	public final static int IPUSH_CONSTANT = 10;
	public final static int IPUSH_ENVIRONMENT = 11;
	public final static int IRETRY_ME_ELSE = 12;
	public final static int IRETURN = 13;
	public final static int ISAVE_CUT = 14;
	public final static int ISTORE_ENVIRONMENT = 15;
	public final static int ITHROW = 16;
	public final static int ITRUE = 17;
	public final static int ITRUST_ME = 18;
	public final static int ITRY_ME_ELSE = 19;
	public final static int IUNIFY = 20;
}
