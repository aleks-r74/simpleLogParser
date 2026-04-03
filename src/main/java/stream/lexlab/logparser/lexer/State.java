package stream.lexlab.logparser.lexer;

public enum State {
    EXPECTS_TYPE, EXPECTS_KEY, EXPECTS_VALUE, IN_QUOTES, IN_MULTILINE
}
