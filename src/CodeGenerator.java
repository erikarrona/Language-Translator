import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
	
	private static int ifCount = 1;
	private static int whileCount = 1;
	private static boolean ifStatement = false;

	public static void generateCode(String quadFile, String symbolFile, String outputFile) {
        List<String> quads = readQuadsFromFile(quadFile);
        List<String> symbols = readSymbolsFromFile(symbolFile);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Write .data segment
            writer.write(".data\n");
            for (String symbol : symbols) {
                String[] parts = symbol.split("\\s+");
                String symbolName = parts[0];
                String classification = parts[1];
                String value = parts[2];
                if (!value.equals("?") && classification.equals("ConstVar")) {
                	System.out.println(classification);
                    writer.write("\t" + symbolName + " dw " + value + "\n");
                } else if (!classification.equals("$program_name") && !classification.equals("Procedure")){
                	System.out.println(classification);
                	writer.write("\t" + symbolName + " dw " + " 0 \n");
                }
            }

            // Write .code segment
            writer.write("\nPgmStart:\n");
            for (String quad : quads) {
            	String[] parts = quad.split(",");
            	if(parts[0].trim() == "IF") {
            		
            	} else {
            		writeAssembly(writer, quad);
            	}
            }
            
            writer.write("fini: \n");
            writer.write("\tmov ah, 4ch\n");
            writer.write("\tint 21h\n");
            writer.write("END PGM");

            System.out.println("Assembly code generated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeAssembly(BufferedWriter writer, String quad) throws IOException {
    	String[] parts = quad.split(",");
    	
        String op = parts[0].trim();
        String arg1 = parts[1].trim();
        String arg2 = parts[2].trim();
        String result = parts[3].trim();

        switch (op) {
            case "=":
                writer.write("\tmov ax, [" + arg2 + "]\n");
                writer.write("\tmov [" + arg1 + "], ax\n");
                
                break;
            case "+":
                writer.write("\tmov ax, [" + arg1 + "]\n");
                writer.write("\tadd ax, [" + arg2 + "]\n");
                writer.write("\tmov [" + result + "], ax\n");
                break;
            case "-":
                writer.write("\tmov ax, [" + arg1 + "]\n");
                writer.write("\tsub ax, [" + arg2 + "]\n");
                writer.write("\tmov [" + result + "], ax\n");
                break;
            case "*":
                writer.write("\tmov ax, [" + arg1 + "]\n");
                writer.write("\timul ax, [" + arg2 + "]\n");
                writer.write("\tmov [" + result + "], ax\n");
                break;
            case "/":
                writer.write("\tmov ax, [" + arg1 + "]\n");
                writer.write("\tcwd\n");
                writer.write("\tidiv word [" + arg2 + "]\n");
                writer.write("\tmov [" + result + "], ax\n");
                break;
            case ">":
            case "<":
            case "<=":
            case ">=":
            case "!=":
            case "==":
                writer.write("\tmov ax, [" + arg2 + "]\n");
                writer.write("\tcmp ax, " + arg1 + "\n");
                break;
            case "THEN":
            	if(ifStatement == true) {
            		writer.write("\tjne " + arg1 + "\n");
            	} else {
            		writer.write("\tjle " + arg1 + "\n");
            		ifStatement = true;
            	}
            	break;
            case "ELSE":
            	writer.write("\tjmp E" + ifCount + "\n");
            	writer.write("L" + ifCount + "\tnop\n");
            	ifCount++;
            	ifStatement = false;
            	break;
            case "DO":
            	writer.write("\tjge L" + whileCount + "\n");
            	break;
            case "JUMP":
            	writer.write("\tjmp W" + whileCount + "\n");
            	writer.write("L" + whileCount + "\tnop\n");
            	whileCount++;
            	break;
            case "PROCEDURE":
            	writer.write("\n" + arg1 + ":\n");
            	break;
            case "CALL":
            	writer.write("\tcall " + arg1 + "\n");
            	break;
            case "WHILE":
            	writer.write(arg1);
            	break;
            default:
                System.err.println("Unsupported operation: " + op);
        }
    }
    
    
    private static List<String> readQuadsFromFile(String quadFile) {
        List<String> quads = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(quadFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                quads.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quads;
    }

    private static List<String> readSymbolsFromFile(String symbolFile) {
        List<String> symbols = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(symbolFile))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                symbols.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return symbols;
    }
}
