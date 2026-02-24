package com.alexportfolio.logParser;

import com.alexportfolio.logParser.lexer.Lexer;
import com.alexportfolio.logParser.lexer.Token;
import com.alexportfolio.logParser.lexer.TokenType;
import com.alexportfolio.logParser.parser.Parser;
import com.alexportfolio.logParser.parser.node.Node;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static int cursor = 0;
    static int startIdx = -1;
    static TokenType lastToken = TokenType.ENDLINE;
    static List<Token> result = new ArrayList<>();

    static int line = 1, col = 1;        // current position
    static int tokenLine = 0, tokenCol = 0; // start position of current multi-char token

    public static void main(String[] args) throws IOException {
        String logs = Files.readString(Path.of(".\\test.log"));
        Lexer lexer = new Lexer(logs);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        Node root = parser.parseDocument();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(root); // rootNode is your ObjectNode
        System.out.println(json);

    }
}