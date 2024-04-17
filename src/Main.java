import java.util.List;

public class Main {
	public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java LexicalAnalyzer inputFilename");
            return;
        }

        String inputFileName = args[0];
        String tokensFile = "tokens.txt";
        String symbolFile = "symbol_table.txt";
        String quadFile = "quads.txt";
        String optimizedQuads = "optimizedQuads.txt";
        String csFile = "codeSegment.txt";

        // Perform lexical analysis
        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.analyze(inputFileName, tokensFile);
        System.out.println("Tokens generated, outputted in " + tokensFile);

        // Analyze tokens
        analyzer.analyzeTokens(tokensFile, symbolFile);
        System.out.println("Symbol Table analyzed, outputted in " + symbolFile);
        
        // Read tokens
        List<String> tokens = analyzer.readTokens(tokensFile);

        // Perform syntax analysis
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokens, quadFile);
        syntaxAnalyzer.analyze();
        System.out.println("Syntax analysis completed.");

        
        CodeOptimizer optimizer = new CodeOptimizer(quadFile, optimizedQuads);
        
        CodeGenerator.generateCode(optimizedQuads, symbolFile, csFile);
    }
}
