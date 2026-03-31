package stream.lexlab.logparser.parser.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MultilineNode implements ReplaceableNode {
    List<String> lines;
    private LinkedHashMap<String, String> metadata;

    private MultilineNode(List<String> lines) {
        this.lines = lines;
        this.metadata = new LinkedHashMap<>();
    }

    public List<String> getLines() {
        return lines;
    }


    @Override
    public String getType() {
        return "MultiLine";
    }

    @Override
    public Map<String, String> metadata() {
        return metadata;
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
