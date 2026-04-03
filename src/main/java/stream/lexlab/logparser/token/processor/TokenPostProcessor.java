package stream.lexlab.logparser.token.processor;

import stream.lexlab.logparser.token.StructureToken;
import stream.lexlab.logparser.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TokenPostProcessor {
    private static final Pattern OBJECT_TYPE_PATTERN = Pattern.compile("^\\s*[A-Za-z_][A-Za-z0-9_]*<\\d+>\\s*$");
    private final List<Token> grammarTokens = new ArrayList<>();
    private ProcessorState state = new ProcessorState();;

    public List<Token> toGrammarTokens(List<StructureToken> structTokens){
        for(StructureToken structToken: structTokens){

            switch(state.getState()){
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
            grammarTokens.add(Token.fromTextTokenAs(structureToken, Token.Type.OBJTYPE, this::normalizeObjectType));
            this.state.update(ProcessorState.Phase.EXPECTS_KEY);
        }
    }

    private void handleExpectsKey(StructureToken structureToken){
        if(structureToken.type == StructureToken.Type.EOL) return;

        switch(structureToken.type) {
            case EQUAL -> {
                grammarTokens.add(state.reduceAccumulator(Token.Type.IDENTIFIER, this::normalizeFieldName));
                grammarTokens.add(Token.fromStructureToken(structureToken));
                this.state.update(ProcessorState.Phase.EXPECTS_VALUE);
            }
            case RBRACE, RBRACKET -> { // case when returning from inner object
                if (state.isAccEmpty())
                    grammarTokens.add(Token.fromStructureToken(structureToken));
                else
                    state.accumulate(structureToken);
            }
            default -> state.accumulate(structureToken);
        }
    }

    private void handleExpectsValue(StructureToken structureToken){
        switch(structureToken.type){
            case EOL -> {
                if(state.isAccEmpty()) return;
                grammarTokens.add(state.reduceAccumulator(Token.Type.VALUE));
                this.state.update(ProcessorState.Phase.EXPECTS_KEY);
            }
            case LBRACE -> {
                grammarTokens.add(Token.fromStructureToken(structureToken));
                this.state.update(ProcessorState.Phase.EXPECTS_TYPE);
            }
            case TEXT -> {
                if (!structureToken.lexeme.equals("...")){
                    state.accumulate(structureToken);
                    return;
                }
                grammarTokens.add(Token.fromTextTokenAs(structureToken, Token.Type.MULTILINE));
                this.state.update(ProcessorState.Phase.IN_MULTILINE);
            }
            case LBRACKET -> {
                grammarTokens.add(Token.fromStructureToken(structureToken));
            }
            case QUOTE -> {
                state.update(ProcessorState.Phase.IN_QUOTES);
                handleInQuotes(structureToken);
            }

            default -> state.accumulate(structureToken);
        }

    }
    private void handleInQuotes(StructureToken structureToken){
        switch(structureToken.type){
            case QUOTE -> {

                if (state.isAccEmpty())
                    state.accumulate(structureToken);
                else if(!state.isAccEmpty()
                        && state.accPeek().lexeme.endsWith("\\"))
                    state.accumulate(structureToken);
                else{
                    state.accumulate(structureToken);
                    grammarTokens.add(state.reduceAccumulator(Token.Type.VALUE));
                    state.update(ProcessorState.Phase.EXPECTS_KEY);
                }

            }
            default -> state.accumulate(structureToken);
        }
    }

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
                this.state.update(ProcessorState.Phase.EXPECTS_KEY);
                return;
            }
            grammarTokens.add(state.reduceAccumulator(Token.Type.LINE));
            return;
        }
        else
            eolCuonter = 0;

        state.accumulate(structureToken);
    }

    private boolean looksLikeObjectType(String lexeme) {
        return OBJECT_TYPE_PATTERN.matcher(lexeme).matches();
    }

    private String normalizeFieldName(String lexeme) {
        return lexeme.replaceFirst("\\[\\d+\\]\\s*$", "");
    }

    private String normalizeObjectType(String lexeme) {
        return lexeme.replaceFirst("<\\d+>\\s*$", "");
    }
}
