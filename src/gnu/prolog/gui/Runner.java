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
package gnu.prolog.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

@Deprecated
public class Runner extends JFrame
{

	/**
	 *
	 */
	private static final long serialVersionUID = 4536135999079155652L;
	private JButton prepareButton = new JButton("Prepare");
	private JButton executeButton = new JButton("Execute");
	private JButton stopButton = new JButton("Stop");
	private JTextArea goalTextArea = new JTextArea();
	private JTextArea resultTextArea = new JTextArea();

	public Runner()
	{
		super("PVM Test Runner");

	}
}
