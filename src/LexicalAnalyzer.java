import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LexicalAnalyzer {
    private int transitionTable[][];

    public LexicalAnalyzer() {
        // Initialize the state transition table
        transitionTable = new int[][]{
            
            {5, 3, 2, 7, 11, 14, 0, 1, 17, 18, 19, 20, 21, 22, 23, 24, 26}, // State 0
            {100},	// State 1 (Error state)
            {2}, 	// State 2 (* <mop> state)
            {4, 3, 4, 4, 4, 4, 4, 1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},	// State 3 
            {4}, // State 4 (Integer <int> state)
            {5, 5, 6, 6, 6, 6, 6, 1, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6},  // State 5
            {6},  // State 6 (Variable state)
            {10, 10, 8, 10, 10, 10, 10, 1, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10}, // State 7
            {8, 8, 9, 8, 8, 8, 8, 1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8}, // State 8
            {8, 8, 8, 0, 8, 8, 8, 1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8}, // State 9
            {10}, // State 10 (/ <div op> state)
            {12, 12, 12, 12, 13, 12, 12, 1, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12}, //state 11
            {12}, // State 12 (=, <assignment>)
            {13}, // State 13 (==, <relop>)
            {15, 15, 15, 15, 16, 15, 15, 1, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15},// state 14
            {15}, // state 15 ( <, <relop>)
            {16}, // state 16 ( <= , <relop>) 
            {17}, // state 17 ( { , <$LB>)
            {18}, // state 18 ( } , <$RB>) 
            {19}, // state 19 ( ( , <$LP>)
            {20}, // state 20 ( ")", <$RP>)
            {21}, // state 21 ( comma, <$comma>)
            {22}, // state 22 ( ; , <$semi>)
            {23}, // state 23 ( + , <$addop>) 
            {24}, // state 24 ( - , <$addop>)
            {26, 26, 26, 26, 27, 26, 26, 1, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26}, // state 25
            {26}, // state 26 ( > , <relop>)
            {27}, // state 27 ( >= , <relop>)
            {1, 1, 1, 1, 29, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // state 28
            {29}, //state 29 (!= , <relop>)
            {100}
        };
    }
    
    public void analyze(String inputFile, String outputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            boolean isClass = false; // Flag to track if the previous token was "CLASS"
            while ((line = reader.readLine()) != null) {
                // Split the line into tokens based on whitespace characters and specific delimiters
                String[] tokens = line.split("\\s+|(?=[(),;])|(?<=[(),;])");

                // Write each token to the output file with its classification
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i];
                    String nextToken = (i < tokens.length - 1) ? tokens[i + 1] : null; // Get the next token or null if it doesn't exist

                    if (!token.isEmpty()) {
                        String classification;
                        if (isClass) {
                            classification = "$program name"; // Classify the token as the program name if the previous token was "CLASS"
                            isClass = false; // Reset the flag
                        } else {
                            classification = classifyToken(token, nextToken); // Classify the token normally
                        }
                        
                        if (token.equals("CLASS")) {
                            isClass = true; // Set the flag if the current token is "CLASS"
                        }

                        writer.write(token + "\t" + classification + "\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }	


    private String classifyToken(String token, String nextToken) {
        // Check if the token matches any known keywords
        switch (token) {
            case "CLASS":
                return "$Class";
            case "CONST":
                return "$Const";
            case "VAR":
                return "$Variable";
            case "{":
                return "$LB";
            case "}":
                return "$RB";
            case "(":
                return "$LP";
            case ")":
                return "$RP";
            case ";":
                return "$semi";
            case ",":
                return "$comma";
            case "=":
                return "$assignment";
                
            default:
                if (token.matches("[0-9]+")) {
                    return "NumLit";
                } else if (token.matches("[a-zA-Z][a-zA-Z0-9]*")) {
                    // Check if the next token is an assignment, indicating a constant variable
                    if ("=".equals(nextToken)) {
                        return "ConstVar";
                    } else {
                        return "Var";
                    }
                } else if (token.matches("[+-]")) {
                    return "$addop";
                } else if (token.matches("[*/]")) {
                    return "$mop";
                } else if (token.matches("[><]|<=|>=|==|!=")) {
                    return "$relop";
                } else {
                    return "Other";
                }
        }
    }

    public void generateSymbolTable(String tokensFile, String symbolTableFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(tokensFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(symbolTableFile))) {

            Map<String, Boolean> addedTokens = new LinkedHashMap<>();
            int codeSegment = 0;
            int dataSegment = 0;
            writer.write(String.format("%-10s%-20s%-10s%-10s%-10s\n", "Symbol", "Classification", "Value", "Address", "Segment"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String token = parts[0];
                String classification = parts[1];

                // Check if the token has already been added
                if (addedTokens.containsKey(token)) {
                    continue; // Skip this token if it has already been added
                } else {
                    addedTokens.put(token, true); // Mark the token as added
                }

                // Add the token to the symbol table based on its classification
                if (classification.startsWith("$")) {
                    // For program name, set the segment as CS (Code Segment)
                    if (classification.equals("$program name")) {
                        writer.write(String.format("%-10s%-20s%-10s%-10s%-10s\n", token, classification, "null", codeSegment, "CS"));
                        codeSegment += 2;
                    }
                } else if (classification.equals("NumLit")) {
                    // For numerical literals, set the segment as DS (Data Segment)
                    writer.write(String.format("%-10s%-20s%-10s%-10s%-10s\n", token, classification, token, dataSegment, "DS"));
                    dataSegment += 2;
                } else if (classification.equals("ConstVar") || classification.equals("Var")) {
                    // For constants and variables, set the segment as DS (Data Segment)
                    writer.write(String.format("%-10s%-20s%-10s%-10s%-10s\n", token, classification, "?", dataSegment, "DS"));
                    dataSegment += 2;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.analyze("input.txt", "tokens.txt");
        analyzer.generateSymbolTable("tokens.txt", "symbol_table.txt");
    }
}
