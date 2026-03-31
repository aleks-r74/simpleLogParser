package stream.lexlab.logparser.parser.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record RefNode(String type, String ref) implements ReplaceableNode {

    @Override
    public String getType() {
        return "RefferenceNode";
    }

    @Override
    public Map<String, String> metadata() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ref", ref);
        map.put("type", type);
        return map;
    }
}
