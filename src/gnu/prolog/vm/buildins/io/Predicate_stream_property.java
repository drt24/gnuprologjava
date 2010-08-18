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
package gnu.prolog.vm.buildins.io;

import gnu.prolog.io.PrologStream;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.BacktrackInfo;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * prolog code
 */
public class Predicate_stream_property extends ExecuteOnlyCode
{

	private static class StreamPropertyBacktrackInfo extends BacktrackInfo
	{
		StreamPropertyBacktrackInfo()
		{
			super(-1, -1);
		}

		int startUndoPosition;
		Map<PrologStream, List<Term>> stream2option;
		Iterator<PrologStream> streams;
		Term currentStream;
		Iterator<Term> options;

		Term stream;
		Term property;
	}

	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[])
			throws PrologException
	{
		if (backtrackMode)
		{
			StreamPropertyBacktrackInfo bi = (StreamPropertyBacktrackInfo) interpreter.popBacktrackInfo();
			interpreter.undo(bi.startUndoPosition);
			return nextSolution(interpreter, bi);
		}
		else
		{
			Term stream = args[0];
			if (!(stream instanceof VariableTerm))
			{
				if (stream instanceof JavaObjectTerm)
				{
					JavaObjectTerm jt = (JavaObjectTerm) stream;
					if (jt.value instanceof PrologStream)
					{
						PrologStream ps = (PrologStream) jt.value;
						ps.checkExists();
					}
					else
					{
						PrologException.domainError(TermConstants.streamAtom, stream);
					}
				}
				else
				{
					PrologException.domainError(TermConstants.streamAtom, stream);
				}
			}
			Term property = args[1];
			if (property instanceof VariableTerm || property == TermConstants.inputAtom
					|| property == TermConstants.outputAtom)
			{
			}
			else if (property instanceof CompoundTerm)
			{
				CompoundTerm ct = (CompoundTerm) property;
				if (ct.tag == PrologStream.filenameTag)
				{
					if (!(ct.args[0] instanceof AtomTerm || ct.args[0] instanceof VariableTerm))
					{
						PrologException.domainError(TermConstants.streamPropertyAtom, property);
					}
				}
				else if (ct.tag == PrologStream.aliasTag)
				{
					if (!(ct.args[0] instanceof AtomTerm || ct.args[0] instanceof VariableTerm))
					{
						PrologException.domainError(TermConstants.streamPropertyAtom, property);
					}
				}
				else if (ct.tag == PrologStream.endOfStreamTag)
				{
					if (!(ct.args[0] == PrologStream.atAtom || ct.args[0] == PrologStream.pastAtom
							|| ct.args[0] == PrologStream.notAtom || ct.args[0] instanceof VariableTerm))
					{
						PrologException.domainError(TermConstants.streamPropertyAtom, property);
					}
				}
				else if (ct.tag == PrologStream.eofActionTag)
				{
					if (!(ct.args[0] == PrologStream.errorAtom || ct.args[0] == PrologStream.eofCodeAtom
							|| ct.args[0] == PrologStream.resetAtom || ct.args[0] instanceof VariableTerm))
					{
						PrologException.domainError(TermConstants.streamPropertyAtom, property);
					}
				}
				else if (ct.tag == PrologStream.repositionTag)
				{
					if (!(ct.args[0] == TermConstants.trueAtom || ct.args[0] == TermConstants.falseAtom || ct.args[0] instanceof VariableTerm))
					{
						PrologException.domainError(TermConstants.streamPropertyAtom, property);
					}
				}
				else if (ct.tag == PrologStream.positionTag)
				{
					if (!(ct.args[0] == TermConstants.trueAtom || ct.args[0] == TermConstants.falseAtom || ct.args[0] instanceof VariableTerm))
					{
						PrologException.domainError(TermConstants.streamPropertyAtom, property);
					}
				}
				else if (ct.tag == PrologStream.typeTag)
				{
					if (!(ct.args[0] == PrologStream.textAtom || ct.args[0] == PrologStream.binaryAtom || ct.args[0] instanceof VariableTerm))
					{
						PrologException.domainError(TermConstants.streamPropertyAtom, property);
					}
				}
				else
				{
					PrologException.domainError(TermConstants.streamPropertyAtom, property);
				}
			}
			else
			{
				PrologException.domainError(TermConstants.streamPropertyAtom, property);
			}
			StreamPropertyBacktrackInfo bi = new StreamPropertyBacktrackInfo();
			bi.startUndoPosition = interpreter.getUndoPosition();
			bi.stream2option = interpreter.getEnvironment().getStreamProperties();
			bi.streams = bi.stream2option.keySet().iterator();
			bi.stream = args[0];
			bi.property = args[1];
			// bi.currentStream;
			// bi.options;
			return nextSolution(interpreter, bi);
		}
	}

	private int nextSolution(Interpreter interpreter, StreamPropertyBacktrackInfo bi) throws PrologException
	{
		int undoPos = interpreter.getUndoPosition();
		while (true)
		{
			if (bi.options == null || !bi.options.hasNext())
			{
				if (bi.streams.hasNext())
				{
					PrologStream stream = bi.streams.next();
					bi.currentStream = stream.getStreamTerm();
					bi.options = bi.stream2option.get(stream).iterator();
					continue;
				}
				else
				{
					return FAIL;
				}
			}
			Term currentProp = bi.options.next();
			if (interpreter.simpleUnify(bi.stream, bi.currentStream) == SUCCESS_LAST
					&& interpreter.simpleUnify(bi.property, currentProp) == SUCCESS_LAST)
			{
				interpreter.pushBacktrackInfo(bi);
				return SUCCESS;
			}
			interpreter.undo(undoPos);
		}
	}
}
