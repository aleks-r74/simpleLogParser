package com.alexportfolio.logParser.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String content;

    private int cursor = 0;
    private int startIdx = -1;
    private TokenType lastToken = TokenType.NEWLINE;

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

                TokenType typeToEmit;
                if (candidate == TokenType.EQUAL)
                    typeToEmit = TokenType.IDENTIFIER;
                else if (candidate == TokenType.NEWLINE && lastToken != TokenType.EQUAL)
                    typeToEmit = lexeme.endsWith(">") ? TokenType.OBJNAME : TokenType.IDENTIFIER;
                else {
                    // here it's either a direct value, or a MULTILINE token
                    typeToEmit = lexeme.equals("...") ?  TokenType.MULTILINE : TokenType.VALUE;
                }

                result.add(new Token(typeToEmit, lexeme, tokenLine, tokenCol));
            }

            // Single-character token
            if (TokenType.isValid(candidate)) {
                result.add(new Token(candidate, line, col));
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