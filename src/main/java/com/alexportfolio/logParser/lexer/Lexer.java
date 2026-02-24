package com.alexportfolio.logParser.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String content;

    private int cursor = 0;
    private int startIdx = -1;
    private TokenType lastToken = TokenType.NEWLINE;
    private TokenType lastGrammarToken = TokenType.UNKNOWN;
    private final List<Token> result = new ArrayList<>();

    private int line = 1, col = 1;
    private int tokenLine = 0, tokenCol = 0;

    public Lexer(String input) {
        String normalized = input.replace("\r\n", "\n");
        if (!normalized.endsWith("\n")) {
            normalized += "\n";
        }
        this.content = normalized;
    }

    public List<Token> tokenize() {

        while (cursor < content.length()) {

            char ch = content.charAt(cursor);
            TokenType candidate = TokenType.getType(ch);

            // Start of multi-character token
            if (candidate == TokenType.UNRESOLVED && startIdx == -1) {
                startIdx = cursor;
                tokenLine = line;
                tokenCol = col;
            }

            // End of multi-character token
            else if (candidate != TokenType.UNKNOWN
                    && candidate != TokenType.UNRESOLVED
                    && startIdx >= 0) {

                String lexeme = content.substring(startIdx, cursor).strip();
                startIdx = -1;

                TokenType keyOrVal = (candidate == TokenType.EQUAL) ? TokenType.IDENTIFIER : TokenType.VALUE;
                // detect type of value
                if(keyOrVal == TokenType.VALUE){
                    if(lexeme.contains("...")) {
                        lastGrammarToken = keyOrVal = TokenType.MULTILINE;
                    }
                    else if(lexeme.contains("<")){
                        keyOrVal = TokenType.OBJNAME;
                    }
                    else if(lastGrammarToken != TokenType.EQUAL)
                        keyOrVal = TokenType.LINE;
                    else
                        lastGrammarToken = TokenType.VALUE;
                }
                result.add(new Token(keyOrVal, lexeme, tokenLine, tokenCol));
            }

            // Single-character token
            if (TokenType.isValid(candidate)) {
                // handling absent values
                if(candidate == TokenType.NEWLINE && lastGrammarToken == TokenType.EQUAL)
                    result.add(new Token(TokenType.VALUE,"null", line, col));

                result.add(new Token(candidate, line, col));

                if(candidate!=TokenType.NEWLINE)
                    lastGrammarToken = candidate;

                lastToken = candidate;
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
}