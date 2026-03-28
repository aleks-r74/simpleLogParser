package com.alexportfolio.logParser.parser;

import com.alexportfolio.logParser.parser.node.*;

import java.util.*;

public class Referencer {

    private Map<ObjectNode, String> nodeRefs = new HashMap<>();

    /**
     * Traverses the tree looking for duplicate nodes. Marks nodes as duplicate by using setRef() with the reference from the internal store.
     * @param on ObjectNode to traverse
     * @param levelName is used to build the canonical key (reference). The root node can provide "root" or a timestamp there to help
     *                  uniquely identify the node
     */
    public void findRefs(ObjectNode on, String levelName){
        for(Map.Entry<String,Node> entry : on.getFields().entrySet()){
            Node n = entry.getValue();
            if(n instanceof ObjectNode innerObj) findRefs(innerObj, "%s:%s.%s".formatted(levelName, on.getType(), entry.getKey()));
            else if(n instanceof ArrayNode arr)
                for(int i=0; i<arr.elements().size(); i++) {
                    ObjectNode arrObj = (ObjectNode) arr.elements().get(i);
                    findRefs(arrObj,  "%s:%s.%s[%d]".formatted(levelName, on.getType(), entry.getKey(), i));
                }
            else continue;
        }
        if(nodeRefs.containsKey(on))
            on.setRef(Optional.of(nodeRefs.get(on)));
        else {
            String refKey = "%s$".formatted(levelName);
            nodeRefs.put(on, refKey);
        }
    }

    /*
    Replaces nodes that have the reference set to the respective nodes of type RefNode, collapsing the tree
     */
    public void collapse(ObjectNode on){
        for(String fieldName : on.getFields().keySet()) {
            Node fieldValue = on.getFields().get(fieldName);

            if (fieldValue instanceof ObjectNode objNode) {
                if (objNode.getRef().isPresent())
                    on.getFields().put(fieldName, new RefNode(objNode.getType(), objNode.getRef().get()));
                else collapse(objNode);
            }

            else if(fieldValue instanceof ArrayNode arrNode){
                for(int i=0; i<arrNode.elements().size(); i++){
                    if(!(arrNode.elements().get(i) instanceof ObjectNode arrItem)) continue;
                    if (arrItem.getRef().isPresent())
                        arrNode.elements().set(i, new RefNode(arrItem.getType(), arrItem.getRef().get()));
                    else collapse(arrItem);
                }
            }
        }
    }

}
