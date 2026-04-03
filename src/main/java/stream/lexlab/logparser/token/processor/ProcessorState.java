package stream.lexlab.logparser.token.processor;

import stream.lexlab.logparser.token.StructureToken;
import stream.lexlab.logparser.token.Token;

import java.util.ArrayList;
import java.util.List;

public class ProcessorState {

    private Phase state = Phase.EXPECTS_TYPE;
    private List<StructureToken> accumulator = new ArrayList<>();


    public void update(Phase newState){
        state = newState;
    }

    public Phase getState() {
        return state;
    }

    public boolean isAccEmpty(){ return accumulator.isEmpty(); }

    public void accumulate(StructureToken stToken) {
        accumulator.add(stToken);
    }

    Token reduceAccumulator(Token.Type resultType){
        if(accumulator.isEmpty()) throw new IllegalStateException("No tokens to merge");
        var first = accumulator.get(0);
        StringBuilder lexeme = new StringBuilder();
        if(accumulator.size() == 1)
            lexeme.append(first.lexeme);
        else
            for(var token : accumulator)
                lexeme.append(token.lexeme);
        accumulator.clear();
        return new Token(resultType, lexeme.toString(), first.line, first.column);
    }

    public enum Phase {
        EXPECTS_TYPE, EXPECTS_KEY, EXPECTS_VALUE, IN_QUOTES, IN_MULTILINE
    }
}
