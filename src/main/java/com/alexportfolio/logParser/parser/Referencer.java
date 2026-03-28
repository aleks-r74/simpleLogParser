package com.alexportfolio.logParser.parser;

import com.alexportfolio.logParser.parser.node.*;

import java.util.*;
import java.util.function.Consumer;

public class Referencer {
    private static int counter;
    private Map<ObjectNode, String> nodeRefs = new HashMap<>();

    public void findRefs(ObjectNode on, String fieldName){
        for(Map.Entry<String,Node> entry : on.getFields().entrySet()){
            Node n = entry.getValue();
            if(n instanceof ObjectNode innerObj) findRefs(innerObj, "%s>%s>%s".formatted(fieldName, on.getType(), entry.getKey()));
            else if(n instanceof ArrayNode arr)
                for(int i=0; i<arr.elements().size(); i++) {
                    ObjectNode arrObj = (ObjectNode) arr.elements().get(i);
                    findRefs(arrObj,  "%s>%s>%s[%d]".formatted(fieldName, on.getType(), entry.getKey(), i));
                }
            else continue;
        }
        if(nodeRefs.containsKey(on))
            on.setRef(Optional.of(nodeRefs.get(on)));
        else {
            String refKey = "%s>%s$%d".formatted(fieldName, on.getType(), counter++);
            nodeRefs.put(on, refKey);
        }
    }

    public void reference(ObjectNode on){
        for(String fieldName : on.getFields().keySet()) {
            Node fieldValue = on.getFields().get(fieldName);

            if (fieldValue instanceof ObjectNode objNode) {
                if (objNode.getRef().isPresent()) {
                    on.getFields().put(fieldName, new RefNode(objNode.getRef().get()));
                    return;
                }
                else reference(objNode);
            }
            else if(fieldValue instanceof ArrayNode arrNode){
                for(int i=0; i<arrNode.elements().size(); i++){
                    ObjectNode arrItem = arrNode.elements().get(i);
                    if (arrItem.getRef().isPresent())
                        arrNode.elements().set(i, new RefNode(arrItem.getRef().get()));
                    else reference(arrItem);
                }
            }
        }
    }
    public LinkedHashMap<String, Object> nodeConverter(ObjectNode in){
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("type", in.getType());
        in.getFields().forEach((k,v)->{
            if (v instanceof StringNode sn) result.put(k, sn.value());
            if (v instanceof MultilineNode mn) result.put(k, mn.lines());
            if (v instanceof ObjectNode on) {
                if(on.getRef().isEmpty())
                    result.put(k, nodeConverter(on));
                else
                    result.put(k, on.getRef().get());
            }
            if (v instanceof ArrayNode an) {
                List<LinkedHashMap<String, Object>> arr = new ArrayList<>();
                an.elements().forEach(arrItem->arr.add(nodeConverter(arrItem)));
                result.put(k, arr);
            }
        });
        return result;
    }
}
