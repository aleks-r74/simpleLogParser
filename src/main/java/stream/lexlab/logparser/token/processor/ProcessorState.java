package stream.lexlab.logparser.token.processor;

import stream.lexlab.logparser.token.StructureToken;
import stream.lexlab.logparser.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ProcessorState {

    private Phase state = Phase.EXPECTS_TYPE;
    private List<StructureToken> accumulator = new ArrayList<>();
    private boolean firstEOL = true;
    private int eolCounter = 0;

    public void setPhase(Phase newState){
        state = newState;
    }

    public Phase getPhase() {
        return state;
    }

    public int getEolCounter(){
        return eolCounter;
    }

    public void incEolCounter(){
        ++eolCounter;
    }

    public boolean isFirstEOL() {
        return firstEOL;
    }

    public void setFirstEOL(boolean firstEOL) {
        this.firstEOL = firstEOL;
    }

    public void resetEolCounter(){
        eolCounter = 0;
        firstEOL = true;
    }

    public boolean isAccEmpty(){ return accumulator.isEmpty(); }

    public void accumulate(StructureToken stToken) {
        accumulator.add(stToken);
    }

    public StructureToken accPeek(){
        if(accumulator.isEmpty())
            throw new UnsupportedOperationException("Accumulator is empty");
        return accumulator.get(accumulator.size()-1);
    }

    public Token reduceAccumulator(Token.Type resultType){
        return reduceAccumulator(resultType, (String s) -> s);
    }

    /**
     * normalizes the final lexeme of the result token
     * @param resultType grammar token of type Token
     * @param normalizer Function<String,String> that performs lexeme normalization
     * @return
     */
    public Token reduceAccumulator(Token.Type resultType, Function<String, String> normalizer){
        if(accumulator.isEmpty()) throw new IllegalStateException("No tokens to merge");
        var first = accumulator.get(0);
        StringBuilder lexeme = new StringBuilder();
        for (var token : accumulator)
            lexeme.append(token.lexeme);
        accumulator.clear();
        String normalized = normalizer.apply(lexeme.toString());
        return new Token(resultType, normalized, first.line, first.column);
    }

    public enum Phase {
        EXPECTS_TYPE, EXPECTS_KEY, EXPECTS_VALUE, IN_QUOTES, IN_MULTILINE
    }

    @Override
    public String toString() {
        return """
                ProcessorState:
                  phase=%s
                  eolCounter=%d
                  accumulator=%s
                """.formatted(state, eolCounter, accumulator);
    }
}
