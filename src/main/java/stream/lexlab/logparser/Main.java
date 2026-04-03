package stream.lexlab.logparser;

import stream.lexlab.logparser.lexer.Lexer;
import stream.lexlab.logparser.token.StructureToken;
import stream.lexlab.logparser.token.Token;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        String logs = Files.readString(Path.of(".\\test.log"));
        // 1. Create tokens
        Lexer lexer = new Lexer(logs);
        List<Token> structureTokens = lexer.tokenPostProcessor();
        structureTokens.forEach(System.out::println);

//        // 2. Create the tree
//        Parser parser = new Parser(structureTokens);
//        ObjectNode root = parser.parseDocument();
//
//        // 3. Create references
//        Referencer referencer = new Referencer();
//        referencer.findRefs(root,  "timestamp:" + root.getType());
//        referencer.collapse(root); // if collapsed, only reference and type preserved in the tree
//
//        System.out.println("_".repeat(20));
//
//        // 4. convert to POJO using custom method
//        var pojo = TreeToMapConverter.convertNode(root, true);
//
//        // 5. convert to JSON and print
//        var gson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gson.toJson(pojo));
//
//        // 6. find Node by reference
//        System.out.println("_".repeat(20));
//        String ref = "timestamp:SessionRoot.customer.address";
//        System.out.printf("Reference \"%s\":\n", ref);
////        var n = referencer.explode(ref);
////        pojo = TreeToMapConverter.convertNode(n, true);
////        System.out.println(gson.toJson(pojo));
//
//        // 7. don't forget to clean the storage when no longer needed:
//        referencer.reset();
//        System.out.println("_".repeat(20));
//        System.out.printf("Reference \"%s\" after resetting the referencer:\n", ref);
//        if(referencer.explode(ref) == null)
//            System.out.println("Reference not found");

    }


}