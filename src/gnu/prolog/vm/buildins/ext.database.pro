/* GNU Prolog for Java
 * Copyright (C) 2010       Daniel Thomas
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
% Database functions

% Unifies the name and arity with of all functors with known to the system 
% (including builtin). This can be used to determine if a given predicate
% exists.
% current_functor(?Name, ?Arity)
:-build_in(current_functor/2,'gnu.prolog.vm.buildins.database.Predicate_current_functor').


% As defined in ISO/IEC DTR 13211-1:2006 
%  8.8 Clause retrieval and information

% 8.8.3 predicate property/2
% predicate_property(Head, Property)
% is true iff the procedure associated with
% the argument Head has predicate property Property.

:-build_in(predicate_property/2,'gnu.prolog.vm.buildins.database.Predicate_predicate_property').
