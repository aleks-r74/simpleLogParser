package com.alexportfolio.logParser.parser;

import java.util.*;

sealed interface Node permits ObjectNode, ArrayNode, StringNode, MultilineNode {}

final class ObjectNode implements Node {
    String name;
    Map<String, Node> fields = new LinkedHashMap<>();
}

final class ArrayNode implements Node {
    List<ObjectNode> elements = new ArrayList<>();
}

final class StringNode implements Node {
    String value;
}

final class MultilineNode implements Node {
    List<String> lines;
}