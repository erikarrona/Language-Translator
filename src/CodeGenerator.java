import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {

	public static void generateCode(String quadFile, String symbolFile, String outputFile) {
        List<String> quads = readQuadsFromFile(quadFile);
        List<String> symbols = readSymbolsFromFile(symbolFile);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Write .data segment
            writer.write("section .data\n");
            for (String symbol : symbols) {
                String[] parts = symbol.split("\\s+");
                String symbolName = parts[0];
                String value = parts[2];
                if (!value.equals("?")) {
                    writer.write(symbolName + " dw " + value + "\n");
                } else {
                	writer.write(symbolName + " dw " + " 0 \n");
                }
            }

            // Write .code segment
            writer.write("\nsection .text\n");
            writer.write("global _start\n");
            writer.write("_start:\n");
            for (String quad : quads) {
                writeAssembly(writer, quad);
            }

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
                writer.write("mov ax, [" + arg2 + "]\n");
                writer.write("mov [" + arg1 + "], ax\n");
                break;
            case "+":
                writer.write("mov ax, [" + arg1 + "]\n");
                writer.write("add ax, [" + arg2 + "]\n");
                writer.write("mov [" + result + "], ax\n");
                break;
            case "-":
                writer.write("mov ax, [" + arg1 + "]\n");
                writer.write("sub ax, [" + arg2 + "]\n");
                writer.write("mov [" + result + "], ax\n");
                break;
            case "*":
                writer.write("mov ax, [" + arg1 + "]\n");
                writer.write("imul ax, [" + arg2 + "]\n");
                writer.write("mov [" + result + "], ax\n");
                break;
            case "/":
                writer.write("mov ax, [" + arg1 + "]\n");
                writer.write("cwd\n");
                writer.write("idiv word [" + arg2 + "]\n");
                writer.write("mov [" + result + "], ax\n");
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
