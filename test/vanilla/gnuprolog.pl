%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%  haness for the validation
%  suite for gnu.prolog
%
%  Jonathan Hodgson.
%
%   2 October 1998.
%


%%%%%%%%%%
%
%   load the utilities.
%

:- ensure_loaded('utils_so.pl').

%%%%%%%%%%%%%%%%%%
%
%  load the database files.
%

:- ensure_loaded('db.pl').

%%%%%%%%%%%%%
%
%   load the test files.
%



:- ensure_loaded('gsec74.pl').     % Directives
:- ensure_loaded('sec78.pl').     % Control Constructs
:- ensure_loaded('sec82.pl').     % Unification
:- ensure_loaded('sec83.pl').     % Types
:- ensure_loaded('sec84.pl').     %   Term comparison
:- ensure_loaded('sec85.pl').     % Term creation and decomposition
:- ensure_loaded('sec86.pl').     %   Arithmetic evaluation
:- ensure_loaded('sec87.pl').     %   Arihmentic comparison
:- ensure_loaded('sec88.pl').     %  Clause retrieval and Information
:- ensure_loaded('sec89.pl').     %  Clause creation and destruction
:- ensure_loaded('sec810.pl').    %  All solutions
:- ensure_loaded('gsec811.pl').    % Stream selection and control
:- ensure_loaded('sec812.pl').    % Character IO.
:- ensure_loaded('sec813.pl').    % Byte IO
:- ensure_loaded('sec814.pl').    % Term IO
:- ensure_loaded('sec815.pl').    % Logic and Control
:- ensure_loaded('sec816.pl').    % Atomic term processing
:- ensure_loaded('sec91.pl').     % Simple arithmentic
:- ensure_loaded('sec92.pl').     % Contains tests that use the flag maxint.
:- ensure_loaded('sec93.pl').     % Other arithmetic functors
:- ensure_loaded('sec94.pl').     % bitwise functors
:- ensure_loaded('gsec817.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%  run the tests
%
%

run_tests:-  
        test_74,
        test_78,
        test_82,
        test_83,    
        test_84,
        test_85,
        test_86,
        test_87,
        test_88,
        test_89,
        test_810,
        test_811,
        test_812,
        test_813,
        test_814,
        test_815,
        test_816,
        test_91,
        test_92,
        test_93,
        test_94,
        test_817.

validate:-
   start_log,
   run_tests,
   end_log.
validate.

member(X, [X|_]).
member(X, [_|Y]):-member(X,Y).
