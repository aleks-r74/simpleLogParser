package com.alexportfolio.logparser.parser.model;

import java.util.ArrayList;
import java.util.List;

public record MultilineNode(List<String> lines) implements Node {
    public static class Builder{
        private final List<String> lines = new ArrayList<>();
        public static Builder builder(){ return new Builder(); }
        public void addLine(String line){ lines.add(line); }
        public MultilineNode build(){ return new MultilineNode(lines); }
    }
}
