import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class PatternPanel extends JPanel {
    private static final int WIDTH = 1600;
    private static final int HEIGHT = 400;
    private static final int msPerFrame = 2000 / 60;

    private static final Random r = new Random();

    private static ArrayList<Boolean> lifeArray; // 1 = alive, 0 = dead
    private static ArrayList<Boolean> forwardLifeArray;
    private static ArrayList<Integer> cellAge;
    private static int maxAge;
    private static String ruleset; //ex: B3/S23 = default, B=birth at x, S=survive at x
    private static boolean[] birthArray;
    private static boolean[] survivalArray;
    private static int width;
    private static int height;
    private static long lastFrameTime = System.currentTimeMillis();

    public PatternPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
    }

    public static void initializeGameOfLife(int width, int height, String ruleset) {
        lifeArray = new ArrayList<Boolean>();
        forwardLifeArray = new ArrayList<Boolean>();
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
            lifeArray.add(r.nextBoolean());
            forwardLifeArray.add(false);
            cellAge.add(-1);
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

    private int getSquareAge(int row, int col) {
        int square = row * width + col;
        return cellAge.get(square);
    }

    private void incrementSquareAge(int row, int col) {
        int square = row * width + col;
        if (cellAge.get(square) == -1) {
            return;
        }
        cellAge.set(square, cellAge.get(square) + 1);
    }

    private void resetSquareAge(int row, int col) {
        int square = row * width + col;
        cellAge.set(square, 0);
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

    private void getMaxAge() {
        maxAge = -1;
        for (Integer integer : cellAge) {
            if (integer > maxAge) {
                maxAge = integer;
            }
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
        ArrayList<Boolean> tmp = lifeArray;
        lifeArray = forwardLifeArray;
        forwardLifeArray = tmp;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int square_width = getWidth() / width;
        int square_height = getHeight() / height;
        getMaxAge();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                g2.setColor(getColorFromAge(getSquareAge(row, col), maxAge, getLifeSquare(row, col)));
                g2.fillRect(col * square_width, row * square_height, square_width, square_height);
            }
        }
        iterateGameOfLife();

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Ariel", Font.PLAIN, 20));
        g2.drawString("" + lastFrameTime, 500, 500);

        // sleep until next frame for constant fps
        long cur_time = System.currentTimeMillis();
        long timeElapsed = cur_time - lastFrameTime;
        lastFrameTime = cur_time;
        try {
            Thread.sleep(Math.max(0, msPerFrame - timeElapsed));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        repaint();
    }
}
