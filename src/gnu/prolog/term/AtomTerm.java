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
package gnu.prolog.term;
import java.util.HashMap;

/**
 * Atom term. The object of this class represent prolog atom.
 * 
 * To get an instance use the {@link #get(String)} method with the String
 * representation of the atom.
 * 
 * @author Constantin Plotnikov
 * @version 0.0.1
 */
public class AtomTerm extends AtomicTerm
{
  private static final long serialVersionUID = 3977202291132125939L;
  
  /** a map from string to atom */
  private final static HashMap<String,AtomTerm> string2atom = new HashMap<String,AtomTerm>();
  /** empty list atom */
  public final static AtomTerm emptyList = get("[]");
  /** empty curly atom */
  public final static AtomTerm emptyCurly = get("{}");
  /** cut atom */
  public final static AtomTerm cut = get("!");
  
  /** get atom term
   * If there is currently no {AtomTerm} of for that string then one is created. 
    * @param s string reprentation of atom.
    */
  public static AtomTerm get(String s)
  {
    synchronized(string2atom)
    {
      AtomTerm atom = string2atom.get(s);
      if (atom == null)
      {
        atom = new AtomTerm(s);
        string2atom.put(s,atom);
      }
      return atom;
    }
  }
  
  private static StringBuffer chbu = new StringBuffer(1);
  /** get atom term
    * @param s string reprentation of atom.
    */
  public static AtomTerm getChar(char ch)
  {
    synchronized(chbu)
    {
      chbu.setLength(0);
      chbu.append(ch);
      return get(chbu.toString());
    }
  }
  
  /** Return an object to replace the object extracted from the stream.
    * The object will be used in the graph in place of the original.
    * @return resloved object
    * @see java.io.Resolvable
    */
  public Object readResolve()
  {
    return get(value);
  }
  /** value of atom */
  final public String value;
  
  /** a constructor.
   * Users should use {@link #get(String)}
    * @param value value of atom
    */
  private AtomTerm(String value) // constructor is private to package
  {
    this.value = value;
  }

  /** get type of term 
    * @return type of term
    */
  @Override
  public int getTermType()
  {
    return ATOM;
  }

  @Override
public String toString(){
      return value;
  }

}


