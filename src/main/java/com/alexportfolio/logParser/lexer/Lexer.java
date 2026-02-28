package com.alexportfolio.logParser.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String content;
    private boolean withNoise = false;
    private int cursor = 0;
    private int startIdx = -1;

    private TokenType lastGrammarToken = TokenType.UNKNOWN;
    private QuoteState quoteState = new QuoteState();

    private final List<Token> result = new ArrayList<>();

    private int line = 1, col = 1;
    private int tokenLine = 0, tokenCol = 0;

    /**
     * @param input     - input text
     * @param withNoise - include tokens NOISE in the result
     */
    public Lexer(String input, Boolean withNoise) {
        this(input);
        this.withNoise = withNoise;
    }

    public Lexer(String input) {
        String normalized = input.replace("\r\n", "\n");
        if (!normalized.endsWith("\n")) {
            normalized += "\n";
        }
        this.content = normalized
                .replaceAll("(\\[\\d+\\]=)", "=")
                .replaceAll("<\\d+>", "");
    }

    public List<Token> tokenize() {

        while (cursor < content.length()) {

            char ch = content.charAt(cursor);

            TokenType currToken;
            if(quoteState.inQuotes(ch))
                currToken = TokenType.UNRESOLVED;
            else
                currToken = TokenType.getType(ch);

            // '[' counts as LBRACKET only after EQUAL and if we aren't building multichar token,
            // ']' counts as RBRACKET only after RBRACE or RBRACKET.
            boolean nonLBracket = currToken == TokenType.LBRACKET && (lastGrammarToken != TokenType.EQUAL || startIdx != -1);
            boolean nonRBracket = currToken == TokenType.RBRACKET && lastGrammarToken != TokenType.RBRACE && lastGrammarToken != TokenType.LBRACKET;
            if (nonLBracket || nonRBracket)
                currToken = TokenType.UNRESOLVED;

            // Start of multi-character token
            if (currToken == TokenType.UNRESOLVED && startIdx == -1) {
                startIdx = cursor;
                tokenLine = line;
                tokenCol = col;
            }

            // End of multi-character token
            else if (TokenType.isKnown(currToken) && startIdx >= 0) {

                String lexeme = getLexeme();
                startIdx = -1;
                TokenType multichar = (currToken == TokenType.EQUAL) ? TokenType.IDENTIFIER : TokenType.VALUE;

                boolean eol = currToken == TokenType.EOL;

                switch (lastGrammarToken) {
                    case UNKNOWN, LBRACE: {
                        if (eol) multichar = TokenType.OBJNAME;
                        break;
                    }
                    case EQUAL: {
                        if (lexeme.equals("...")) multichar = TokenType.MULTILINE;
                        else if (eol) multichar = TokenType.VALUE;
                        break;
                    }
                    case MULTILINE, LINE: {
                        if (eol) multichar = TokenType.LINE;
                    }
                }
                // any VALUE without assignment is NOISE
                if (multichar == TokenType.VALUE && lastGrammarToken != TokenType.EQUAL) {
                    multichar = TokenType.NOISE;
                    if (!withNoise) continue;
                }

                result.add(new Token(multichar, lexeme, tokenLine, tokenCol));
                lastGrammarToken = multichar;
            }

            // Single-character token
            if (TokenType.isKnown(currToken)) {
                // handling absent values
                if (lastGrammarToken == TokenType.EQUAL && currToken == TokenType.EOL)
                    result.add(new Token(TokenType.VALUE, "null", line, col));

                if (currToken != TokenType.EOL) {
                    result.add(new Token(currToken, line, col));
                    lastGrammarToken = currToken;
                }

            }

            // Advance position
            cursor++;
            if (ch == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }

        result.add(new Token(TokenType.EOF, line, col));
        return result;
    }

    private String getLexeme() {
        String val = content.substring(startIdx, cursor);
        int start = 0;
        int end = val.length();
        if (val.startsWith("\"")) start++;
        if (val.endsWith("\"")) end--;
        return val.substring(start, end);
    }

    private static class QuoteState {
        private boolean inside = false;
        private char prevCh = 0;

        boolean inQuotes(char ch) {
            if (ch == '"' && prevCh != '\\') inside = !inside;
            prevCh = ch;
            return inside;
        }
    }
}