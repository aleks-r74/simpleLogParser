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
    static int line = 1, col = 0;
    public static void main(String[] args) throws IOException {
        String content = Files.readString(Path.of(".\\test.log")).replace("\r\n", "\n");
        while (cursor < content.length()) {

            char ch = content.charAt(cursor);

            TokenType candidate = TokenType.getType(ch);
            // multichar token start
            if (candidate == TokenType.UNRESOLVED && startIdx == -1)
                startIdx = cursor;
            // end of multichar token
            else if (candidate != TokenType.UNKNOWN && candidate != TokenType.UNRESOLVED && startIdx >= 0) {
                String lexeme = content.substring(startIdx, cursor);
                startIdx = -1; // reset
                if(candidate == TokenType.EQUAL)
                    result.add(new Token(TokenType.IDENTIFIER, lexeme, line, col));
                else if (candidate == TokenType.NEWLINE && lastToken != TokenType.EQUAL)
                    result.add(new Token(TokenType.IDENTIFIER, lexeme, line, col));
                else
                    result.add(new Token(TokenType.VALUE, lexeme, line, col));
            }
            // add single-char token
            if(TokenType.isValid(candidate) ) {
                lastToken = candidate;
                result.add(new Token(candidate, line, col));
            }
            cursor++;
        }
        // edge case when log finished without new line
        if(startIdx > 0) {
            String lexeme = content.substring(startIdx, content.length());
            result.add(new Token(TokenType.IDENTIFIER, lexeme, 0, 0));
        }
        result.add(new Token(TokenType.EOF, 0, 0));
    result.forEach(System.out::println);

    }

}