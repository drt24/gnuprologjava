/* GNU Prolog for Java
 * Copyright (C) 2011  Matt Lilley
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
% Meta-call extensions
%

% setup_call_catcher_cleanup(+Setup, +Call, -Catcher, +Cleanup)
:-build_in(setup_call_catcher_cleanup/4,'gnu.prolog.vm.buildins.meta.Predicate_setup_call_catcher_cleanup'). 

% :(+Module, +Goal)
:-build_in((:)/2,'gnu.prolog.vm.buildins.meta.Predicate_colon').
