import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class CustomizableJButton<BUTTON_TYPE> {
    int x;
    int xOffset;
    int y;
    int width;
    int height;
    int font;
    Font textFont;
    int fillet;
    String message;
    boolean hovering;
    Area rrectArea;
    Color normalColor = new Color(255, 255, 255);
    Color hoverColor = new Color(200, 200, 200);
    BUTTON_TYPE buttonType;
    BasicStroke fontSize;

    public CustomizableJButton(int xOffset, int y, int width, int height, int panelWidth, int font, int fillet, String message, BUTTON_TYPE buttonType) {
        super();
        this.xOffset = xOffset;
        this.y = y;
        this.width = width;
        this.height = height;
        this.font = font;
        this.textFont = new Font("Arial", Font.PLAIN, font);
        this.fillet = fillet;
        this.message = message;
        this.buttonType = buttonType;
        fontSize = new BasicStroke(font);
        x = panelWidth - xOffset;
        rrectArea = new Area(new RoundRectangle2D.Double(x, y, width, height, fillet, fillet));
    }

    public void drawButton(Graphics g, int panelWidth) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        x = panelWidth - xOffset;
        rrectArea = new Area(new RoundRectangle2D.Double(x, y, width, height, fillet, fillet));
        g2d.setStroke(fontSize);
        if (hovering) {
            g2d.setColor(hoverColor);
        } else {
            g2d.setColor(normalColor);
        }

        g2d.fill(rrectArea);
        g2d.setColor(Color.BLACK);
        g2d.draw(rrectArea);
        g2d.setColor(Color.BLACK);
        g2d.setFont(textFont);
        g2d.drawString(message, x + width / 10, y + height / 2 + font / 3);
    }
}
