:-module(a, [predicate_exported_from_a/1]).

predicate_exported_from_a(X):-
	write('>a:predicate_exported_from_a'), nl,
	local_predicate,
	predicate_with_choicepoint(X).

% Load a module midway through loading another module. This tests that we restore the stack
% of modules after ensure_loaded/1 completes.
:-ensure_loaded(test_module_c).

predicate_only_present_in_a.

% This is defined in every module so we can test that the right one is executed
local_predicate:-
	write('>a:local_predicate'), nl.

% This is a weak override for the one in user
predicate_with_choicepoint(X):-
        member(X, [a,b,c]).


issue_14(a).
issue_14(b).
issue_14(c).
issue_14(X):- b:issue_14(X).
issue_14(d).
issue_14(e).
issue_14(f).