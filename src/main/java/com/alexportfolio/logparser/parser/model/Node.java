package com.alexportfolio.logparser.parser.model;

public sealed interface Node permits StringNode, ReplaceableNode {
    String getType();
    void setId(String id);
    String getId();
}







