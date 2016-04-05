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
package gnu.prolog.vm.buildins.allsolutions;

import gnu.prolog.database.MetaPredicateInfo;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.TermUtils;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyMetaCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * prolog code
 */
public class Predicate_bagof extends ExecuteOnlyMetaCode
{
	static final CompoundTermTag plusTag = CompoundTermTag.get("+", 2);

	private static class BagOfBacktrackInfo extends BacktrackInfo
	{
		BagOfBacktrackInfo()
		{
			super(-1, -1);
		}

		int startUndoPosition;
		List<Term> solutionList;
		Term witness;
		Term instances;
	}

	@Override
	public RC execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[]) throws PrologException
	{
		if (backtrackMode)
		{
			BagOfBacktrackInfo bi = (BagOfBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			Term ptemplate = args[0];
			Term pgoal = args[1];
			Term pinstances = args[2];
			Predicate_findall.checkList(pinstances);
			Set<Term> wset = new HashSet<Term>();
			Term findallGoal = TermUtils.getFreeVariableSet(pgoal, ptemplate, wset);
			Term witness = TermUtils.getWitness(wset);
			CompoundTerm findallTemplate = new CompoundTerm(plusTag, witness, ptemplate);
			List<Term> list = new ArrayList<Term>();
			RC rc = Predicate_findall.findall(interpreter, false, findallTemplate, findallGoal, list);
			if (rc == RC.FAIL || list.size() == 0)
			{
				return RC.FAIL;
			}
			BagOfBacktrackInfo bi = new BagOfBacktrackInfo();
			bi.startUndoPosition = interpreter.getUndoPosition();
			bi.solutionList = list;
			bi.witness = witness;
			bi.instances = pinstances;
			return nextSolution(interpreter, bi);
		}

	}

	public RC nextSolution(Interpreter interpreter, BagOfBacktrackInfo bi) throws PrologException
	{
		List<Term> curTList = new ArrayList<Term>();
		int undoPos = interpreter.getUndoPosition();
		while (bi.solutionList.size() != 0)
		{
			CompoundTerm curInstance = (CompoundTerm) (bi.solutionList.remove(0)).dereference();
			Term curWitness = curInstance.args[0].dereference();
			RC rc = interpreter.simpleUnify(bi.witness, curWitness);
			if (rc == RC.FAIL)
			{
				throw new IllegalStateException("unexpected unify fail");
			}
			curTList.add(curInstance.args[1].dereference());
			ListIterator<Term> isol = bi.solutionList.listIterator();
			while (isol.hasNext())
			{
				CompoundTerm ct = (CompoundTerm) isol.next();
				Term w = ct.args[0].dereference();
				if (TermUtils.isVariant(curWitness, w))
				{
					rc = interpreter.simpleUnify(bi.witness, w);
					if (rc == RC.FAIL)
					{
						throw new IllegalStateException("unexpected unify fail");
					}
					curTList.add(ct.args[1].dereference());
					isol.remove();
				}
			}
			processList(curTList);
			rc = interpreter.unify(CompoundTerm.getList(curTList), bi.instances.dereference());
			if (rc == RC.SUCCESS_LAST)
			{
				if (bi.solutionList.size() != 0)
				{
					interpreter.pushBacktrackInfo(bi);
					return RC.SUCCESS;
				}
				else
				{
					return RC.SUCCESS_LAST;
				}
			}
			interpreter.undo(undoPos);
			curTList.clear();
		}
		return RC.FAIL;
	}

	protected void processList(List<Term> curTList)
	{}

	private static MetaPredicateInfo metaPredicateInfo = new MetaPredicateInfo(new MetaPredicateInfo.MetaType[]{MetaPredicateInfo.MetaType.NORMAL,
														    MetaPredicateInfo.MetaType.EXISTS,
														    MetaPredicateInfo.MetaType.NORMAL});
	public MetaPredicateInfo getMetaPredicateInfo()
	{
		return metaPredicateInfo;
	}

}
