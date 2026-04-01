package stream.lexlab.logparser.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Lexer {

    private static final Pattern OBJECT_TYPE_PATTERN =
            Pattern.compile("^\\s*[A-Za-z_][A-Za-z0-9_]*<\\d+>\\s*$");

    private final String content;
    private boolean withNoise = false;
    private int cursor = 0;
    private int startIdx = -1;

    private TokenType lastGrammarToken = TokenType.UNKNOWN;
    private final QuoteState quoteState = new QuoteState();
    private boolean multilineActive = false;
    private final List<Token> result = new ArrayList<>();

    private int line = 1;
    private int col = 1;
    private int tokenLine = 0;
    private int tokenCol = 0;

    /**
     * @param input     input text
     * @param withNoise include NOISE tokens in the result
     */
    public Lexer(String input, Boolean withNoise) {
        this(input);
        this.withNoise = withNoise;
    }

    public Lexer(String input) {
        this.content = input.replace("\r\n", "\n") + "\n";
    }

    public List<Token> tokenize() {
        TokenType currToken;

        while ((currToken = nextToken())!=TokenType.EOF) {

            // Start of a multi-character token
            if (currToken == TokenType.UNRESOLVED && startIdx == -1) {
                startIdx = cursor - 1;
                tokenLine = line;
                tokenCol = col;
            }

            // End of a multi-character token
            else if (TokenType.isKnown(currToken) && startIdx >= 0) {
                String rawLexeme = getRawLexeme();
                String lexeme = trimQuotedValue(rawLexeme);
                startIdx = -1;

                TokenType multichar =
                        (currToken == TokenType.EQUAL) ? TokenType.IDENTIFIER : TokenType.VALUE;

                boolean eol = currToken == TokenType.EOL; // EOL is a control token (non-grammar token)

                switch (lastGrammarToken) {
                    case UNKNOWN, NOISE, LBRACE -> {
                        if (eol && looksLikeObjectType(rawLexeme)) {
                            multichar = TokenType.OBJTYPE;
                            lexeme = normalizeObjectType(rawLexeme);
                        } else if (eol) {
                            multichar = TokenType.VALUE;
                        }
                    }
                    case EQUAL -> {
                        if (lexeme.equals("...")) {
                            multichar = TokenType.MULTILINE;
                            multilineActive = true;
                        } else if (eol) {
                            multichar = TokenType.VALUE;
                        }
                    }
                    case MULTILINE, LINE -> {
                        if (eol) {
                            multichar = TokenType.LINE;
                        }
                    }
                }

                if (multichar == TokenType.IDENTIFIER) {
                    lexeme = normalizeFieldName(rawLexeme);
                }

                // Any VALUE without assignment is NOISE
                if (multichar == TokenType.VALUE && lastGrammarToken != TokenType.EQUAL) {
                    multichar = TokenType.NOISE;
                    if(!withNoise) continue;
                }

                result.add(new Token(multichar, lexeme, tokenLine, tokenCol));
                lastGrammarToken = multichar;
            }

            // Single-character token
            if (TokenType.isKnown(currToken)) {
                // Handle absent values
                if (lastGrammarToken == TokenType.EQUAL && currToken == TokenType.EOL) {
                    result.add(new Token(TokenType.VALUE, "null", line, col));
                }

                if (currToken != TokenType.EOL) {
                    result.add(new Token(currToken, line, col));
                    lastGrammarToken = currToken;
                }
            }

        }

        result.add(new Token(TokenType.EOF, line, col));
        return result;
    }

    /**
     * skips whitespaces except the EOL if not inside quotes and returns the next token
     */
    private TokenType nextToken() {
        if (cursor >= content.length()) return TokenType.EOF;

        int c = cursor;
        char ch = content.charAt(c++);
        advancePosition(ch);

        while (
                c < content.length()
                        && Character.isWhitespace(ch)
                        && ch != '\n'
                        && !quoteState.isInside()
        ) {
            ch = content.charAt(c++);
            advancePosition(ch);
        }

        TokenType currToken = quoteState.isInside()
                ? TokenType.UNRESOLVED
                : TokenType.getType(ch);

        // If we are already building a text token, brackets belong to that token.
        if (startIdx >= 0 && (currToken == TokenType.LBRACKET || currToken == TokenType.RBRACKET)) {
            currToken = TokenType.UNRESOLVED;
        }

        // Only classify brackets as grammar tokens when we are not inside a text token.
        if (startIdx < 0)
            if ((currToken == TokenType.LBRACKET && nextGrammarToken() != TokenType.LBRACE)             // '[' must be followed by '{' to be LBRACKET
                    || (currToken == TokenType.RBRACKET && lastGrammarToken != TokenType.RBRACE)) {     // ']' must be preceeded by '}' to be RBRACKET
                currToken = TokenType.UNRESOLVED;
            }


        return currToken;
    }

    /**
     * skips all whitespaces including EOL and returns the next grammar token
     * @return
     */
    private TokenType nextGrammarToken(){
        if (cursor >= content.length()) return TokenType.EOF;
        int c = cursor;
        Character ch = content.charAt(c++);
        var tmpQuoteTracker = new QuoteState(quoteState.inside);
        while(
                c < content.length()
                        && Character.isWhitespace(ch)
                        && !tmpQuoteTracker.trackQuotes(ch)
        )
        {
            ch = content.charAt(c++);
        }
        return TokenType.getType(ch);
    }

    private void advancePosition(char ch) {
        cursor++;
        if (ch == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        quoteState.trackQuotes(ch);
    }

    private String getRawLexeme() {
        return content.substring(startIdx, cursor-1);
    }

    private String trimQuotedValue(String lexeme) {
        if (lexeme.length() >= 2 && lexeme.startsWith("\"") && lexeme.endsWith("\"")) {
            return lexeme.substring(1, lexeme.length() - 1);
        }
        return lexeme;
    }

    private boolean looksLikeObjectType(String lexeme) {
        return OBJECT_TYPE_PATTERN.matcher(lexeme).matches();
    }

    private String normalizeFieldName(String lexeme) {
        return lexeme
                .stripTrailing()
                .replaceFirst("\\[\\d+\\]\\s*$", "");
    }

    private String normalizeObjectType(String lexeme) {
        return lexeme.replaceFirst("<\\d+>\\s*$", "");
    }

    private static class QuoteState {
        private boolean inside = false;
        private int backslashCount = 0;

        boolean isInside() {
            return inside;
        }

        public QuoteState() {
        }

        public QuoteState(boolean inside) {
            this.inside = inside;
        }

        boolean trackQuotes(char ch) {
            boolean wasInside = inside;

            if (ch == '\\') {
                backslashCount++;
            } else if (ch == '"') {
                if (backslashCount % 2 == 0) {
                    inside = !inside;
                }
                backslashCount = 0;
            } else {
                backslashCount = 0;
            }

            return wasInside;
        }
    }
}