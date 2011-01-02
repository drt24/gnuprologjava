:- dynamic(b_t_c_strictBefore/2).

b_t_c_setup(0).
b_t_c_setup(N) :-
	N1 is N - 1,
	N1 >= 0,
	assertz(b_t_c_strictBefore(N1,N)),
	b_t_c_setup(N1).


%% This adds all transitive facts to the b_t_c_strictBefore/2 order.
b_t_c_complete_transitive_closure :-
	b_t_c_strictBefore(A,B),
	b_t_c_strictBefore(B,C),
	\+ b_t_c_strictBefore(A,C),
	assertz(b_t_c_strictBefore(A,C)),
	fail.
b_t_c_complete_transitive_closure.