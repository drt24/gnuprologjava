
%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%  harness for the validation
%  suite for gnuprologjava
%
%  Jonathan Hodgson.
%
%   2 October 1998.
%


%%%%%%%%%%
%
%   load the utilities.
%

:- ensure_loaded(utils_so).

%%%%%%%%%%%%%%%%%%
%
%  load the database files.
%

:- ensure_loaded(db).

%%%%%%%%%%%%%
%
%   load the test files.
%



:- ensure_loaded(sec74).
:- ensure_loaded(sec78).
:- ensure_loaded(sec82).
:- ensure_loaded(sec83). 
:- ensure_loaded(sec84).
:- ensure_loaded(sec85).
:- ensure_loaded(sec86).
:- ensure_loaded(sec87).
:- ensure_loaded(sec88).
:- ensure_loaded(sec89).
:- ensure_loaded(sec810).
:- ensure_loaded(sec811).
:- ensure_loaded(sec812).
:- ensure_loaded(sec813).
:- ensure_loaded(sec814).
:- ensure_loaded(sec815).
:- ensure_loaded(sec816).

:- ensure_loaded(sec91).
:- ensure_loaded(sec92).   % Contains tests that use the flag maxint.
:- ensure_loaded(sec93).
:- ensure_loaded(sec94).
:- ensure_loaded(sec817).

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
