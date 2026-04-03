package stream.lexlab.logparser.parser;

import stream.lexlab.logparser.lexer.StructureToken;
import stream.lexlab.logparser.lexer.StructureTokenType;
import stream.lexlab.logparser.parser.model.*;

import java.util.List;

public final class Parser {

    private final List<StructureToken> structureTokens;
    private int current;

    public Parser(List<StructureToken> structureTokens){
        this.structureTokens = structureTokens;
    }

    public ObjectNode parseDocument() {
        StructureToken first = consume(StructureTokenType.OBJTYPE);
        String type = first.lexeme;
        var onBuilder = ObjectNode.Builder.builder().type(type);
        while(!isAtEnd()) {
            parseKeyValueInto(onBuilder);
        }
        return onBuilder.build();
    }

    private void parseKeyValueInto(ObjectNode.Builder onBuilder) {
        StructureToken t = consume(StructureTokenType.IDENTIFIER);
        String key = t.lexeme;
        consume(StructureTokenType.EQUAL);
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
        consume(StructureTokenType.LBRACE);
        String type = consume(StructureTokenType.OBJTYPE).lexeme;
        var onBuilder = ObjectNode.Builder.builder().type(type);
        while (!isAtEnd() && peek().type != StructureTokenType.RBRACE) {
            parseKeyValueInto(onBuilder);
        }
        consume(StructureTokenType.RBRACE);
        return onBuilder.build();
    }

    private ArrayNode parseArray() {
        consume(StructureTokenType.LBRACKET);
        var anBuilder = ArrayNode.Builder.builder();
        while(peek().type != StructureTokenType.RBRACKET){
            anBuilder.add(parseObject());
        }
        consume(StructureTokenType.RBRACKET);
        return anBuilder.build();
    }

    private StringNode parseString() {
        var strValue = consume(StructureTokenType.VALUE).lexeme;
        return new StringNode(strValue);
    }

    private MultilineNode parseMultiline() {
        consume(StructureTokenType.MULTILINE);
        var mlnBuilder = MultilineNode.Builder.builder();
        while(peek().type == StructureTokenType.LINE)
            mlnBuilder.addLine(advance().lexeme);
        return mlnBuilder.build();
    }

    private StructureToken peek() {
        if (current >= structureTokens.size()) return structureTokens.get(structureTokens.size() - 1);
        return structureTokens.get(current);
    }

    private StructureToken advance() {
        if (!isAtEnd()) current++;
        return structureTokens.get(current - 1);
    }

    private StructureToken consume(StructureTokenType expected) {
        if (peek().type == expected) return advance();
        throw new IllegalStateException("Expected token: " + expected + ", got: " + peek());
    }

    private boolean isAtEnd() {
        return current >= structureTokens.size()
                || structureTokens.get(current).type == StructureTokenType.EOD;
    }

}
