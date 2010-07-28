/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
 *
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

import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.vm.interpreter.Tracer.TraceLevel;

import java.util.EventObject;

/**
 * The event object send to TracerEventListeners
 * 
 * @author Michiel Hendriks
 */
public class TracerEvent extends EventObject
{
	private static final long serialVersionUID = -3951954998561990757L;

	protected TraceLevel level;
	protected CompoundTermTag tag;
	protected Term[] args;

	public TracerEvent(Tracer tracer, TraceLevel level, CompoundTermTag tag, Term[] args)
	{
		super(tracer);
		this.level = level;
		this.tag = tag;
		this.args = args.clone();
	}

	public Term[] getArgs()
	{
		return args;
	}

	public TraceLevel getLevel()
	{
		return level;
	}

	public CompoundTermTag getTag()
	{
		return tag;
	}

	public Tracer getTracer()
	{
		return (Tracer) getSource();
	}
}
