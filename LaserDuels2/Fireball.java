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
    private long creationTime;
    private boolean speedIncreased = false;

    public Fireball(int screenHeight, boolean fromLeft, Image image) {
        this.fromLeft = fromLeft;
        this.image = image;
        this.y = 100 + new Random().nextInt(screenHeight - 200);

        Random rand = new Random();
        int speed = 2 + rand.nextInt(2);
        this.vx = fromLeft ? speed : -speed;
        this.x = fromLeft ? -width : 800;
        this.creationTime = System.currentTimeMillis();
    }

    public void update() {
        x += vx;
        long elapsedTime = System.currentTimeMillis() - creationTime;

        if (!speedIncreased && elapsedTime >= 50000) {  // 50,000 ms = 50 seconds
            if (vx > 0) {
                vx += 1;
            } else {
                vx -= 1;
            }
            speedIncreased = true;  // only increase speed once after 50 seconds
        }
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
