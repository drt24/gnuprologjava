%% Simple speed test for Prolog.

:- dynamic(b_t_c_r_strictBefore/2).
:- dynamic(b_t_c_r_transitive_closure_continue/1).

b_t_c_r_setup(0).
b_t_c_r_setup(N) :-
	N1 is N - 1,
	N1 >= 0,
	assertz(b_t_c_r_strictBefore(N1,N)),
	b_t_c_r_setup(N1).


%% This adds all transitive facts to the strictBefore/2 order.
b_t_c_r_complete_transitive_closure :-
	b_t_c_r_strictBefore(A,B),
	b_t_c_r_strictBefore(B,C),
	\+ b_t_c_r_strictBefore(A,C),
	assertz(b_t_c_r_strictBefore(A,C)),
	assertz(b_t_c_r_transitive_closure_continue(yes)),
	fail.
b_t_c_r_complete_transitive_closure :-
	( b_t_c_r_transitive_closure_continue(yes) ->
	  retract(b_t_c_r_transitive_closure_continue(_)),
	  b_t_c_r_complete_transitive_closure
	;
	  true
	).