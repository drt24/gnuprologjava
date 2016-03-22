:-module(c, []).

% Except it isnt
predicate_exported_from_b:-
	write('>c:predicate_exported_from_b'), nl,
	predicate_exported_from_a.

local_predicate:-
	write('>c:local_predicate'), nl.


predicate_only_present_in_c.