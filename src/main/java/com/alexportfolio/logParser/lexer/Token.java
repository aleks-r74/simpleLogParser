package com.alexportfolio.logParser.lexer;

public class Token {
        public final TokenType type;
        public final String lexeme;
        public final int line;
        public final int column;

        public Token(TokenType type, String lexeme, int line, int column){
            this.type = type;
            this.lexeme = lexeme;
            this.line = line;
            this.column = column;
        }

    public Token(TokenType type, int line, int column){
        this.type = type;
        this.lexeme = switch(type){
            case EQUAL -> "=";
            case LBRACE -> "{";
            case RBRACE -> "}";
            case LBRACKET -> "[";
            case RBRACKET -> "]";
            case NEWLINE -> "\\n";
            case EOF -> "END";
            default -> throw new IllegalArgumentException("No description for token %s".formatted(type.toString()));
        };
        this.line = line;
        this.column = column;
    }
    @Override
    public String toString() {
        return type + "['" + lexeme + "'] @" + line + ":" + column;
    }
}
