package com.alexportfolio.logparser.transform;

import com.alexportfolio.logparser.parser.model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TreeToMapConverter {
    private TreeToMapConverter() {}

    /**
     * Converts a Node into a plain Java representation suitable for JSON serialization.
     * Depending on the node type, the result may be a String, a List, or a Map.
     *
     * @param node the node to convert
     * @return a Java representation of the node
     * @throws IllegalArgumentException if the node is null or has an unsupported type
     */
    public static Object convertNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("node cannot be null");
        }
        if (node instanceof RefNode rn) {
            return rn.asMap();
        }
        if (node instanceof StringNode sn) {
            return sn.getValue();
        }
        if (node instanceof MultilineNode mn) {
            return mn.getLines();
        }
        if (node instanceof ArrayNode an) {
            return convertArrayNode(an);
        }
        if (node instanceof ObjectNode on) {
            return convertObjectNode(on);
        }
        throw new IllegalArgumentException("Unsupported node type: " + node.getClass().getName());
    }

    private static List<Object> convertArrayNode(ArrayNode node) {
        List<Object> result = new ArrayList<>();
        for (var element : node.getElements()) {
            var map = new LinkedHashMap<String, Object>();
            Object converted = convertNode(element);
            if (converted instanceof Map<?, ?> convertedMap) {
                map.putAll((Map<String, Object>) convertedMap);
            }
            result.add(map);
        }
        return result;
    }

    private static Map<String, Object> convertObjectNode(ObjectNode node) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        if (node.getId() != null)
            result.put("id", node.getId());
        result.put("type", node.getType());
        node.getFields().forEach((key, value) -> result.put(key, convertNode(value)));
        return result;
    }
}