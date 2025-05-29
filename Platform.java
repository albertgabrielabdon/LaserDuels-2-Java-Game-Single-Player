import java.awt.*;
import javax.swing.*;

public class Platform {
    public int x, y, width, height;
    private static final Image platformImage = new ImageIcon("platform.gif").getImage();
    private static final int imageWidth = platformImage.getWidth(null);
    private static final int imageHeight = platformImage.getHeight(null);

    public Platform(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = imageHeight;
    }

    public void draw(Graphics g) {
        for (int i = 0; i < width; i += imageWidth) {
            g.drawImage(platformImage, x + i, y, null);
        }
    }

    public boolean isUnderPlayer(Player player) {
        int px = player.getX();
        int py = player.getY() + player.getSize();
        int pvy = player.getVelocityY();

        return pvy >= 0 &&  
               px + player.getSize() > x &&
               px < x + width &&
               py >= y &&
               py <= y + height;
    }
}
