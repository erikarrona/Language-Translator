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
    private Set<String> assignedValues;
    private Set<String> addedSymbols;
    private int dataAddress;
    private int codeAddress;

    public LexicalAnalyzer() {
    	classifiedIdentifiers = new HashMap<>();
    	symbolValue = new HashMap<>();
    	addedSymbols = new HashSet<>();
    	assignedValues = new HashSet<>();
        dataAddress = 0;
        codeAddress = 0;
        
        // Initialize the state transition table
        stateTable = new int[28][17];

        // Row 0: Transition from state 0
        stateTable[0][0] = 5;  // Letter
        stateTable[0][1] = 3;  // Digit
        stateTable[0][2] = 2;  // "*"
        stateTable[0][3] = 7;  // "/"
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
        for (int col = 0; col < 17; col++) {
            if (col == 1) {
                stateTable[3][col] = 3;
            } else if (col == 7){
            	stateTable[3][col] = 1;
            } else {
                stateTable[3][col] = 4; 
            }
        }

        // Row 4: Integer state
        for (int col = 0; col < 17; col++) {
            stateTable[4][col] = 4; // Integer state remains in itself for all inputs
        }



        // Rows 5 Variable state transition
        for(int col = 0; col < 17; col++) {
        	if(col == 0 || col == 1) {
        		stateTable[5][col] = 5;
        	} else if(col == 7) {
        		stateTable[5][col] = 1;
        	} else {
        		stateTable[5][col] = 6;
        	}
        }

        // Row 6: Variable state
        for(int col = 0; col < 17; col++) {
        	stateTable[6][col] = 6;
        	
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
    
    private boolean isDelimeter(char c) {
        String punctuationAndOperators = ",;";
        return punctuationAndOperators.indexOf(c) != -1;
    }

    private List<String> processTokens(String line) {
        List<String> tokenList = new ArrayList<>();
        int strNdx = 0;
        int strLen = line.length();
        while (strNdx < strLen) {
            StringBuilder tokenBuilder = new StringBuilder();
            // Skip leading blanks
            while (strNdx < strLen && (line.charAt(strNdx) == ' ' || line.charAt(strNdx) == '\t' || line.charAt(strNdx) == '\n')) {
                strNdx++;
            }
            // Build token delimited by a space or end of string or punctuation
            while (strNdx < strLen && line.charAt(strNdx) != ' ' && !isDelimeter(line.charAt(strNdx))) {
                tokenBuilder.append(line.charAt(strNdx));
                strNdx++;
            }
            // Add token to the list
            if (tokenBuilder.length() > 0) {
            	tokenBuilder.append(' ');
                tokenList.add(tokenBuilder.toString());
            }
            // Check if the current character is a punctuation or operator
            if (strNdx < strLen && isDelimeter(line.charAt(strNdx))) {
            	tokenBuilder.append(' ');
                tokenList.add(Character.toString(line.charAt(strNdx)));
                strNdx++;
            }
        }
        return tokenList;
    }

    
 
    
    private String classifyToken(String token) {
    	
    	
    	
        int currentState = 0; // Start at state 0
        for (char c : token.toCharArray()) {
            int column;
            if (Character.isLetter(c)) {
                column = 0; // Letter
            } else if (Character.isDigit(c)) {
                column = 1; // Digit
            } else if (c == '*') {
                column = 2; // "*"
            } else if (c == '/') {
                column = 3; // "/"
            } else if (c == '=') {
                column = 4; // "="
            } else if (c == '<') {
                column = 5; // "<"
            } else if (Character.isWhitespace(c)) {
                column = 6; // Whitespace
            } else if (c == '{') {
                column = 8; // "{"
            } else if (c == '}') {
                column = 9; // "}"
            } else if (c == '(') {
                column = 10; // "("
            } else if (c == ')') {
                column = 11; // ")"
            } else if (c == ',') {
                column = 12; // ","
            } else if (c == ';') {
                column = 13; // ";"
            } else if (c == '+') {
                column = 14; // "+"
            } else if (c == '-') {
                column = 15; // "-"
            } else if (c == '>') {
                column = 16; // ">"
            } else {
                column = 7; // Other
            }
            
            
            currentState = stateTable[currentState][column]; // Transition to next state
            System.out.println("TOKEN: " + token + " state = " + currentState);
        }
        // Determine the classification based on the final state
        switch (currentState) {
        
        	case 10:	//mop state
            case 2:
                return "<mop>";
            case 4:  // Integer state
                return "<integer>";
            case 6:  // Variable state
        		switch(token) {
        			case "CLASS ":
        				return "$CLASS";
        			case "IF ":
        				return "$IF";
        			case "CONST ":
        				return "$CONST";
        			case "VAR ":
        				return "$VAR";
        			case "ELSE ":
        				return "$ELSE";
        			case "CALL ":
        				return "$CALL";
        			case "WHILE ":
        				return "$WHILE";
        			case "DO ":
        				return "$DO";
        			case "PROCEDURE ":
        				return "$PROCEDURE";
    				default:
    	                return "<var>";
        		}
            	
            case 12: // Assignment state
                return "<assign>";
            case 13: 
            	System.out.println(token);
            case 15: // Relational operator state
            case 16: 
            case 26:
            case 27:
                return "<relop>";
            case 17:
                return "$LB";
            case 18: 
                return "$RB";
            case 19: 
                return "$LP";
            case 20: 
                return "$RP";
            case 21: 
                return "<comma>";
            case 22: // Semicolon state
                return "<semi>";
            case 23: // Additive operator state
            case 24: // Additive operator state
                return "<addop>";
            default:
                return "Other"; // Default classification for unrecognized tokens
        }
    }

    private void writeTokensFile(String inputFile, String outputFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Process each line using the lexical analyzer
                List<String> tokens = processTokens(line);
                // Output the tokens for this line to the file
                for (String token : tokens) {
                    // Classify the token
                    String classification = classifyToken(token);   
                    // Write the token and its classification to the output file
                    bw.write(token + "\t" + classification);                       
                    //bw.write(token);
                	bw.newLine();
                }
            }
            System.out.println("Tokens with classifications written to " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
    	
    	String inputFilename = "input.txt";
        String tokensFile = "tokens.txt";
        String symbolTable = "symbolTable.txt";

        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.writeTokensFile(inputFilename, tokensFile);
        
    }
}