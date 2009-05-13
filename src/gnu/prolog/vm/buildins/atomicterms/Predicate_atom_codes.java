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
package gnu.prolog.vm.buildins.atomicterms;
import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.TermConstants;

/** prolog code 
  */
public class Predicate_atom_codes implements PrologCode
{ 
 
  /** this method is used for execution of code
    * @param interpreter interpreter in which context code is executed 
    * @param backtrackMode true if predicate is called on backtracking and false otherwise
    * @param args arguments of code
    * @return either SUCCESS, SUCCESS_LAST, or FAIL.
    */
  public int execute(Interpreter interpreter, boolean backtrackMode, gnu.prolog.term.Term args[]) 
         throws PrologException
  {
    Term ta = args[0];
    Term tl = args[1];
    if (ta instanceof VariableTerm)
    {
      VariableTerm va = (VariableTerm)ta;
      StringBuffer bu = new StringBuffer();
      Term cur = tl;
      while (cur != TermConstants.emptyListAtom)
      {
        if (cur instanceof VariableTerm)
        {
          PrologException.instantiationError();
        }
        if (!(cur instanceof CompoundTerm))
        {
          PrologException.typeError(TermConstants.listAtom,tl);
        }
        CompoundTerm ct = (CompoundTerm)cur;
        if (ct.tag != TermConstants.listTag)
        {
          PrologException.typeError(TermConstants.listAtom,tl);
        }
        Term head = ct.args[0].dereference();
        cur = ct.args[1].dereference();
        if (head instanceof VariableTerm)
        {
          PrologException.instantiationError();
        }
        if (!(head instanceof IntegerTerm))
        {
          PrologException.representationError(TermConstants.characterCodeAtom); 
        }
        IntegerTerm e = (IntegerTerm)head;
        if (e.value < 0 || 0xffff < e.value )
        {
          PrologException.representationError(TermConstants.characterCodeAtom); 
        }
        bu.append((char)e.value);
      }
      interpreter.addVariableUndo(va);
      va.value = AtomTerm.get(bu.toString());
      return SUCCESS_LAST;
    }
    else if (ta instanceof AtomTerm)
    {
      AtomTerm a = (AtomTerm)ta;
      Term list = TermConstants.emptyListAtom;
      for(int i=a.value.length()-1;i>=0;i--)
      {
        list = CompoundTerm.getList(IntegerTerm.get(a.value.charAt(i)), list);
      }
      return interpreter.unify(list,tl);
    }
    else
    {
      PrologException.typeError(TermConstants.atomAtom,ta);
    }
    return FAIL; // fake return
  }

  /** this method is called when code is installed to the environment
    * code can be installed only for one environment.
    * @param environment environemnt to install the predicate
    */
  public void install(Environment env)
  {

  }

  /** this method is called when code is uninstalled from the environment
    * @param environment environemnt to install the predicate
    */
  public void uninstall(Environment env)
  {
  }
    
}

