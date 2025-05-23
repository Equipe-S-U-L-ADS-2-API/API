package GUI;

import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TextAreaOutputStream extends OutputStream {
    private final JTextArea textArea;
    private final StringBuilder buffer = new StringBuilder();
    
    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }
    
    @Override
    public void write(int b) {
        buffer.append((char) b);
        if (b == '\n') {
            flush();
        }
    }
    
    @Override
    public void flush() {
        final String text = buffer.toString();
        buffer.setLength(0);
        
        SwingUtilities.invokeLater(() -> {
            textArea.append(text);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}