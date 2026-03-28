package com.alexportfolio.logParser.parser;

import com.alexportfolio.logParser.lexer.Token;
import com.alexportfolio.logParser.lexer.TokenType;
import com.alexportfolio.logParser.parser.node.*;

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
        ObjectNode node = new ObjectNode(type);
        while(!isAtEnd()) {
            parseKeyValueInto(node);
        }
        return node;
    }

    private void parseKeyValueInto(ObjectNode target) {
        Token t = consume(TokenType.IDENTIFIER);
        String key = t.lexeme;
        consume(TokenType.EQUAL);
        target.getFields().put(key, parseValue());
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
        ObjectNode objNode = new ObjectNode(type);
        while (!isAtEnd() && peek().type != TokenType.RBRACE) {
            parseKeyValueInto(objNode);
        }
        consume(TokenType.RBRACE);
        return objNode;
    }

    private ArrayNode parseArray() {
        consume(TokenType.LBRACKET);
        var arrNodeBldr = ArrayNode.Builder.builder();
        while(peek().type != TokenType.RBRACKET){
            arrNodeBldr.add(parseObject());
        }
        consume(TokenType.RBRACKET);
        return arrNodeBldr.build();
    }

    private StringNode parseString() {
        var strValue = consume(TokenType.VALUE).lexeme;
        return new StringNode(strValue);
    }

    private MultilineNode parseMultiline() {
        consume(TokenType.MULTILINE);
        var multilineNodeBldr = MultilineNode.Builder.builder();
        while(peek().type == TokenType.LINE)
            multilineNodeBldr.addLine(advance().lexeme);
        return multilineNodeBldr.build();
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
