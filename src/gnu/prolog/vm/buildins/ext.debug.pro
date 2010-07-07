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
% Debugging extensions
%

% Enable tracing
:-build_in(trace/0,'gnu.prolog.vm.buildins.debug.Predicate_trace'). 

% Disable tracing
:-build_in(notrace/0,'gnu.prolog.vm.buildins.debug.Predicate_notrace'). 

% True when tracing is enabled, or fail when it is not
:-build_in(tracing/0,'gnu.prolog.vm.buildins.debug.Predicate_tracing'). 

% Set a trace point
% spy(+Pred,+Ports)
% @param the predicate to trace
% @param the event to trace (all,call,redo,exit,fail)
%	use +call to add tracing for call events,
%	use -call to remove the trace for call events
%	use call to set tracing for just call events
:-build_in(spy/2,'gnu.prolog.vm.buildins.debug.Predicate_spy'). 
spy(Pred):-spy(Pred,all).
trace(Pred):-spy(Pred,all).
trace(Pred,Opt):-spy(Pred,Opt).

% Remove a trace point
% nospy(+Pred)
% @param the predicate to remove a trace point for
:-build_in(nospy/1,'gnu.prolog.vm.buildins.debug.Predicate_nospy'). 

% Remove all trace points
:-build_in(nospyall/0,'gnu.prolog.vm.buildins.debug.Predicate_nospyall'). 

% Enable debugging (TODO not supported)
%:-build_in(debug/0,'gnu.prolog.vm.buildins.debug.Predicate_debug'). 

% Disable debugging (TODO not supported)
%:-build_in(nodebug/0,'gnu.prolog.vm.buildins.debug.Predicate_nodebug'). 

% Disable debugging (TODO not supported)
:-build_in(debugging/0,'gnu.prolog.vm.buildins.debug.Predicate_debugging'). 

% TODO
%build_in(leash/1,'gnu.prolog.vm.buildins.debug.Predicate_leash'). 

