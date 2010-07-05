%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                                              %
%   sec817.pro                                 %
%                                              %
%  code to validate prolog flags.              %
%                                              %
%   Started 10:12AM  20/12/1995                %
%                                              %
%                                              %
%  Copyright  J.P.E Hodgson                    %
%             Saint Joseph's University        %
%             Philadelphia.   PA 19131         %
%                                              %
%   May be used freely provided                %
%   acknowledgement is made.                   %
%                                              %
%   Thanks to Ken Bowen of ALS for support     %
%                                              %
%   Version for calypso   1 oct 1998           %
%                                              %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                                              %
%   The standard requires the following        %
%   flags                                      %
%                                              %
%   bounded                                    %
%     values:   true or false                  %
%     default: implementation defined          %
%     determines whether integers              %
%	are bounded or not.  not changeable    %
%                                              %	
%   max_integer                                %
%     default: implementation defined          %
%      not changeable                          %
%                                              %
%   min_integer                                %
%     default: implementation defined          %
%      not changeable                          %
%                                              %
%   integer_rounding_function                  %
%      values: down, toward_zero               %
%      default: implementation defined         %
%      not changeable                          %
%                                              %
%   char_conversion                            %
%      values  on, off                         %
%      default on                              %
%      changeable                              %
%                                              %
%   debug                                      %
%     values on, off                           %
%     default off                              %
%     changeable                               %
%                                              %
%   max_arity                                  %
%      default implementation defined          %
%      not changeable                          %
%                                              %
%   flag_unknown                               %
%      values  error, fail, warning            %
%      default error                           %
%      changeable                              %
%                                              %
%   double_quotes                              %
%      values: chars, codes, atom              %
%      default implmentation defined           %
%      changeable                              %
%                                              %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	   



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                                              %
%   flags are manipulated by  two predicates   %
%                                              %
%   set_prolog_flag(+Flag, @Value)             %
%   current_prolog_flag(?Flag, ?Value)         %
%                                              %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

required_flags([bounded, char_conversion, debug, double_quotes,
        integer_rounding_function, max_arity, max_integer,
        min_integer, unknown]).




%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%   Test to see which flags are present
%   and which are aditional
%

find_flags :-
      defined(current_prolog_flag/2),
      (
       setof(Flag, Val ^ current_prolog_flag(Flag, Val), Flags)
       -> 
      (
      required_flags(Rqd),
      log('Implemented flags:' ), log(Flags),log_nl,
      log('Required Flags: ' ), log(Rqd), log_nl
      )
      ;
     (
     log('No flags implemented.'), log_nl
    )) .
      
find_flags :-
	log('current_prolog_flag not implemented'), log_nl.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%  test of halt/1 
%


test_halt1 :-
	test_true(halt(0)).




test_817 :-
        log('Testing section 8.17: testing flags'), log_nl,
        find_flags,
        log_nl,
        log('testing  halt/1: All other tests completed'), log_nl,
        test_halt1,
        log_nl,
        log('FAIL: halt did not halt'), !.
        
