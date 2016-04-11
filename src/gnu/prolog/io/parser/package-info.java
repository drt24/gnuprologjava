/**
 * This package contains the TermParser.jj file which is used by javacc 6 to
 * generate the parser found in {@link gnu.prolog.io.parser.gen}.
 * 
 * You only need to make changes in the TermParser.jj file which will reflect
 * the changes automatically in the .gen files and you are done. 
 *
 *
 * There is also a parser branch on github which is the rewriting of parser
 * in ANTLR 4 but ANTLR 4 does not support left-recursion with parameters
 * 
 * This package also contains classes such as
 * {@link gnu.prolog.io.parser.NameToken},
 * {@link gnu.prolog.io.parser.TokenFactory} and
 * {@link gnu.prolog.io.parser.TermParserUtils} which contain
 * some of the code needed to integrate the generated parser with the rest of
 * the codebase.
 * 
 */
package gnu.prolog.io.parser;
