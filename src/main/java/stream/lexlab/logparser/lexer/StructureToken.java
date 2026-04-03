package stream.lexlab.logparser.lexer;

public class StructureToken {
        public StructureTokenType type;
        public String lexeme;
        public final int line;
        public final int column;

        public StructureToken(StructureTokenType type, String lexeme, int line, int column){
            this.type = type;
            this.lexeme = lexeme;
            this.line = line;
            this.column = column;
        }

    public StructureToken(StructureTokenType type, int line, int column){
        this.type = type;
        this.lexeme = switch(type){
            case EQUAL -> "=";
            case LBRACE -> "{";
            case RBRACE -> "}";
            case LBRACKET -> "[";
            case RBRACKET -> "]";
            case EOL -> "\\n";
            case EOD -> "END";
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
