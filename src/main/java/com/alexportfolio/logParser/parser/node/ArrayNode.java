package com.alexportfolio.logParser.parser.node;

import java.util.ArrayList;
import java.util.List;

public record ArrayNode(List<ObjectNode> elements) implements Node {
    public static class Builder {
        List<ObjectNode> elements = new ArrayList<>();
        public static Builder builder() { return new Builder(); }
        public void add(ObjectNode node){ elements.add(node); }
        public ArrayNode build(){ return new ArrayNode(elements); }
    }
}
