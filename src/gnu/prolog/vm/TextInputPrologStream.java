/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA. The text ol license can be also found 
 * at http://www.gnu.org/copyleft/lgpl.html
 */
package gnu.prolog.vm;
import gnu.prolog.io.ReadOptions;
import gnu.prolog.io.TermReader;
import gnu.prolog.io.WriteOptions;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
public class TextInputPrologStream extends PrologStream
{
  TermReader termReader;
  
  RandomAccessFileReader fileReader;

  public TextInputPrologStream(OpenOptions options, Reader rd) throws PrologException
  {
    super(options);
    termReader = new TermReader(new BufferedReader(rd));
  }
  
  /**
	 * @param options
	 * @param raf
	 */
	public TextInputPrologStream(OpenOptions options, RandomAccessFile raf)
	{
		super(options);
		this.fileReader = new RandomAccessFileReader(raf);
		termReader = new TermReader(this.fileReader);
	}

	// byte io
  public int getByte(Term streamTerm, Interpreter interptreter) throws PrologException
  {
    checkExists();
    PrologException.permissionError(inputAtom, TermConstants.textStreamAtom, streamTerm);
    return 0;
  }
  public int peekByte(Term streamTerm, Interpreter interptreter) throws PrologException
  {
    checkExists();
    PrologException.permissionError(inputAtom, TermConstants.textStreamAtom, streamTerm);
    return 0;
  }
  public void putByte(Term streamTerm, Interpreter interptreter, int _byte) throws PrologException
  {
    checkExists();
    PrologException.permissionError(outputAtom, TermConstants.textStreamAtom, streamTerm);
  }
  
  // repositioning
  public Term getPosition(Term streamTerm, Interpreter interptreter) throws PrologException
  {
  	checkExists();
  	if (fileReader != null)
  	{
  		try
      {
  			return new JavaObjectTerm(Long.valueOf(fileReader.getPosition()));
      }
  		catch(IOException ex)
      {
        PrologException.systemError(ex);
        return null;
      }
  	}
    PrologException.permissionError(repositionAtom, TermConstants.textStreamAtom, streamTerm);
    return null;
  }
  public void setPosition(Term streamTerm, Interpreter interptreter, Term position) throws PrologException
  {
    checkExists();
    if (fileReader != null)
  	{
    	try
      {
        if (reposition == TermConstants.falseAtom)
        {
          PrologException.permissionError(repositionAtom,streamAtom,getStreamTerm());
        }
        if (position instanceof VariableTerm)
        {
          PrologException.instantiationError();
        }
        else if (!(position instanceof JavaObjectTerm))
        {
          PrologException.domainError(TermConstants.streamPositionAtom, position);
        }
        JavaObjectTerm jt = (JavaObjectTerm)position;
        if (!(jt.value  instanceof Long))
        {
          PrologException.domainError(TermConstants.streamPositionAtom, position);
        }
        long pos =  ((Long)jt.value).longValue();
        if (pos > fileReader.size())
        {
          PrologException.domainError(TermConstants.streamPositionAtom, position);
        }
        fileReader.seek(pos);
      }
      catch(IOException ex)
      {
        PrologException.systemError(ex);
      }
      return;
  	}
    PrologException.permissionError(repositionAtom, streamAtom, streamTerm);
  }

  public int getCode(Term streamTerm, Interpreter interptreter) throws PrologException
  {
    checkExists();
    try
    {
      if (endOfStream == pastAtom)
      {
        if (eofAction == errorAtom)
        {
          PrologException.permissionError(inputAtom, TermConstants.pastEndOfStreamAtom, streamTerm);
        }
        else if (eofAction == eofCodeAtom)
        {
          return -1;
        }
      }
      int rc = termReader.read();
      if (rc == -1) // eof 
      {
        endOfStream = pastAtom;
        return -1;
      }
      else
      {
        return rc;
      }
    }
    catch(IOException ex)
    {
      PrologException.systemError(ex);
      return -1;
    }
  }
  
  public int peekCode(Term streamTerm, Interpreter interptreter) throws PrologException
  {
    checkExists();
    try
    {
      if (endOfStream == pastAtom)
      {
        if (eofAction == errorAtom)
        {
          PrologException.permissionError(inputAtom, TermConstants.pastEndOfStreamAtom, streamTerm);
        }
        else if (eofAction == eofCodeAtom)
        {
          return -1;
        }
      }
      termReader.mark(1);
      int rc = termReader.read();
      termReader.reset();
      if (rc == -1) // eof 
      {
        // endOfStream = pastAtom;
        endOfStream = atAtom;
        return -1;
      }
      else
      {
        return rc;
      }
    }
    catch(IOException ex)
    {
      PrologException.systemError(ex);
      return -1;
    }
  }
  
  public void putCode(Term streamTerm, Interpreter interptreter, int code) throws PrologException
  {
    checkExists();
    PrologException.permissionError(outputAtom, streamAtom, streamTerm);
  }

  public void putCodeSequence(Term streamTerm, Interpreter interptreter, String seq) throws PrologException
  {
  	checkExists();
    PrologException.permissionError(outputAtom, streamAtom, streamTerm);  	
  }

  public Term readTerm(Term streamTerm, Interpreter interptreter, ReadOptions options) throws PrologException
  {
    checkExists();
    try
    {
      if (endOfStream == pastAtom)
      {
        if (eofAction == errorAtom)
        {
          PrologException.permissionError(inputAtom, TermConstants.pastEndOfStreamAtom, streamTerm);
        }
        else if (eofAction == eofCodeAtom)
        {
          PrologException.syntaxError(TermConstants.pastEndOfStreamAtom);
        }
      }
      Term term = termReader.readTerm(options);
      if (term == null)
      {
        endOfStream = pastAtom;
        term = PrologStream.endOfFileAtom;
      }
      return term;
    }
    catch(IOException ex)
    {
      PrologException.syntaxError(inputAtom);
      return null;
    }

  }
  
  public void writeTerm(Term streamTerm, Interpreter interptreter, WriteOptions options, Term term) throws PrologException
  {
    checkExists();
    PrologException.permissionError(outputAtom, streamAtom, streamTerm);
  }

  public void flushOutput(Term streamTerm) throws PrologException
  {
    checkExists();
    PrologException.permissionError(outputAtom, streamAtom, streamTerm);
  }
  
  public void close(boolean force) throws PrologException
  {
    checkExists();
    try
    {
      termReader.close();
    }
    catch(IOException ex)
    {
      if (!force)
      {
        PrologException.systemError(ex);
      }
    }
    super.close(force);
    
  }

  public Term getEndOfStreamState() throws PrologException
  {
    try
    {
      if (termReader.ready())
      {
        endOfStream = notAtom;
      }
      if (endOfStream != pastAtom)
      {
        if (eofAction == resetAtom || eofAction == eofCodeAtom)
        {
          endOfStream = atAtom;
        }
      }
    }
    catch(IOException ex)
    {
      PrologException.systemError(ex);
    }
    return super.getEndOfStreamState();
  }


}
  

