/**
 * This package contains the TermParser.jj file which is used by javacc to
 * generate the parser found in {@link gnu.prolog.io.parser.gen}.
 * 
 * There is also a TermParser.g file which is an in progress conversion of
 * JavaCC grammar to antlr.
 * 
 * This package also contains classes such as {@link gnu.prolog.io.parser.NameToken NameToken} and
 * {@link gnu.prolog.io.parser.ReaderCharStream ReaderCharStream} and {@link gnu.prolog.io.parser.TermParserUtils TermParserUtils} which contain some of
 * the code needed to integrate the generated parser with the rest of the
 * codebase.
 * 
 * TODO: parser does not properly handle unicode even in comments @see ReaderCharStream
 */
package gnu.prolog.io.parser;

