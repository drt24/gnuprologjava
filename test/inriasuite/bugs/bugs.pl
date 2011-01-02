% file from which tests designed to catch specific bugs can be included

:- multifile(file/1).
:- multifile(file/2).

%file(F) :- bugs(N), atom_concat('bugs/',N,F).

file(TF,IF) :-
	bugs(NTF,NIF),
	atom_concat('bugs/',NTF,TF),
	atom_concat('bugs/', NIF, IF).

bugs('jstar/transitive_closure','jstar/transitive_closure.pl').
bugs('jstar/transitive_closure_abolish','jstar/transitive_closure_abolish.pl').
bugs('jstar/transitive_closure_retract','jstar/transitive_closure_retract.pl').