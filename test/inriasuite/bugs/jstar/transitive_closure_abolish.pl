
:- dynamic(b_t_c_a_strictBefore/2).
:- dynamic(b_t_c_a_transitive_closure_continue/1).

b_t_c_a_setup(0).
b_t_c_a_setup(N) :-
	N1 is N - 1,
	N1 >= 0,
	assertz(b_t_c_a_strictBefore(N1,N)),
	b_t_c_a_setup(N1).


%% This adds all transitive facts to the strictBefore/2 order.
b_t_c_a_complete_transitive_closure :-
	b_t_c_a_strictBefore(A,B),
	b_t_c_a_strictBefore(B,C),
	\+ b_t_c_a_strictBefore(A,C),
	assertz(b_t_c_a_strictBefore(A,C)),
	assertz(b_t_c_a_transitive_closure_continue(yes)),
	fail.
b_t_c_a_complete_transitive_closure :-
	( b_t_c_a_transitive_closure_continue(yes) ->
	  abolish(b_t_c_a_transitive_closure_continue/1),
	  assertz(b_t_c_a_transitive_closure_continue(maybe)), %because abolish deletes the predicate too!
	  b_t_c_a_complete_transitive_closure
	;
	  true
	).