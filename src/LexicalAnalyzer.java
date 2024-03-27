import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LexicalAnalyzer {
    private int stateTable[][];
    private Map<String, String> classifiedIdentifiers;
    private Map<String, String> symbolValue;
    private Map<String, String[]> symbolTable;
    private Set<String> assignedValues;
    private Set<String> addedSymbols;
    private int dataAddress;
    private int codeAddress;

    public LexicalAnalyzer() {
    	classifiedIdentifiers = new HashMap<>();
    	symbolValue = new HashMap<>();
    	symbolTable = new HashMap<>();
    	addedSymbols = new HashSet<>();
    	assignedValues = new HashSet<>();
        dataAddress = 0;
        codeAddress = 0;
        
        // Initialize the state transition table
        stateTable = new int[28][17];

        // Row 0: Transition from state 0
        stateTable[0][0] = 5;  // Letter
        stateTable[0][1] = 3;  // Digit
        stateTable[0][2] = 1;  // "*"
        stateTable[0][3] = 1;  // "/"
        stateTable[0][4] = 11; // "="
        stateTable[0][5] = 14; // "<"
        stateTable[0][6] = 0;  // Whitespace
        stateTable[0][7] = 1;  // Other
        stateTable[0][8] = 17; // "{"
        stateTable[0][9] = 18; // "}"
        stateTable[0][10] = 19; // "("
        stateTable[0][11] = 20; // ")"
        stateTable[0][12] = 21; // ","
        stateTable[0][13] = 22; // ";"
        stateTable[0][14] = 23; // "+"
        stateTable[0][15] = 24; // "-"
        stateTable[0][16] = 25; // ">"

        // Rows 1: Error state
        for (int col = 0; col < 17; col++) {
            stateTable[1][col] = 1; // Stay in error state for all inputs
        }

        // Row 2: $mop "*"
        for (int col = 0; col < 17; col++) {
            stateTable[2][col] = 2; // Stay in $mop state for all inputs
        }

        // Row 3: Transition for Digits
        for(int col = 0; col < 17; col++) {
        	if(col == 1) {
        		stateTable[3][col] = 3;
        	} else if(col == 6) {
        		stateTable[3][col] = 0;
        	} else if(col == 7) {
        		stateTable[3][col] = 1;
        	} else {
        		stateTable[3][col] = 4;
        	}
        }
        

        // Row 4: Integer state
        for(int col = 0; col < 17; col++) {
        	stateTable[4][col] = 4;
        }


        // Rows 5 Variable state transition
        for(int col = 0; col < 17; col++) {
        	if(col == 0 || col == 1) {
        		stateTable[5][col] = 5;
        	} else if(col == 6) {
        		stateTable[5][col] = 0;
        	} else if(col == 7) {
        		stateTable[5][col] = 1;
        	} else {
        		stateTable[5][col] = 6;
        	}
        }

        // Row 6: Variable state
        for(int col = 0; col < 17; col++) {
        	if (col == 6) {
        		stateTable[6][col] = 0;
        	}else if (col == 7) {
        		stateTable[6][col] = 1;
        	} else {
        		stateTable[6][col] = 6;
        	}
        }
        
        // Row 7: Transition for "/"
        for(int col = 0; col < 17; col ++) {
        	if(col == 2) {
        		stateTable[7][col] = 8;
        	} else if (col == 7) {
        		stateTable[7][col] = 1;
        	} else {
        		stateTable[7][col] = 10;
        	}
        }
        
        // Row 8: elimnitate comments /* o o o */
        for(int col = 0; col < 17; col ++) {
        	if(col == 2) {
        		stateTable[8][col] = 9;
        	} else if (col == 7) {
        		stateTable[8][col] = 1;
        	} else {
        		stateTable[8][col] = 8;
        	}
        }
        
        // Row 9: read comment
        for (int col = 0; col < 17; col++) {
            if (col == 3) {
                stateTable[9][col] = 0; // Transition to state 0 on "/" input
            } else if(col == 7) {
            	stateTable[9][col] = 1;
            } else {
                stateTable[9][col] = 8; // Transition to state 8 for all other inputs
            }
        }
        
        // Row 10: Transition for "/" operator
        for (int col = 0; col < 17; col++) {
            stateTable[10][col] = 10; // Stay in state 10 for all inputs
        }
        
        // Row 11: Transition for state 11
        for(int col = 0; col < 17; col++) {
        	if(col == 4) {
                stateTable[11][col] = 13;  // "="
        	} else if(col == 6) {
                stateTable[11][col] = 0;  // Whitespace
        	} else if(col == 7) {
        		stateTable[11][col] = 1;  // Other
        	} else {
        		stateTable[11][col] = 12;
        	}
        }
        
        // Row 12: "=" $assignment
        for (int col = 0; col < 17; col++) {
            stateTable[12][col] = 12; // Stay in state 12 for all inputs
        }
        // Row 13: "==" $relop
        for (int col = 0; col < 17; col++) {
            stateTable[13][col] = 13; // Stay in state 13 for all inputs
        }
        
        // Row 14: Transition for state 14
        for (int col = 0; col < 17; col++) {
            if (col == 4) {
                stateTable[14][col] = 16; // Transition to state 16 on "<" input
            } else if(col == 6) {
            	stateTable[14][col] = 0;
            } else {
                stateTable[14][col] = 15; // Transition to state 15 for all other inputs
            }
        }
        
        // Row 15: "<" $relop
        for (int col = 0; col < 17; col++) {
            stateTable[15][col] = 15; // Stay in state 15 for all inputs
        }

        // Row 16: "<=" $relop
        for (int col = 0; col < 17; col++) {
            stateTable[16][col] = 16; // Stay in state 16 for all inputs
        }

        // Row 17: "{" $LB
        for (int col = 0; col < 17; col++) {
            stateTable[17][col] = 17; // Stay in state 17 for all inputs
        }

        // Row 18: "}" $RB
        for (int col = 0; col < 17; col++) {
            stateTable[18][col] = 18; // Stay in state 18 for all inputs
        }

        // Row 19: "(" $LP
        for (int col = 0; col < 17; col++) {
            stateTable[19][col] = 19; // Stay in state 19 for all inputs
        }

        // Row 20: ")" $RP
        for (int col = 0; col < 17; col++) {
            stateTable[20][col] = 20; // Stay in state 20 for all inputs
        }
        
        // Row 21: "," $comma
        for (int col = 0; col < 17; col++) {
        	stateTable[21][col] = 21;
        }

        // Row 22: ";" $semicolon
        for (int col = 0; col < 17; col++) {
        	stateTable[22][col] = 22;
        }


        // Row 23: "+" $addop
        for (int col = 0; col < 17; col++) {
            stateTable[23][col] = 23; // Stay in state 23 for all inputs
        }

        // Row 24: "-" $addop
        for (int col = 0; col < 17; col++) {
            stateTable[24][col] = 24; // Stay in state 24 for all inputs
        }
        // Row 25 transition
        for (int col = 0; col < 17; col++) {
            if (col == 4) {
                stateTable[25][col] = 27; // Transition to state 27 on "=" input
            } else if(col == 6) {
            	stateTable[25][col] = 0;
            } else {
                stateTable[25][col] = 26; // Transition to state 26 for all other inputs
            }
        }
        
        // Row 26: ">" $relop
        for (int col = 0; col < 17; col++) {
            stateTable[26][col] = 26; // Stay in state 26 for all inputs
        }

        // Row 27: ">=" $relop
        for (int col = 0; col < 17; col++) {
            stateTable[27][col] = 27; // Stay in state 27 for all inputs
        }
        
    }
    
    public List<String> processTokens(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        int currentState = 0;
        boolean inComment = false; // Flag to track whether we're inside a comment

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Skip characters inside comments
            if (inComment) {
                if (c == '*' && i < input.length() - 1 && input.charAt(i + 1) == '/') {
                    inComment = false; // End of multi-line comment
                    i++; // Skip the next character '/'
                }
                continue; // Skip processing this character
            }

            // Check for comment start
            if (c == '/' && i < input.length() - 1) {
                char nextChar = input.charAt(i + 1);
                if (nextChar == '/') {
                    // Single-line comment found, skip remaining characters in the line
                    break;
                } else if (nextChar == '*') {
                    // Multi-line comment found, set flag to skip characters until the end of comment
                    inComment = true;
                    i++; // Skip the next character '*'
                    continue; // Skip processing this character
                }
            }

            int inputIndex = getInputIndex(c);
            int nextState = stateTable[currentState][inputIndex];

            if (nextState != 0 && isPunctuation(c) == false) {
                currentToken.append(c);
                currentState = nextState;
            } else {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken = new StringBuilder();
                }
                // Check if the character is a punctuation
                if (isPunctuation(c)) {
                    tokens.add(String.valueOf(c));
                }
                currentState = 0; // Reset state
            }
        }

        // Add the last token if it's not empty
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
            System.out.println("Token Added: " + currentToken);
        }

        return tokens;
    }


    private boolean isPunctuation(char c) {
        return c == '{' || c == '}' || c == '(' || c == ')' || c == ',' || c == ';' || c == '+' || c == '-' || c == '*' || c == '/';
    }
    
    private int getInputIndex(char c) {
        if (Character.isLetter(c)) {
            return 0; // Letter
        } else if (Character.isDigit(c)) {
            return 1; // Digit
        } else if (c == '*') {
            return 2; // Asterisk
        } else if (c == '/') {
            return 3; // Slash
        } else if (c == '=') {
            return 4; // Equals
        } else if (c == '<') {
            return 5; // Less than
        } else if (Character.isWhitespace(c)) {
            return 6; // Whitespace
        } else if (c == '{') {
            return 8; // Left curly brace
        } else if (c == '}') {
            return 9; // Right curly brace
        } else if (c == '(') {
            return 10; // Left parenthesis
        } else if (c == ')') {
            return 11; // Right parenthesis
        } else if (c == ',') {
            return 12; // Comma
        } else if (c == ';') {
            return 13; // Semicolon
        } else if (c == '+') {
            return 14; // Plus
        } else if (c == '-') {
            return 15; // Minus
        } else if (c == '>') {
            return 16; // Greater than
        } else {
            return 7; // Other
        }
    }
    
    public String classifyToken(String token, List<String> tokens) {

    	if (classifiedIdentifiers.containsKey(token)) {
            // If yes, return its existing classification
            return classifiedIdentifiers.get(token);
        }
    	
    	if (token.matches("[a-zA-Z][a-zA-Z0-9_]*")){
    		if(token.toLowerCase().matches("class")) {
    			classifiedIdentifiers.put(token, "$Class");
    			return "$Class";
    		}
    		int index = tokens.indexOf(token); // Get the index of the current token
    		
            if (index > 0 && tokens.get(index - 1).toLowerCase().equals("class")) {
            	classifiedIdentifiers.put(token, "$program name");
                return "$Program Name"; 
            } else if(index < tokens.size() - 2 && tokens.get(index + 1).equals("=") && isInteger(tokens.get(index + 2))){
            	classifiedIdentifiers.put(token, "ConstVar");
            	symbolValue.put(token, tokens.get(index + 2));
            	return "ConstVar";// Return the classification for ConstVar
            } else if (index > 0){
            	classifiedIdentifiers.put(token, "Var");
                return "Var"; // Return the classification for VAR
            }
    	}
    	if(token.matches("[0-9]*")) {
    		classifiedIdentifiers.put(token, "NumLit");
    		return "NumLit";
    		
    	}
    	
    	switch (token.toLowerCase()) {
            case "const":
                return "$Const"; // Return the classification for CONST
            case "var":
            	return "$Var";
            case "{":
            	return "$LB"; 
            case "}":
                return "$RB"; // Return the classification for brackets
            case "(":
            	return "$LP";
            case ")":
                return "$RP"; // Return the classification for parenthesis
            case ",":
                return "$Comma"; // Return the classification for comma
            case ";":
                return "$Semicolon";
            case "*":
            case "/":
                return "$mop"; // Return the classification for multiplication and division operators
            case "+":
            case "-":
                return "$addop"; // Return the classification for addition and subtraction operators
            case "==":
            case ">=":
            case "<=":
            case "!=":
            case ">":
            case "<":
                return "$relop"; // Return the classification for relational operators
            case "=":
                return "$="; // Return the classification for assignment operator
            default:
                return "Unknown";
        }
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void writeTokensFile(String inputFile, String outputFile) {
    	try (	BufferedReader br = new BufferedReader(new FileReader(inputFile));
    			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
    		String line;
    		while ((line = br.readLine()) != null) {
    			// Process each line using the lexical analyzer
    			List<String> tokens = processTokens(line);
    			// Output the tokens for this line to the file
    			for (String token : tokens) {
    				// Classify the token
    				String classification = classifyToken(token, tokens);   
                       // Write the token and its classification to the output file                       
    				bw.write(token + "\t" + classification);                       
    				bw.newLine();
    				}
    			}
    		System.out.println("Tokens with classifications written to " + outputFile);
    		} 
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    }

    public static void main(String[] args) {
    	
    	String inputFilename = "input.txt";
        String tokensFile = "tokens.txt";
        String symbolTable = "symbolTable.txt";

        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.writeTokensFile(inputFilename, tokensFile);
        analyzer.writeSymbolTable(tokensFile, symbolTable);
        
    }
    
    public void writeSymbolTable(String tokensFile, String outputFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            // Define column widths
            int symbolWidth = 10;
            int classificationWidth = 15;
            int valueWidth = 10;
            int addressWidth = 10;
            int segmentWidth = 10;

            // Write header with padding
            bw.write(String.format("%-" + symbolWidth + "s", "Symbol"));
            bw.write(String.format("%-" + classificationWidth + "s", "Classification"));
            bw.write(String.format("%-" + valueWidth + "s", "Value"));
            bw.write(String.format("%-" + addressWidth + "s", "Address"));
            bw.write(String.format("%-" + segmentWidth + "s", "Segment"));
            bw.newLine();

            // Set to store symbols already added to the table
            Set<String> addedSymbols = new HashSet<>();

            List<String> tokens = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(tokensFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    tokens.add(line.trim());
                }
            }

            String segment = "$DS"; // Start with Data Segment

            for (String token : tokens) {
                // Split the token into symbol and classification parts
                String[] parts = token.split("\t");
                if (parts.length != 2) {
                    System.out.println("Invalid token format: " + token);
                    continue; // Skip invalid tokens
                }
                String symbol = parts[0];
                String classification = parts[1];

                // Find value for symbol
                String value = "?"; // Initialize value as unknown by default
                if (classification.equals("ConstVar")) {
                    value = symbolValue.getOrDefault(symbol, "?");
                    assignedValues.add(value);
                } else if (classification.equals("NumLit")) {
                    // Check if the value corresponds to a symbol that has already been assigned
                    if (assignedValues.contains(symbol)) {
                        continue; // Skip treating it as a NumLit
                    }
                    // If not, update the assigned values set for NumLit
                    assignedValues.add(symbol);
                    value = symbol; // Value is the token itself
                }

                // Find address for symbol
                String address;
                if (classification.equals("$Program Name")) {
                    address = String.valueOf(codeAddress);
                    codeAddress += 2;
                    value = ""; // Clear value for program name
                    segment = "$CS"; // Switch to Code Segment for program name
                } else if (!classification.startsWith("$")) {
                    address = String.valueOf(dataAddress);
                    dataAddress += 2; // Increment data address by 2
                    segment = "$DS";
                } else {
                    address = "?"; // For other segments, address is unknown
                }

                // Add the symbol to the symbol table only if it's not already added
                if (!addedSymbols.contains(symbol)) {
                    // Write into symbol table
                	if (!classification.startsWith("$") || classification.equals("$Program Name")) {
	                    bw.write(String.format("%-" + symbolWidth + "s", symbol));
	                    bw.write(String.format("%-" + classificationWidth + "s", classification));
	                    bw.write(String.format("%-" + valueWidth + "s", value));
	                    bw.write(String.format("%-" + addressWidth + "s", address));
	                    bw.write(String.format("%-" + segmentWidth + "s", segment));
	                    bw.newLine();
                	}

                    // Add the symbol to the set of added symbols
                    addedSymbols.add(symbol);
                }
            }

            System.out.println("Symbol table written to " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
