package stream.lexlab.logparser.parser.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ObjectNode implements ReplaceableNode {
    private final String type;
    private final LinkedHashMap<String, Node> fields;
    private LinkedHashMap<String, String> metadata;

    private ObjectNode(String type, LinkedHashMap<String, Node> fields) {
        this.type = type;
        this.fields = fields;
        this.metadata = new LinkedHashMap<>();
    }
    @Override
    public String getType() {
        return type;
    }

    public Map<String, Node> getFields() {
        return fields;
    }

    @Override
    public Map<String, String> metadata() {
        return metadata;
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
