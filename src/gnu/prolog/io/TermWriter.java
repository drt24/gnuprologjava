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

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.JavaObjectTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.TermConstants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

/**
 * This class is intendent for printing terms.
 * 
 * @author Constantine A. Plotnikov.
 * @version 0.0.1
 */

public class TermWriter extends PrintWriter
{
	// static variables
	protected static final CompoundTermTag numbervarsTag = CompoundTermTag.get("$VAR", 1);
	protected static final CompoundTermTag curly1Tag = CompoundTermTag.get("{}", 1);
	protected static final OperatorSet defaultOperatorSet = new OperatorSet();
	protected static final WriteOptions defaultWriteOptions = new WriteOptions(defaultOperatorSet);

	static
	{
		defaultWriteOptions.ignoreOps = false;
		defaultWriteOptions.quoted = true;
		defaultWriteOptions.numbervars = false;
	}

	/**
	 * convert term passed as argument to string
	 * 
	 * @param term
	 *          a term to convert
	 * @return String representation of the term
	 */
	public static String toString(Term term)
	{
		try
		{
			StringWriter sout = new StringWriter();
			TermWriter tout = new TermWriter(sout);
			tout.print(term);
			tout.flush();
			return sout.toString();
		}
		catch (Exception ex)
		{
			throw new IllegalArgumentException("BAD TERM: " + ex.toString());
		}
	}

	/**
	 * convert term passed as argument to string
	 * 
	 * @param term
	 *          a term to convert
	 * @param options
	 *          the WriteOptions to use for converting the term to a String
	 * @return String representation of the term
	 */
	public static String toString(Term term, WriteOptions options)
	{
		try
		{
			StringWriter sout = new StringWriter();
			TermWriter tout = new TermWriter(sout);
			tout.print(options, term);
			tout.flush();
			return sout.toString();
		}
		catch (Exception ex)
		{
			throw new IllegalArgumentException("BAD TERM: " + ex.toString());
		}
	}

	/**
	 * create term writer over other writer.
	 * 
	 * @param w
	 *          underlying writer
	 */
	public TermWriter(Writer w)
	{
		super(w, true);
	}

	/**
	 * print term using specified write options
	 * 
	 * @param options
	 *          write options
	 * @param term
	 *          term to print
	 */
	public void print(WriteOptions options, Term term)
	{
		displayTerm(options, 1200, term);
	}

	/**
	 * print term using default write options
	 * 
	 * @param term
	 *          term to print
	 */
	public void print(Term term)
	{
		print((WriteOptions) defaultWriteOptions.clone(), term);
	}

	/**
	 * print term using default write options and specified operator set
	 * 
	 * @param opSet
	 *          operator set to use
	 * @param term
	 *          term to print
	 */
	public void print(OperatorSet opSet, Term term)
	{
		WriteOptions options = new WriteOptions(opSet);
		options.ignoreOps = false;
		options.quoted = true;
		options.numbervars = false;
		print(options, term);
	}

	/**
	 * display term
	 * 
	 * @param options
	 *          current write options
	 * @param priority
	 *          priority of nearest operation, this variable is only defined if
	 *          ignoreOps is false.
	 * @param term
	 *          term to write
	 */
	protected void displayTerm(WriteOptions options, int priority, Term term)
	{
		if (term == null)
		{
			print("<<NULL>>");
			// term = new VariableTerm(); // create anonymous variable
		}
		else
		{
			term = term.dereference();
		}
		if (term instanceof AtomTerm)
		{
			boolean isOp = isOperator(options.operatorSet, (AtomTerm) term) && options.quoted;
			if (isOp)
			{
				print("(");
			}
			displayAtom(options, (AtomTerm) term);
			if (isOp)
			{
				print(")");
			}
		}
		else if (term instanceof IntegerTerm)
		{
			displayInteger(options, (IntegerTerm) term);
		}
		else if (term instanceof FloatTerm)
		{
			displayFloat(options, (FloatTerm) term);
		}
		else if (term instanceof CompoundTerm)
		{
			displayCompound(options, priority, (CompoundTerm) term);
		}
		else if (term instanceof VariableTerm)
		{
			displayVariable(options, (VariableTerm) term);
		}
		else if (term instanceof JavaObjectTerm)
		{
			displayJavaObject(options, (JavaObjectTerm) term);
		}
	}

	/**
	 * display compound term
	 * 
	 * @param options
	 *          current write options
	 * @param priority
	 *          priority of nearest operation, this variable is only defined if
	 *          ignoreOps is false.
	 * @param term
	 *          compound term to write
	 */
	protected void displayCompound(WriteOptions options, int priority, CompoundTerm term)
	{
		// check for numbervars
		if (options.numbervars && term.tag == numbervarsTag && term.args[0] instanceof IntegerTerm)
		{
			int n = ((IntegerTerm) term.args[0]).value;
			print((char) (n % 26 + 'A'));
			print(n / 26);
			return;
		}
		if (!options.ignoreOps)
		{
			if (term.tag == TermConstants.listTag) // if list term
			{
				print('[');
				displayList(options, term);
				print(']');
				return;
			}
			if (term.tag == curly1Tag) // if {}/1 term
			{
				print('{');
				displayTerm(options, 1201, term.args[0]);
				print('}');
				return;
			}
			// operators
			Operator op = options.operatorSet.getOperatorForTag(term.tag);
			if (op != Operator.nonOperator)
			{
				if (op.priority > priority)
				{
					print('(');
				}
				switch (op.specifier)
				{
					case FX:
						displayAtom(options, term.tag.functor);
						print(" ");
						displayTerm(options, op.priority - 1, term.args[0]);
						break;
					case FY:
						displayAtom(options, term.tag.functor);
						print(" ");
						displayTerm(options, op.priority, term.args[0]);
						break;
					case XFX:
						displayTerm(options, op.priority - 1, term.args[0]);
						print(" ");
						displayAtom(options, term.tag.functor);
						print(" ");
						displayTerm(options, op.priority - 1, term.args[1]);
						break;
					case XFY:
						displayTerm(options, op.priority - 1, term.args[0]);
						print(" ");
						displayAtom(options, term.tag.functor);
						print(" ");
						displayTerm(options, op.priority, term.args[1]);
						break;
					case YFX:
						displayTerm(options, op.priority, term.args[0]);
						print(" ");
						displayAtom(options, term.tag.functor);
						print(" ");
						displayTerm(options, op.priority - 1, term.args[1]);
						break;
					case XF:
						displayTerm(options, op.priority - 1, term.args[0]);
						print(" ");
						displayAtom(options, term.tag.functor);
						break;
					case YF:
						displayTerm(options, op.priority, term.args[0]);
						print(" ");
						displayAtom(options, term.tag.functor);
						break;
					default:
						throw new IllegalArgumentException("Wrong operator specifier = " + op.specifier);
				}

				if (op.priority > priority)
				{
					print(')');
				}
				return;
			}
		}

		// canonical form term
		displayAtom(options, term.tag.functor);
		print('(');
		for (int i = 0; i < term.args.length; i++)
		{
			if (i > 0)
			{
				print(",");
			}
			displayTerm(options, 999, term.args[i]);
		}
		print(')');
	}

	/**
	 * display list
	 * 
	 * @param options
	 *          current write options
	 * @param term
	 *          list term to write
	 */
	protected void displayList(WriteOptions options, CompoundTerm term)
	{
		displayTerm(options, 999, term.args[0]);
		Term tail = term.args[1];
		if (tail != null)
		{
			tail = tail.dereference();
		}
		if (tail == TermConstants.emptyListAtom)
		{
			// do nothing
		}
		else if (tail instanceof CompoundTerm && ((CompoundTerm) tail).tag == TermConstants.listTag)
		{
			print(",");
			displayList(options, (CompoundTerm) tail);
		}
		else
		{
			print("|");
			displayTerm(options, 999, tail);
		}
	}

	/**
	 * display float term
	 * 
	 * @param options
	 *          current write options
	 * @param term
	 *          fload term to write
	 */
	protected void displayFloat(WriteOptions options, FloatTerm term)
	{
		if (options.quoted)
		{
			print(term.value); // check later if it is enough
		}
		else
		{
			print(term.value);
		}
	}

	/**
	 * display integer term
	 * 
	 * @param options
	 *          current write options
	 * @param term
	 *          integer term to write
	 */
	protected void displayInteger(WriteOptions options, IntegerTerm term)
	{
		print(term.value);
	}

	/**
	 * display variable term
	 * 
	 * @param options
	 *          current write options
	 * @param variable
	 *          variable to display
	 */
	protected void displayVariable(WriteOptions options, VariableTerm variable)
	{
		if (options.variable2name == null)
		{
			options.variable2name = new HashMap<Term, String>();
		}
		String name = options.variable2name.get(variable);
		if (options.declaredVariableNames && name == null)
		{
			name = variable.name;
		}
		if (name == null)
		{
			int n = options.numberOfVariables++;
			name = "_" + (char) (n % 26 + 'A') + n / 26;
			options.variable2name.put(variable, name);
		}
		print(name);
	}

	protected void displayJavaObject(WriteOptions options, JavaObjectTerm term)
	{
		if (options.javaObjects)
		{
			if (options.javaObjectsToString)
			{
				if (term.value == null)
				{
					print("null");
				}
				else
				{
					print(getSingleQuoted(term.value.toString()));
				}
			}
			else
			{
				print("java_object('");
				if (term.value == null)
				{
					print("null");
				}
				else
				{
					print(term.value.getClass().getName());
					print(" ");
					print(System.identityHashCode(term.value));
				}
				print("')");
			}
		}
	}

	protected static boolean isOperator(OperatorSet set, AtomTerm term)
	{
		Operator fxOp = set.lookupFx(term.value);
		Operator xfOp = set.lookupXf(term.value);
		return fxOp != Operator.nonOperator || xfOp != Operator.nonOperator;
	}

	/**
	 * display atom.
	 * 
	 * @param options
	 *          current write options
	 * @param atom
	 *          atom to display
	 */
	protected void displayAtom(WriteOptions options, AtomTerm atom)
	{
		if (options.quoted)
		{
			String value = atom.value;
			print(needBeQuoted(value) ? getSingleQuoted(value) : value);
		}
		else
		{
			print(atom.value);
		}
	}

	/**
	 * get single quoted string.
	 * 
	 * @param s
	 *          string to quote
	 * @return single quoted string
	 */
	protected static String getSingleQuoted(String s)
	{
		StringBuffer buf = new StringBuffer(s.length() + 6);
		buf.append('\'');
		int i, n = s.length();
		for (i = 0; i < n; i++)
		{
			appendQuotedChar(buf, s.charAt(i), '\'');
		}
		buf.append('\'');
		return buf.toString();
	}

	/**
	 * check if the string is needed to be quoted .
	 * 
	 * @param s
	 *          string to test
	 * @return true if string need to quoted in displayq
	 */
	protected static boolean needBeQuoted(String s)
	{
		if (s.length() == 0)
		{
			return true;
		}
		char ch = s.charAt(0);
		if (isSoloChar(ch))
		{
			// solo char need to be quoted if length of atom >1 or it is %
			return s.length() != 1 || ch == '%';
		}
		else if (isGraphicsChar(ch))
		{
			// graphics need to be quoted if if not all chars are graphics
			int i, n = s.length();
			if (n >= 2 && s.charAt(0) == '/' && s.charAt(1) == '*')
			{
				return true;
			}
			for (i = 1; i < n; i++)
			{
				if (!isGraphicsChar(s.charAt(i)))
				{
					return true;
				}
			}
			return false;
		}
		else if (isAtomStartChar(ch))
		{
			// identifier need to be quoted if if not all chars are alphanmeric
			int i, n = s.length();
			for (i = 1; i < n; i++)
			{
				if (!isAtomChar(s.charAt(i)))
				{
					return true;
				}
			}
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * check if character is solo char.
	 * 
	 * @param c
	 *          character to test
	 * @return true if character is solo char
	 */
	protected static boolean isSoloChar(char c)
	{
		switch (c)
		{
			case ';':
			case '!':
			case '(':
			case ')':
			case ',':
			case '[':
			case ']':
			case '{':
			case '}':
			case '%':
				return true;
			default:
				return false;
		}
	}

	/**
	 * check if character is graphics char.
	 * 
	 * @param ch
	 *          character to test
	 * @return true if character is graphics char
	 */
	protected static boolean isGraphicsChar(char ch)
	{
		switch (ch)
		{
			case '#':
			case '$':
			case '&':
			case '*':
			case '+':
			case '-':
			case '.':
			case '/':
			case ':':
			case '<':
			case '=':
			case '>':
			case '?':
			case '@':
			case '^':
			case '~':
				return true;
			default:
				return false;
		}

	}

	/**
	 * check if character is valid start of atom.
	 * 
	 * @param c
	 *          character to test
	 * @return true if character is valid start of atom.
	 */
	protected static boolean isAtomStartChar(char c)
	{
		return 'a' <= c && c <= 'z';
	}

	/**
	 * check if character is valid continuation of atom.
	 * 
	 * @param c
	 *          character to test
	 * @return true if character is valid continuation of atom.
	 */
	protected static boolean isAtomChar(char c)
	{
		return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || '0' <= c && c <= '9' || c == '_';
	}

	/**
	 * append quoted char to string buffer.
	 * 
	 * @param buf
	 *          buffer to which character is added
	 * @param ch
	 *          character to add
	 * @param quote
	 *          a quote of string
	 */
	protected static void appendQuotedChar(StringBuffer buf, char ch, char quote)
	{
		if (ch == quote) // if quote append "\" quote
		{
			buf.append('\\');
			buf.append(quote);
		}
		else if (ch <= ' ' || ch >= 127) // if control character or non ascii
		{
			buf.append('\\');
			switch (ch)
			{
				case '\u0007':
					buf.append('a');
					break;
				case '\b':
					buf.append('b');
					break;
				case '\f':
					buf.append('f');
					break;
				case '\n':
					buf.append('n');
					break;
				case '\t':
					buf.append('t');
					break;
				case '\u000b':
					buf.append('v');
					break;
				case '\r':
					buf.append('r');
					break;
				default:
					buf.append('x');
					buf.append(Integer.toHexString(ch));
					buf.append('\\');
			}
		}
		else
		{
			switch (ch)
			{
				case '\\':
					buf.append("\\");
				default:
					buf.append(ch);
			}
		}
	}
}
