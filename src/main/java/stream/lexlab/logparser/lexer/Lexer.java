package stream.lexlab.logparser.lexer;

import stream.lexlab.logparser.token.StructureToken;

import java.util.ArrayList;
import java.util.List;


public class Lexer {
    private final String content;
    private int cursor = 0;
    private int line = 1;
    private int col = 1;

    public Lexer(String input) {
        this.content = input.replace("\r\n", "\n") + "\n";
    }

    public List<StructureToken> tokenize() {
        List<StructureToken> structureTokens = new ArrayList<>();
        StructureToken structureToken = nextToken();

        while(structureToken.type != StructureToken.Type.EOD) {
            structureTokens.add(structureToken);
            structureToken = nextToken();
        }

        structureTokens.add(structureToken);
        return structureTokens;
    }


    /**
     * Returns type of the next token without moving the cursor
     * @return
     */
    private StructureToken.Type nextTokenType(){
        if(cursor >= content.length())
            return StructureToken.Type.EOD;
        return StructureToken.Type.getType(content.charAt(cursor));
    }

    private StructureToken nextToken(){
        final int tLine = line;
        final int tCol = col;
        final int start = cursor;
        boolean hitUnresolved = false;
        int indentCounter = 0;
        StructureToken.Type tt;
        do{
            tt = nextTokenType();
            // tracks empty characters before first UNRESOLVED
            if(tt == StructureToken.Type.EMPTY && !hitUnresolved)
                indentCounter++;
            else
                hitUnresolved = true;

            if(tt != StructureToken.Type.UNRESOLVED && tt != StructureToken.Type.EMPTY)
                break;
            advancePosition(tt);
        } while(true);
        String text = content.substring(start+indentCounter, cursor);
        if(!text.isEmpty())
            return new StructureToken(StructureToken.Type.TEXT, text, tLine, tCol + indentCounter);

        advancePosition(tt);
        return new StructureToken(tt, tt.getChar(), tLine, tCol);
    }

    private void advancePosition(StructureToken.Type tt) {
        cursor++;
        if (tt == StructureToken.Type.EOL) {
            line++;
            col = 1;
        } else
            col++;
    }

}