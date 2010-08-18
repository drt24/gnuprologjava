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
 
/*
	Common dialog options:
	title(+Title)
		the dialog title
	message(+Message)
		the dialog message (not used by dialog_file_save and dialog_file_open)
	selection(+Value)
		the default selection, depends on the dialog
	
	dialog_file_save/dialog_file_open:
	filemask(+Mask)
		Mask is a file mask like: '*.pro' or: '*.pro;*.pl' 
		A description can be given with: Mask(+Description)
		Multiple filemask(+Mask) entries can be used in the options
		It will always contain: filemask('*.*'('All Files'))
		filemask(prolog) is shorthand for: filemask('*.pro;*.pl'('Prolog files'))
		
	dialog_confirm:
	ok, cancel, yes, no, abort, retry, ignore
		The buttons to include, these are also the result. If no buttons are given
		it will default to: ok, cancel
		
	dialog_message, dialog_confirm:
	type(+Type)
		type is one of the following: error, warning, info, question
		if no type is specified it will show a plain message
*/

% Prompt for a filename to save data to. If the dialog is canceled the predicate
% will fail. 
% dialog_file_save(?Filename)
% dialog_file_save(?Filename, +Options)
:-build_in(dialog_file_save/1,'gnu.prolog.vm.buildins.dialogs.Predicate_file_save').
:-build_in(dialog_file_save/2,'gnu.prolog.vm.buildins.dialogs.Predicate_file_save').

% Prompt for a filename to open. If the dialog is canceled the predicate will fail.
% dialog_file_open(?Filename)
% dialog_file_open(?Filename, +Options)
:-build_in(dialog_file_open/1,'gnu.prolog.vm.buildins.dialogs.Predicate_file_open').
:-build_in(dialog_file_open/2,'gnu.prolog.vm.buildins.dialogs.Predicate_file_open').

% Prompt the user to enter some text. If the dialog is canceled the predicate will fail.
% dialog_prompt(?UserInput)
% dialog_prompt(?UserInput, +Options)
:-build_in(dialog_prompt/1,'gnu.prolog.vm.buildins.dialogs.Predicate_prompt').
:-build_in(dialog_prompt/2,'gnu.prolog.vm.buildins.dialogs.Predicate_prompt').

% Prompt the user to press a button. Pressing the cancel button does not result
% in this predicate failing. Closing the dialog (i.e. making no selection) does
% result in a fail.
% dialog_confirm(?Selection)
% dialog_confirm(?Selection, +Options)
:-build_in(dialog_confirm/1,'gnu.prolog.vm.buildins.dialogs.Predicate_confirm').
:-build_in(dialog_confirm/2,'gnu.prolog.vm.buildins.dialogs.Predicate_confirm').

% A simple message dialog
% dialog_message(+Options)
:-build_in(dialog_message/1,'gnu.prolog.vm.buildins.dialogs.Predicate_message').
