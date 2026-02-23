package com.alexportfolio.logParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static int cursor = 0;
    static int startIdx = -1;
    static TokenType lastToken = TokenType.NEWLINE;
    static List<Token> result = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        String content = Files.readString(Path.of(".\\test.log")).replace("\r\n", "\n");
        while (cursor < content.length()) {
            char ch = content.charAt(cursor);
            TokenType candidate = TokenType.getType(ch);
            // multichar token start
            if (candidate == TokenType.PART && startIdx == -1)
                startIdx = cursor;
            // end of multichar token
            else if (candidate != null && candidate != TokenType.PART && startIdx >= 0) {
                String lexeme = content.substring(startIdx, cursor);
                startIdx = -1; // reset
                if(candidate == TokenType.EQUAL)
                    result.add(new Token(TokenType.IDENTIFIER, lexeme, 0, 0));
                else if (candidate == TokenType.NEWLINE && lastToken != TokenType.EQUAL)
                    result.add(new Token(TokenType.IDENTIFIER, lexeme, 0, 0));
                else
                    result.add(new Token(TokenType.VALUE, lexeme, 0, 0));
            }
            // add single-char token
            if(TokenType.isValid(candidate) ) {
                lastToken = candidate;
                result.add(new Token(candidate, 0, 0));
            }
            cursor++;
        }
    result.forEach(System.out::println);

    }

}