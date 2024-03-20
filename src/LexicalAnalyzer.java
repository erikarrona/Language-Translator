import java.io.*;
import java.util.*;

public class LexicalAnalyzer {
    // Symbol table to store constants, variables, and integers
    private Set<String> symbolTable = new HashSet<>();

    private enum State {
        START, CLASS, CONST, VAR, IDENTIFIER, NUMBER, OPERATOR, ASSIGN, ADDOP, MOP, COMMA, SEMICOLON, LB, RB, LP, RP, OTHER
    }

    private State currentState = State.START;

    private State[][] transitionTable = {
        //             0-9   a-z   +-*   whitespace  =   ,   ;   {   }   (   )   other
        /* START */   {State.NUMBER, State.IDENTIFIER, State.OPERATOR, State.START, State.ASSIGN, State.COMMA, State.SEMICOLON, State.LB, State.RB, State.LP, State.RP, State.OTHER},
        /* CLASS */   {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* CONST */   {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* VAR */     {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* IDENTIFIER */ {State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER, State.IDENTIFIER},
        /* NUMBER */  {State.NUMBER, State.NUMBER, State.NUMBER, State.NUMBER, State.NUMBER, State.NUMBER, State.NUMBER, State.NUMBER, State.NUMBER, State.NUMBER, State.NUMBER, State.NUMBER},
        /* OPERATOR */ {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* ASSIGN */   {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* ADDOP */    {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* MOP */      {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* COMMA */    {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* SEMICOLON */{State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* LB */       {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* RB */       {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* LP */       {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* RP */       {State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START, State.START},
        /* OTHER */    {State.OTHER, State.OTHER, State.OTHER, State.OTHER, State.OTHER, State.OTHER, State.OTHER, State.OTHER, State.OTHER, State.OTHER, State.OTHER, State.OTHER}
    };

    private String[] keywords = {"CLASS", "CONST", "VAR"};
    private String[] assignmentOperators = {"="};
    private String[] arithmeticOperators = {"+", "-", "*", "/"};
    private String[] punctuation = {",", ";", "{", "}", "(", ")"};

    private void createSymbolTable(String outputFileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(outputFileName));
            FileWriter writer = new FileWriter("symbol_table.txt");
            writer.write("Symbol\tClassification\tValue\tAddress\tSegment\n");

            // Initialize addresses for data and code segments
            int dataAddress = 0;
            int codeAddress = 0;

            // Counter for generating unique symbol IDs
            int symbolId = 1;

            // Set to keep track of encountered symbols
            Set<String> encounteredSymbols = new HashSet<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2) { // Check if the line has expected format
                    String classification = parts[1];

                	System.out.println(classification);

                    // Include program name as part of code segment
                    if (classification.equals("$program")) {
                        // Write program name to symbol_table.txt
                        writer.write(symbolId++ + "\t" + parts[0] + "\t" + classification + "\t" + "null" + "\t" + codeAddress + "\tCS\n");

                        // Increment code address by 2
                        codeAddress += 2;

                        // Skip further processing for program name
                        continue;
                    }

                    // Only include constants, variables, and numerical literals in the symbol table
                    if (isIncludedClassification(classification)) {
                        String symbol = parts[0];
                        if (!encounteredSymbols.contains(symbol)) {
                            // Determine segment and update address accordingly
                            String segment;
                            int address;
                            if (classification.equals("ConstVar")) {
                                segment = "DS"; // Data Segment
                                address = dataAddress;
                                dataAddress += 2; // Increment data address by 2
                            } else {
                                segment = "DS"; // Data Segment (assuming numerical literals are stored in the data segment)
                                address = dataAddress;
                                dataAddress += 2; // Increment data address by 2
                            }

                            // Get value (if present) for variables
                            String value = "null";
                            if (classification.equals("ConstVar")) {
                                // Check if the value is a numerical literal or a reference to another symbol
                                if (parts.length >= 4 && parts[3].matches("[0-9]+")) {
                                    value = parts[3];
                                    System.out.println(value);
                                } else {
                                    value = "null"; // Assuming the value is a reference to another symbol
                                }
                            } else if (classification.equals("Var")) {
                                value = "null";
                            } else if (classification.equals("NumLit")) {
                                value = parts[0];
                                System.out.println(value);// Assign the symbol itself as its value for numerical literals
                            }

                            // Write symbol information to symbol_table.txt
                            writer.write(symbolId++ + "\t" + symbol + "\t" + classification + "\t" + value + "\t" + address + "\t" + segment + "\n");

                            // Add the symbol to encountered symbols set
                            encounteredSymbols.add(symbol);
                        }
                    }
                }
            }

            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean isIncludedClassification(String classification) {
        // Include only constants, variables, and numerical literals
        return classification.equals("Class") || classification.equals("ConstVar") || classification.equals("NumLit") || classification.equals("Var");
    }

    private void generateSymbolTable(String outputFileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(outputFileName));
            FileWriter writer = new FileWriter("symbol_table.txt");
            writer.write("Symbol\tClassification\tValue\tAddress\tSegment\n");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length == 5) { // Check if the line has expected format
                    writer.write(String.format("%s\t%s\t%s\t%s\t%s\n", parts[0], parts[1], parts[2], parts[3], parts[4])); // Write symbol and its classification
                }
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void analyze() {
        try {
            // Read input from input.txt
            BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
            StringBuilder inputBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                inputBuilder.append(line).append("\n");
            }
            reader.close();
            String input = inputBuilder.toString();

            // Write tokens output to output.txt
            StringBuilder tokensOutput = new StringBuilder();
            HashSet<String> tokenSet = new HashSet<>(); // HashSet to store unique tokens

            String[] tokens = input.split("\\s*(?=[,;{}=])|(?<=[,;{}=])\\s*|\\s+");

            // Max length of tokens
            int maxLength = 0;
            for (String token : tokens) {
                maxLength = Math.max(maxLength, token.length());
            }

            // Output tokens with consistent tab spacing
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim(); // Trim token to remove leading/trailing whitespace
                if (!token.isEmpty() && tokenSet.add(token)) { // Ignore empty tokens and add to set
                    String classification = getClassification(token);
                    tokensOutput.append(String.format("%-" + (maxLength + 2) + "s", token)).append(" \t ").append(classification).append("\n"); // Append token and classification to output
                }
            }

            writeTokensOutput(tokensOutput.toString());

            // Generate symbol table
            generateSymbolTable("output.txt");
            createSymbolTable("output.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String getClassification(String token) {
        // Check for keywords
        for (String keyword : keywords) {
            if (token.equals(keyword)) {
                return "$" + keyword.toLowerCase();
            }
        }

        // Check for assignment operators
        for (String op : assignmentOperators) {
            if (token.equals(op)) {
                return "$assign";
            }
        }

        // Check for arithmetic operators
        for (String op : arithmeticOperators) {
            if (token.equals(op)) {
                return "$" + op.toLowerCase() + "op";
            }
        }

        // Check for punctuation
        for (String p : punctuation) {
            if (token.equals(p)) {
                return "$" + p.toLowerCase();
            }
        }

        if (token.matches("[a-zA-Z]+\\s*=.*") ) {
            return "ConstVar";
        }

        // Check for variables
        if (token.matches("[a-zA-Z]+")) {
            return "Var";
        }

        // Check for numeric literals
        if (token.matches("[0-9]+")) {
            return "NumLit";
        }

        // Check for the program name pattern using regular expression
        if (token.matches("[a-zA-Z_$][a-zA-Z_$0-9]*")) {
            return "$program";
        }

        return "Other";
    }

    private void writeTokensOutput(String outputString) {
        try {
            FileWriter writer = new FileWriter("output.txt");
            writer.write(outputString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        lexicalAnalyzer.analyze();
    }
}
