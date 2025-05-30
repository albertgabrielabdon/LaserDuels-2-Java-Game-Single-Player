import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Dragon {
    private int x, y;
    private int width, height;
    private int screenHeight;

    private BufferedImage attackSpriteSheet;
    private ImageIcon idleIcon;
    private ImageIcon currentIcon;

    private boolean isVisible;
    private long spawnTime;
    private boolean isRetreating = false;
    private long retreatStartTime;
    private final long visibleDuration = 2000;
    private final long retreatDuration = 4000;

    private int retreatDistance;
    private double retreatSpeed;

    private final int FRAME_WIDTH = 128;
    private final int FRAME_HEIGHT = 128;
    private final int TOTAL_FRAMES = 64;
    private final int COLS = 8;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private final int FRAME_DELAY = 100; 

    private final long bossWaveDuration = 50000;
    private long bossWaveStartTime;

    public Dragon(int screenWidth, int screenHeight) {
        this.screenHeight = screenHeight;
        loadImages();

        height = (int)(screenHeight * (0.60 + Math.random() * 0.25));
        double aspectRatio = (double) FRAME_WIDTH / FRAME_HEIGHT;
        width = (int)(height * aspectRatio);

        x = (int)(Math.random() * (screenWidth - width));
        y = screenHeight;

        isVisible = true;
        spawnTime = System.currentTimeMillis();
        lastFrameTime = spawnTime;

        retreatDistance = height;
        retreatSpeed = (double) retreatDistance / retreatDuration * 1.3;
        bossWaveStartTime = spawnTime;
    }

    private void loadImages() {
        try {
            attackSpriteSheet = ImageIO.read(new File("dragon2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        idleIcon = new ImageIcon("dragon_idle.gif");  // Only load once
        currentIcon = idleIcon;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        if (!isRetreating) {
            // Advance attack frame
            if (currentTime - lastFrameTime >= FRAME_DELAY) {
                currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
                lastFrameTime = currentTime;
            }

            // Check if visibleDuration passed to start retreat
            if (currentTime - spawnTime >= visibleDuration) {
                isRetreating = true;
                retreatStartTime = currentTime;
                currentIcon = idleIcon;
                currentFrame = 0;
            }

            // Check if boss wave duration passed — start raising dragon base every 1 second by 10px
            if (currentTime - bossWaveStartTime >= bossWaveDuration) {
                // Calculate how many seconds have passed since bossWaveDuration ended
                long secondsSinceBossWaveEnded = (currentTime - (bossWaveStartTime + bossWaveDuration)) / 1000;
                // Move the dragon up by 10px per second * number of seconds
                y = screenHeight - (int)(10 * secondsSinceBossWaveEnded);
            }
        } else {
            long timePassed = currentTime - retreatStartTime;
            y += retreatSpeed * timePassed;
            retreatStartTime = currentTime;

            if (timePassed >= retreatDuration || y > screenHeight + height) {
                isVisible = false;
            }
        }
    }
    public void draw(Graphics2D g2, JPanel panel) {
        if (!isVisible) return;

        boolean flip = x < panel.getWidth() / 2; // Flip if on left half

        if (!isRetreating && attackSpriteSheet != null) {
            int row = currentFrame / COLS;
            int col = currentFrame % COLS;

            BufferedImage frame = attackSpriteSheet.getSubimage(
                col * FRAME_WIDTH,
                row * FRAME_HEIGHT,
                FRAME_WIDTH,
                FRAME_HEIGHT
            );

            if (flip) {
                AffineTransform originalTransform = g2.getTransform(); // Save

                g2.translate(x + width, y - height);
                g2.scale(-1, 1);
                g2.drawImage(frame, 0, 0, width, height, panel);

                g2.setTransform(originalTransform); // Restore
            } else {
                g2.drawImage(frame, x, y - height, width, height, panel);
            }
        } else {
            Image idleImage = idleIcon.getImage();

            if (flip) {
                AffineTransform originalTransform = g2.getTransform();

                g2.translate(x + width, y - height);
                g2.scale(-1, 1);
                g2.drawImage(idleImage, 0, 0, width, height, panel);

                g2.setTransform(originalTransform);
            } else {
                g2.drawImage(idleImage, x, y - height, width, height, panel);
            }
        }
    }


    public Rectangle getBounds() {
        return new Rectangle(x, y - height, width, height);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public int getX() {
        return x;
    }
}
