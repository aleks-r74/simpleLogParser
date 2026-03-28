package com.alexportfolio.logParser.parser;

import com.alexportfolio.logParser.lexer.Token;
import com.alexportfolio.logParser.lexer.TokenType;
import com.alexportfolio.logParser.parser.model.*;

import java.util.List;

public final class Parser {

    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    public ObjectNode parseDocument() {
        Token first = consume(TokenType.OBJTYPE);
        String type = first.lexeme;
        var onBuilder = ObjectNode.Builder.builder().type(type);
        while(!isAtEnd()) {
            parseKeyValueInto(onBuilder);
        }
        return onBuilder.build();
    }

    private void parseKeyValueInto(ObjectNode.Builder onBuilder) {
        Token t = consume(TokenType.IDENTIFIER);
        String key = t.lexeme;
        consume(TokenType.EQUAL);
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
        consume(TokenType.LBRACE);
        String type = consume(TokenType.OBJTYPE).lexeme;
        var onBuilder = ObjectNode.Builder.builder().type(type);
        while (!isAtEnd() && peek().type != TokenType.RBRACE) {
            parseKeyValueInto(onBuilder);
        }
        consume(TokenType.RBRACE);
        return onBuilder.build();
    }

    private ArrayNode parseArray() {
        consume(TokenType.LBRACKET);
        var anBuilder = ArrayNode.Builder.builder();
        while(peek().type != TokenType.RBRACKET){
            anBuilder.add(parseObject());
        }
        consume(TokenType.RBRACKET);
        return anBuilder.build();
    }

    private StringNode parseString() {
        var strValue = consume(TokenType.VALUE).lexeme;
        return new StringNode(strValue);
    }

    private MultilineNode parseMultiline() {
        consume(TokenType.MULTILINE);
        var mlnBuilder = MultilineNode.Builder.builder();
        while(peek().type == TokenType.LINE)
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

    private Token consume(TokenType expected) {
        if (peek().type == expected) return advance();
        throw new RuntimeException("Expected token: " + expected + ", got: " + peek());
    }

    private boolean isAtEnd() {
        return current >= tokens.size()
                || tokens.get(current).type == TokenType.EOF;
    }

}
