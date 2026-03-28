package com.alexportfolio.logParser.parser.node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public non-sealed class ObjectNode implements Node, ObjectNodeInterface {
    private final String type;
    private final Map<String, Node> fields = new LinkedHashMap<>();
    private Optional<String> ref = Optional.empty();

    public ObjectNode(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Map<String, Node> getFields() {
        return fields;
    }

    public Optional<String> getRef() {
        return ref;
    }

    public void setRef(Optional<String> ref) {
        this.ref = ref;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ObjectNode that = (ObjectNode) object;
        return Objects.equals(type, that.type) && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fields);
    }
}
