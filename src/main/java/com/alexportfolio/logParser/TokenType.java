package com.alexportfolio.logParser;

public enum TokenType {
    IDENTIFIER, EQUAL, VALUE, LBRACE, RBRACE, LBRACKET, RBRACKET, NEWLINE, MULTILINE, EOF, PART;
    public static boolean isValid(TokenType t){
        if(t != null && t!= TokenType.PART) return true;
        return false;
    }
    public static TokenType getType(char c){
        return switch (c){
            case '=' -> TokenType.EQUAL;
            case '{' -> TokenType.LBRACE;
            case '}' -> TokenType.RBRACE;
            case '[' -> TokenType.LBRACKET;
            case ']' -> TokenType.RBRACKET;
            case '\n', '\r' -> TokenType.NEWLINE;
            default -> {
                if (Character.isLetterOrDigit(c) || c == '<' || c == '>') yield PART;
                yield null;
            }
        };
    }
}
