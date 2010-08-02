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

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.AtomicTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;
import gnu.prolog.vm.interpreter.instruction.IAllocate;
import gnu.prolog.vm.interpreter.instruction.ICall;
import gnu.prolog.vm.interpreter.instruction.ICreateCompoundTerm;
import gnu.prolog.vm.interpreter.instruction.ICut;
import gnu.prolog.vm.interpreter.instruction.IFail;
import gnu.prolog.vm.interpreter.instruction.IJump;
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
import gnu.prolog.vm.interpreter.instruction.RetryInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * compiler from predicate to interpreted code This version assume following
 * <ul>
 * <li>All variables are stored in environment</li>
 * <li>Environment is not reused for different branches</li>
 * </ul>
 */

public class InterpretedCodeCompiler
{
	// constant used in analysis and compilation
	/** unify tag */
	public static final CompoundTermTag unifyTag = CompoundTermTag.get("=", 2);
	/** throw tag */
	public static final CompoundTermTag throwTag = CompoundTermTag.get("throw", 1);
	/** catch tag */
	public static final CompoundTermTag catchTag = CompoundTermTag.get("catch", 3);
	/** array type constant for Instruction.class */
	public final static Instruction instructionArrayConstant[] = new Instruction[0];
	/** array type constant ExceptionHandlerInfo.class */
	public final static ExceptionHandlerInfo exceptionHandlerArrayConstant[] = new ExceptionHandlerInfo[0];

	// compilation variables
	/** code so far compiled */
	protected List<Instruction> code = new ArrayList<Instruction>();
	/** exception handlers */
	protected List<ExceptionHandlerInfo> exceptionHandlers = new ArrayList<ExceptionHandlerInfo>();
	/** current code position */
	protected int currentCodePosition = 0;
	/** number of already allocated reserved variables */
	protected int allocatedReserved = 0;
	/** mapping from variables to environment indexes */
	protected Map<Term, Integer> variableToEnvironmentIndex = new HashMap<Term, Integer>();
	/** this predicate tag */
	protected CompoundTermTag codeTag;

	// Analysis variables
	/** number of reserved fields, one position for saving cut */
	protected int numberOfReserved = 1;

	/** cut position stack */
	protected List<Integer> cutPositionStack = new ArrayList<Integer>();
	/** clauses to compile */
	protected List<Term> passedClauses;

	/**
	 * a constructor
	 * 
	 * @param clauses
	 */
	public InterpretedCodeCompiler(List<Term> clauses)
	{
		passedClauses = clauses;
	}

	protected int allocReserved()
	{
		return allocatedReserved++;
	}

	/**
	 * compile creation of term
	 * 
	 * @param term
	 *          term to create
	 * @throws PrologException
	 *           #systemError() if term cannot be compiled
	 */
	void compileTermCreation(Term term) throws PrologException
	{
		if (term instanceof VariableTerm)
		{
			iPushEnvironment(getEnvironmentIndex((VariableTerm) term));
		}
		else if (term instanceof AtomicTerm)
		{
			iPushConstant((AtomicTerm) term);
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			for (Term arg : ct.args)
			{
				compileTermCreation(arg);
			}
			iCreateCompoundTerm(ct.tag);
		}
		else
		// unknown type of term
		{
			PrologException.systemError();
		}
	}

	/**
	 * compile head of clause
	 * 
	 * @param headTerm
	 *          term to compile
	 * @throws PrologException
	 *           #typeError(callable,head) if term cannot be compiled
	 */
	void compileHead(Term headTerm) throws PrologException
	{
		if (headTerm instanceof AtomTerm)
		{
			// do nothing
			if (codeTag == null)
			{
				codeTag = CompoundTermTag.get((AtomTerm) headTerm, 0);
			}
		}
		else if (headTerm instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) headTerm;
			for (int i = 0; i < ct.tag.arity; i++)
			{
				iPushArgument(i);
				compileTermCreation(ct.args[i]);
				iUnify();
			}
			if (codeTag == null)
			{
				codeTag = ct.tag;
			}
		}
		else
		{
			PrologException.typeError(TermConstants.callableAtom, headTerm);
		}
	}

	/**
	 * compile body of clause
	 * 
	 * @param body
	 *          term to compile
	 * @throws PrologException
	 *           #typeError(callable,head) if term cannot be compiled
	 */
	void compileBody(Term body) throws PrologException
	{
		if (body instanceof VariableTerm) // all variable are converted to call(Var)
		{
			compileTermCreation(body);
			iCall(TermConstants.callTag);
		}
		else if (body instanceof AtomTerm)
		{
			if (body == TermConstants.cutAtom) // cut
			{
				iCut(getCutPosition());
			}
			else if (body == TermConstants.trueAtom) // true
			{
				iTrue();
			}
			else if (body == TermConstants.failAtom) // false
			{
				iFail();
			}
			else
			// user defined procedure
			{
				iCall(CompoundTermTag.get((AtomTerm) body, 0));
			}
		}
		else if (body instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) body;
			CompoundTermTag tag = ct.tag;
			if (tag == TermConstants.conjunctionTag)
			{
				compileBody(ct.args[0]);
				compileBody(ct.args[1]);
			}
			else if (tag == TermConstants.disjunctionTag)
			{
				if (ct.args[0] instanceof CompoundTerm // if then else
						&& ((CompoundTerm) ct.args[0]).tag == TermConstants.ifTag)
				{
					CompoundTerm ct2 = (CompoundTerm) ct.args[0];
					compileIfThenElse(ct2.args[0], ct2.args[1], ct.args[1]);
				}
				else
				// just disjunction
				{
					ITryMeElse tryMeElse = iTryMeElse(-1);
					compileBody(ct.args[0]);
					IJump jump = iJump(-1);
					ITrustMe trustMe = iTrustMe();
					tryMeElse.retryPosition = trustMe.codePosition;
					compileBody(ct.args[1]);
					jump.jumpPosition = currentCodePosition;
				}
			}
			else if (tag == throwTag)
			{
				compileTermCreation(ct.args[0]);
				iThrow();
			}
			else if (tag == TermConstants.ifTag)
			{
				compileIfThenElse(ct.args[0], ct.args[1], TermConstants.failAtom);
			}
			else if (tag == catchTag)
			{
				ExceptionHandlerInfo eh = new ExceptionHandlerInfo();
				// compile body
				eh.startPosition = currentCodePosition;
				compileTermCreation(ct.args[0]);
				iCall(TermConstants.callTag);
				eh.endPosition = currentCodePosition;
				IJump jumpOut = iJump(-1);
				// compile handler
				eh.handlerPosition = currentCodePosition;
				int coughtTermPos = allocReserved();
				int handlerCutPos = allocReserved();
				iStoreEnvironment(coughtTermPos); // save cought term in environment
				iSaveCut(handlerCutPos);
				ITryMeElse tryMeElse = iTryMeElse(-1);
				// unify head
				iPushEnvironment(coughtTermPos); // get cought from term in environment
				compileTermCreation(ct.args[1]);
				iUnify();
				iCut(handlerCutPos);
				// compile handler body
				compileTermCreation(ct.args[2]);
				iCall(TermConstants.callTag);
				IJump jump = iJump(-1);
				ITrustMe trustMe = iTrustMe();
				tryMeElse.retryPosition = trustMe.codePosition;
				iPushEnvironment(coughtTermPos); // get cought from term in environment
				iThrow();
				jump.jumpPosition = currentCodePosition;
				jumpOut.jumpPosition = currentCodePosition;
				addExceptionHandler(eh);
			}
			else if (tag == unifyTag)
			{
				compileTermCreation(ct.args[0]);
				compileTermCreation(ct.args[1]);
				iUnify();
			}
			else
			// user defined predicate
			{
				int i, n = tag.arity;
				for (i = 0; i < n; i++)
				{
					compileTermCreation(ct.args[i]);
				}
				iCall(tag);
			}
		}
		else
		// other type
		{
			PrologException.typeError(TermConstants.callableAtom, body);
		}
	}

	/**
	 * compile is then else construct
	 * 
	 * @param ifTerm
	 *          term for if
	 * @param thenTerm
	 *          term for then
	 * @param elseTerm
	 *          else term or atom fail if just "->"
	 * @throws PrologException
	 *           if one of terms cannot be compiled
	 */
	void compileIfThenElse(Term ifTerm, Term thenTerm, Term elseTerm) throws PrologException
	{
		int envPos = allocReserved();
		iSaveCut(envPos);
		ITryMeElse tryMeElse = iTryMeElse(-1);
		pushCutPosition(envPos);
		// compile if part
		compileBody(ifTerm);
		iCut(envPos);
		popCutPosition();
		// compile then
		compileBody(thenTerm);
		IJump jump = iJump(-1);
		ITrustMe trustMe = iTrustMe();
		tryMeElse.retryPosition = trustMe.codePosition;
		compileBody(elseTerm);
		jump.jumpPosition = currentCodePosition;
	}

	/**
	 * get reserved environment size for body term
	 * 
	 * @param body
	 *          term to analyse
	 * @return amount of allocated environment
	 * @throws PrologException
	 *           #typeError(callable,head) if term cannot be compiled
	 */
	int getReservedEnvironemt(Term body) throws PrologException
	{
		int rc = 0;
		if (body instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) body;
			CompoundTermTag tag = ct.tag;
			if (tag == TermConstants.conjunctionTag)
			{
				rc += getReservedEnvironemt(ct.args[0]);
				rc += getReservedEnvironemt(ct.args[1]);
			}
			else if (tag == TermConstants.disjunctionTag)
			{
				if (ct.args[0] instanceof CompoundTerm // if then else
						&& ((CompoundTerm) ct.args[0]).tag == TermConstants.ifTag)
				{
					CompoundTerm ct2 = (CompoundTerm) ct.args[0];
					rc += 1;
					rc += getReservedEnvironemt(ct2.args[0]);
					rc += getReservedEnvironemt(ct2.args[1]);
					rc += getReservedEnvironemt(ct.args[1]);
				}
				else
				// just disjunction
				{
					rc += getReservedEnvironemt(ct.args[0]);
					rc += getReservedEnvironemt(ct.args[1]);
				}
			}
			else if (tag == TermConstants.ifTag)
			{
				rc += 1;
				rc += getReservedEnvironemt(ct.args[0]);
				rc += getReservedEnvironemt(ct.args[1]);
			}
			else if (tag == catchTag)
			{
				rc += 2;
			}
		}
		return rc;
	}

	/**
	 * get all variables from term and populate variableToEnvironmentIndex map
	 * 
	 * @param term
	 *          to analyse
	 * @param currentEnvPositon
	 *          current position in environment
	 * @return current position in environment after call
	 */
	int getAllVariables(Term term, int currentEnvPositon)
	{
		if (term instanceof VariableTerm)
		{
			if (!variableToEnvironmentIndex.containsKey(term))
			{
				variableToEnvironmentIndex.put(term, Integer.valueOf(currentEnvPositon++));
			}
		}
		else if (term instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) term;
			int n = ct.tag.arity;
			Term args[] = ct.args;
			for (int i = 0; i < n; i++)
			{
				currentEnvPositon = getAllVariables(args[i], currentEnvPositon);
			}
		}
		return currentEnvPositon;
	}

	/**
	 * recursively dereference term
	 * 
	 * @param term
	 *          to be recursively dereferenced
	 * @return recursively dereferenced term
	 */
	public static Term rdereferenced(Term term)
	{
		term = term.dereference();
		if (term instanceof CompoundTerm)
		{
			CompoundTerm ct1 = (CompoundTerm) term;
			int n = ct1.tag.arity;
			Term args1[] = ct1.args;
			Term args2[] = new Term[n];
			for (int i = 0; i < n; i++)
			{
				args2[i] = rdereferenced(args1[i]);
			}
			term = new CompoundTerm(ct1.tag, args2);
		}
		return term;
	}

	/**
	 * compile one clause of predicate
	 * 
	 * @param clause
	 * @throws PrologException
	 */
	void compileClause(Term clause) throws PrologException
	{
		if (clause instanceof CompoundTerm)
		{
			CompoundTerm ct = (CompoundTerm) clause;
			if (ct.tag == TermConstants.clauseTag)
			{
				compileHead(ct.args[0]);
				compileBody(ct.args[1]);
				return;
			}
		}
		compileHead(clause);
	}

	/**
	 * compile set of clauses to interpreted code
	 * 
	 * @param passedClauses
	 *          clauses passed to compiler
	 * @return instance of interpreted code
	 * @throws PrologException
	 */
	public static PrologCode compile(List<Term> passedClauses) throws PrologException
	{
		synchronized (passedClauses)
		{
			return new InterpretedCodeCompiler(passedClauses).compilePredicate();
		}
	}

	/**
	 * compile set of clauses to interpreted code
	 * 
	 * @return instance of interpreted code
	 * @throws PrologException
	 */
	PrologCode compilePredicate() throws PrologException
	{
		List<Term> clauses = new ArrayList<Term>();

		int i, n;
		n = passedClauses.size();
		if (n == 0)
		{
			iFail();
		}
		else
		{
			Iterator<Term> iclauses;
			// dereference all clauses, it will simplify analysis a bit
			for (iclauses = passedClauses.iterator(); iclauses.hasNext();)
			{
				clauses.add(rdereferenced(iclauses.next()));
			}
			// get number of reserved variables
			numberOfReserved = 1; // init number of reserved variable, 1 is reserved
			// for cut
			for (iclauses = clauses.iterator(); iclauses.hasNext();)
			{
				Term term = iclauses.next();
				if (term instanceof CompoundTerm)
				{
					CompoundTerm ct = (CompoundTerm) term;
					if (ct.tag == TermConstants.clauseTag)
					{
						numberOfReserved += getReservedEnvironemt(ct.args[1]);
					}
				}
			}
			// get number of variables
			int environmentSize = numberOfReserved;
			for (iclauses = clauses.iterator(); iclauses.hasNext();)
			{
				environmentSize = getAllVariables(iclauses.next(), environmentSize);
			}

			// compile predicate
			// predicate prefix
			iAllocate(environmentSize, numberOfReserved);
			int envPos = allocReserved();
			iSaveCut(envPos);
			pushCutPosition(envPos);
			// compile clauses
			n = clauses.size();
			if (n > 1) // if more then one clause
			{
				List<IJump> jumps = new ArrayList<IJump>();
				RetryInstruction prv = iTryMeElse(-1);
				compileClause(clauses.get(0));
				jumps.add(iJump(-1));
				for (i = 1; i < n - 1; i++)
				{
					prv.retryPosition = currentCodePosition;
					prv = iRetryMeElse(-1);
					compileClause(clauses.get(i));
					jumps.add(iJump(-1));
				}
				prv.retryPosition = currentCodePosition;
				iTrustMe();
				compileClause(clauses.get(n - 1));
				for (IJump jump2 : jumps)
				{
					IJump jump = jump2;
					jump.jumpPosition = currentCodePosition;
				}
			}
			else
			// there is just one clause
			{
				compileClause(clauses.get(0));
			}
			iReturn();
			popCutPosition();
		}
		// predicate compilation finished, construct InterpretedCode
		Instruction instr[] = code.toArray(instructionArrayConstant);
		ExceptionHandlerInfo ehs[] = exceptionHandlers.toArray(exceptionHandlerArrayConstant);
		return new InterpretedByteCode(codeTag, instr, ehs);
		// return new InterpretedCode(codeTag, instr, ehs);
	}

	/**
	 * get index of variable in environment
	 * 
	 * @param term
	 * @return the index in the environment for the variable
	 */
	int getEnvironmentIndex(VariableTerm term)
	{
		return (variableToEnvironmentIndex.get(term)).intValue();
	}

	/**
	 * push cut position
	 * 
	 * @param envPos
	 */
	void pushCutPosition(int envPos)
	{
		cutPositionStack.add(Integer.valueOf(envPos));
	}

	/**
	 * pop cut position
	 * 
	 * @return the popped position of the cut
	 */
	int popCutPosition()
	{
		return (cutPositionStack.remove(cutPositionStack.size() - 1)).intValue();
	}

	/**
	 * get current cut position
	 * 
	 * @return the current cut position
	 */
	int getCutPosition()
	{
		return (cutPositionStack.get(cutPositionStack.size() - 1)).intValue();
	}

	// instructions
	/**
	 * add instruction
	 * 
	 * @param i
	 *          Instruction to add
	 */
	void addInstruction(Instruction i)
	{
		i.codePosition = currentCodePosition;
		code.add(i);
		currentCodePosition++;
	}

	/**
	 * add allocate instruction
	 * 
	 * @param environmentSize
	 * @param numberOfReserved
	 * @return instruction which has been added
	 */
	IAllocate iAllocate(int environmentSize, int numberOfReserved)
	{
		IAllocate rc = new IAllocate(environmentSize, numberOfReserved);
		addInstruction(rc);
		return rc;
	}

	/**
	 * add call instruction
	 * 
	 * @param tag
	 * @return instruction which has been added
	 */
	ICall iCall(CompoundTermTag tag)
	{
		ICall rc = new ICall(tag);
		addInstruction(rc);
		return rc;
	}

	/**
	 * add create compound tag instruction
	 * 
	 * @param tag
	 * @return instruction which has been added
	 */
	ICreateCompoundTerm iCreateCompoundTerm(CompoundTermTag tag)
	{
		ICreateCompoundTerm rc = new ICreateCompoundTerm(tag);
		addInstruction(rc);
		return rc;
	}

	/**
	 * add cut instruction
	 * 
	 * @param envPos
	 * @return instruction which has been added
	 */
	ICut iCut(int envPos)
	{
		ICut rc = new ICut(envPos);
		addInstruction(rc);
		return rc;
	}

	/**
	 * add fail instruction
	 * 
	 * @return instruction which has been added
	 */
	IFail iFail()
	{
		IFail rc = new IFail();
		addInstruction(rc);
		return rc;
	}

	/**
	 * add jump instruction
	 * 
	 * @param pos
	 * @return instruction which has been added
	 */
	IJump iJump(int pos)
	{
		IJump rc = new IJump(pos);
		addInstruction(rc);
		return rc;
	}

	/**
	 * add push argument instruction
	 * 
	 * @param i
	 * @return instruction which has been added
	 */
	IPushArgument iPushArgument(int i)
	{
		IPushArgument rc = new IPushArgument(i);
		addInstruction(rc);
		return rc;
	}

	/**
	 * push constant
	 * 
	 * @param term
	 * @return instruction which has been added
	 */
	IPushConstant iPushConstant(AtomicTerm term)
	{
		IPushConstant rc = new IPushConstant(term);
		addInstruction(rc);
		return rc;
	}

	/**
	 * push term from environment
	 * 
	 * @param envIdx
	 * @return instruction which has been added
	 */
	IPushEnvironment iPushEnvironment(int envIdx)
	{
		IPushEnvironment rc = new IPushEnvironment(envIdx);
		addInstruction(rc);
		return rc;
	}

	/**
	 * add retry me else instruction
	 * 
	 * @param retryPos
	 * @return instruction which has been added
	 */
	IRetryMeElse iRetryMeElse(int retryPos)
	{
		IRetryMeElse rc = new IRetryMeElse(retryPos);
		addInstruction(rc);
		return rc;
	}

	/**
	 * add return instruction
	 * 
	 * @return instruction which has been added
	 */
	IReturn iReturn()
	{
		IReturn rc = new IReturn();
		addInstruction(rc);
		return rc;
	}

	/**
	 * add cut instruction
	 * 
	 * @param envPos
	 * @return instruction which has been added
	 */
	ISaveCut iSaveCut(int envPos)
	{
		ISaveCut rc = new ISaveCut(envPos);
		addInstruction(rc);
		return rc;
	}

	IStoreEnvironment iStoreEnvironment(int envPos)
	{
		IStoreEnvironment rc = new IStoreEnvironment(envPos);
		addInstruction(rc);
		return rc;
	}

	/**
	 * add try me else instruction
	 * 
	 * @param retryPos
	 * @return instruction which has been added
	 */
	ITryMeElse iTryMeElse(int retryPos)
	{
		ITryMeElse rc = new ITryMeElse(retryPos);
		addInstruction(rc);
		return rc;
	}

	/**
	 * add throw instruction
	 * 
	 * @return instruction which has been added
	 */
	IThrow iThrow()
	{
		IThrow rc = new IThrow();
		addInstruction(rc);
		return rc;
	}

	/**
	 * add true instruction
	 * 
	 * @return instruction which has been added
	 */
	ITrue iTrue()
	{
		ITrue rc = new ITrue();
		addInstruction(rc);
		return rc;
	}

	/**
	 * add true instruction
	 * 
	 * @return instruction which has been added
	 */
	ITrustMe iTrustMe()
	{
		ITrustMe rc = new ITrustMe();
		addInstruction(rc);
		return rc;
	}

	/**
	 * add fail instruction
	 * 
	 * @return instruction which has been added
	 */
	IUnify iUnify()
	{
		IUnify rc = new IUnify();
		addInstruction(rc);
		return rc;
	}

	/**
	 * add exception handler
	 * 
	 * @param eh
	 */
	void addExceptionHandler(ExceptionHandlerInfo eh)
	{
		exceptionHandlers.add(eh);
	}
}
