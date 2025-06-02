import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

public class Player {
    private int x, y;
    private int health = 2000;
    private boolean gameOver = false;
    private long gameOverTime = 0;

    private final int size = 40;
    private int velocityX = 0;
    private double velocityY = 0;
    private final int speed = 10;
    private final double gravity = 0.6;
    private final double jumpStrength = -13;
    private int jumpCount = 0;
    private final int maxJumps = 2;
    private boolean isFastFalling = false;
    private final double fastFallGravity = 1.5;
    private final int groundY = 600 - size;
    
    private boolean facingLeft = false;
    private boolean knockbackActive = false;
    private long knockbackStartTime = 0;
    private final long knockbackDuration = 125; 
    private int knockbackDirection = 0;
    private float knockbackSpeed = 0.0f;
    private float knockbackAcceleration = 6.5f; 
    private final float baseKnockbackAcceleration = 6.5f;
    
    private boolean gameWin = false;
    private long gameWinTime = 0;

    private boolean slowed = false;
    private long slowedStartTime = 0;
    private final long slowedDuration = 4000; // 4 seconds
    private final long damageCooldown = 1500; // 1.5 seconds
    private long lastDamageTime = 0;

    private long cooldownReductionEndTime = 0;
    private final long dragonEffectDuration = 5000;


    private Image idleImage;
    private Image runImage;
    private Image currentImage;
    private Image jumpImage;
    private Image slowEffectImage = new ImageIcon("evilfx.gif").getImage();
    private Image dragonEffectImage = new ImageIcon("burn.gif").getImage();




    public Player(int startX, int startY) {
        this.idleImage = new ImageIcon("penguin_idle.gif").getImage();
        this.runImage = new ImageIcon("penguin_run.gif").getImage();
        this.jumpImage = new ImageIcon("penguin_jump.gif").getImage();
        this.x = startX;
        this.y = startY;
    }

    public void moveLeft() {
        velocityX = -getSpeed();
        facingLeft = true;
        currentImage = runImage;
    }

    public void moveRight() {
        velocityX = getSpeed();
        facingLeft = false;
        currentImage = runImage;
    }

    public void jump() {
        if (jumpCount < maxJumps) {
            velocityY = getJumpStrength();
            jumpCount++;
            currentImage = jumpImage;
        }
    }
    public void update() {

         long currentTime = System.currentTimeMillis();

        if (knockbackActive) {
            if (currentTime - knockbackStartTime > knockbackDuration) {
                knockbackActive = false;
                knockbackSpeed = 0;
            } else {
                x += knockbackDirection * knockbackSpeed;
                knockbackSpeed += knockbackAcceleration;  
            }
        }
        velocityY += isFastFalling ? fastFallGravity : gravity;
        y += velocityY;

        x += velocityX;

        if (velocityX == 0) {
            if (velocityY < 0) {
                currentImage = jumpImage; 
            } else {
                currentImage = idleImage;
            }
        }

        velocityX = 0;

        if (y >= groundY) {
            y = groundY;
            velocityY = 0;
            jumpCount = 0;
        }

        if (x < 0) x = 0;
        if (x + size > 800) x = 800 - size;
        if (y < 0) y = 0;

        if (!isUnderDragonEffect() && knockbackAcceleration != baseKnockbackAcceleration) {
            knockbackAcceleration = baseKnockbackAcceleration;
        }
    }


    public int getCenterX() {
    return x + size / 2;
}

    public int getCenterY() {
        return y + size / 2;
    }

    public boolean canPunch(int targetX, int targetY) {
        int dx = targetX - getCenterX();
        int dy = targetY - getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= 350; 
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getVelocityY() { return (int) velocityY; }
    public int getSize() { return size; }

    public void landOn(int platformY) {
        y = platformY - size;
        velocityY = 0;
        jumpCount = 0;
        isFastFalling = false;
    }
    public void fastFall(boolean enable) {
        isFastFalling = enable;
    }

    public boolean isFastFalling() {
        return isFastFalling;
    }

    public void respawn(int newX, int newY) {
        this.x = newX;
        this.y = newY;
        this.velocityY = 0;
        this.jumpCount = 0;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public void startKnockback(int direction) {
        knockbackActive = true;
        knockbackStartTime = System.currentTimeMillis();
        knockbackDirection = direction;
        knockbackSpeed = 1.5f; 
    }

    public void takeDamage(int dmg) {
        if (isGameWin()) return;

        health -= dmg;
        if (health < 0) {
            health = 0;
        }

        if (health <= 0 && !gameOver) {
            triggerGameOver(); 
        }
    }

    public void triggerGameOver() {
        gameOver = true;
        gameOverTime = System.currentTimeMillis();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public long getGameOverTime() {
        return gameOverTime;
    }

    public int getHealth() {
        return health;
    }


    public void reset() {
        health = 2000;
        gameOver = false;
        gameOverTime = 0;
        currentImage = idleImage;
        jumpCount = 0;
        velocityX = 0;
        velocityY = 0;
        knockbackActive = false;
        gameWin = false;
        gameWinTime = 0;
    }


    public boolean isGameWin() {
        return gameWin;
    }

   
    public long getGameWinTime() {
        return gameWinTime;
    }

    public void setGameOverTime(long time) {
        this.gameOverTime = time;
    }

    public void setGameWin(boolean win) {
        if (win && !this.gameWin) {
            gameWinTime = System.currentTimeMillis();
        }
        this.gameWin = win;
    }

    public int getSpeed() {
        return slowed ? speed - 2 : speed;
    }

    public double getJumpStrength() {
        return slowed ? jumpStrength / 2 : jumpStrength;
    }

    public boolean isSlowed() {
        if (slowed && System.currentTimeMillis() - slowedStartTime >= slowedDuration) {
            slowed = false;
        }
        return slowed;
    }

    public void applySlowEffect() {
        if (!isSlowed()) {
            slowed = true;
            slowedStartTime = System.currentTimeMillis();
        }
    }

    public boolean canTakeDamage() {
        long cooldown = isUnderDragonEffect() ? damageCooldown / 3 : damageCooldown;
        return System.currentTimeMillis() - lastDamageTime >= cooldown;
    }

    public void markDamageTaken() {
        lastDamageTime = System.currentTimeMillis();
    }

    private boolean isUnderDragonEffect() {
        return System.currentTimeMillis() <= cooldownReductionEndTime;
    }

    public void applyDragonEffect() {
   
        knockbackStartTime = System.currentTimeMillis();
        knockbackAcceleration = baseKnockbackAcceleration * 1.3f;

        jumpCount = 0;

        cooldownReductionEndTime = System.currentTimeMillis() + dragonEffectDuration;
    }

    public void draw(Graphics g, Component c) {
        Graphics2D g2d = (Graphics2D) g;
        double scale = 1.4;
        AffineTransform original = g2d.getTransform();

        if (facingLeft) {
            g2d.translate(x + size, y);
            g2d.scale(-scale, scale);
        } else {
            g2d.translate(x, y);
            g2d.scale(scale, scale);
        }

        g2d.drawImage(currentImage, 0, -8, size, size, c);

        if (isSlowed()) {
            g2d.drawImage(slowEffectImage, 0, -8, size, size, c);
        }
        if (isUnderDragonEffect()) {
            g2d.drawImage(dragonEffectImage, 0, -8, size, size, c);
        }

        g2d.setTransform(original);
    }


}
