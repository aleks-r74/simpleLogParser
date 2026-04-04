package stream.lexlab.logparser;

import stream.lexlab.logparser.lexer.Lexer;
import stream.lexlab.logparser.parser.Parser;
import stream.lexlab.logparser.parser.model.ObjectNode;
import stream.lexlab.logparser.token.StructureToken;
import stream.lexlab.logparser.token.Token;
import stream.lexlab.logparser.token.processor.TokenPostProcessor;
import stream.lexlab.logparser.transform.Referencer;
import stream.lexlab.logparser.transform.TreeToMapConverter;

import java.util.List;
import java.util.Map;

public class LogParser {
    Referencer referencer = new Referencer();

    /**
     * Creates a tree
     * @param logs - input logs as a string
     * @return ObjectNode that represents the root node of the tree
     */
    public ObjectNode getTree(String logs){
        // 1. Create tokens
        Lexer lexer = new Lexer(logs);
        List<StructureToken> structureTokens = lexer.tokenize();

        var tokenPostProcessor = new TokenPostProcessor();
        List<Token> grammarTokens = tokenPostProcessor.toGrammarTokens(structureTokens);

        // 2. Create the tree
        Parser parser = new Parser(grammarTokens);
        return parser.parseDocument();
    }

    /**
     * Builds a tree structure from the given logs and optionally deduplicates objects.
     * When `collapse` is true, duplicate objects are replaced with references to their
     * first occurrence. Deduplication is stateful, meaning future calls will reuse
     * these references unless `reset()` is invoked.
     *
     * @param logs - the input logs as a string
     * @param treeName - a prefix to use as the root identifier for each tree
     * @param collapse - if true, duplicate branches are collapsed into references
     * @param hideMetadata - if true, object metadata (including IDs) is excluded from the tree
     * @return a Map representing the tree, ready for serialization
     */
    public Map<String, Object> getTreeWithRefs(String logs, String treeName, boolean collapse, boolean hideMetadata){
        var root = getTree(logs);
        if(collapse) {
            referencer.findRefs(root,  treeName + root.getType());
            referencer.collapse(root);
        }
        return TreeToMapConverter.convertObjectNode(root, hideMetadata);
    }

    /**
     * Returns node from the internal state by its reference
     * @param ref
     * @return
     */
    public Object getNodeByRef(String ref){
        var n = referencer.explode(ref);
        return TreeToMapConverter.convertNode(n, true);
    }

    /**
     * Cleans the referencer state
     */
    public void reset(){
        referencer.reset();
    }
}
