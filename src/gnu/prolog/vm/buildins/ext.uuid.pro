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
% UUID extensions
%

% Create a UUID
% uuid(-UUID) 
%	Create a type 4 UUID (random)
% uuid(-UUID, +Atom)
%	Create a type 3 UUID (md5)
:-build_in(uuid/1,'gnu.prolog.vm.buildins.uuid.Predicate_uuid4'). 
:-build_in(uuid/2,'gnu.prolog.vm.buildins.uuid.Predicate_uuid3').

% Compare two UUIDs. This predicate is compatible with predsort/3
% uuid_compare(-Delta, +UUID, +UUID)
:-build_in(uuid_compare/3,'gnu.prolog.vm.buildins.uuid.Predicate_uuid_compare').

% uuid_version(+UUID, ?Version)
:-build_in(uuid_version/2,'gnu.prolog.vm.buildins.uuid.Predicate_uuid_version').

% uuid_timestamp(+UUID, ?Variant)
:-build_in(uuid_variant/2,'gnu.prolog.vm.buildins.uuid.Predicate_uuid_variant').
