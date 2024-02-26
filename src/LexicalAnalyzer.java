import java.io.*;
import java.util.*;

public class LexicalAnalyzer {
    // Symbol table to store constants, variables, and integers
    private Set<String> symbolTable = new HashSet<>();

    private void processInput(String str) {
        int state = 0; // Initial state

        // Loop through each character in the input string
        for (char c : str.toCharArray()) {
            switch (state) {
                case 0:
                    if (c == 'c') state = 1;
                    else if (c == '<') state = 2;
                    else if (c == '{') state = 3;
                    break;
                case 1:
                    if (c == '<') state = 4;
                    break;
                case 2:
                    if (c == 'C') state = 5;
                    else if (c == 'V') state = 8;
                    else if (c == '<') state = 10;
                    break;
                case 3:
                    // No transitions defined for state 3
                    break;
                case 4:
                    if (c == '=') state = 6;
                    else if (c == '<') state = 10;
                    break;
                case 5:
                    // Action: Add constant to symbol table
                    symbolTable.add("CONST" + "");
                    if (c == ';') state = 7;
                    break;
                case 6:
                    if (Character.isDigit(c)) state = 11;
                    break;
                case 7:
                    // No transitions defined for state 7
                    break;
                case 8:
                    // Action: Add variable to symbol table
                    symbolTable.add("VAR");
                    if (c == ',') state = 9;
                    else if (c == '<') state = 10;
                    break;
                case 9:
                    if (c == '<') state = 8;
                    break;
                case 10:
                    if (c == ',') state = 12;
                    else if (c == '<') state = 10;
                    break;
                case 11:
                    // Action: Add integer to symbol table
                    symbolTable.add("INT");
                    // Transitions for EOF and any other character are not explicitly defined
                    break;
                case 12:
                    // No transitions defined for state 12
                    break;
            }
        }
    }

    private void displaySymbolTable() {
        try {
            FileWriter writer = new FileWriter("symbol_table.txt");
            writer.write("Symbol Table: " + "\nSymbol \tClassification\t Value\t Address\t Segment");
            writer.write("\n" + symbolTable);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            // Process input
            String input = inputBuilder.toString();
            processInput(input);

            // Write symbol table to symbol_table.txt
            displaySymbolTable();

            // Write tokens output to output.txt
            StringBuilder tokensOutput = new StringBuilder();
            tokensOutput.append("Tokens output:\n");
            String[] tokens = input.split("[\\s,]+"); // Splitting input by whitespace
            for (String token : tokens) {
            	if (!token.isEmpty()) { // Ignore empty tokens
                tokensOutput.append(token).append("\n");
                processInput(token);
            	}
            }
            writeTokensOutput(tokensOutput.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
        lexicalAnalyzer.analyze();
    }
}
