package com.alexportfolio.logparser.parser.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ArrayNode implements ReplaceableNode {
    private List<ReplaceableNode> elements;
    private LinkedHashMap<String, String> metadata;
    private String id;

    private ArrayNode(List<ReplaceableNode> elements) {
        this.elements = elements;
        this.metadata = new LinkedHashMap<>();
    }

    public List<ReplaceableNode> getElements() {
        return elements;
    }

    @Override
    public String getType() {
        if(elements == null || elements.size() == 0)
            return "Array[Empty]";
        String types = elements.stream()
                .map(Node::getType)
                .distinct()
                .collect(Collectors.joining(", "));
            return "Array[%s]".formatted(types);

    }

    @Override
    public Map<String, String> metadata() {
        return metadata;
    }

    public static class Builder {
        List<ReplaceableNode> elements = new ArrayList<>();
        public static Builder builder() { return new Builder(); }
        public void add(ObjectNode node){ elements.add(node); }
        public ArrayNode build(){ return new ArrayNode(elements); }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ArrayNode arrayNode = (ArrayNode) object;
        return elements.equals(arrayNode.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }
}
