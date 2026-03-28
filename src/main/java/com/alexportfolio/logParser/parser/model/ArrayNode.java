package com.alexportfolio.logParser.parser.model;

import java.util.ArrayList;
import java.util.List;

public record ArrayNode(List<? super ObjectNodeInterface> elements) implements Node {
    public static class Builder {
        List<ObjectNodeInterface> elements = new ArrayList<>();
        public static Builder builder() { return new Builder(); }
        public void add(ObjectNodeInterface node){ elements.add(node); }
        public ArrayNode build(){ return new ArrayNode(elements); }
    }
}
