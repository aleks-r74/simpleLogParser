package com.alexportfolio.logparser.parser.model;

/*
marker interface that is used for the purpose of replaceing ArrayNode's items with RefNode
 */
public sealed interface ReplaceableNode extends Node permits ArrayNode, MultilineNode, ObjectNode, RefNode {
    void setRef(String ref);
    String getRef();
}
