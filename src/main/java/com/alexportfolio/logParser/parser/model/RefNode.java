package com.alexportfolio.logParser.parser.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record RefNode(String type, String ref) implements ObjectNodeInterface, Node{
    public Map asMap(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", type);
        map.put("ref", ref);
        return map;
    }
}
