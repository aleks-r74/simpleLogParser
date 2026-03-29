package com.alexportfolio.logparser.parser.model;

import java.util.ArrayList;
import java.util.List;

public final class MultilineNode implements ReplaceableNode {
    List<String> lines;
    private String ref;
    private String id;

    private MultilineNode(List<String> lines) {
        this.lines = lines;
    }

    public List<String> getLines() {
        return lines;
    }

    @Override
    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public String getType() {
        return "MultiLine";
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static class Builder{
        private final List<String> lines = new ArrayList<>();
        public static Builder builder(){ return new Builder(); }
        public void addLine(String line){ lines.add(line); }
        public MultilineNode build(){ return new MultilineNode(lines); }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MultilineNode that = (MultilineNode) object;
        return lines.equals(that.lines);
    }

    @Override
    public int hashCode() {
        return lines.hashCode();
    }
}
