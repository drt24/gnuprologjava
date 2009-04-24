/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
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
 
%
% List extensions
%

% Fail if member is not part of the list
% member(?Elem, ?List)
:-build_in(member/2,'gnu.prolog.vm.buildins.list.Predicate_member').

% True if Term is bound to the empty list () or a term with functor `.' 
% and arity 2 and the second argument is a list.
% is_list(+Term)
:-build_in(is_list/1,'gnu.prolog.vm.buildins.list.Predicate_is_list').

% True if Int represents the number of elements of list List. Can be used to 
% create a list holding only variables. 
% length(?List, ?Int)
:-build_in(length/2,'gnu.prolog.vm.buildins.list.Predicate_length').
