package com.alexportfolio.logParser;

import com.alexportfolio.logParser.lexer.Lexer;
import com.alexportfolio.logParser.lexer.Token;
import com.alexportfolio.logParser.parser.Parser;
import com.alexportfolio.logParser.parser.node.*;
import com.alexportfolio.logParser.serializer.ObjectNodeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        String logs = Files.readString(Path.of(".\\test.log"));
        Lexer lexer = new Lexer(logs);
        List<Token> tokens = lexer.tokenize();
        tokens.forEach(System.out::println);
        Parser parser = new Parser(tokens);
        Node root = parser.parseDocument();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ObjectNode.class, new ObjectNodeAdapter())
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(root); // rootNode is your ObjectNode
        System.out.println(json);

        System.out.println("_".repeat(20));
        var pojo = nodeConverter((ObjectNode) root);
        gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(pojo));
    }

    /**
     * converts Nodes to real pojo Map<String, Object> where Object is String, List<String> or another Map<String, Object>
     * @param in
     * @return
     */
    private static LinkedHashMap<String, Object> nodeConverter(ObjectNode in){
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("name", in.name);
        in.fields.forEach((k,v)->{
            if (v instanceof StringNode sn) result.put(k, sn.value);
            if (v instanceof MultilineNode mn) result.put(k, mn.lines);
            if (v instanceof ObjectNode on) result.put(k, nodeConverter(on));
            if (v instanceof ArrayNode an) {
                List<LinkedHashMap<String, Object>> arr = new ArrayList<>();
                an.elements.forEach(arrObj->arr.add(nodeConverter(arrObj)));
                result.put(k, arr);
            }
        });
        return result;
    }
}