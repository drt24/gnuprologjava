/**********************************************************************

12 Test-cases for the reference implementations

12.1 Built-in predicates and user-defined hook predicates

**********************************************************************/

% built-in predicates:

gr_pred_test(phrase(_, _,_), [built_in, static]).

gr_pred_test(phrase(_, _), [built_in, static]).

% simple test predicate:

test_gr_preds :-
	write('Testing existence of built-in predicates'), nl,
	write('and user-defined hook predicates...'), nl, nl,
	gr_pred_test(Pred, ExpectedProps),
	functor(Pred, Functor, Arity),
	write('Testing predicate '), write(Functor/Arity), nl,
	write(' Expected properties: '), write(ExpectedProps), nl,
	findall(Prop, predicate_property(Pred, Prop), ActualProps),
	write(' Actual properties: '), write(ActualProps), nl,
	fail.

test_gr_preds.

/***********************************************************************

12.2  phrase/2-3  built-in predicate tests

Tests needed!

************************************************************************/

/************************************************************************

12.3 Grammar-rule translator tests

Know any hard to translate grammar rules? Contribute them! When checking 
compliance of a particular grammar rule translator, results of the tests 
in this section must be compliant with the logical expansion of grammar 
rules, as specified in section 10.

*************************************************************************/


% terminal tests with list notation:

gr_tr_test(101, (p --> []), success).

gr_tr_test(102, (p --> [b]), success).

gr_tr_test(103, (p --> [abc, xyz]), success).

gr_tr_test(104, (p --> [abc | xyz]), error).

gr_tr_test(105, (p --> [[], {}, 3, 3.2, a(b)]), success).

gr_tr_test(106, (p --> [_]), success).

% terminal tests with string notation:

gr_tr_test(151, (p --> "b"), success).

gr_tr_test(152, (p --> "abc", "q"), success).

gr_tr_test(153, (p --> "abc" ; "q"), success).

% simple non-terminal tests:

gr_tr_test(201, (p --> b), success).

gr_tr_test(202, (p --> 3), error).

gr_tr_test(203, (p(X) --> b(X)), success).

% conjunction tests:

gr_tr_test(301, (p --> b, c), success).

gr_tr_test(311, (p --> true, c), success).

gr_tr_test(312, (p --> fail, c), success).

gr_tr_test(313, (p(X) --> call(X), c), success).

% disjunction tests:

gr_tr_test(351, (p --> b ; c), success).

gr_tr_test(352, (p --> q ; []), success).

gr_tr_test(353, (p --> [a] ; [b]), success).

% if-then-else tests:

gr_tr_test(401, (p --> b -> c), success).

gr_tr_test(411, (p --> b -> c; d), success).

gr_tr_test(421, (p --> b -> c1, c2 ; d), success).

gr_tr_test(422, (p --> b -> c ; d1, d2), success).

gr_tr_test(431, (p --> b1, b2 -> c ; d), success).

gr_tr_test(441, (p --> [x] -> [] ; q), success).

% negation tests:

gr_tr_test(451, (p --> \+ b, c), success).

gr_tr_test(452, (p --> b, \+ c, d), success).

 % cut tests:

gr_tr_test(501, (p --> !, [a]), success).

gr_tr_test(502, (p --> b, !, c, d), success).

gr_tr_test(503, (p --> b, !, c ; d), success).

gr_tr_test(504, (p --> [a], !, {fail}), success).

gr_tr_test(505, (p(a), [X] --> !, [X, a], q), success).

gr_tr_test(506, (p --> a, ! ; b), success).

% {}/1 tests:

gr_tr_test(601, (p --> {b}), success).

gr_tr_test(602, (p --> {3}), error).

gr_tr_test(603, (p --> {c,d}), success).

gr_tr_test(604, (p --> '{}'((c,d))), success).

gr_tr_test(605, (p --> {a}, {b}, {c}), success).

gr_tr_test(606, (p --> {q} -> [a] ; [b]), success).

gr_tr_test(607, (p --> {q} -> [] ; b), success).

gr_tr_test(608, (p --> [foo], {write(x)}, [bar]), success).

gr_tr_test(609, (p --> [foo], {write(hello)},{nl}), success).

gr_tr_test(610, (p --> [foo], {write(hello), nl}), success).

% "metacall" tests:

gr_tr_test(701, (p --> X), success).

gr_tr_test(702, (p --> _), success).

% non-terminals corresponding to "graphic" characters

% or built-in operators/predicates:

gr_tr_test(801, ('[' --> b, c), success).

gr_tr_test(802, ('=' --> b, c), success).

% pushback tests:

gr_tr_test(901, (p, [t] --> b, c), success).

gr_tr_test(902, (p, [t] --> b, [t]), success).

gr_tr_test(903, (p, [t] --> b, [s, t]), success).

gr_tr_test(904, (p, [t] --> b, [s], [t]), success).

gr_tr_test(905, (p(X), [X] --> [X]), success).

gr_tr_test(906, (p(X, Y), [X, Y] --> [X, Y]), success).

gr_tr_test(907, (p(a), [X] --> !, [X, a], q), success).

gr_tr_test(908, (p, [a,b] --> [foo], {write(hello), nl}), success).

gr_tr_test(909, (p, [t1], [t2] --> b, c), error).

gr_tr_test(910, (p, b --> b), error).

gr_tr_test(911, ([t], p --> b), error).

gr_tr_test(911, ([t1], p, [t2] --> b), error).

% simple expand_term/2 test predicate:

test_gr_tr :-
	write('Testing expand_term/2 predicate...'), nl, nl,
	gr_tr_test(N, GR, Result),
	write(N), write(': '), writeq(GR), write(' --- '),
	write(Result), write(' expected'), nl,
	( catch(
	expand_term(GR, Clause),
	Error,
	(write(' error: '), write(Error), nl, fail)) ->
	write(' '), writeq(Clause)
	; 	write(' expansion failed!')
	),
	nl, nl,
	fail.

test_gr_tr.

% simple predicate for dumping test grammar rules into a file:
% (restricted to rules whose expansion is expected to succeed)

create_gr_file :-
	write('Creating grammar rules file "gr.pl" ...'),
	open('gr.pl', write, Stream),
	( gr_tr_test(N, GR, success),
	write(Stream, '% '), write(Stream, N),
	write(Stream, ':'), nl(Stream),
	write_canonical(Stream, GR), write(Stream, '.'),
	nl(Stream), fail
	; 	close(Stream)
	),
	write(' created.'), nl.


/************************************************************************

A phrase/3 meta-interpreter

Note that this alternative reference implementation makes it simple to 
report existence errors at the same abstraction level as grammar rules.

*************************************************************************/

phrase(GRBody, S0, S) :-
	phrase(GRBody, Cont, S0, S1),
	( Cont == {} ->
	S = S1
	; 	Cont = !(SBody),
	!,
	phrase(SBody, S1, S)
	).

phrase(GRBody, _, S0, S) :-
	var(GRBody),
	throw(error(instantiation_error, phrase(GRBody, S0, S))).

phrase(GRBody, _, S0, S) :-
	\+ callable(GRBody),
	throw(error(type_error(callable, GRBody), phrase(GRBody, S0,
	 S))).

phrase(GRBody, _, S0, S) :-
	nonvar(S0),
	\+ is_list(S0),
	throw(error(type_error(list, S0), phrase(GRBody, S0, S))).

phrase(GRBody, _, S0, S) :-
	nonvar(S),
	\+ is_list(S),
	throw(error(type_error(list, S), phrase(GRBody, S0, S))).

phrase(!, Cont, S0, S) :-
	!,
	Cont = !({}),
	S = S0.

phrase((GRBody1, GRBody2), Cont, S0, S) :-
	!,
	phrase(GRBody1, ContGRBody1, S0, S1),
	( ContGRBody1 == {} ->
	phrase(GRBody2, Cont, S1, S)
	; 	ContGRBody1 = !(SGRBody1),
	Cont = !((SGRBody1, GRBody2)),
	S = S1
	).

phrase(\+ GRBody, Cont, S0, S) :-
	!,
	\+ phrase(GRBody, S0, S),
	Cont = {},
	S0 = S.

phrase((GRBody1; GRBody2), Cont, S0, S) :-
	!,
	( phrase(GRBody1, Cont, S0, S)
	; phrase(GRBody2, Cont, S0, S)
	).

phrase((GRBody1 -> GRBody2), Cont, S0, S) :-
	!,
	phrase(GRBody1, S0, S1),
	phrase(GRBody2, Cont, S1, S).

phrase({}, Cont, S0, S) :-
	!,
	Cont = {},
	S = S0.

phrase({Goal}, Cont, S0, S) :-
	!,
	call(Goal),
	Cont = {},
	S = S0.

phrase([], Cont, S0, S) :-
	!,
	Cont = {},
	S = S0.

phrase([Head| Tail], Cont, S0, S) :-
	!,
	append([Head| Tail], S, S0),
	Cont = {}.

phrase(GRHead, _, S0, S) :-
	\+ dcg_clause((GRHead --> _)),
	current_prolog_flag(unknown, Value),
	( Value == fail ->
		fail
	; 	( Value == warning ->
		(write('This is an implementation specific warning'),nl)

		; 	functor(GRHead, NonTerminal, Arity)
		),
	  	
	throw(error(
	existence_error(procedure, NonTerminal//Arity),
	phrase(GRHead, S0, S)))
	).

phrase(GRHead, {}, S0, S) :-
	dcg_clause(GRHead, GRBody),
	phrase(GRBody, ContY, S0, S1),
	( ContY == {} ->
	S = S1
	; 	ContY = !(SBody),
	!,
	phrase(SBody, S1, S)
	).

