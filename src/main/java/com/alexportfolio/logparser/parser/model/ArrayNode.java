package com.alexportfolio.logparser.parser.model;

import java.util.ArrayList;
import java.util.List;

public final class ArrayNode implements ReplaceableNode {
    private List<ReplaceableNode> elements;
    private String ref;
    private String id;

    private ArrayNode(List<ReplaceableNode> elements) {
        this.elements = elements;
    }

    public List<ReplaceableNode> getElements() {
        return elements;
    }

    @Override
    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public String getType() {
        if(elements != null && elements.size() > 0)
            return "Array[%s]".formatted(elements.get(0).getType());
        return "Array[Empty]";
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
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
