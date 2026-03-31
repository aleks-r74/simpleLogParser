package stream.lexlab.logparser.parser.model;

import java.util.Map;

public sealed interface ReplaceableNode extends Node permits ArrayNode, MultilineNode, ObjectNode, RefNode {
    Map<String, String> metadata();
}
