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
% Date/time functions
% Similar to SWI-Prolog's date/time functionality
% http://www.swi-prolog.org/pldoc/doc_for?object=section(3%2c%20%274.34.1%27%2c%20swi(%27%2fdoc%2fManual%2fsystem.html%27))
%

% get_time(-TimeStamp)
:-build_in(get_time/1,'gnu.prolog.vm.buildins.datetime.Predicate_get_time').

% stamp_date_time(+TimeStamp, -DateTime, +TimeZone)
:-build_in(stamp_date_time/3,'gnu.prolog.vm.buildins.datetime.Predicate_stamp_date_time').

% date_time_stamp(+DateTime, -TimeStamp)
:-build_in(date_time_stamp/2,'gnu.prolog.vm.buildins.datetime.Predicate_date_time_stamp').

% date_time_value(?Key, +DateTime, ?Value)
:-build_in(date_time_value/3,'gnu.prolog.vm.buildins.datetime.Predicate_date_time_value').

% format_time(+Out, +Format, +StampOrDateTime)
% Format guidelines: http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html
:-build_in(format_time/3,'gnu.prolog.vm.buildins.datetime.Predicate_format_time').

% format_time(+Out, +Format, +StampOrDateTime, +Locale)
:-build_in(format_time/4,'gnu.prolog.vm.buildins.datetime.Predicate_format_time').

% parse_time(+Text, -Stamp)
% Parses RFC 1123 time
:-build_in(parse_time/2,'gnu.prolog.vm.buildins.datetime.Predicate_parse_time').

% parse_time(+Text, -Stamp, +Format)
:-build_in(parse_time/3,'gnu.prolog.vm.buildins.datetime.Predicate_parse_time').
