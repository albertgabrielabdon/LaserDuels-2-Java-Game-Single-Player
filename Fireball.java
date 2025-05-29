import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image; 
import java.awt.Rectangle;
import java.util.Random;

public class Fireball {
    private int x, y;
    private int vx;
    private final int width = 40, height = 40;
    public boolean fromLeft;
    private final Image image;
    private int clickCount = 0;

    public Fireball(int screenHeight, boolean fromLeft, Image image) {
        this.fromLeft = fromLeft;
        this.image = image;
        this.y = 100 + new Random().nextInt(screenHeight - 200);
        this.vx = fromLeft ? 4 : -4;
        this.x = fromLeft ? -width : 800;
    }

    public void update() {
        x += vx;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g, Component c) {
        if (fromLeft) {
            g.drawImage(image, x, y, width, height, c);
        } else {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x + width, y);
            g2.scale(-1, 1);
            g2.drawImage(image, 0, 0, width, height, c);
            g2.dispose();
        }
    }

    public boolean isOffScreen(int screenWidth) {
        return x < -width || x > screenWidth;
    }

    public boolean isMouseHovering(int mouseX, int mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }

    public void incrementClick() {
        clickCount++;
    }

    public int getClickCount() {
        return clickCount;
    }
}
