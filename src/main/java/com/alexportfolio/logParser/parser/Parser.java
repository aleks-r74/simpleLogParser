package com.alexportfolio.logParser.parser;

import com.alexportfolio.logParser.lexer.Token;
import com.alexportfolio.logParser.lexer.TokenType;
import com.alexportfolio.logParser.parser.node.*;

import java.util.List;
import java.util.function.Supplier;

public final class Parser {

    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    public ObjectNode parseDocument() {
        ObjectNode node = new ObjectNode();
        Token first = consume(TokenType.OBJNAME);
        node.name = first.lexeme;
        while(!isAtEnd()) {
            parseKeyValueInto(node);
        }
        return node;
    }

    /**
     * Parses a key-value pair and inserts it into the given ObjectNode.
     */
    private void parseKeyValueInto(ObjectNode target) {
        Token t = skipNewLinesThen(()->consume(TokenType.IDENTIFIER));
        String key = t.lexeme;
        consume(TokenType.EQUAL);
        target.fields.put(key, parseValue());
    }

    /**
     * Dispatches parsing to the correct Node type
     * (object, array, string, or multiline).
     */
    private Node parseValue() {
        return switch (skipNewLinesThen(()->peek()).type){
            case VALUE -> parseString();
            case LBRACKET -> parseArray();
            case LBRACE -> parseObject();
            case MULTILINE -> parseMultiline();
            default -> throw new IllegalArgumentException("Unexpected token type: " + peek().type);
        };
    }

    /**
     * parses an object {...}
     * @return ObjectNode
     */
    private ObjectNode parseObject() {
        consume(TokenType.LBRACE);
        ObjectNode objNode = new ObjectNode();
        objNode.name = skipNewLinesThen(()->consume(TokenType.OBJNAME)).lexeme;
        do{
            Token t = skipNewLinesThen(this::peek);
            if(t.type == TokenType.RBRACE) {advance(); break;}
            parseKeyValueInto(objNode);
        }while(!isAtEnd());
        return objNode;
    }

    /**
     * parses an array of objects [{...} {...}]
     * @return ArrayNode
     */
    private ArrayNode parseArray() {
        consume(TokenType.LBRACKET);
        ArrayNode arrNode = new ArrayNode();
        do{
            Token t = skipNewLinesThen(this::peek);
            if(t.type == TokenType.RBRACKET){ advance(); break;}
            arrNode.elements.add(parseObject());
        }while(!isAtEnd());
        return arrNode;
    }

    private StringNode parseString() {
        StringNode strNode = new StringNode();
        strNode.value = consume(TokenType.VALUE).lexeme;
        return strNode;
    }

    /**
     * This method needs to be changed. lines after Multiline should be tokenized as LINE. just a stub implementation for now
     * @return
     */
    private MultilineNode parseMultiline() {
        consume(TokenType.MULTILINE);
        MultilineNode linesNode = new MultilineNode();
        Token t;
        while(!isAtEnd()) {
            linesNode.lines.add(skipNewLinesThen(()->advance()).lexeme);
        }
        return linesNode;
    }

    // --- Navigation helpers ---

    /** Returns true if the next token matches the given type (does not advance). */
    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type == type;
    }

    /** Returns the next token without advancing the cursor. */
    private Token peek() {
        return tokens.get(current);
    }

    /** Returns true and advances if the next token matches the given type. */
    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    /** Returns the previous token (the one most recently consumed). */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /** Consumes the next token and returns it. */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /** Consumes the next token if it matches the expected type, otherwise throws. */
    private Token consume(TokenType expected) {
        if (check(expected)) return advance();
        throw new RuntimeException("Expected token: " + expected + ", got: " + peek());
    }

    /** Returns true if we have reached the end of the token list. */
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF || current >= tokens.size();
    }

    private Token skipNewLinesThen(Supplier<Token> action){
        skipNewlines();
        return action.get();
    }

    private void skipNewlines(){
        while (!isAtEnd() && peek().type == TokenType.NEWLINE) {
            advance();
        }
    }
}
