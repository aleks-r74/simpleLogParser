package com.alexportfolio.logparser.transform;

import com.alexportfolio.logparser.parser.model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TreeToMapConverter {
    private TreeToMapConverter() {}

    /**
     * converts Nodes to Map<String, Object> where Object is String, List<String> or another Map<String, Object>
     * @param rootNode the root element of the tree
     * @return Map representation of the tree
     */
    public static Map<String, Object> nodeConverter(ObjectNode rootNode){
        if(rootNode == null)
            throw new IllegalArgumentException("rootNode can not be null");

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("type", rootNode.getType());
        rootNode.getFields().forEach((k,v)->{
            if (v instanceof RefNode rn) result.put(k, rn);
            if (v instanceof StringNode sn) result.put(k, sn.value());
            if (v instanceof MultilineNode mn) result.put(k, mn.lines());

            if (v instanceof ObjectNode on) {
                var map = new LinkedHashMap<String, Object>();
                if(on.getRef() != null)
                    map.put("ref", on.getRef());
                map.putAll(nodeConverter(on));
                result.put(k, map);
            }

            if (v instanceof ArrayNode an) {
                List<LinkedHashMap<String, Object>> arr = new ArrayList<>();
                for(var arrItem: an.elements())
                    if(arrItem instanceof ObjectNode arrObjNode) {
                        var map = new LinkedHashMap<String, Object>();
                        if(arrObjNode.getRef() != null)
                            map.put("ref", arrObjNode.getRef());
                        map.putAll(nodeConverter(arrObjNode));
                        arr.add(map);
                    }
                    else if(arrItem instanceof RefNode arrObjRef)
                        arr.add(new LinkedHashMap<>(arrObjRef.asMap()));

                result.put(k, arr);
            }

        });
        return result;
    }
}
