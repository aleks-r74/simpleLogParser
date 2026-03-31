package stream.lexlab.logparser.parser.model;

public sealed interface Node permits StringNode, ReplaceableNode {
    String getType();
}







