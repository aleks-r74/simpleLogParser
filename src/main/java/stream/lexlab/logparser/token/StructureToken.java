package stream.lexlab.logparser.token;

public class StructureToken {
        public Type type;
        public String lexeme;
        public final int line;
        public final int column;

        public StructureToken(StructureToken.Type type, String lexeme, int line, int column){
            this.type = type;
            this.lexeme = lexeme;
            this.line = line;
            this.column = column;
        }

    @Override
    public String toString() {
        return type + "['" + lexeme + "'] @" + line + ":" + column;
    }

    public enum Type {
        // these constants match corresponding Token.Type constants
        EQUAL("="),
        LBRACE("{"),
        RBRACE("}"),
        LBRACKET("["),
        RBRACKET("]"),
        EOL("\\n"),
        EOD("END"),
        // these aren't
        UNRESOLVED("X"),
        EMPTY("E"),
        QUOTE("\""),
        TEXT;

        private String ch;

        Type(String ch){
            this.ch = ch;
        }

        Type(){}

        public String getChar(){
            return this.ch;
        }

        public static Type getType(char c) {
            return switch (c) {
                case '=' -> EQUAL;
                case '{' -> LBRACE;
                case '}' -> RBRACE;
                case '[' -> LBRACKET;
                case ']' -> RBRACKET;
                case '"' -> QUOTE;
                case '\n', '\r' -> EOL;
                default -> {
                    if(Character.isWhitespace(c)) yield EMPTY;
                    yield UNRESOLVED;
                }
            };
        }
    }
}
