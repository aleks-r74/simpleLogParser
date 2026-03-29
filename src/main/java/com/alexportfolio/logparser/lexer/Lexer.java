package com.alexportfolio.logparser.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Lexer {

    private static final Pattern OBJECT_TYPE_PATTERN =
            Pattern.compile("^\\s*[A-Za-z_][A-Za-z0-9_]*<\\d+>\\s*$");

    private final String content;
    private boolean withNoise = false;
    private int cursor = 0;
    private int startIdx = -1;

    private TokenType lastGrammarToken = TokenType.UNKNOWN;
    private final QuoteState quoteState = new QuoteState();

    private final List<Token> result = new ArrayList<>();

    private int line = 1;
    private int col = 1;
    private int tokenLine = 0;
    private int tokenCol = 0;

    /**
     * @param input     input text
     * @param withNoise include NOISE tokens in the result
     */
    public Lexer(String input, Boolean withNoise) {
        this(input);
        this.withNoise = withNoise;
    }

    public Lexer(String input) {
        String normalized = input.replace("\r\n", "\n");
        if (!normalized.endsWith("\n")) {
            normalized += "\n";
        }
        this.content = normalized;
    }

    public List<Token> tokenize() {
        int openBrackets = 0; // for array detection

        while (cursor < content.length()) {
            char ch = content.charAt(cursor);

            TokenType currToken;
            if (quoteState.inQuotes(ch)) {
                currToken = TokenType.UNRESOLVED;
            } else {
                currToken = TokenType.getType(ch);
            }

            // '[' counts as LBRACKET only after EQUAL and only if we are not building a multichar token
            boolean nonLBracket =
                    currToken == TokenType.LBRACKET
                            && (lastGrammarToken != TokenType.EQUAL || startIdx != -1);
            if (currToken == TokenType.LBRACKET && !nonLBracket) {
                openBrackets++;
            }

            // ']' counts as RBRACKET only when inside an array
            boolean nonRBracket = openBrackets == 0 && currToken == TokenType.RBRACKET;
            if (currToken == TokenType.RBRACKET && openBrackets > 0) {
                openBrackets--;
            }

            if (nonLBracket || nonRBracket) {
                currToken = TokenType.UNRESOLVED;
            }

            // Start of a multi-character token
            if (currToken == TokenType.UNRESOLVED && startIdx == -1) {
                startIdx = cursor;
                tokenLine = line;
                tokenCol = col;
            }

            // End of a multi-character token
            else if (TokenType.isKnown(currToken) && startIdx >= 0) {
                String rawLexeme = getRawLexeme();
                String lexeme = trimQuotedValue(rawLexeme);
                startIdx = -1;

                TokenType multichar =
                        (currToken == TokenType.EQUAL) ? TokenType.IDENTIFIER : TokenType.VALUE;

                boolean eol = currToken == TokenType.EOL;

                switch (lastGrammarToken) {
                    case UNKNOWN, NOISE, LBRACE -> {
                        if (eol && looksLikeObjectType(rawLexeme)) {
                            multichar = TokenType.OBJTYPE;
                            lexeme = normalizeObjectType(rawLexeme.strip());
                        } else if (eol) {
                            multichar = TokenType.VALUE;
                        }
                    }
                    case EQUAL -> {
                        if (lexeme.equals("...")) {
                            multichar = TokenType.MULTILINE;
                        } else if (eol) {
                            multichar = TokenType.VALUE;
                        }
                    }
                    case MULTILINE, LINE -> {
                        if (eol) {
                            multichar = TokenType.LINE;
                        }
                    }
                }

                if (multichar == TokenType.IDENTIFIER) {
                    lexeme = normalizeFieldName(rawLexeme);
                }

                // Any VALUE without assignment is NOISE
                if (multichar == TokenType.VALUE && lastGrammarToken != TokenType.EQUAL) {
                    multichar = TokenType.NOISE;
                    if (withNoise) {
                        result.add(new Token(multichar, lexeme, tokenLine, tokenCol));
                        lastGrammarToken = multichar;
                    }
                } else {
                    result.add(new Token(multichar, lexeme, tokenLine, tokenCol));
                    lastGrammarToken = multichar;
                }
            }

            // Single-character token
            if (TokenType.isKnown(currToken)) {
                // Handle absent values
                if (lastGrammarToken == TokenType.EQUAL && currToken == TokenType.EOL) {
                    result.add(new Token(TokenType.VALUE, "null", line, col));
                }

                if (currToken != TokenType.EOL) {
                    result.add(new Token(currToken, line, col));
                    lastGrammarToken = currToken;
                }
            }

            advancePosition(ch);
        }

        result.add(new Token(TokenType.EOF, line, col));
        return result;
    }

    private void advancePosition(char ch) {
        cursor++;
        if (ch == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
    }

    private String getRawLexeme() {
        return content.substring(startIdx, cursor);
    }

    private String trimQuotedValue(String lexeme) {
        int start = 0;
        int end = lexeme.length();
        if (lexeme.startsWith("\"")) {
            start++;
        }
        if (lexeme.endsWith("\"")) {
            end--;
        }
        return lexeme.substring(start, end);
    }

    private boolean looksLikeObjectType(String lexeme) {
        return OBJECT_TYPE_PATTERN.matcher(lexeme).matches();
    }

    private String normalizeFieldName(String lexeme) {
        return lexeme
                .stripTrailing()
                .replaceFirst("\\[\\d+\\]\\s*$", "");
    }

    private String normalizeObjectType(String lexeme) {
        return lexeme.replaceFirst("<\\d+>\\s*$", "");
    }

    private static class QuoteState {
        private boolean inside = false;
        private char prevCh = 0;

        boolean inQuotes(char ch) {
            boolean wasInside = inside;
            if (ch == '"' && prevCh != '\\') {
                inside = !inside;
            }
            prevCh = ch;
            return wasInside;
        }
    }
}