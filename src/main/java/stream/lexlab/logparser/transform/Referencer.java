package stream.lexlab.logparser.transform;

import stream.lexlab.logparser.parser.model.*;

import java.util.*;

public class Referencer {
    // for long-running processes, these maps need to be cleaned to avoid uncontrollable growth
    private final Map<ReplaceableNode, String> nodeRefs = new HashMap<>();
    private final Map<String, ReplaceableNode> reverseMap = new HashMap<>();

    /**
     * Traverses the tree and detects duplicate nodes. Duplicate nodes are marked by calling setRef()
     * with the reference resolved from the internal store.
     *
     * @param node the ObjectNode to traverse
     * @param refId used to build the reference path; for the root node, this may include a timestamp
     *                  to make the original node uniquely identifiable
     */
    public void findRefs(Node node, String refId){
        if(!(node instanceof ReplaceableNode vNode)) return;

        if(nodeRefs.containsKey(node)) {
            vNode.metadata().put("ref", nodeRefs.get(node));
            return;
        }

        vNode.metadata().put("id", refId);

        // put this node into the storage
        nodeRefs.put(vNode, refId);
        reverseMap.put(refId, vNode);

        // explore children
        if(node instanceof ArrayNode arr)
            for (int i = 0; i < arr.getElements().size(); i++) {
                ObjectNode arrItem = (ObjectNode) arr.getElements().get(i);
                findRefs(arrItem, "%s[%d]".formatted(refId, i));
            }

        else if(node instanceof ObjectNode oNode)
            for(Map.Entry<String,Node> entry : oNode.getFields().entrySet())
                findRefs(entry.getValue(), "%s.%s".formatted(refId, entry.getKey()));

    }

    /**
     * Replaces duplicate nodes with RefNodes, collapsing the tree
     * @param stNode root element of the tree to collapse
     */
    public ReplaceableNode collapse(ReplaceableNode stNode){
        var ref = stNode.metadata().get("ref");
        if (ref != null)
            return new RefNode(stNode.getType(), ref);

        if(stNode instanceof ArrayNode arrNode){
            var origList = arrNode.getElements();
            var updList = origList.stream().map(this::collapse).toList();
            origList.clear();
            origList.addAll(updList);
        }

        else if (stNode instanceof ObjectNode objNode) {
            for(var objEntry : objNode.getFields().entrySet()) {
                String fieldName = objEntry.getKey();
                Node fieldValue = objEntry.getValue();
                if(fieldValue instanceof ReplaceableNode fv)
                    objNode.getFields().put(fieldName, collapse(fv));
            }
        }

        return stNode;
    }

    public ReplaceableNode explode(String ref){
        return reverseMap.get(ref);
    }

    public void reset(){
        this.nodeRefs.clear();
        this.reverseMap.clear();
    }
}
