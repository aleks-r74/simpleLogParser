package com.alexportfolio.logParser;

import com.alexportfolio.logParser.lexer.Lexer;
import com.alexportfolio.logParser.lexer.Token;
import com.alexportfolio.logParser.parser.Parser;
import com.alexportfolio.logParser.transform.Referencer;
import com.alexportfolio.logParser.parser.model.*;
import com.alexportfolio.logParser.transform.TreeToMapConverter;
import com.google.gson.GsonBuilder;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        String logs = Files.readString(Path.of(".\\test.log"));
        // 1. Create tokens
        Lexer lexer = new Lexer(logs);
        List<Token> tokens = lexer.tokenize();
        tokens.forEach(System.out::println);

        // 2. Create the tree
        Parser parser = new Parser(tokens);
        ObjectNode root = parser.parseDocument();

        // 3. Create references
        Referencer referencer = new Referencer();
        referencer.findRefs(root, "timestamp");
        referencer.collapse(root); // if collapsed, only reference and type preserved in the tree

        System.out.println("_".repeat(20));

        // 4. convert to POJO using custom method
        var pojo = TreeToMapConverter.nodeConverter(root);

        // 5. convert to JSON and print
        var gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(pojo));

        // 6. find Node by reference
        System.out.println("_".repeat(20));
        String ref = "timestamp:SessionRoot.customer$";
        System.out.printf("Reference \"%s\":\n", ref);
        ObjectNode n = referencer.explode(ref);
        pojo = TreeToMapConverter.nodeConverter(n);
        System.out.println(gson.toJson(pojo));

        // 7. don't forget to clean the storage when no longer needed:
        referencer.reset();
        System.out.println("_".repeat(20));
        System.out.printf("Reference \"%s\" after resetting the referencer:\n", ref);
        if(referencer.explode(ref) == null)
            System.out.println("Reference not found");

    }


}