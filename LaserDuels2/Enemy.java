import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.ImageIcon;

public class Enemy {
    private int x, y;
    private int size = 40;
    private int health = 3;
    private int speed;


    private Image enemyImage;
    private Image spawnImage;

    private boolean spawning = true;
    private boolean dying = false;
    private boolean isDead = false;

    private long spawnStartTime;
    private final long spawnDuration = 1000; // 1 second
    private boolean facingRight = false;

    private long lastHitTime = 0; // To prevent multiple hits in quick succession
    private final long damageCooldown = 500; // 1 second cooldown between hits

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        enemyImage = new ImageIcon("evil.gif").getImage();
        spawnImage = new ImageIcon("spawn.gif").getImage();
        spawnStartTime = System.currentTimeMillis();
        this.speed = ThreadLocalRandom.current().nextInt(2, 5); //tweak speed
    }

    public void update(Player player) {
        if (isDead) return;

        long now = System.currentTimeMillis();

        if (spawning && now - spawnStartTime > spawnDuration) {
            spawning = false;
        }

        if (dying && now - spawnStartTime > spawnDuration) {
            isDead = true;
            return;
        }

        if (!spawning && !dying) {
            int dx = player.getX() - x;
            int dy = player.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance != 0) {
                facingRight = dx > 0;
                x += (int) (speed * dx / distance);
                y += (int) (speed * dy / distance);
            }
        }
    }

    public void draw(Graphics g, Component c) {
        if (isDead) return;

        Graphics2D g2d = (Graphics2D) g;

        Image imgToDraw = spawning || dying ? spawnImage : enemyImage;

        if (facingRight) {
            // Flip horizontally
            g2d.drawImage(imgToDraw, x + size, y, -size, size, c);
        } else {
            g2d.drawImage(imgToDraw, x, y, size, size, c);
        }

        if (!spawning && !dying) {
            // Health bar
            g.setColor(Color.RED);
            g.fillRect(x, y - 10, size, 6);
            g.setColor(Color.GREEN);
            g.fillRect(x, y - 10, size * health / 3, 6);
            g.setColor(Color.BLACK);
            g.drawRect(x, y - 10, size, 6);
        }
    }

    public void takeDamage() {
        if (!spawning && !dying) {
            long now = System.currentTimeMillis();
            if (now - lastHitTime < damageCooldown) return; // immune

            health--;
            lastHitTime = now;

            if (health <= 0) {
                dying = true;
                spawnStartTime = System.currentTimeMillis(); // reuse for death timer
            }
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
