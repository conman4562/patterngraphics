import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatternPanel extends JPanel implements KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {
    private static final Logger logger = Logger.getLogger(PatternPanel.class.getName());

    private static final int WIDTH = 1600;
    private static final int HEIGHT = 400;
    private static final int SIDE_BAR_WIDTH = 200;
    private static final int msPerFrame = 6000 / 60;

    private static final Random r = new Random();

    private static ArrayList<Boolean> lifeArray; // 1 = alive, 0 = dead
    private static ArrayList<Boolean> forwardLifeArray;
    private static ArrayList<Integer> cellAge;
    private static ArrayList<Integer> forwardCellAge;
    private static Timer simTimer;
    private BufferedImage lifeImage;
    private static String ruleset; //ex: B3/S23 = default, B=birth at x, S=survive at x
    private static boolean[] birthArray;
    private static boolean[] survivalArray;
    private static int width;
    private static int height;
    private static boolean calculating;
    private static final ArrayList<CustomizableJButton<BUTTON_TYPE>> buttons = new ArrayList<>();
    private enum BUTTON_TYPE {
        BUTTON_1,
        BUTTON_2,
        BUTTON_3,
        BUTTON_4,
    }

    public PatternPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        buttons.add(new CustomizableJButton<>(100, 20, 70, 60, getWidth(), 10, 5, "Test", BUTTON_TYPE.BUTTON_1));
    }

    public void initializeGameOfLife(int width, int height, String ruleset) {
        simTimer = new Timer(msPerFrame, e -> {
            if (!calculating) {
                calculating = true;
                new BackgroundWorker().execute();
            }
        });
        lifeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        lifeArray = new ArrayList<Boolean>();
        forwardLifeArray = new ArrayList<Boolean>();
        forwardCellAge = new ArrayList<>();
        cellAge = new ArrayList<Integer>();
        birthArray = new boolean[9];
        survivalArray = new boolean[9];
        for (int i = 0; i < 8; i++) {
            birthArray[i] = false;
            survivalArray[i] = false;
        }
        PatternPanel.width = width;
        PatternPanel.height = height;
        PatternPanel.ruleset = ruleset;
        for (int i = 0; i < width * height; i++) {
            if (i / width > height/(10/4.0) && i % width > width/(10/4.0)
            && i / width < height/(10/6.0) && i % width < width/(10/6.0)) {
                lifeArray.add(r.nextBoolean());
            } else {
                lifeArray.add(r.nextBoolean());
            }
            forwardLifeArray.add(false);
            cellAge.add(-1);
            forwardCellAge.add(-1);
        }
        boolean birth = true;
        for (int i = 0; i < ruleset.length(); i++) {
            if (ruleset.charAt(i) == 'B' || ruleset.charAt(i) == '/') {
                continue;
            } else if (ruleset.charAt(i) == 'S') {
                birth = false;
                continue;
            } else {
                if (birth) {
                    birthArray[ruleset.charAt(i) - '0'] = true;
                } else {
                    survivalArray[ruleset.charAt(i) - '0'] = true;
                }
            }
        }
        simTimer.start();
    }

    private boolean getLifeSquareOffset(int row, int col, int rowOffset, int colOffset) {
        int newcol = col;
        if (colOffset == -1) {
            if (col == 0) {
                return false;
            }
            newcol--;
        }
        if (colOffset == 1) {
            if (col == width - 1) {
                return false;
            }
            newcol++;
        }
        int newrow = row;
        if (rowOffset == -1) {
            if (row == 0) {
                return false;
            }
            newrow--;
        }
        if (rowOffset == 1) {
            if (row == height - 1) {
                return false;
            }
            newrow++;
        }
        return getLifeSquare(newrow, newcol);
    }

    private boolean getLifeSquare(int row, int col) {
        int square = row * width + col;
        if (square < 0 || square >= width * height) {
            return false;
        }
        return lifeArray.get(square);
    }

    private void setLifeSquare(int row, int col, boolean state) {
        int square = row * width + col;
        forwardLifeArray.set(square, state);
    }

    private void incrementSquareAge(int row, int col) {
        int square = row * width + col;
        if (forwardCellAge.get(square) == -1) {
            return;
        }
        forwardCellAge.set(square, forwardCellAge.get(square) + 1);
    }

    private void resetSquareAge(int row, int col) {
        int square = row * width + col;
        forwardCellAge.set(square, 0);
    }

    private Color getColorFromAge(int age, int maxAge, boolean alive) {
        if (age == -1) {
            return Color.BLACK;
        }
        if (age == 0 && alive) {
            return Color.WHITE;
        }
        float t = age / (float) maxAge;
        if (alive) {
            float hue = t * 0.8f;
            return Color.getHSBColor(hue, 1f, 1f);

        } else {
            float value = 0.4f - t * 0.3f;
            return Color.getHSBColor(0, 0.0f, value);
        }
    }

    private void iterateGameOfLife() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int surrounding_count = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) {
                            continue;
                        }
                        if (getLifeSquareOffset(row, col, i, j)) {
                            surrounding_count++;
                        }
                    }
                }
                if (getLifeSquare(row, col)) {
                    if (survivalArray[surrounding_count]) {
                        // keep square alive if it meets survival reqs
                        incrementSquareAge(row, col);
                        setLifeSquare(row, col, true);
                    } else {
                        // kill square if it does not meet survival reqs
                        resetSquareAge(row, col);
                        setLifeSquare(row, col, false);
                    }
                } else {
                    if (birthArray[surrounding_count]) {
                        // birth square if it meets birth req
                        resetSquareAge(row, col);
                        setLifeSquare(row, col, true);
                    } else {
                        // do not birth square if it does not meet birth req
                        setLifeSquare(row, col, false);
                        incrementSquareAge(row, col);
                    }
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (lifeImage != null) {
            int square_width = getWidth() / width;
            int square_height = getHeight() / height;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(lifeImage, 0, 0, width * square_width, height * square_height, null);
        }

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Ariel", Font.PLAIN, 20));

        for (CustomizableJButton<BUTTON_TYPE> button : buttons) {
            button.drawButton(g, getWidth());
        }
    }

    private void buttonPressed(BUTTON_TYPE button) {
        switch (button) {
            case BUTTON_1:
                break;
            case BUTTON_2:
                break;
            case BUTTON_3:
                break;
            case BUTTON_4:
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_Q) {
            System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        for (CustomizableJButton<BUTTON_TYPE> button : buttons) {
            if (button.rrectArea.contains(e.getPoint())) {
                buttonPressed(button.buttonType);
            }
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for (CustomizableJButton<BUTTON_TYPE> button : buttons) {
            button.hovering = button.rrectArea.contains(e.getPoint());
        }
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }

    private class BackgroundWorker extends SwingWorker<BufferedImage, Void> {
        @Override
        protected BufferedImage doInBackground() {
            forwardCellAge = new ArrayList<>(cellAge);
            iterateGameOfLife();
            int maxAge = Collections.max(forwardCellAge);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int square = 0; square < forwardLifeArray.size(); square++) {
                image.setRGB(square % width, square / width,
                        getColorFromAge(forwardCellAge.get(square), maxAge, forwardLifeArray.get(square)).getRGB());
            }
            return image;
        }

        @Override
        protected void done() {
            try {
                lifeImage = get();
                ArrayList<Boolean> temp = lifeArray;
                lifeArray = forwardLifeArray;
                forwardLifeArray = temp;
                cellAge = forwardCellAge;
                repaint();
            } catch (ExecutionException e) {
                simTimer.stop();
                logger.log(Level.SEVERE, "Execution error in SwingWorker", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                simTimer.stop();
            } finally {
                calculating = false;
            }
        }
    }

}
