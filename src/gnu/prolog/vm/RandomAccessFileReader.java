/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 *
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;

/**
 * 
 * @author Michiel Hendriks
 */
public class RandomAccessFileReader extends Reader
{
	RandomAccessFile raf;

	InputStreamReader rd;

	/**
	 * @param fileReader
	 */
	public RandomAccessFileReader(RandomAccessFile randomaccess)
	{
		super();
		raf = randomaccess;
		createReader();
	}

	private void createReader()
	{
		rd = new InputStreamReader(new InputStream()
		{
			public int read() throws IOException
			{
				return raf.read();
			}
		});
	}

	public void seek(long pos) throws IOException
	{
		raf.seek(pos);
		createReader();
	}

	public long getPosition() throws IOException
	{
		return raf.getFilePointer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException
	{
		rd.close();
		raf.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		return rd.read(cbuf, off, len);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public long size() throws IOException
	{
		return raf.length();
	}
}
