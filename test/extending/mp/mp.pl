% First a few helpful non-ISO predicates

forall(A, B) :- \+ ( A, \+ B ).

between(Value, Value, Value):- !.
between(Min, Max, Value):-
        ( Value = Min
        ; NewMin is Min + 1,
          between(NewMin, Max, Value)
        ).


div_ok(X, Y) :-
        Q is div(X, Y),
	M is mod(X, Y),
	(   X =:= Y*Q+M
	->  true
        ;   throw(division_failed(X,Y))
        ).


% Then some basic tests


create_biginteger(1099511627776).

create_biginteger_by_exp(X):-
        X is 2 ** 41.

create_rational_by_rdiv(X):-
        X is rdiv(2, 3).

create_canonical_rational_1(X):-
        X is rdiv(4,6).

create_canonical_rational_2(X):-
        X is rdiv(4, -6).

create_canonical_rational_3(X):-
        X is rdiv(-4, -6).



% Then some not-so-basic tests, borrowed from SWI-Prolog
% div
test_div_mod(_) :- % Ignore result
	forall(between(-10, 10, X),
               forall((between(-10, 10, Y), Y =\= 0),
                      div_ok(X, Y))).

test_div_minint(A):- % Expect A == -2147483648
        div_ok(-9223372036854775808, 4294967297),
        A is div(-9223372036854775808, 4294967297).

% gdiv
test_gdiv_minint(X):- % Expect A == 9223372036854775808 if unbounded, evaluation_error(int_overflow) if not
        X is -9223372036854775808 // -1.

% rem

test_rem_small(R):- % Expect R == 2
	R is 5 rem 3.
test_rem_small_divneg(R):- % Expect R == 2
	R is 5 rem -3.
test_rem_small_neg(R):- % Expect R == -2
	R is -5 rem 3.
test_rem_big(R):- % R == 6 if MP
	R is (1<<100) rem 10.
test_rem_big_neg(R):- % Expect R == -6 if MP
        R is -(1<<100) rem 10.

test_rem_exhaust(_) :- % Ignore result. If predicate suceeds, test has passed
        findall(N rem D =:= M,
                ( maplist(between(-3,3),[N,D]),
                  D =\= 0,
                  M is N rem D
                ),
                [-3 rem-3=:=0,-3 rem-2=:= -1,-3 rem-1=:=0,-3 rem 1=:=0,-3 rem 2=:= -1,-3 rem 3=:=0,-2 rem-3=:= -2,-2 rem-2=:=0,-2 rem-1=:=0,-2 rem 1=:=0,-2 rem 2=:=0,-2 rem 3=:= -2,-1 rem-3=:= -1,-1 rem-2=:= -1,-1 rem-1=:=0,-1 rem 1=:=0,-1 rem 2=:= -1,-1 rem 3=:= -1,0 rem-3=:=0,0 rem-2=:=0,0 rem-1=:=0,0 rem 1=:=0,0 rem 2=:=0,0 rem 3=:=0,1 rem-3=:=1,1 rem-2=:=1,1 rem-1=:=0,1 rem 1=:=0,1 rem 2=:=1,1 rem 3=:=1,2 rem-3=:=2,2 rem-2=:=0,2 rem-1=:=0,2 rem 1=:=0,2 rem 2=:=0,2 rem 3=:=2,3 rem-3=:=0,3 rem-2=:=1,3 rem-1=:=0,3 rem 1=:=0,3 rem 2=:=1,3 rem 3=:=0]).

test_rem_big(R):- % Expect R =:= -3 if MP
        R is -3 rem (10^50).

test_rem_allq(_):- % Ignore result. If predicate succeeds, test has passed
        ( maplist(between(-50,50),[X,Y]),
          Y =\= 0, X =\= (X rem Y) + (X // Y) * Y->
            throw(test_failed)
        ; otherwise->
            true
        ).

% mod

test_mod_small(R):- % Expect R == 2
	R is 5 mod 3.
test_mod_small_divneg(R):- % Expect R == -1
	R is 5 mod -3.
test_mod_small_neg(R):- % Expect R == 1
	R is -5 mod 3.
test_mod_big(R):- % Expect R == 6 if MP
	R is (1<<100) mod 10.
test_mod_big_neg(R):- % Expect R == 4 if MP
	R is -(1<<100) mod 10.
test_mod_exhaust(_):-
        findall(N mod D =:= M,
                ( maplist(between(-3,3),[N,D]),
                  D =\= 0,
                  M is N mod D
                ),
                [-3 mod-3=:=0,-3 mod-2=:= -1,-3 mod-1=:=0,-3 mod 1=:=0,-3 mod 2=:=1,-3 mod 3=:=0,-2 mod-3=:= -2,-2 mod-2=:=0,-2 mod-1=:=0,-2 mod 1=:=0,-2 mod 2=:=0,-2 mod 3=:=1,-1 mod-3=:= -1,-1 mod-2=:= -1,-1 mod-1=:=0,-1 mod 1=:=0,-1 mod 2=:=1,-1 mod 3=:=2,0 mod-3=:=0,0 mod-2=:=0,0 mod-1=:=0,0 mod 1=:=0,0 mod 2=:=0,0 mod 3=:=0,1 mod-3=:= -2,1 mod-2=:= -1,1 mod-1=:=0,1 mod 1=:=0,1 mod 2=:=1,1 mod 3=:=1,2 mod-3=:= -1,2 mod-2=:=0,2 mod-1=:=0,2 mod 1=:=0,2 mod 2=:=0,2 mod 3=:=2,3 mod-3=:=0,3 mod-2=:= -1,3 mod-1=:=0,3 mod 1=:=0,3 mod 2=:=1,3 mod 3=:=0]).
test_mod_big(R):- % Expect R =:= 99999999999999999999999999999999999999999999999997 if MP
        R is -3 mod (10**50).


test_shift_right_large(X):- % expect X == 0
	X is 5>>64.
test_shift_right_large(X):- % expect X == -1
        X is -5>>64.
test_shift_right_large(X):- % expect X == 0
	X is 5>>(1<<62).
test_shift_right_large(X):- % expect  X == 0 if MP
	X is 5>>(1<<100).
test_shift_left_large(X):- % expect  X == -18446744073709551616 if MP
	X is (-1<<40)<<24.


test_gcd(X):- % expect X == 4
	X is gcd(100, 24).
test_gcd2(X), % expect X == 4
        X is gcd(24, 100).


round(X, R) :- R is round(X*1000)/1000.

test_hyperbolic_sinh(V):- % Expect V =:= 1.175
        X is sinh(1.0), round(X,V).
test_hyperbolic_cosh(R):- % Expect V =:= 1.543
        X is cosh(1.0), round(X,V).
test_hyperbolic_tanh(R):- % Expect V =:= 0.762
        X is tanh(1.0), round(X,V).
test_hyperbolic_asinh(R):- % Expect V =:= 1.0
        X is asinh(sinh(1.0)), round(X,V).
test_hyperbolic_acosh(R):- % Expect V =:= 1.0
        X is acosh(cosh(1.0)), round(X,V).
test_hyperbolic_atanh(R):- % Expect V =:= 1.0
        X is atanh(tanh(1.0)), round(X,V).


test_rationalize(R):- % Expect R == 51 rdiv 10
        R is rationalize(5.1).

test_minint:-
        integer(N),
        format(atom(A), '~w', [N]),
        A == '-9223372036854775808'.

minint_tests(_):-
        test_minint(-9223372036854775808),
        test_minint(-9 223 372 036 854 775 808),
        test_minint( -0b1000000000000000000000000000000000000000000000000000000000000000),
        test_minint(-0b10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000),
        test_minint(-0o1000000000000000000000),
        test_minint(-0o10 0000 0000 0000 0000 0000),
        test_minint(-0x8000000000000000),
        test_minint(-0x8000_0000_0000_0000).

test_minint_promotion(bounded, N) :-
        float(N).
test_minint_promotion(unbounded, N):-
        integer(N),
        format(atom(A), '~w', [N]),
        A == '-9223372036854775809'.

minint_promotion_tests(_):-
        test_minint_promotion(unbounded, -9223372036854775809),
        test_minint_promotion(unbounded, -9 223 372 036 854 775 809),
        test_minint_promotion(unbounded, -0b1000000000000000000000000000000000000000000000000000000000000001),
        test_minint_promotion(unbounded, -0b10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000001),
        test_minint_promotion(unbounded, -0o1000000000000000000001),
        test_minint_promotion(unbounded, -0o10 0000 0000 0000 0000 0001),
        test_minint_promotion(unbounded, -0x8000000000000001),
        test_minint_promotion(unbounded, -0x8000_0000_0000_0001).

minint_promotion_test(A):- % Expect A == -9223372036854775808 if MP
	A is -9223372036854775808-1+1.

%  maxint
%  maxint_promotion
%  float_overflow (only if MP is off)
%  float_zero
%  float_special
%  arith_misc