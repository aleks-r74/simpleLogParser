package com.alexportfolio.logparser.parser.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record RefNode(String type, String ref) implements ReplaceableNode {
    public Map<String, Object> asMap(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ref", ref);
        map.put("type", type);
        return map;
    }

    @Override
    public void setRef(String ref) {
        //NO-OP
    }

    @Override
    public String getRef() {
        return null;
    }

    @Override
    public String getType() {
        return "Refference";
    }

    @Override
    public void setId(String id) {
        // NO-OP
    }

    @Override
    public String getId() {
        return "";
    }
}
