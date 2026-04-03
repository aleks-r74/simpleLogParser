package stream.lexlab.logparser.parser;

import stream.lexlab.logparser.parser.model.*;
import stream.lexlab.logparser.token.Token;

import java.util.List;

public final class Parser {

    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    public ObjectNode parseDocument() {
        Token first = consume(Token.Type.OBJTYPE);
        String type = first.lexeme;
        var onBuilder = ObjectNode.Builder.builder().type(type);
        while(!isAtEnd()) {
            parseKeyValueInto(onBuilder);
        }
        return onBuilder.build();
    }

    private void parseKeyValueInto(ObjectNode.Builder onBuilder) {
        Token t = consume(Token.Type.IDENTIFIER);
        String key = t.lexeme;
        consume(Token.Type.EQUAL);
        onBuilder.addField(key, parseValue());
    }

    private Node parseValue() {
        return switch (peek().type){
            case VALUE -> parseString();
            case LBRACKET -> parseArray();
            case LBRACE -> parseObject();
            case MULTILINE -> parseMultiline();
            default -> throw new IllegalArgumentException("Unexpected token type: " + peek().type);
        };
    }

    private ObjectNode parseObject() {
        consume(Token.Type.LBRACE);
        String type = consume(Token.Type.OBJTYPE).lexeme;
        var onBuilder = ObjectNode.Builder.builder().type(type);
        while (!isAtEnd() && peek().type != Token.Type.RBRACE) {
            parseKeyValueInto(onBuilder);
        }
        consume(Token.Type.RBRACE);
        return onBuilder.build();
    }

    private ArrayNode parseArray() {
        consume(Token.Type.LBRACKET);
        var anBuilder = ArrayNode.Builder.builder();
        while(peek().type != Token.Type.RBRACKET){
            anBuilder.add(parseObject());
        }
        consume(Token.Type.RBRACKET);
        return anBuilder.build();
    }

    private StringNode parseString() {
        var strValue = consume(Token.Type.VALUE).lexeme;
        return new StringNode(strValue);
    }

    private MultilineNode parseMultiline() {
        consume(Token.Type.MULTILINE);
        var mlnBuilder = MultilineNode.Builder.builder();
        while(peek().type == Token.Type.LINE)
            mlnBuilder.addLine(advance().lexeme);
        return mlnBuilder.build();
    }

    private Token peek() {
        if (current >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(current);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return tokens.get(current - 1);
    }

    private Token consume(Token.Type expected) {
        if (peek().type == expected) return advance();
        throw new IllegalStateException("Expected token: " + expected + ", got: " + peek());
    }

    private boolean isAtEnd() {
        return current >= tokens.size()
                || tokens.get(current).type == Token.Type.EOD;
    }

}
