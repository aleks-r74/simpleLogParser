package stream.lexlab.logparser.token.processor;

import stream.lexlab.logparser.token.StructureToken;
import stream.lexlab.logparser.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class TokenPostProcessor {
    private final Logger logger = Logger.getLogger(this.getClass().toString());
    private static final int EXIT_EOL_THRESHOLD = 2;
    private static final Pattern OBJECT_TYPE_PATTERN = Pattern.compile("^\\s*[A-Za-z_][A-Za-z0-9_]*<\\d+>\\s*$");
    private final List<Token> grammarTokens = new ArrayList<>();
    private ProcessorState state = new ProcessorState();;

    public List<Token> toGrammarTokens(List<StructureToken> structTokens){
        try {
            for (StructureToken structToken : structTokens) {
                state.freezeIfComment(structToken);
                switch (state.getPhase()) {
                    case EXPECTS_TYPE -> handleExpectsType(structToken);
                    case EXPECTS_KEY -> handleExpectsKey(structToken);
                    case EXPECTS_VALUE -> handleExpectsValue(structToken);
                    case IN_QUOTES -> handleInQuotes(structToken);
                    case IN_MULTILINE -> handleInMultiline(structToken);
                    case IN_COMMENT -> handleInComment(structToken);
                }
            }
        } catch (IllegalStateException | UnsupportedOperationException e){
            logger.info(state.toString());
            throw e;
        }
        return grammarTokens;
    }

    private void handleExpectsType(StructureToken structureToken){
        if(looksLikeObjectType(structureToken.lexeme)) {
            grammarTokens.add(Token.fromTextTokenAs(structureToken, Token.Type.OBJTYPE, this::normalizeObjectType));
            state.setPhase(ProcessorState.Phase.EXPECTS_KEY);
        }
    }

    private void handleExpectsKey(StructureToken structureToken){
        if(structureToken.type == StructureToken.Type.EOL) return;

        switch(structureToken.type) {
            case EQUAL -> {
                grammarTokens.add(state.reduceAccumulator(Token.Type.IDENTIFIER, this::normalizeFieldName));
                grammarTokens.add(Token.fromStructureToken(structureToken));
                state.setPhase(ProcessorState.Phase.EXPECTS_VALUE);
            }
            case RBRACE, RBRACKET -> { // when returning from inner object or array
                if (state.isAccEmpty())
                    grammarTokens.add(Token.fromStructureToken(structureToken));
                else // when building a field name
                    state.accumulate(structureToken);
            }
            case LBRACE -> {    // entering nested object inside an array
                grammarTokens.add(Token.fromStructureToken(structureToken));
                state.setPhase(ProcessorState.Phase.EXPECTS_TYPE);
            }
            default -> state.accumulate(structureToken);
        }
    }

    private void handleExpectsValue(StructureToken structureToken){
        switch(structureToken.type){
            case EOL -> { // Ignores EOL tokens unless a value is present, allowing the value to appear on a different line than its key.
                if(state.isAccEmpty()) return;
                grammarTokens.add(state.reduceAccumulator(Token.Type.VALUE, String::stripTrailing));
                state.setPhase(ProcessorState.Phase.EXPECTS_KEY);
            }

            case TEXT -> { // multiline or unquoted values
                if (structureToken.lexeme.equals("...")){
                    grammarTokens.add(Token.fromTextTokenAs(structureToken, Token.Type.MULTILINE));
                    state.setPhase(ProcessorState.Phase.IN_MULTILINE);
                    return;
                }
                state.accumulate(structureToken);
            }

            case LBRACE -> { // value as object
                grammarTokens.add(Token.fromStructureToken(structureToken));
                state.setPhase(ProcessorState.Phase.EXPECTS_TYPE);
            }

            case LBRACKET -> grammarTokens.add(Token.fromStructureToken(structureToken));

            case RBRACKET -> { // empty array case
                grammarTokens.add(Token.fromStructureToken(structureToken));
                state.setPhase(ProcessorState.Phase.EXPECTS_KEY);
            }

            case QUOTE -> { // quoted values.
                state.setPhase(ProcessorState.Phase.IN_QUOTES);
                handleInQuotes(structureToken);
            }

            default -> throw new IllegalStateException("Token %s has no value handler".formatted(structureToken));
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
                    grammarTokens.add(state.reduceAccumulator(Token.Type.VALUE, this::cutEnds));
                    state.setPhase(ProcessorState.Phase.EXPECTS_KEY);
                }

            }
            default -> state.accumulate(structureToken);
        }
    }

    private void handleInMultiline(StructureToken structureToken){
        switch(structureToken.type){
            case EOL -> {
                if(state.isFirstEOL()){
                    state.setFirstEOL(false);
                    return;
                }

                if(!state.isAccEmpty())
                    grammarTokens.add(state.reduceAccumulator(Token.Type.LINE));

                if(state.getEolCounter() >= EXIT_EOL_THRESHOLD){
                    state.resetEolCounter();
                    state.setPhase(ProcessorState.Phase.EXPECTS_KEY);
                    return;
                }

                state.incEolCounter();
            }
            default -> state.accumulate(structureToken);
        }
    }

    private void handleInComment(StructureToken structureToken){
        if(structureToken.type == StructureToken.Type.EOL)
            state.unfreeze();
    }

    private boolean looksLikeObjectType(String lexeme) {
        return OBJECT_TYPE_PATTERN.matcher(lexeme).matches();
    }

    private String normalizeFieldName(String lexeme) {
        return lexeme.replaceFirst("\\[\\d+\\]", "").stripTrailing();
    }

    private String normalizeObjectType(String lexeme) {
        return lexeme.replaceFirst("<\\d+>", "").strip();
    }

    private String cutEnds(String lexeme){
        if (lexeme.length() < 2)
            return lexeme;
        return lexeme.substring(1, lexeme.length() - 1);
    }
}
