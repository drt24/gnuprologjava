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
package gnu.prolog.io;

import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;

/**
 * Prolog stream which writes output as text
 * 
 */
public class TextOutputPrologStream extends PrologStream
{
	protected TermWriter termWriter;
	protected RandomAccessFileWriter fileWriter;

	public TextOutputPrologStream(OpenOptions options, Writer wr)
	{
		super(options);
		endOfStream = atAtom;
		termWriter = new TermWriter(wr);
	}

	/**
	 * @param options
	 * @param raf
	 */
	public TextOutputPrologStream(OpenOptions options, RandomAccessFile raf)
	{
		super(options);
		endOfStream = atAtom;
		fileWriter = new RandomAccessFileWriter(raf);
		termWriter = new TermWriter(fileWriter);
	}

	@Override
	public int getByte(Term streamTerm, Interpreter interptreter) throws PrologException
	{
		checkExists();
		PrologException.permissionError(inputAtom, TermConstants.textStreamAtom, streamTerm);
		return 0;
	}

	@Override
	public int peekByte(Term streamTerm, Interpreter interptreter) throws PrologException
	{
		checkExists();
		PrologException.permissionError(inputAtom, TermConstants.textStreamAtom, streamTerm);
		return 0;
	}

	@Override
	public void putByte(Term streamTerm, Interpreter interptreter, int _byte) throws PrologException
	{
		checkExists();
		PrologException.permissionError(outputAtom, TermConstants.textStreamAtom, streamTerm);
	}

	@Override
	public Term getPosition(Term streamTerm, Interpreter interptreter) throws PrologException
	{
		checkExists();
		if (fileWriter != null)
		{
			try
			{
				return new JavaObjectTerm(Long.valueOf(fileWriter.getPosition()));
			}
			catch (IOException ex)
			{
				debug(ex);
				PrologException.systemError(ex);
				return null;
			}
		}
		PrologException.permissionError(repositionAtom, TermConstants.textStreamAtom, streamTerm);
		return null;
	}

	@Override
	public void setPosition(Term streamTerm, Interpreter interptreter, Term position) throws PrologException
	{
		checkExists();
		if (fileWriter != null)
		{
			try
			{
				if (reposition == TermConstants.falseAtom)
				{
					PrologException.permissionError(repositionAtom, streamAtom, getStreamTerm());
				}
				if (position instanceof VariableTerm)
				{
					PrologException.instantiationError();
				}
				else if (!(position instanceof JavaObjectTerm))
				{
					PrologException.domainError(TermConstants.streamPositionAtom, position);
				}
				JavaObjectTerm jt = (JavaObjectTerm) position;
				if (!(jt.value instanceof Long))
				{
					PrologException.domainError(TermConstants.streamPositionAtom, position);
				}
				long pos = ((Long) jt.value).longValue();
				if (pos > fileWriter.size())
				{
					PrologException.domainError(TermConstants.streamPositionAtom, position);
				}
				fileWriter.seek(pos);
			}
			catch (IOException ex)
			{
				debug(ex);
				PrologException.systemError(ex);
			}
			return;
		}
		PrologException.permissionError(repositionAtom, streamAtom, streamTerm);
	}

	@Override
	public int getCode(Term streamTerm, Interpreter interptreter) throws PrologException
	{
		checkExists();
		PrologException.permissionError(inputAtom, streamAtom, streamTerm);
		return 0;
	}

	@Override
	public int peekCode(Term streamTerm, Interpreter interptreter) throws PrologException
	{
		checkExists();
		PrologException.permissionError(inputAtom, streamAtom, streamTerm);
		return 0;
	}

	@Override
	public void putCode(Term streamTerm, Interpreter interptreter, int code) throws PrologException
	{
		termWriter.print((char) code);
	}

	@Override
	public void putCodeSequence(Term streamTerm, Interpreter interptreter, String seq) throws PrologException
	{
		termWriter.print(seq);
	}

	@Override
	public Term readTerm(Term streamTerm, Interpreter interptreter, ReadOptions options) throws PrologException
	{
		PrologException.permissionError(inputAtom, streamAtom, streamTerm);
		return null;
	}

	@Override
	public void writeTerm(Term streamTerm, Interpreter interptreter, WriteOptions options, Term term)
			throws PrologException
	{
		termWriter.print(options, term);
	}

	@Override
	public void flushOutput(Term streamTerm) throws PrologException
	{
		termWriter.flush();
	}

	@Override
	public void close(boolean force) throws PrologException
	{
		try
		{
			termWriter.close();
		}
		catch (Exception ex)
		{
			debug(ex);
			if (!force)
			{
				PrologException.systemError(ex);
			}
		}
		super.close(force);
	}

}
