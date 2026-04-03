package stream.lexlab.logparser.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Lexer {

    private static final Pattern OBJECT_TYPE_PATTERN =
            Pattern.compile("^\\s*[A-Za-z_][A-Za-z0-9_]*<\\d+>\\s*$");

    private final String content;
    private int cursor = 0;
    private State lexState = State.EXPECTS_TYPE;

    private final List<StructureToken> result = new ArrayList<>();

    private int line = 1;
    private int col = 1;

    public Lexer(String input) {
        this.content = input.replace("\r\n", "\n") + "\n";
    }

    public List<StructureToken> tokenize() {
        StructureToken structureToken = nextToken();

        while(structureToken.type != StructureTokenType.EOD) {
            result.add(structureToken);
            structureToken = nextToken();
        }

        result.add(structureToken);
        return result;
    }

    private void handleExpectsType(StructureToken structureToken){
        if(looksLikeObjectType(structureToken.lexeme)) {
            structureToken.type = StructureTokenType.OBJTYPE;
            result.add(structureToken);
        }
    }
    private void handleExpectsKey(StructureToken structureToken){

    }
    private void handleExpectsValue(StructureToken structureToken){

    }
    private void handleInQuotes(StructureToken structureToken){

    }
    private void handleInMultiline(StructureToken structureToken){

    }
    /**
     * Returns type of the next token without moving the cursor
     * @return
     */
    private StructureTokenType nextTokenType(){
        if(cursor >= content.length())
            return StructureTokenType.EOD;
        return StructureTokenType.getType(content.charAt(cursor));
    }

    private StructureToken nextToken(){
        final int tLine = line;
        final int tCol = col;
        final int start = cursor;
        boolean hitUnresolved = false;
        int indentCounter = 0;
        StructureTokenType tt;
        do{
            tt = nextTokenType();
            // tracks empty characters before first UNRESOLVED
            if(tt == StructureTokenType.EMPTY && !hitUnresolved)
                indentCounter++;
            else if(!hitUnresolved)
                hitUnresolved = true;

            if(tt != StructureTokenType.UNRESOLVED && tt != StructureTokenType.EMPTY)
                break;
            advancePosition(tt);
        } while(true);

        if(cursor - start > 1)
            return new StructureToken(StructureTokenType.TEXT, content.substring(start+indentCounter, cursor), tLine, tCol + indentCounter);

        advancePosition(tt);
        return new StructureToken(tt, tt.getChar(), tLine, tCol);
    }

    private void advancePosition(StructureTokenType tt) {
        cursor++;
        if (tt == StructureTokenType.EOL) {
            line++;
            col = 1;
        } else
            col++;
    }


    private String trimQuotedValue(String lexeme) {
        if (lexeme.length() >= 2 && lexeme.startsWith("\"") && lexeme.endsWith("\"")) {
            return lexeme.substring(1, lexeme.length() - 1);
        }
        return lexeme;
    }

    private boolean looksLikeObjectType(String lexeme) {
        return OBJECT_TYPE_PATTERN.matcher(lexeme).matches();
    }

    private String normalizeFieldName(String lexeme) {
        return lexeme
                .stripTrailing()
                .replaceFirst("\\[\\d+\\]\\s*$", "");
    }

    private String normalizeObjectType(String lexeme) {
        return lexeme.replaceFirst("<\\d+>\\s*$", "");
    }


}