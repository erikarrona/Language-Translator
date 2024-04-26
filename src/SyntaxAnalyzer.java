import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SyntaxAnalyzer {
    private List<String> tokens;
    private Stack<String> operandStack;
    private int tempCount;
    private List<String> tempPool;
    private BufferedWriter writer;
    private int labelCount = 1;
    private Stack<Integer> startWhileStack = new Stack<>();
    private Stack<Integer> endFixUpStack = new Stack<>();
    
    public SyntaxAnalyzer(List<String> tokens, String outputFile) {
        this.tokens = tokens;
        this.operandStack = new Stack<>();
        this.tempCount = 1;
        this.tempPool = new ArrayList<>();
        try {
            this.writer = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void analyze() {
        try {
            parseProgram();
            System.out.println("Syntax analysis successful.");
            closeWriter();
        } catch (Exception e) {
            System.out.println("Syntax error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getTempVariable() {
        if (!tempPool.isEmpty()) {
            return tempPool.remove(0); 
        } else {
            return "T" + tempCount++; 
        }
    }

    private void recycleTempVariable(String temp) {
        tempPool.add(temp); 
    }

    private void writeQuad(String quad) {
        try {
            writer.write(quad + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing quad: " + quad);
        }
    }

    private void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseProgram() {
        expect("CLASS");
        expectIdent();
        expect("{");
        parseBlock();
        expect("}");
        expectEndOfFile();
    }

    private void parseBlock() {
        while (!peek().equals("}")) {
            String token = peek();
            if (token.equals("CONST")) {
                parseConstDefPart();
            } else if (token.equals("VAR")) {
                parseVariableDefPart();
            } else if (token.equals("PROCEDURE")) {
                parseProcedureDefPart();
            } else {
                parseStmt();
                if (peek().equals(";")) {
                    expect(";");
                }
            }
        }
    }

    private void parseConstDefPart() {
        if (peek().equals("CONST")) {
            expect("CONST");
            parseConstList();
            expect(";");
        }
    }

    private void parseConstList() {
        expectIdent();
        expect("=");
        expectInteger();
        while (peek().equals(",")) {
            expect(",");
            expectIdent();
            expect("=");
            expectInteger();
        }
    }

    private void parseVariableDefPart() {
        if (peek().equals("VAR")) {
            expect("VAR");
            parseVarList();
            expect(";");
        }
    }

    private void parseVarList() {
        expectIdent();
        while (peek().equals(",")) {
            expect(",");
            expectIdent();
        }
    }

    private void parseProcedureDefPart() {
        if (peek().equals("PROCEDURE")) {
            expect("PROCEDURE");
            String procedureName = getNextToken(); // Get the name of the procedure
            if (peek().equals("(")) {
                expect("(");
                expect(")");
            }
            if (peek().equals("{")) {
                expect("{");
                String quad = "PROCEDURE, " + procedureName + ", N, N"; // Generate quad for the procedure definition
                writeQuad(quad);
                parseBlock();
                expect("}");
            }
        }
    }

    private void parseStmt() {
        while (!tokens.isEmpty()) {
            String token = tokens.get(0);
            if (token.equals("}")) {
                break;
            } else if (isIdent(token)) {
                parseSimpleStmt();
            } else if (token.equals("CALL")) {
                parseCallStmt();
            } else if (token.equals("{")) {
                parseCompoundStmt();
            } else if (token.equals("IF")) {
                parseIfStmt();
            } else if (token.equals("WHILE")) {
                parseWhileStmt();
            } else if (token.equals(";")) {
                expect(";");
                break;
            } else {
                throw new RuntimeException("Unexpected token: " + token);
            }
        }
    }

    private void parseSimpleStmt() {
        String identifier = getNextToken();
        expect("=");
        parseExp();
        String result = operandStack.pop();
        String quad = "=, " + identifier + ", " + result + ", " + "N";
        writeQuad(quad);
    }

    private void parseCallStmt() {
        expect("CALL");
        String procedureName = getNextToken(); // Get the name of the procedure being called
        String quad = "CALL, " + procedureName + ", N, N"; // Generate quad for the procedure call
        writeQuad(quad);
        if (peek().equals("{")) {
            parseBlock(); // If the procedure has a block of code, parse it
        }
    }


    private void parseCompoundStmt() {
        expect("{");
        while (!tokens.isEmpty() && !peek().equals("}")) {
            parseStmt();
            if (!tokens.isEmpty() && peek().equals(";")) {
                expect(";");
            }
        }
        if (!tokens.isEmpty()) {
            expect("}");
        }
    }

    private void parseIfStmt() {
    	int label = getLabel();
    	String quad;
        expect("IF");
        int L1 = label;
        int E1 = label;
        quad = "IF, L" + L1 + ", ?, ?";
        writeQuad(quad);
        parseBE(L1, E1);
        expect("THEN");
        quad = "THEN, L" + L1 + ", E" + E1 + ", ?";
        writeQuad(quad);
        parseStmt();
        quad = "L" + L1 + ", ?, ?, ?";
        writeQuad(quad);
        if (peek().equals("ELSE")) {
        	expect("ELSE");
        	quad = "ELSE, ?, ?, ?";
        	writeQuad(quad);
        }
    }

    private int getLabel() {
        return labelCount++;
    }
    
    private void parseWhileStmt() {
    	int label = getLabel();
        int W1 = label;
        int L1 = label;
        int fixUpAddress = label;

        startWhileStack.push(W1);

        expect("WHILE");

        String quad = "WHILE, W" + W1 + ", ?, ?";
        writeQuad(quad);

        parseBE(W1, L1);

        expect("DO");

        quad = "L" + L1 + ", ?, ?, ?";
        writeQuad(quad);

        parseStmt();

        quad = "JUMP, W" + W1 + ", ?, ?";
        writeQuad(quad);

        endFixUpStack.push(fixUpAddress);
        quad = "L" + fixUpAddress + ", ?, ?, ?";
        writeQuad(quad);
        endFixUpStack.pop();

        startWhileStack.pop();
    }

    
    private void parseBE(int L1, int LE) {
        String temp = getTempVariable(); // Obtain a new temporary variable
        if (peek().equals("ODD")) {
            expect("ODD");
            parseExp();
            String operand1 = operandStack.pop();
            String quad = "ODD, " + operand1 + ", " + temp + ", N";
            writeQuad(quad);
        } else {
            parseExp();
            String operand2 = operandStack.pop();
            String operator = getNextToken();
            parseExp();
            String operand1 = operandStack.pop();
            String quad = operator + ", " + operand1 + ", " + operand2 + ", " + temp;
            writeQuad(quad);
        }
    }

    private void parseExp() {
        parseTerm();
        while (isAddop(peek())) {
            String operator = getNextToken();
            parseTerm();
            String operand2 = operandStack.pop();
            String operand1 = operandStack.pop();
            String temp = getTempVariable(); // Obtain a new temporary variable
            operandStack.push(temp);
            String quad = operator + ", " + operand1 + ", " + operand2 + ", " + temp;
            writeQuad(quad);
        }
    }

    private void parseTerm() {
        parseFac();
        while (isMop(peek())) {
            String operator = getNextToken();
            parseFac();
            String operand2 = operandStack.pop();
            String operand1 = operandStack.pop();
            String temp = getTempVariable(); // Obtain a new temporary variable
            operandStack.push(temp);
            String quad = operator + ", " + operand1 + ", " + operand2 + ", " + temp;
            writeQuad(quad);
            recycleTempVariable(temp); // Release the temporary variable
        }
    }

    private void parseFac() {
        if (isIdent(peek()) || isInteger(peek())) {
            operandStack.push(getNextToken());
        } else if (peek().equals("(")) {
            expect("(");
            parseExp();
            expect(")");
        } else {
            throw new RuntimeException("Expected identifier, integer, or '(' but found '" + peek() + "'");
        }
    }

    private void expectRelop() {
        String token = getNextToken();
        if (!(token.equals("==") || token.equals("!=") || token.equals(">") ||
                token.equals("<") || token.equals(">=") || token.equals("<="))) {
            throw new RuntimeException("Expected relational operator but found '" + token + "'");
        }
    }

    private boolean isAddop(String token) {
        return token.equals("+") || token.equals("-");
    }

    private boolean isMop(String token) {
        return token.equals("*") || token.equals("/");
    }

    private boolean isInteger(String token) {
        return token.matches("\\d+"); // Matches one or more digits
    }

    private String peek() {
        if (!tokens.isEmpty()) {
            return tokens.get(0);
        } else {
            throw new RuntimeException("Unexpected end of input: no more tokens available");
        }
    }

    private String getNextToken() {
        String token = peek();
        tokens.remove(0);
        return token;
    }

    private void expect(String expectedToken) {
        String token = getNextToken();
        if (!token.equals(expectedToken)) {
            throw new RuntimeException("Expected '" + expectedToken + "' but found '" + token + "'");
        }
    }

    private void expectInteger() {
        String token = getNextToken();
        if (!isInteger(token)) {
            throw new RuntimeException("Expected an integer but found '" + token + "'");
        }
    }

    private void expectIdent() {
        String token = getNextToken();
        if (!isIdent(token)) {
            throw new RuntimeException("Expected identifier but found '" + token + "'");
        }
    }

    private boolean isIdent(String token) {
        return token.matches("[a-zA-Z][a-zA-Z0-9_()]*") && !isKeyword(token);
    }

    private boolean isKeyword(String token) {
        return token.equals("IF") || token.equals("WHILE") || token.equals("DO") || token.equals("CALL");
    }

    private void expectEndOfFile() {
        if (!tokens.isEmpty()) {
            throw new RuntimeException("Unexpected tokens at end of input");
        }
    }
}