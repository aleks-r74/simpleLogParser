package stream.lexlab.logparser.token;

import java.util.function.Function;

public class Token {
    public Type type;
    public String lexeme;
    public final int line;
    public final int column;

    public Token(Type type, String lexeme, int line, int column){
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public Token(Type type, int line, int column){
        this.type = type;
        this.lexeme = switch(type){
            case EQUAL -> "=";
            case LBRACE -> "{";
            case RBRACE -> "}";
            case LBRACKET -> "[";
            case RBRACKET -> "]";
            case EOL -> "\n";
            case EOD -> "END";
            default -> throw new IllegalArgumentException("No description for token %s".formatted(type.toString()));
        };
        this.line = line;
        this.column = column;
    }

    public static Token fromTextTokenAs(StructureToken textToken, Type type){
        return new Token(type, textToken.lexeme, textToken.line, textToken.column);
    }

    public static Token fromTextTokenAs(StructureToken textToken, Type type, Function<String, String> normalizer){
        return new Token(type, normalizer.apply(textToken.lexeme), textToken.line, textToken.column);
    }

    public static Token fromStructureToken(StructureToken token){
        var type = fromStructureType(token.type);
        return new Token(type, token.line, token.column);
    }

    @Override
    public String toString() {
        return type + "['" + lexeme + "'] @" + line + ":" + column;
    }

    public enum Type {
        EQUAL,
        LBRACE,
        RBRACE,
        LBRACKET,
        RBRACKET,
        EOL,
        EOD,
        IDENTIFIER,
        VALUE,
        MULTILINE,
        LINE,
        OBJTYPE;
    }

    private static Type fromStructureType(StructureToken.Type type){
        return switch(type){
            case EOL -> Type.EOL;
            case EQUAL -> Type.EQUAL;
            case LBRACE -> Type.LBRACE;
            case RBRACE -> Type.RBRACE;
            case LBRACKET -> Type.LBRACKET;
            case RBRACKET -> Type.RBRACKET;
            case EOD -> Type.EOD;
            default -> throw new IllegalArgumentException("No corresponding GrammarToken for type %s " + type);
        };
    }
}
