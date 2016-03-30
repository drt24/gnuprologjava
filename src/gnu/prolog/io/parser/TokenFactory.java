package gnu.prolog.io.parser;
import gnu.prolog.io.parser.gen.Token;
import gnu.prolog.io.parser.gen.TermParserConstants;

public class TokenFactory
{
	public static Token newToken(int ofKind, String image)
	{
		switch(ofKind)
		{
		case TermParserConstants.NAME_TOKEN:
			return new gnu.prolog.io.parser.NameToken(ofKind, image);
		default : return new Token(ofKind, image);
		}
	}
}
