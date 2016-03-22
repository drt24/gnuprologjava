/* This part of the test is defined in user */

:-ensure_loaded(test_module_a).
:-ensure_loaded(test_module_b).

test:-
	% Call a local predicate
	local_predicate,
	% Make sure that if we backtrack into a goal that relies on changing to a new module that we do so correctly
	findall(X, predicate_exported_from_a(X), Xs),
	(  Xs == [a,b,c]
	-> true
	;  write('FAIL: predicate_exported_from_a returns the wrong solutions'), nl
	),
	% Call an exported goal that calls another exported goal
	predicate_exported_from_b,
	catch(predicate_exported_from_c, Error, true),
	(  nonvar(Error)
	-> true
	;  write('FAIL: predicate_exported_from_c should not exist'), nl
	),
	% Explicitly call a goal in another module
	c:local_predicate.

local_predicate:-
	write('>predicate_in_user'), nl.


local_predicate.

% This masks the predicate_with_choicepoint/1 in a. If, on backtracking, the module stack is not restored correctly
% these solutions could end up in the findall in test/0.
predicate_with_choicepoint(x).
predicate_with_choicepoint(y).