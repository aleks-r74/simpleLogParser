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
        referencer.collapse(root); // if collapsed, only reference and type preserved in duplicate nodes

        System.out.println("_".repeat(20));

        // 4. convert to POJO using custom method
        var pojo = TreeToMapConverter.nodeConverter(root);

        // 5. convert to JSON and print
        var gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(pojo));

    }


}