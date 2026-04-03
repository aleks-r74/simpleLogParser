package stream.lexlab.logparser.lexer;

public enum StructureTokenType {
    EOL("\\n"),UNRESOLVED("X"), EMPTY("E"), EQUAL("="), LBRACE("{"), RBRACE("}"), LBRACKET("["), RBRACKET("]"), EOD("END"), QUOTE("\""), TEXT,
    IDENTIFIER,  VALUE,  MULTILINE, LINE, OBJTYPE; // grammar tokens

    private String ch;

    StructureTokenType(String ch){
        this.ch = ch;
    }

    StructureTokenType(){}

    String getChar(){
        return this.ch;
    }

    public static StructureTokenType getType(char c) {
        return switch (c) {
            case '=' -> StructureTokenType.EQUAL;
            case '{' -> StructureTokenType.LBRACE;
            case '}' -> StructureTokenType.RBRACE;
            case '[' -> StructureTokenType.LBRACKET;
            case ']' -> StructureTokenType.RBRACKET;
            case '"' -> StructureTokenType.QUOTE;
            case '\n', '\r' -> StructureTokenType.EOL;
            default -> {
                if(Character.isWhitespace(c)) yield EMPTY;
                yield UNRESOLVED;
            }
        };
    }
}
