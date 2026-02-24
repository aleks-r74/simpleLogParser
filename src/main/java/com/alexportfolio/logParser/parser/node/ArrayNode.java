package com.alexportfolio.logParser.parser.node;

import java.util.ArrayList;
import java.util.List;

public final class ArrayNode implements Node {
    public List<ObjectNode> elements = new ArrayList<>();
}
