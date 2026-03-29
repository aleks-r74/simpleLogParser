package com.alexportfolio.logparser.parser.model;

import java.util.Map;

/*
marker interface that is used for the purpose of replaceing ArrayNode's items with RefNode
 */
public sealed interface ReplaceableNode extends Node permits ArrayNode, MultilineNode, ObjectNode, RefNode {
    Map<String, String> metadata();
}
