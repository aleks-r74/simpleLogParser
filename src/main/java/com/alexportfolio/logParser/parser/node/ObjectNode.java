package com.alexportfolio.logParser.parser.node;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ObjectNode implements Node {
    public String name;
    public Map<String, Node> fields = new LinkedHashMap<>();
}
