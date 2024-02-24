import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class LexicalAnalyzer extends JPanel implements ActionListener {
    // GUI components
    JLabel prompt;
    JTextField input;
    JTextArea output;

    public void init() {
        prompt = new JLabel("Enter a sentence to parse:");
        input = new JTextField(50); // Can edit by default.
        input.addActionListener(this);
        output = new JTextArea(10, 30);
        output.setEditable(false); // Turn off default ability to edit.
        add(prompt);
        add(input);
        add(output);
    }

    private void scanner1(String str) {
        // Create default scanner using " \n\t\r" (space, newline, tab,
        // and a carriage return known as white space) as delimiters.
        // Default is the same as the following explicit declaration:
        // StringTokenizer tokens = new StringTokenizer( str," \n\t\r" );
        // Works for a + Bob -c but fails for a+Bob-c.
        // This scanner would work for COBOL as all operators must be
        // surrounded by one or more spaces. It fails for "C" where
        // things can be run together.
        StringTokenizer tokens = new StringTokenizer(str);
        output.append("Number of elements: " + tokens.countTokens() + "\nThe tokens are:\n");
        while (tokens.hasMoreTokens())
            output.append(tokens.nextToken() + "\n");
    }

    private void scanner2(String str) {
        // Create scanner using " \n\t\r+-*/{}=;" as delimiters.
        // Note we are including the operators. Fails to find operators
        // like "for," "if," and "else." It also still misses illegal
        // characters like $,%,and &. If the third parameter is true,
        // delimiters are returned as tokens. If false, delimiters
        // are not returned.
        // Works for a + Bob -c and a+Bob-c. Fails for "==" and other two
        // character tokens.
        StringTokenizer tokens = new StringTokenizer(str, " \n\t\r+-*/{};=", true);
        output.append("Number of elements: " + tokens.countTokens() + "\nThe tokens are:\n");
        while (tokens.hasMoreTokens())
            output.append(tokens.nextToken() + "\n");
    }

    public void actionPerformed(ActionEvent event) {
        String sentenceToScan = input.getText(); // Read text field.
        output.setText("");
        scanner1(sentenceToScan);
        output.append("\n\nTokens from scanner2:\n");
        scanner2(sentenceToScan);
    }
}
