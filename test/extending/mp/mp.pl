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
                ( between(-3,3, N),
                  between(-3,3, D),
                  D =\= 0,
                  M is N rem D
                ),
                [-3 rem-3=:=0,-3 rem-2=:= -1,-3 rem-1=:=0,-3 rem 1=:=0,-3 rem 2=:= -1,-3 rem 3=:=0,-2 rem-3=:= -2,-2 rem-2=:=0,-2 rem-1=:=0,-2 rem 1=:=0,-2 rem 2=:=0,-2 rem 3=:= -2,-1 rem-3=:= -1,-1 rem-2=:= -1,-1 rem-1=:=0,-1 rem 1=:=0,-1 rem 2=:= -1,-1 rem 3=:= -1,0 rem-3=:=0,0 rem-2=:=0,0 rem-1=:=0,0 rem 1=:=0,0 rem 2=:=0,0 rem 3=:=0,1 rem-3=:=1,1 rem-2=:=1,1 rem-1=:=0,1 rem 1=:=0,1 rem 2=:=1,1 rem 3=:=1,2 rem-3=:=2,2 rem-2=:=0,2 rem-1=:=0,2 rem 1=:=0,2 rem 2=:=0,2 rem 3=:=2,3 rem-3=:=0,3 rem-2=:=1,3 rem-1=:=0,3 rem 1=:=0,3 rem 2=:=1,3 rem 3=:=0]).

test_rem_big2(R):- % Expect R =:= -3 if MP
        R is -3 rem (10 ** 50).

test_rem_allq(_):- % Ignore result. If predicate succeeds, test has passed
        ( between(-50,50, X),
          between(-50, 50,Y),
          Y =\= 0, X =\= (X rem Y) + (X // Y) * Y->
            throw(test_failed)
        ; true
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
                ( between(-3,3,N),
                  between(-3,3,D),
                  D =\= 0,
                  M is N mod D
                ),
                [-3 mod-3=:=0,-3 mod-2=:= -1,-3 mod-1=:=0,-3 mod 1=:=0,-3 mod 2=:=1,-3 mod 3=:=0,-2 mod-3=:= -2,-2 mod-2=:=0,-2 mod-1=:=0,-2 mod 1=:=0,-2 mod 2=:=0,-2 mod 3=:=1,-1 mod-3=:= -1,-1 mod-2=:= -1,-1 mod-1=:=0,-1 mod 1=:=0,-1 mod 2=:=1,-1 mod 3=:=2,0 mod-3=:=0,0 mod-2=:=0,0 mod-1=:=0,0 mod 1=:=0,0 mod 2=:=0,0 mod 3=:=0,1 mod-3=:= -2,1 mod-2=:= -1,1 mod-1=:=0,1 mod 1=:=0,1 mod 2=:=1,1 mod 3=:=1,2 mod-3=:= -1,2 mod-2=:=0,2 mod-1=:=0,2 mod 1=:=0,2 mod 2=:=0,2 mod 3=:=2,3 mod-3=:=0,3 mod-2=:= -1,3 mod-1=:=0,3 mod 1=:=0,3 mod 2=:=1,3 mod 3=:=0]).
test_mod_big2(R):- % Expect R =:= 99999999999999999999999999999999999999999999999997 if MP
        R is -3 mod (10**50).

% shift
test_shift_right_large_1(X):- % expect X == 0
	X is 5>>64.
test_shift_right_large_2(X):- % expect X == -1
        X is -5>>64.
test_shift_right_large_3(X):- % expect X == 0
	X is 5>>(1<<62).
test_shift_right_large_4(X):- % expect  X == 0 if MP
	X is 5>>(1<<100).
test_shift_left_large(X):- % expect  X == -1844674407370955161 if MP
	X is (-1<<40)<<24.

% gcd
test_gcd(X):- % expect X == 4
	X is gcd(100, 24).
test_gcd_2(X):- % expect X == 4
        X is gcd(24, 100).

% hyperbolic
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

% rationalize
test_rationalize(R):- % Expect R == 51 rdiv 10 if  MP
        R is rationalize(5.1).

% rational
test_rational(R):- % Expect R == 2871044762448691 rdiv 562949953421312 if  MP
        R is rational(5.1).


% minint
test_minint(bounded, N):-
        float(N).
test_minint(unbounded, N):-
        integer(N),
        N == -9223372036854775808.

minint_tests(_):-
        test_minint(unbounded, -9223372036854775808),
        %test_minint(unbounded, -9 223 372 036 854 775 808),
        test_minint(unbounded,  -0b1000000000000000000000000000000000000000000000000000000000000000),
        %test_minint(-0b10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000),
        test_minint(unbounded, -0o1000000000000000000000),
        %test_minint(-0o10 0000 0000 0000 0000 0000),
        test_minint(unbounded, -0x8000000000000000),
        %test_minint(-0x8000_0000_0000_0000).
        true.


% minint_promotion
test_minint_promotion(bounded, N) :-
        float(N).
test_minint_promotion(unbounded, N):-
        integer(N),
        N == -9223372036854775809.

minint_promotion_tests(_):-
        write(a),
        test_minint_promotion(unbounded, -9223372036854775809),
        write(b),
        %test_minint_promotion(unbounded, -9 223 372 036 854 775 809),
        test_minint_promotion(unbounded, -0b1000000000000000000000000000000000000000000000000000000000000001),
        write(c),
        %test_minint_promotion(unbounded, -0b10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000001),
        test_minint_promotion(unbounded, -0o1000000000000000000001),
        write(d),
        %test_minint_promotion(unbounded, -0o10 0000 0000 0000 0000 0001),
        test_minint_promotion(unbounded, -0x8000000000000001),
        write(e),
        %test_minint_promotion(unbounded, -0x8000_0000_0000_0001).
        true.

minint_promotion_test(A):- % Expect A == -9223372036854775808 if MP
	A is -9223372036854775808-1+1.

% maxint
test_maxint(N) :-
        integer(N),
        N == 9223372036854775807.

maxint_tests(_):-
        test_maxint(9223372036854775807),
        %test_maxint(9 223 372 036 854 775 807),
        test_maxint(0b111111111111111111111111111111111111111111111111111111111111111),
        %test_maxint(0b1111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111),
        test_maxint(0o777777777777777777777),
        %test_maxint(0o7 7777 7777 7777 7777 7777),
        test_maxint(0x7fffffffffffffff),
        %test_maxint(0x7fff_ffff_ffff_ffff).
        true.

% maxint_promotion_tests
test_maxint_promotion(bounded, N) :-
        float(N).
test_maxint_promotion(unbounded, N) :-
        integer(N),
        N == 9223372036854775808.

maxint_promotion_tests(_):-
        test_maxint_promotion(unbounded, 9223372036854775808),
        %test_maxint_promotion(unbounded, 9 223 372 036 854 775 808),
        test_maxint_promotion(unbounded, 0b1000000000000000000000000000000000000000000000000000000000000000),
        %test_maxint_promotion(unbounded, 0b10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000),
        test_maxint_promotion(unbounded, 0o1000000000000000000000),
        %test_maxint_promotion(unbounded, 0o10 0000 0000 0000 0000 0000),
        test_maxint_promotion(unbounded, 0x8000000000000000),
        %test_maxint_promotion(unbounded, 0x8000_0000_0000_0000).
        true.

%  FIXME: Put float_overflow tests here, but only if MP is off

% float_zero
/* FIXME: Parser cannot parse 10e300 :(
min_zero(X) :-
	X is -1.0/10e300/10e300.

test_float_zero_eq(_):-
	min_zero(X),
	X =:= 0.0.
test_float_zero_lt(_):-
        ( min_zero(X),
          X < 0.0->
   throw(test_failed)
        ; true
        ).
test_float_zero_gt(_):-
        ( min_zero(X),
          X > 0.0->
            throw(test_failed)
        ; true
        ).
test_float_zero_eq(_):-
        ( min_zero(X),
          X == 0.0->
            throw(test_failed)
        ; true
        ).
test_float_zero_lt(_) :- % Ignore result
	min_zero(X),
	X @< 0.0.
test_float_zero_gt(_):-
        ( min_zero(X),
          X > 0.0->
            throw(test_failed)
        ; true
        ).
test_float_zero_cmp(R):- % Expect D == (<)
	min_zero(X),
	compare(D, X, 0.0).
test_float_zero_cmp(D):- % Expect D == (>)
	min_zero(X),
	compare(D, 0.0, X).
*/

%  float_special
test_float_zero_cmp(_):-
        ( ( nan > nan
          ; nan =:= nan
          ; nan < nan
          )->
            throw(test_failed)
        ; true
        ).

% arith_misc
test_arith_misc_string(_):-
        0'a =:= "a".