import java.io.*;
import java.util.*;

public class LexicalAnalyzer {
    private static final int[][] STATE_TABLE = new int[30][18];
    private Map<String, Integer> tokenStateMap = new HashMap<>();
    private Map<String, String> classifiedTokens = new HashMap<>();
    private static int codeAddress = 0, dataAddress = 0;

    static {
        initializeStateTable();
    }

    private static void initializeStateTable() {
    	// Row 0: Transition from state 0
        STATE_TABLE[0][0] = 5;  // Letter
        STATE_TABLE[0][1] = 3;  // Digit
        STATE_TABLE[0][2] = 2;  // "*"
        STATE_TABLE[0][3] = 7;  // "/"
        STATE_TABLE[0][4] = 11; // "="
        STATE_TABLE[0][5] = 14; // "<"
        STATE_TABLE[0][6] = 0;  // Whitespace
        STATE_TABLE[0][7] = 1;  // Other
        STATE_TABLE[0][8] = 17; // "{"
        STATE_TABLE[0][9] = 18; // "}"
        STATE_TABLE[0][10] = 19; // "("
        STATE_TABLE[0][11] = 20; // ")"
        STATE_TABLE[0][12] = 21; // ","
        STATE_TABLE[0][13] = 22; // ";"
        STATE_TABLE[0][14] = 23; // "+"
        STATE_TABLE[0][15] = 24; // "-"
        STATE_TABLE[0][16] = 25; // ">"
        STATE_TABLE[0][17] = 28; // "!"

        // Rows 1: Error state
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[1][col] = 1; // Stay in error state for all inputs
        }

        // Row 2: $mop "*"
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[2][col] = 2; // Stay in $mop state for all inputs
        }

        // Row 3: Transition for Digits
        for (int col = 0; col < 18; col++) {
            if (col == 1) {
                STATE_TABLE[3][col] = 3;
            } else {
                STATE_TABLE[3][col] = 4; 
            }
        }

        // Row 4: Integer state
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[4][col] = 4; // Integer state remains in itself for all inputs
        }



        // Rows 5 Variable state transition
        for(int col = 0; col < 18; col++) {
        	if(col == 0 || col == 1 ) {
        		STATE_TABLE[5][col] = 5;
        	}else {
        		STATE_TABLE[5][col] = 6;
        	}
        }

        // Row 6: Variable state
        for(int col = 0; col < 18; col++) {
        	STATE_TABLE[6][col] = 6;
        	
        }
        
        // Row 7: Transition for "/"
        for(int col = 0; col < 18; col ++) {
        	if(col == 2) {	// c is a *
        		STATE_TABLE[7][col] = 8;
        	} else {
        		STATE_TABLE[7][col] = 10;
        	}
        }
        
        // Row 8: elimnitate comments /* o o o */
        for(int col = 0; col < 18; col ++) {
        	if(col == 2) {
        		STATE_TABLE[8][col] = 9;	// goes to state 9 for '*'
        	} else {
        		STATE_TABLE[8][col] = 8; 	// stay in 8 for all other inputs
        	}
        }
        
        // Row 9: read comment
        for (int col = 0; col < 18; col++) {
            if (col == 3) {
                STATE_TABLE[9][col] = 9; // Transition to state 0 on "/" input and finish comment
            } else {
                STATE_TABLE[9][col] = 8; // Transition to state 8 for all other inputs
            }
        }
        
        // Row 10: Transition for "/" operator
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[10][col] = 10; // Stay in state 10 for all inputs
        }
        
        // Row 11: Transition for state 11
        for(int col = 0; col < 18; col++) {
            if(col == 4) {
                STATE_TABLE[11][col] = 13;  // encounters another assignment operator, goes to relop
            } else if (col == 7) {
                STATE_TABLE[11][col] = 1; // Error state
            } else {
                STATE_TABLE[11][col] = 12; // Transition to state 12 for all other inputs
            }
        }

        
        // Row 12: "=" $assignment
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[12][col] = 12;
        }
        // Row 13: "==" $relop
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[13][col] = 13; // Stay in state 13 for all inputs
        }
        
        // Row 14: Transition for state 14
        for (int col = 0; col < 18; col++) {
            if (col == 4) {
                STATE_TABLE[14][col] = 16; // Transition to state 16 on "<" input
            } else {
                STATE_TABLE[14][col] = 15; // Transition to state 15 for all other inputs
            }
        }
        
        // Row 15: "<" $relop
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[15][col] = 15; // Stay in state 15 for all inputs
        }

        // Row 16: "<=" $relop
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[16][col] = 16; // Stay in state 16 for all inputs
        }

        // Row 17: "{" $LB
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[17][col] = 17; // Stay in state 17 for all inputs
        }

        // Row 18: "}" $RB
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[18][col] = 18; // Stay in state 18 for all inputs
        }

        // Row 19: "(" $LP
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[19][col] = 19; // Stay in state 19 for all inputs
        }

        // Row 20: ")" $RP
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[20][col] = 20; // Stay in state 20 for all inputs
        }
        
        // Row 21: "," $comma
        for (int col = 0; col < 18; col++) {
        	STATE_TABLE[21][col] = 21;
        }

        // Row 22: ";" $semicolon
        for (int col = 0; col < 18; col++) {
        	STATE_TABLE[22][col] = 22;
        }


        // Row 23: "+" $addop
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[23][col] = 23; // Stay in state 23 for all inputs
        }

        // Row 24: "-" $addop
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[24][col] = 24; // Stay in state 24 for all inputs
        }
        // Row 25 transition
        for (int col = 0; col < 18; col++) {
            if (col == 4) {
                STATE_TABLE[25][col] = 27; // Transition to state 27 on "=" input
            } else {
                STATE_TABLE[25][col] = 26; // Transition to state 26 for all other inputs
            }
        }
        
        // Row 26: ">" $relop
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[26][col] = 26; // Stay in state 26 for all inputs
        }

        // Row 27: ">=" $relop
        for (int col = 0; col < 18; col++) {
            STATE_TABLE[27][col] = 27; // Stay in state 27 for all inputs
        }
        
        // row 28 "!"
        for (int col = 0; col < 18; col ++) {
        	if(col == 4) {
        		STATE_TABLE[28][col] = 29;
        	} else {
        		STATE_TABLE[28][col] = 1;
        	}
        }

        // row 29 "!=" <relop>
        for (int col = 0; col < 18; col ++) {
        	
    		STATE_TABLE[29][col] = 29;
        	
        }
    }

    private int getColumn(char c) {
        if (Character.isLetter(c)) {
            return 0; // Letter
        } else if (Character.isDigit(c)) {
            return 1; // Digit
        } else if (c == '*') {
            return 2; // "*"
        } else if (c == '/') {
            return 3; // "/"
        } else if (c == '=') {
            return 4; // "="
        } else if (c == '<') {
            return 5; // "<"
        } else if (Character.isWhitespace(c)) {
            return 6; // Whitespace
        } else if (c == '{') {
            return 8; // "{"
        } else if (c == '}') {
            return 9; // "}"
        } else if (c == '(') {
            return 10; // "("
        } else if (c == ')') {
            return 11; // ")"
        } else if (c == ',') {
            return 12; // ","
        } else if (c == ';') {
            return 13; // ";"
        } else if (c == '+') {
            return 14; // "+"
        } else if (c == '-') {
            return 15; // "-"
        } else if (c == '>') {
            return 16; // ">"
        } else if (c == '!') {
            return 17; // "!"
        } else {
            return 7; // Other
        }
    }

    private List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        int currentState = 0, nextState = 0;

        for (char c : line.toCharArray()) {
            int column = getColumn(c);
            currentState = nextState;
            nextState = STATE_TABLE[currentState][column]; // Transition to next state

            // If the column is whitespace and not transitioning to an error state
            if (column == 6 && currentState != 1 && nextState != 1) {
                if (currentState != 8 && currentState != 9) {
                    if (currentToken.length() > 0) {
                        tokens.add(currentToken.toString());
                        tokenStateMap.put(currentToken.toString(), nextState); // Map token to currentState
                        currentToken.setLength(0);
                        currentState = 0;
                        nextState = 0;
                    }
                }
            } else if (column == 12 || column == 13 || column == 8 || column == 9) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    tokenStateMap.put(currentToken.toString(), nextState); // Map token to currentState
                    currentToken.setLength(0);
                    currentState = 0;
                    nextState = 0;
                }
                // add commas and semicolons
                nextState = STATE_TABLE[currentState][column]; // Transition to next state
                tokenStateMap.put(String.valueOf(c), nextState); // Map token to currentState
                tokens.add(String.valueOf(c)); // Add comma or semicolon as a separate token
                currentState = 0;
                nextState = 0;
            } else {
                currentToken.append(c);
            }
        }

        // Check if there's a token remaining in the buffer
        if (currentToken.length() > 0 && (currentState != 8 && currentState != 9)) {
            tokens.add(currentToken.toString());
            tokenStateMap.put(currentToken.toString(), nextState);
        }

        return tokens;
    }


    private String classifyToken(String token) {
        Integer currentState = tokenStateMap.get(token);
        if (currentState == null) {
            return "Other"; // Default classification for unrecognized tokens
        }

        switch (currentState) {
            case 2:
            case 10:
                return "<mop>";
            case 3:
            case 4:
                return "<integer>";
            
            case 6:
                switch (token) {
                    case "CLASS":
                        return "$CLASS";
                    case "IF":
                        return "$IF";
                    case "CONST":
                        return "$CONST";
                    case "VAR":
                        return "$VAR";
                    case "ELSE":
                        return "$ELSE";
                    case "CALL":
                        return "$CALL";
                    case "WHILE":
                        return "$WHILE";
                    case "DO":
                        return "$DO";
                    case "PROCEDURE":
                        return "$PROCEDURE";
                    case "ODD":
                    	return "$ODD";
                    default:
                        return "<var>";
                }
            case 12:
                return "<assign>";
            case 13:
            case 15:
            case 16:
            case 26:
            case 27:
            case 29:
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
            case 22:
                return "<semi>";
            case 23:
            case 24:
                return "<addop>";
            default:
                return "Other"; // Default classification for unrecognized tokens
        }
    }

    
    private void processLine(String line, BufferedWriter bw) throws IOException {
    	
        List<String> tokens = tokenize(line);
        for (String token : tokens) {
            String classification;
            // Check if the token has already been classified
            if (classifiedTokens.containsKey(token)) {
                classification = classifiedTokens.get(token);
            } else {
                // Classify the token and store its classification
                classification = classifyToken(token);
                classifiedTokens.put(token, classification);
            }
            bw.write(token + "\t" + classification);
            bw.newLine();
        }
    }

    public void analyze(String inputFile, String tokensFile) {
        try (BufferedReader inputReader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter tokenWriter = new BufferedWriter(new FileWriter(tokensFile))){
            String line;
            while ((line = inputReader.readLine()) != null) {
                processLine(line, tokenWriter);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static final int[][] SYMBOL_STATE_TABLE = {
    		//class | var | { | CONST | <int> | $assingment | $semi | $comma | VAR | <var>, etc, | ANY
        		{ 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},  // State 0
                {-1,  2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},  // State 1
                {-1, -1,  3, -1, -1, -1, -1, -1, -1, -1, -1, -1},  // State 2
                {-1, 10, -1,  4, -1, -1, -1, -1,  8, 10, -1, 12},  // State 3
                {-1,  5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},  // State 4
                {-1, -1, -1, -1, -1,  6, -1, -1, -1, -1, -1, -1},  // State 5
                {-1, -1, -1, -1,  7, -1, -1, -1, -1, -1, -1, -1},  // State 6
                {-1, -1, -1, -1, -1, -1,  3,  4, -1, -1, -1, -1},  // State 7
                {-1,  9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},  // State 8
                {-1, -1, -1, -1, -1, -1,  3,  8,  0, -1, -1, -1},  // State 9
                {-1, -1, -1, -1, 11, -1, -1, -1, -1, -1, 10, -1}, // State 10
                {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, -1}, // State 11
                {-1, 13, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, // State 12
                {-1, -1, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1} // State 13
                
        };
    
    public void analyzeTokens(String tokensFile, String symbolFile) {
    	try (
    		BufferedReader br = new BufferedReader(new FileReader(tokensFile)); 
			BufferedWriter bw = new BufferedWriter(new FileWriter(symbolFile))) {
    		
    		String line;
    		int col = 0, currentState = 0, nextState = 0;
    		String[] symbols = new String[100];
            String[] classifications = new String[100];
            String[] values = new String[100];
            int[] addresses = new int[100];
            String[] segments = new String[100];
            int symbolTableIndex = 0;
            String tempSymbol;

            String tokenClassification = " ";
            
    		while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String token = parts[0];
                String classification = parts[1];
                col = getClassificationCode(classification);
                currentState = symbolTransition(currentState, col);
                
                
                if(currentState == 2) {	//from state 1, case <var> is program name
                	symbols[symbolTableIndex] = token;
                	classifications[symbolTableIndex] = "$program name";
                	values[symbolTableIndex] = " ";
                	addresses[symbolTableIndex] = codeAddress;
                	segments[symbolTableIndex] = "CS";
                    symbolTableIndex++;
                    codeAddress += 2;
                	
                } if (currentState == 3) {
                	tokenClassification = " ";
                } else if(currentState == 4){	// from { , state 4 for CONST 
            		
                	if(tokenClassification == " ") {
                		tokenClassification = getSymbolClassification(token);
                	}
            		
                } else if (currentState == 5) {	// transition 5 <var> was found
                	symbols[symbolTableIndex] = token;
                	classifications[symbolTableIndex] = tokenClassification;
                	values[symbolTableIndex] = "?";
                	addresses[symbolTableIndex] = dataAddress;
                	segments[symbolTableIndex] = "DS";
                	dataAddress += 2;
                	
                } else if(currentState == 7) {	// <int> was found, moved to 7 and saves int as value
                	values[symbolTableIndex] = token;
                	symbolTableIndex++;	
                } else if(currentState == 8){	//VAR was found
                	if(tokenClassification == " ") {
                		tokenClassification = getSymbolClassification(token);
                	}
                } else if(currentState == 9){ // <var> was found
                	symbols[symbolTableIndex] = token;
                	classifications[symbolTableIndex] = tokenClassification;
                	values[symbolTableIndex] = "?";
                	addresses[symbolTableIndex] = dataAddress;
                	segments[symbolTableIndex] = "DS";
                	dataAddress += 2;
                	symbolTableIndex++;	
                } else if(currentState == 10) {
                	tokenClassification = " ";
                } else if(currentState == 11){
                	symbols[symbolTableIndex] = token;
                	classifications[symbolTableIndex] = "NumLit";
                	values[symbolTableIndex] = token;
                	addresses[symbolTableIndex] = dataAddress;
                	segments[symbolTableIndex] = "DS";
                	dataAddress += 2;
                	symbolTableIndex++;	
                	
                } else if(currentState == 13){
                	symbols[symbolTableIndex] = token;
                	classifications[symbolTableIndex] = "Procedure";
                	values[symbolTableIndex] = " ";
                	addresses[symbolTableIndex] = codeAddress;
                	segments[symbolTableIndex] = "CS";
                	dataAddress += 2;
                	symbolTableIndex++;	
                } else {
                	continue;
                }
                
                
            }
    		bw.write(String.format("%-10s %-15s %-10s %-10s %-10s\n", "SYMBOL", "CLASSIFICATION", "VALUE", "ADDRESS", "SEGMENT"));
    		for (int i = 0; i < symbolTableIndex; i++) {
    		    bw.write(String.format("%-10s %-15s %-10s %-10s %-10s\n", symbols[i], classifications[i], values[i], addresses[i], segments[i]));
    		}
    		for (int j = 1; j < 4; j++) {
    		    bw.write(String.format("%-10s %-15s %-10s %-10s %-10s\n", "Temp" + j, "Var(Int)", "", dataAddress, "DS"));
    		    dataAddress += 2;
    		}

    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    private String getSymbolClassification(String token) {
		switch(token) {
			case "CONST":
				return "ConstVar";
			case "VAR":
				return "Variable";
		}
		return null;
	}

	private static int symbolTransition(int currentState, int classification) {
        int nextState = SYMBOL_STATE_TABLE[currentState][classification];
        if (nextState == -1) {
        	return currentState;
        }
        // Otherwise, return the next state
        return nextState;
    }

    private static boolean isReserved(String classification) {
        switch (classification) {
            case "IF":
            case "ELSE":
            case "CALL":
            case "WHILE":
            case "DO":
            case "ODD":
                return true;
            default:
                return false;
        }
    }
    
    private static int getClassificationCode(String classification) {
    	switch (classification) {
            case "$CLASS":
                return 0;
            case "<var>":
                return 1;
            case "$LB":
                return 2;
            case "$CONST":
                return 3;
            case "<integer>":
                return 4;
            case "<assign>":
                return 5;
            case "<semi>":
                return 6;
            case "<comma>":
                return 7;
            case "$VAR":
                return 8;
            case "$PROCEDURE":
            	return 11;
            default:
            	if (isReserved(classification)) {
                	return 9;
                } else if (classification.startsWith("<") || classification.startsWith("$")){
                	return 10;
                } else {
                	return -1;
                }
        }
    }
    
    public List<String> readTokens(String tokensFile) {
        List<String> tokens = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tokensFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Assuming each token is on a separate line in the tokens file
                tokens.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading tokens file: " + e.getMessage());
        }
        return tokens;
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java LexicalAnalyzer inputFilename");
            return;
        }

        String inputFilename = args[0];
        String tokensFile = "tokens.txt";
        String symbolFile = "symbol_table.txt";

        // Perform lexical analysis
        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.analyze(inputFilename, tokensFile);
        System.out.println("Tokens generated, outputted in " + tokensFile);

        // Read tokens
        List<String> tokens = analyzer.readTokens(tokensFile);

        // Perform syntax analysis
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokens);
        syntaxAnalyzer.analyze();
        System.out.println("Syntax analysis completed.");

        // Analyze tokens
        analyzer.analyzeTokens(tokensFile, symbolFile);
        System.out.println("Symbol Table analyzed, outputted in " + symbolFile);
    }

}
