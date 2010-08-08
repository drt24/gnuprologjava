% GNU Prolog for Java - Mental Arithmetic demo
% Copyright (C) 2010 Daniel Thomas
% This library is free software; you can redistribute it and/or
% modify it under the terms of the GNU Library General Public
% License as published by the Free Software Foundation; either
% version 3 of the License, or (at your option) any later version.
% This library is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
% Library General Public License for more details.

% You should have received a copy of the GNU Library General Public
% License along with this library; if not, write to the
% Free Software Foundation, Inc., 59 Temple Place - Suite 330,
% Boston, MA  02111-1307, USA. The text of license can be also found 
% at http://www.gnu.org/copyleft/lgpl.html


%arithmetic(+Limit, +Length, -List, -Answer) Given the Limit which is the maximum 
%  size of answer to use, find a sequence of operations of the given Length which 
%  produces an answer less than Limit and return the list of operations in List 
%  and the Answer in Answer.
arithmetic(Limit, Length, [S|List], Answer) :- S is random(Limit), mental(Limit, Length, S, List, Answer).

%mental(+Limit, +Length, +CurrentAnswer, -List, -Answer) recursively compute the List
%  and Answer that satisfy the Limit and Length constraints using the CurrentAnswer as
%  a starting point.
mental(_, 0, C, [], C) :- !. % Length is 0 and so we are done.
mental(I, L, C, [O,N|T], A) :- pickOperation(I, C, O, N, NC), NL is L-1 , mental(I,NL, NC, T, A). 

%pickOperation(+Limit, +CurrentAnswer, -Operation, -Number, -Answer) Pick an operation and number
pickOperation(L,C,O,N,A) :- repeat, P is random(4), innerPickOperation(P,L,C,O,N,A).
%I*2 to reduce probability of operation being picked and I//2 to increase probability.
innerPickOperation(0,I, C, '+', N, A) :- N is random(I), A is C + N, A < I.
innerPickOperation(1,I, C, '-', N, A) :- N is random(I), A is C - N, A > 0.
innerPickOperation(2,I, C, '*', N, A) :- L is I//C, N is random(L), N > 1, A is C * N, A < I.
innerPickOperation(3,_, C, '/', N, A) :- factors(C,L), member(N, L), N > 1, A is C // N.

%factors(+N, -List) list of factors of N
factors(N, List) :- S is ceiling(N / 2), recFactors(N, S, List).
recFactors(_, C, []) :- C =< 1,!. %No more factors
recFactors(N, C, [C|T]) :- 0 is N mod C, !, NC is C -1, recFactors(N,NC,T).
%we could perhaps gain 2 factors here and speed things up
recFactors(N, C, L) :- NC is C -1, recFactors(N, NC, L).%case where C is not a factor.

:- factors(1,[]), factors(0,[]), factors(2,[]), factors(3,[]), factors(4,[2]),
        factors(5,[]), factors(6,[3,2]), factors(7,[]), factors(8,[4,2]), 
        factors(9,[3]), factors(10,[5,2]), factors(11,[]), factors(12,[6,4,3,2]).
