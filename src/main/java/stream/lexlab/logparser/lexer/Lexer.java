package stream.lexlab.logparser.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private List<StructureToken> tokenize() {
        StructureToken structureToken = nextToken();

        while(structureToken.type != StructureTokenType.EOD) {
            result.add(structureToken);
            structureToken = nextToken();
        }

        result.add(structureToken);
        return result;
    }

    public List<StructureToken> tokenPostProcessor(){
        tokenize();

        List<StructureToken> grammarTokens = new ArrayList<>();
        for(StructureToken st: result){
            switch(lexState){
                case EXPECTS_TYPE   -> handleExpectsType(st, grammarTokens);
                case EXPECTS_KEY    -> handleExpectsKey(st, grammarTokens);
                case EXPECTS_VALUE  -> handleExpectsValue(st, grammarTokens);
                case IN_QUOTES      -> handleInQuotes(st, grammarTokens);
                case IN_MULTILINE   -> handleInMultiline(st, grammarTokens);
            }
        }
        return grammarTokens;
    }

    private void handleExpectsType(StructureToken structureToken, List<StructureToken> grammarTokens){
        if(looksLikeObjectType(structureToken.lexeme)) {
            structureToken.type = StructureTokenType.OBJTYPE;
            grammarTokens.add(structureToken);
            this.lexState = State.EXPECTS_KEY;
        }
    }
    private List<StructureToken> keyHandlerAcc = new ArrayList<>();
    StructureToken mergeAs(List<StructureToken> tokens, StructureTokenType type){
        if(tokens.isEmpty()) throw new IllegalStateException("Empty key");
        var first = tokens.get(0);
        String lexeme;
        if(tokens.size() == 1) lexeme = first.lexeme;
        else lexeme = tokens.stream().map(st->st.lexeme).collect(Collectors.joining());
        return new StructureToken(type, lexeme, first.line, first.column);
    }
    private void handleExpectsKey(StructureToken structureToken, List<StructureToken> grammarTokens){
        if(structureToken.type == StructureTokenType.EOL) return;

        switch(structureToken.type) {
            case EQUAL -> {
                grammarTokens.add(mergeAs(keyHandlerAcc, StructureTokenType.IDENTIFIER));
                grammarTokens.add(structureToken);
                keyHandlerAcc.clear();
                lexState = State.EXPECTS_VALUE;
            }
            case RBRACE, RBRACKET -> { // case when returning from inner object
                if (keyHandlerAcc.isEmpty())
                    grammarTokens.add(structureToken);
                else
                    keyHandlerAcc.add(structureToken);
            }
            default -> keyHandlerAcc.add(structureToken);
        }
    }

    private List<StructureToken> valueHandlerAcc = new ArrayList<>();
    private void handleExpectsValue(StructureToken structureToken, List<StructureToken> grammarTokens){
        switch(structureToken.type){
            case EOL -> {
                if(valueHandlerAcc.isEmpty()) return;
                grammarTokens.add(mergeAs(valueHandlerAcc, StructureTokenType.VALUE));
                valueHandlerAcc.clear();
                this.lexState = State.EXPECTS_KEY;
            }
            case LBRACE -> {
                grammarTokens.add(structureToken);
                valueHandlerAcc.clear();
                this.lexState = State.EXPECTS_TYPE;
            }
            case TEXT -> {
                if (!structureToken.lexeme.equals("...")){
                    valueHandlerAcc.add(structureToken);
                    return;
                }
                structureToken.type = StructureTokenType.MULTILINE;
                grammarTokens.add(structureToken);
                valueHandlerAcc.clear();
                this.lexState = State.IN_MULTILINE;
            }
            case LBRACKET -> {
                grammarTokens.add(structureToken);
            }
            default -> {
                valueHandlerAcc.add(structureToken);
            }
        }

    }
    private void handleInQuotes(StructureToken structureToken, List<StructureToken> grammarTokens){

    }
    private List<StructureToken> multilineHandlerAcc = new ArrayList<>();
    int eolCuonter = -1;
    private void handleInMultiline(StructureToken structureToken, List<StructureToken> grammarTokens){
        // first structure token after MULTILINE is EOL - skip it
        if(structureToken.type == StructureTokenType.EOL && eolCuonter == -1){
            eolCuonter++;
            return;
        }
        // change of state on 2 sequential EOL
        if (structureToken.type == StructureTokenType.EOL){
            if(++eolCuonter >= 2){
                eolCuonter = 0;
                multilineHandlerAcc.clear();
                this.lexState = State.EXPECTS_KEY;
                return;
            }
            grammarTokens.add(mergeAs(multilineHandlerAcc, StructureTokenType.LINE));
            multilineHandlerAcc.clear();
            return;
        }
        else
            eolCuonter = 0;

        multilineHandlerAcc.add(structureToken);
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
            else
                hitUnresolved = true;

            if(tt != StructureTokenType.UNRESOLVED && tt != StructureTokenType.EMPTY)
                break;
            advancePosition(tt);
        } while(true);
        String text = content.substring(start+indentCounter, cursor);
        if(text.length()>0)
            return new StructureToken(StructureTokenType.TEXT, text, tLine, tCol + indentCounter);

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