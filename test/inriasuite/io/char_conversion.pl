
rw_char_conver(CharOut, ConvertTo, ReadIn) :-
        set_prolog_flag(char_conversion, on),
        open('io/charconv.tmp', write, Sout),
        put_char(Sout, CharOut), write(Sout, '.'), nl(Sout),
        close(Sout),
        char_conversion(CharOut, ConvertTo),
        open('io/charconv.tmp', read, Sin),
        read(Sin, ReadIn),
        char_conversion(CharOut, CharOut),
        set_prolog_flag(char_conversion, off),
        close(Sin),!.
%ensure that the char_conversion is undone again on failure.
rw_char_conver(CharOut, _, _) :-
        char_conversion(CharOut, CharOut),
        set_prolog_flag(char_conversion, off),
        !,fail.

rw_char_conver_term(CharOut, ConvertTo, OutTerm, ReadIn) :-
        set_prolog_flag(char_conversion, on),
        open('io/charconv.tmp', write, Sout),
        write(Sout, OutTerm), write(Sout, '.'), nl(Sout),
        close(Sout),
        char_conversion(CharOut, ConvertTo),
        open('io/charconv.tmp', read, Sin),
        read(Sin, ReadIn),
        char_conversion(CharOut, CharOut),
        set_prolog_flag(char_conversion, off),
        close(Sin),!.
%ensure that the char_conversion is undone again on failure.
rw_char_conver_term(CharOut, _, _, _) :-
        char_conversion(CharOut, CharOut),
        set_prolog_flag(char_conversion, off),
        !,fail.