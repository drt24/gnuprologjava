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
% Miscellaneous extensions
%

% List all perdicates with the given name
% listing(+Pred)
:-build_in(listing/1,'gnu.prolog.vm.buildins.misc.Predicate_listing'). 
:-build_in(listing/0,'gnu.prolog.vm.buildins.misc.Predicate_listing'). 

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

%
% Datastore predicates
%

% Store a set of terms in the data store. An optional ID can be provided.
% When an ID is given it will overwrite the previous value in the datastore.
% Always succeeds.
% ds_store(+List)
% ds_store(+Atom,+List)
:-build_in(ds_store/1,'gnu.prolog.vm.buildins.datastore.Predicate_store').
:-build_in(ds_store/2,'gnu.prolog.vm.buildins.datastore.Predicate_store').

% Append a list of terms to the data store. Instead of overwriting the
% previous list it will append data. Always succeeds.
% ds_append(+Atom,+List)
:-build_in(ds_append/2,'gnu.prolog.vm.buildins.datastore.Predicate_append').

% Remove a previously stored set of terms with the given ID. Always succeeds,
% even when the ID doesn't exist.
% ds_remove(+Atom)
:-build_in(ds_remove/1,'gnu.prolog.vm.buildins.datastore.Predicate_remove').
:-build_in(ds_remove/0,'gnu.prolog.vm.buildins.datastore.Predicate_remove').

% Get the list of stored values from the datastore. Fails if the datastore
% entry was not found. 
% ds_get(?List) -- the last added set
% ds_get(?Atom,?List) -- if ?Atom is a variable it will use the last
%		added set. If the previous stored entry has no id assigned it
%		doesn't unify the term.
:-build_in(ds_get/1,'gnu.prolog.vm.buildins.datastore.Predicate_get').
:-build_in(ds_get/2,'gnu.prolog.vm.buildins.datastore.Predicate_get').

