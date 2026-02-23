package com.alexportfolio.logParser;

public enum TokenType {
    IDENTIFIER, EQUAL, VALUE, LBRACE, RBRACE, LBRACKET, RBRACKET, NEWLINE, MULTILINE, EOF, UNRESOLVED, UNKNOWN;
    public static boolean isValid(TokenType t) {
        if (t != null && t != TokenType.UNRESOLVED && t != TokenType.UNKNOWN) return true;
        return false;
    }

    public static TokenType getType(char c) {
        return switch (c) {
            case '=' -> TokenType.EQUAL;
            case '{' -> TokenType.LBRACE;
            case '}' -> TokenType.RBRACE;
            case '[' -> TokenType.LBRACKET;
            case ']' -> TokenType.RBRACKET;
            case '\n', '\r' -> TokenType.NEWLINE;
            default -> {
                if(Character.isWhitespace(c)) yield UNKNOWN;
                yield UNRESOLVED;
            }
        };
    }
}
