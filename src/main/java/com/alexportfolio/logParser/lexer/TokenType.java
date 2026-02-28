package com.alexportfolio.logParser.lexer;

public enum TokenType {
    EOL,UNRESOLVED, UNKNOWN, // internal
    IDENTIFIER, EQUAL, VALUE, LBRACE, RBRACE, LBRACKET, RBRACKET, MULTILINE, LINE, OBJNAME, EOF, NOISE; // grammar tokens

    public static boolean isKnown(TokenType t) {
        if( t == null || t == TokenType.UNKNOWN || t == TokenType.UNRESOLVED ) return false;
        return true;
    }

    public static TokenType getType(char c) {
        return switch (c) {
            case '=' -> TokenType.EQUAL;
            case '{' -> TokenType.LBRACE;
            case '}' -> TokenType.RBRACE;
            case '[' -> TokenType.LBRACKET;
            case ']' -> TokenType.RBRACKET;
            case '\n', '\r' -> TokenType.EOL;
            default -> {
                if(Character.isWhitespace(c)) yield UNKNOWN;
                yield UNRESOLVED;
            }
        };
    }
}
