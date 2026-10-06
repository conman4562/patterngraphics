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
        PatternPanel p = new PatternPanel();
        p.initializeGameOfLife(400, 100, "B12/S234");
        frame.add(p);
        frame.setUndecorated(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
