import java.util.List;

public class SyntaxAnalyzer {
    private List<String> tokens;

    public SyntaxAnalyzer(List<String> tokens) {
        this.tokens = tokens;
    }

    public void analyze() {
        try {
            parseProgram();
            System.out.println("Syntax analysis successful.");
        } catch (Exception e) {
            System.out.println("Syntax error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseProgram() {
    	System.out.println("START CLASS");
        expect("CLASS");
        expectIdent();
        expect("{");
        parseBlock();
        expect("}");
    	System.out.println("END CLASS");
        expectEndOfFile();
    }

    private void parseBlock() {
    	System.out.println("START BLOCK");
        while (!peek().equals("}")) {
            String token = peek();
            if (token.equals("CONST")) {
            	System.out.println("START CONST");
                parseConstDefPart();
            	System.out.println("END CONST");
            } else if (token.equals("VAR")) {
            	System.out.println("START VAR");
                parseVariableDefPart();
            	System.out.println("END VAR");
            } else if (token.equals("PROCEDURE")) {
            	System.out.println("START PROC");
                parseProcedureDefPart();
            	System.out.println("END PROC");
            } else {
                parseStmt();
                if (peek().equals(";")) {
                    expect(";");
                }
            }
        }
    	System.out.println("END BLOCK");
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
            expectIdent();
            if (peek().equals("(")) {
                expect("(");
                expect(")");
            }
            if (peek().equals("{")) {
                expect("{");
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
    	expectIdent();
        expect("=");
        parseExp();
    }

    private void parseCallStmt() {
        expect("CALL");
        expectIdent();
        
        if (peek().equals("{")) {
            parseBlock();
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
        expect("IF");
        parseBE();
        expect("THEN");
        parseStmt();
    }

    private void parseWhileStmt() {
        expect("WHILE");
        parseBE();
        expect("DO");
        parseStmt(); 
    }

    private void parseBE() {
        if (peek().equals("ODD")) {
            expect("ODD");
            parseExp();
        } else {
            parseExp();
            expectRelop();
            parseExp();
        }
    }

    private void parseExp() {
        parseTerm();
        while (isAddop(peek())) {
            expectAddop();
            parseTerm();
        }
    }

    private void parseTerm() {
        parseFac();
        while (isMop(peek())) {
            expectMop();
            parseFac();
        }
    }

    private void parseFac() {
        if (isIdent(peek())) {
            expectIdent();
        } else if (isInteger(peek())) {
            expectInteger();
        } else if (peek().equals("(")) {
            expect("(");
            parseExp();
            expect(")");
        } else {
            throw new RuntimeException("Expected identifier, integer, or '(' but found '" + peek() + "'");
        }
    }

    private void expectRelop() {
        String token = peek();
        if (token.equals("==") || token.equals("!=") || token.equals(">") ||
                token.equals("<") || token.equals(">=") || token.equals("<=")) {
            getNextToken();
        } else {
            throw new RuntimeException("Expected relational operator but found '" + token + "'");
        }
    }

    private void expectAddop() {
        String token = peek();
        if (token.equals("+") || token.equals("-")) {
            getNextToken();
        } else {
            throw new RuntimeException("Expected '+' or '-' but found '" + token + "'");
        }
    }

    private void expectMop() {
        String token = peek();
        if (token.equals("*") || token.equals("/")) {
            getNextToken();
        } else {
            throw new RuntimeException("Expected '*' or '/' but found '" + token + "'");
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
        	return null;
        }
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

    private String getNextToken() {
        String token = peek();
        if (token != null) {
            tokens.remove(0);
            return token;
        } else {
            throw new RuntimeException("Unexpected end of input: no more tokens available");
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
