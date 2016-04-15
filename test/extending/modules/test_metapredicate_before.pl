:-module(a, [test/1]).

test(X):-
	a_meta_predicate(bar, A, goal(A), X).

goal(w).
goal(x).
goal(y).

goal.

goal(a, w).
goal(a, b).

%a_meta_predicate(foo, _, _, wrong).

:-module(b, [a_meta_predicate/4]).

:-meta_predicate(a_meta_predicate(?, ?, 0, ?)).

a_meta_predicate(bar, Template, Goal, X):-
	b_predicate,
	findall(Template, Goal, X).

goal(wrong).
goal(wrong).
goal(wrong).
goal.

goal(wrong, wrong).
goal(wrong, wrong).

b_predicate.