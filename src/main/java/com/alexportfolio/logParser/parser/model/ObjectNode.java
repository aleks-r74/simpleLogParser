package com.alexportfolio.logParser.parser.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public non-sealed class ObjectNode implements Node, ObjectNodeInterface {
    private final String type;
    private final LinkedHashMap<String, Node> fields;
    private Optional<String> ref = Optional.empty();

    private ObjectNode(String type, LinkedHashMap<String, Node> fields) {
        this.type = type;
        this.fields = fields;
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

    public static class Builder {
        private String type;
        private final LinkedHashMap<String, Node> fields = new LinkedHashMap<>();

        public static Builder builder(){ return new Builder(); }

        public Builder type(String type){ this.type = type; return this;}

        public Builder addField(String fieldName, Node fieldValue){
            fields.put(fieldName,fieldValue);
            return this;
        }

        public ObjectNode build(){ return new ObjectNode(type, fields); }
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
