package stream.lexlab.logparser.lexer;

import stream.lexlab.logparser.token.Token;
import stream.lexlab.logparser.token.StructureToken;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Lexer {

    private static final Pattern OBJECT_TYPE_PATTERN =
            Pattern.compile("^\\s*[A-Za-z_][A-Za-z0-9_]*<\\d+>\\s*$");

    private final String content;

    private State lexState = State.EXPECTS_TYPE;

    private final List<StructureToken> structureTokens = new ArrayList<>();
    private final List<Token> grammarTokens = new ArrayList<>();

    private int cursor = 0;
    private int line = 1;
    private int col = 1;

    public Lexer(String input) {
        this.content = input.replace("\r\n", "\n") + "\n";
    }

    private List<StructureToken> tokenize() {
        StructureToken structureToken = nextToken();

        while(structureToken.type != StructureToken.Type.EOD) {
            structureTokens.add(structureToken);
            structureToken = nextToken();
        }

        structureTokens.add(structureToken);
        return structureTokens;
    }

    public List<Token> tokenPostProcessor(){
        tokenize();

        for(StructureToken structToken: structureTokens){
            switch(lexState){
                case EXPECTS_TYPE   -> handleExpectsType(structToken);
                case EXPECTS_KEY    -> handleExpectsKey(structToken);
                case EXPECTS_VALUE  -> handleExpectsValue(structToken);
                case IN_QUOTES      -> handleInQuotes(structToken);
                case IN_MULTILINE   -> handleInMultiline(structToken);
            }
        }
        return grammarTokens;
    }

    private void handleExpectsType(StructureToken structureToken){
        if(looksLikeObjectType(structureToken.lexeme)) {
            grammarTokens.add(Token.fromTextTokenAs(structureToken, Token.Type.OBJTYPE));
            this.lexState = State.EXPECTS_KEY;
        }
    }
    private List<StructureToken> keyHandlerAcc = new ArrayList<>();
    Token mergeAs(List<StructureToken> tokens, Token.Type type){

        if(tokens.isEmpty()) throw new IllegalStateException("No tokens to merge");
        var first = tokens.get(0);
        String lexeme;
        if(tokens.size() == 1) lexeme = first.lexeme;
        else lexeme = tokens.stream().map(st->st.lexeme).collect(Collectors.joining());
        return new Token(type, lexeme, first.line, first.column);
    }
    private void handleExpectsKey(StructureToken structureToken){
        if(structureToken.type == StructureToken.Type.EOL) return;

        switch(structureToken.type) {
            case EQUAL -> {
                grammarTokens.add(mergeAs(keyHandlerAcc, Token.Type.IDENTIFIER));
                grammarTokens.add(Token.fromStructureToken(structureToken));
                keyHandlerAcc.clear();
                lexState = State.EXPECTS_VALUE;
            }
            case RBRACE, RBRACKET -> { // case when returning from inner object
                if (keyHandlerAcc.isEmpty())
                    grammarTokens.add(Token.fromStructureToken(structureToken));
                else
                    keyHandlerAcc.add(structureToken);
            }
            default -> keyHandlerAcc.add(structureToken);
        }
    }

    private List<StructureToken> valueHandlerAcc = new ArrayList<>();
    private void handleExpectsValue(StructureToken structureToken){
        switch(structureToken.type){
            case EOL -> {
                if(valueHandlerAcc.isEmpty()) return;
                grammarTokens.add(mergeAs(valueHandlerAcc, Token.Type.VALUE));
                valueHandlerAcc.clear();
                this.lexState = State.EXPECTS_KEY;
            }
            case LBRACE -> {
                grammarTokens.add(Token.fromStructureToken(structureToken));
                valueHandlerAcc.clear();
                this.lexState = State.EXPECTS_TYPE;
            }
            case TEXT -> {
                if (!structureToken.lexeme.equals("...")){
                    valueHandlerAcc.add(structureToken);
                    return;
                }
                grammarTokens.add(Token.fromTextTokenAs(structureToken, Token.Type.MULTILINE));
                valueHandlerAcc.clear();
                this.lexState = State.IN_MULTILINE;
            }
            case LBRACKET -> {
                grammarTokens.add(Token.fromStructureToken(structureToken));
            }
            default -> {
                valueHandlerAcc.add(structureToken);
            }
        }

    }
    private void handleInQuotes(StructureToken structureToken){

    }
    private List<StructureToken> multilineHandlerAcc = new ArrayList<>();
    int eolCuonter = -1;
    private void handleInMultiline(StructureToken structureToken){
        // first structure token after MULTILINE is EOL - skip it
        if(structureToken.type == StructureToken.Type.EOL && eolCuonter == -1){
            eolCuonter++;
            return;
        }
        // change of state on 2 sequential EOL
        if (structureToken.type == StructureToken.Type.EOL){
            if(++eolCuonter >= 2){
                eolCuonter = 0;
                multilineHandlerAcc.clear();
                this.lexState = State.EXPECTS_KEY;
                return;
            }
            grammarTokens.add(mergeAs(multilineHandlerAcc, Token.Type.LINE));
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
        if(text.length()>0)
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