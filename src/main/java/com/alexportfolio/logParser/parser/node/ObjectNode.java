package com.alexportfolio.logParser.parser.node;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

public record ObjectNode(String type, LinkedHashMap<String, Node> fields, Optional<String> ref) implements Node {

    public static class Builder{

        private String type;

        private LinkedHashMap<String, Node> fields = new LinkedHashMap<>();

        private Optional<String> ref = Optional.empty();

        public static Builder builder() { return new Builder(); }

        public Builder type(String type){ this.type = type; return this;}

        public Builder ref(String ref) { this.ref = Optional.of(ref); return this; }

        public void addField(String fieldName, Node fieldValue){ fields.put(fieldName, fieldValue); }

        public ObjectNode build(){ return new ObjectNode(type, fields, ref);}

    }
    // we need to exclude Optional<String> ref from hashCode & equals
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
