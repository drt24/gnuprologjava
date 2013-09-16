/**
 * This package contains the TermParser.jj file which is used by javacc 5 to
 * generate the parser found in {@link gnu.prolog.io.parser.gen}.
 * 
 * You only need to make changes in the TermParser.jj file which will reflect
 * the changes automatically in the .gen files and you are done. 
 *
 * However, on using javacc TermParser.jj, you need to make the following changes:
 * 1. The first line of the code of JavaCharStream.java needs to implement CharStream:
 *      --package gnu.prolog.io.parser.gen;
 *	++package gnu.prolog.io.parser;
 *   	++import gnu.prolog.io.parser.gen.CharStream;
 *   	--public class JavaCharStream
 *	++public class JavaCharStream implements CharStream 
 *      ...
 *
 * 2. The method jjFillToken in TermParserToken manager should include the following lines:
 *   	protected Token jjFillToken()
 *   	...
 *   	t = Token.newToken(jjmatchedKind, curTokenImage);
 *   	++t.kind = jjmatchedKind;
 *   	++t.image = curTokenImage;
 *   	...
 *
 * 3. The file Token.java needs the addition of the following two lines of code:
 *	public static Token newToken(int ofKind, String image)
 * 	{
 *   		switch(ofKind)
 *   		{
 *   			++case TermParserConstants.NAME_TOKEN:
 *				++return new gnu.prolog.io.parser.NameToken();
 *     			default : return new Token(ofKind, image);
 *   		}
 * 	}
 *
 * Finding a way so that the .jj file can be edited to get the above changes on its 
 * own could be an improvement and ideas to do the same are welcome by users.
 *
 * There is also a parser branch on github which is the rewriting of parser
 * in ANTLR 4 but ANTLR 4 does not support left-recursion with parameters
 * 
 * This package also contains classes such as
 * {@link gnu.prolog.io.parser.NameToken NameToken},
 * {@link gnu.prolog.io.parser.JavaCharStream JavaCharStream} and
 * {@link gnu.prolog.io.parser.TermParserUtils TermParserUtils} which contain
 * some of the code needed to integrate the generated parser with the rest of
 * the codebase.
 * 
 */
package gnu.prolog.io.parser;
