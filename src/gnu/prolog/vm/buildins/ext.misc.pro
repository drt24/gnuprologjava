/* GNU Prolog for Java
 * Copyright (C) 1997-1999  Constantine Plotnikov
 * Copyright (C) 2009       Michiel Hendriks
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
 
%
% Miscellaneous extensions
%

% List all perdicates with the given name
% listing(+Pred)
:-build_in(listing/1,'gnu.prolog.vm.buildins.misc.Predicate_listing'). 
:-build_in(listing/0,'gnu.prolog.vm.buildins.misc.Predicate_listing').

% Determine or test the Order between two terms in the standard order of terms. 
% Order is one of <, > or =, with the obvious meaning.
% compare(?Order, +Term1, +Term2)
:-build_in(compare/3,'gnu.prolog.vm.buildins.termcomparsion.Predicate_compare').  

% Retrieve the current stacktrace of evaluating predicates (excluding
% the current predicate). Note: it will only contain the compound tags
% of the executed predicates.
% stacktrace(?List)
:-build_in(stacktrace/1,'gnu.prolog.vm.buildins.misc.Predicate_stacktrace').

% Unifies the name and arity with of all functors with known to the system 
% (including builtin). This can be used to determine if a given predicate
% exists.
% current_functor(?Name, ?Arity)
:-build_in(current_functor/2,'gnu.prolog.vm.buildins.misc.Predicate_current_functor').
