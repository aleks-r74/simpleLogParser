package com.alexportfolio.logparser.transform;

import com.alexportfolio.logparser.parser.model.*;

import java.util.*;

public class Referencer {
    // for long-running processes, these maps need to be cleaned to avoid uncontrollable growth
    private final Map<ObjectNode, String> nodeRefs = new HashMap<>();
    private final Map<String, ObjectNode> reverseMap = new HashMap<>();
    /**
     * Traverses the tree looking for duplicate nodes. Marks nodes as duplicate by using setRef() with the reference from the internal store.
     * @param on ObjectNode to traverse
     * @param levelName is used to build the canonical key (reference). The root node can provide a timestamp to help uniquely identify the original node
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
        }
        if(nodeRefs.containsKey(on))
            on.setRef(nodeRefs.get(on));
        else {
            String refKey = "%s$".formatted(levelName);
            nodeRefs.put(on, refKey);
            reverseMap.put(refKey, on);
        }
    }

    /**
     * Replaces duplicate nodes with RefNodes, collapsing the tree
     * @param on root element of the tree to collapse
     */
    public void collapse(ObjectNode on){
        for(String fieldName : on.getFields().keySet()) {
            Node fieldValue = on.getFields().get(fieldName);
            // check if the field is an Object. Then check if it has a reference, if it is - replace the node with RefNode, otherwise 'dive' in and repeat
            if (fieldValue instanceof ObjectNode objNode) {
                if (objNode.getRef() != null)
                    on.getFields().put(fieldName, new RefNode(objNode.getType(), objNode.getRef()));
                else collapse(objNode);
            }
            // same, but for arrays
            else if(fieldValue instanceof ArrayNode arrNode){
                for(int i=0; i<arrNode.elements().size(); i++){
                    if(!(arrNode.elements().get(i) instanceof ObjectNode arrItem)) continue;
                    if (arrItem.getRef() != null)
                        arrNode.elements().set(i, new RefNode(arrItem.getType(), arrItem.getRef()));
                    else collapse(arrItem);
                }
            }
        }
    }

    public ObjectNode explode(String ref){
        return reverseMap.get(ref);
    }

    /**
     * Cleans internal storage
     */
    public void reset(){
        this.nodeRefs.clear();
        this.reverseMap.clear();
    }
}
