import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowWindow);
    }

    private static void createAndShowWindow() {
        JFrame frame = new JFrame("Pattern Graphics");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        PatternPanel.initializeGameOfLife(400, 100, "B3/S234");
        frame.add(new PatternPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
