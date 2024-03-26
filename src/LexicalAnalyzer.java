import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LexicalAnalyzer {
    private int stateTable[][];

    public LexicalAnalyzer() {
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
        stateTable[3][0] = 4;  // Letter
        stateTable[3][1] = 3;  // Digit
        stateTable[3][2] = 4;  // "*"
        stateTable[3][3] = 4;  // "/"
        stateTable[3][4] = 4; // "="
        stateTable[3][5] = 4; // "<"
        stateTable[3][6] = 0;  // Whitespace
        stateTable[3][7] = 1;  // Other
        stateTable[3][8] = 4; // "{"
        stateTable[3][9] = 4; // "}"
        stateTable[3][10] = 4; // "("
        stateTable[3][11] = 4; // ")"
        stateTable[3][12] = 4; // ","
        stateTable[3][13] = 4; // ";"
        stateTable[3][14] = 4; // "+"
        stateTable[3][15] = 4; // "-"
        stateTable[3][16] = 4; // ">"

        // Row 4: Integer state
        stateTable[4][0] = 0;  // Letter
        stateTable[4][1] = 0;  // Digit
        stateTable[4][2] = 0;  // "*"
        stateTable[4][3] = 0;  // "/"
        stateTable[4][4] = 0;  // "="
        stateTable[4][5] = 0;  // "<"
        stateTable[4][6] = 0;  // Whitespace
        stateTable[4][7] = 1;  // Other
        stateTable[4][8] = 0;  // "{"
        stateTable[4][9] = 0;  // "}"
        stateTable[4][10] = 0; // "("
        stateTable[4][11] = 0; // ")"
        stateTable[4][12] = 0; // ","
        stateTable[4][13] = 0; // ";"
        stateTable[4][14] = 0; // "+"
        stateTable[4][15] = 0; // "-"
        stateTable[4][16] = 0; // ">"


        // Rows 5 Variable state transition
        stateTable[5][0] = 5;  // Letter
        stateTable[5][1] = 5;  // Digit
        stateTable[5][2] = 6;  // "*"
        stateTable[5][3] = 6;  // "/"
        stateTable[5][4] = 6; // "="
        stateTable[5][5] = 6; // "<"
        stateTable[5][6] = 0;  // Whitespace
        stateTable[5][7] = 1;  // Other
        stateTable[5][8] = 6; // "{"
        stateTable[5][9] = 6; // "}"
        stateTable[5][10] = 6; // "("
        stateTable[5][11] = 6; // ")"
        stateTable[5][12] = 6; // ","
        stateTable[5][13] = 6; // ";"
        stateTable[5][14] = 6; // "+"
        stateTable[5][15] = 6; // "-"
        stateTable[5][16] = 6; // ">"

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
        
        // Row 7: Transition
        stateTable[7][0] = 10;  // Letter
        stateTable[7][1] = 10;  // Digit
        stateTable[7][2] = 8;  // "*"
        stateTable[7][3] = 10;  // "/"
        stateTable[7][4] = 10; // "="
        stateTable[7][5] = 10; // "<"
        stateTable[7][6] = 0;  // Whitespace
        stateTable[7][7] = 1;  // Other
        stateTable[7][8] = 10; // "{"
        stateTable[7][9] = 10; // "}"
        stateTable[7][10] = 10; // "("
        stateTable[7][11] = 10; // ")"
        stateTable[7][12] = 10; // ","
        stateTable[7][13] = 10; // ";"
        stateTable[7][14] = 10; // "+"
        stateTable[7][15] = 10; // "-"
        stateTable[7][16] = 10; // ">"
        
        // Row 8:
        stateTable[8][0] = 8;  // Letter
        stateTable[8][1] = 8;  // Digit
        stateTable[8][2] = 9;  // "*"
        stateTable[8][3] = 8;  // "/"
        stateTable[8][4] = 8; // "="
        stateTable[8][5] = 8; // "<"
        stateTable[8][6] = 0;  // Whitespace
        stateTable[8][7] = 1;  // Other
        stateTable[8][8] = 8; // "{"
        stateTable[8][9] = 8; // "}"
        stateTable[8][10] = 8; // "("
        stateTable[8][11] = 8; // ")"
        stateTable[8][12] = 8; // ","
        stateTable[8][13] = 8; // ";"
        stateTable[8][14] = 8; // "+"
        stateTable[8][15] = 8; // "-"
        stateTable[8][16] = 8; // ">"
        
        // Row 9: Transition for state 9
        for (int col = 0; col < 17; col++) {
            if (col == 2) {
                stateTable[9][col] = 0; // Transition to state 0 on "*" input
            } else if(col == 6) {
            	stateTable[9][col] = 0;
            } else {
                stateTable[9][col] = 8; // Transition to state 8 for all other inputs
            }
        }
        
        // Row 10: Transition for "/" operator
        for (int col = 0; col < 17; col++) {
            stateTable[10][col] = 10; // Stay in state 10 for all inputs
        }
        
        // Row 11: Transition for state 11
        stateTable[11][0] = 12;  // Letter
        stateTable[11][1] = 12;  // Digit
        stateTable[11][2] = 12;   // "*"
        stateTable[11][3] = 12;   // "/"
        stateTable[11][4] = 13;  // "="
        stateTable[11][5] = 12;  // "<"
        stateTable[11][6] = 0;  // Whitespace
        stateTable[11][7] = 1;  // Other
        stateTable[11][8] = 12;  // "{"
        stateTable[11][9] = 12;  // "}"
        stateTable[11][10] = 12;  // "("
        stateTable[11][11] = 12;  // ")"
        stateTable[11][12] = 12;  // ","
        stateTable[11][13] = 12;  // ";"
        stateTable[11][14] = 12;  // "+"
        stateTable[11][15] = 12;  // "-"
        stateTable[11][16] = 12;  // ">"
        
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

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
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
    	
    	if (token.matches("[a-zA-Z][a-zA-Z0-9_]*")){
    		if(token.toLowerCase().matches("class")) {
    			return "$Class";
    		}
    		int index = tokens.indexOf(token); // Get the index of the current token
    		System.out.println(token + "'s Index: " + index);
            if (index > 0 && tokens.get(index - 1).toLowerCase().equals("class")) {
                return "$Program Name"; 
            } else if(index < tokens.size() - 2 && tokens.get(index + 1).equals("=") && isInteger(tokens.get(index + 2))){
            	return "ConstVar";// Return the classification for ConstVar
            } else {
                return "Var"; // Return the classification for VAR
            }
    	}
    	if(token.matches("[0-9]*")) {
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



    public static void main(String[] args) {
        String inputFilename = "input.txt"; // Replace "input.txt" with the path to your input file
        String outputFilename = "tokens.txt"; // Output file name
        
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilename));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilename))) {
            LexicalAnalyzer analyzer = new LexicalAnalyzer(); // Create an instance of your lexical analyzer
            String line;
            while ((line = br.readLine()) != null) {
                // Process each line using the lexical analyzer
                List<String> tokens = analyzer.processTokens(line);
                // Output the tokens for this line to the file
                for (String token : tokens) {
                    // Classify the token
                    String classification = analyzer.classifyToken(token, tokens);
                    // Write the token and its classification to the output file
                    bw.write(token + "\t" + classification);
                    bw.newLine();
                }
            }
            System.out.println("Tokens with classifications written to " + outputFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
