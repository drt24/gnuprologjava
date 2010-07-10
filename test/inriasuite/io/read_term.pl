:-dynamic(get_steam_for_read_term/1).


:- open('io/read_term.tmp', read, S), asserta(get_stream_for_read_term(S)).

close_stream_for_read_term :- get_stream_for_read_term(S), close(S).

set_read_term_file_contents(Content) :-
        open('io/read_term.tmp', write, S),
        write(S, Content), close(S).

get_read_term_file(S) :- open('io/read_term.tmp', read, S).