package com.alexportfolio.logParser.serializer;

import com.alexportfolio.logParser.parser.node.*;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ObjectNodeAdapter implements JsonSerializer<ObjectNode> {
    @Override
    public JsonElement serialize(ObjectNode src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", src.name); // keep name

        for (var entry : src.fields.entrySet()) {
            String key = entry.getKey();
            Node value = entry.getValue();

            // Serialize Node differently depending on type
            if (value instanceof StringNode sn) {
                obj.addProperty(key, sn.value);
            } else if (value instanceof MultilineNode mn) {
                JsonArray arr = new JsonArray();
                mn.lines.forEach(arr::add);
                obj.add(key, arr);
            } else if (value instanceof ArrayNode an) {
                obj.add(key, context.serialize(an.elements));
            } else if (value instanceof ObjectNode on) {
                obj.add(key, context.serialize(on));
            }
        }

        return obj;
    }
}