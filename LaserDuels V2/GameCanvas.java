import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class GameCanvas extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 800, HEIGHT = 600;
    private final Timer timer;
    private final Player player;
    private boolean leftPressed = false, rightPressed = false;
    private Font pixelFont30;
    private final MusicPlayer musicPlayer = new MusicPlayer();

    private int mouseX = 0, mouseY = 0;
    private boolean punching = false;
    private long punchStartTime = 0;
    private final int punchDuration = 300;

    private List<Platform> platforms = new ArrayList<>();
    private long lastPlatformRefreshTime = System.currentTimeMillis();
    private final int platformRefreshInterval = 10000; 
    private final Random rand = new Random();
    private final Image backgroundImage = Toolkit.getDefaultToolkit().getImage("bg2.gif");
    private final Image lavaImage = Toolkit.getDefaultToolkit().getImage("lava.gif");
    private final Image handImage = Toolkit.getDefaultToolkit().getImage("hand.gif");

    private boolean lavaVisible = false;
    private long gameStartTime = System.currentTimeMillis();
    private final int lavaDelay = 10000;

    private boolean countdownStarted = false;
    private long countdownStartTime = 0;
    private final int countdownDuration = 10000; 

    private boolean showStartOverlay = true;
    private boolean lavaSpawned = false;
    private long lavaSpawnedStartTime = 0;
    private final int lavaSpawnedAnimationDuration = 1200; 
    private boolean showPlayer1Text = false;

    private final List<Fireball> fireballs = new ArrayList<>();
    private final Image fireballImage = Toolkit.getDefaultToolkit().getImage("fireball.gif");
    private int fireballCount = 3;
    private long lastFireballWaveTime = System.currentTimeMillis();
    private final int fireballInterval = 10000; 
    private final int gameRestartDelay = 5000;

    private final long gameTimerDuration = 2*60 * 1000; 
    private long gameTimerStartTime = System.currentTimeMillis();

    private final Image sparkImage = Toolkit.getDefaultToolkit().getImage("spark.gif");
    private boolean showHitEffect = false;
    private long hitEffectStartTime = 0;
    private final int hitEffectDuration = 1500; 
    

    public GameCanvas() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();

                for (int i = 0; i < fireballs.size(); i++) {
                    Fireball f = fireballs.get(i);
                    if (f.isMouseHovering(mouseX, mouseY)) {
                        f.incrementClick();
                        if (f.getClickCount() >= 1) {
                            fireballs.remove(i);
                            i--;
                        }
                        return;
                    }
                }
                
                if (player.canPunch(mouseX, mouseY)) {
                    punching = true;
                    punchStartTime = System.currentTimeMillis();
                }
            }
        });

        player = new Player(100, HEIGHT - 100);
        generatePlatforms();
        timer = new Timer(16, this); 
        musicPlayer.start();

        try {
            pixelFont30 = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/Pixeled.ttf")).deriveFont(30f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(pixelFont30);
        } catch (Exception ex) {
            ex.printStackTrace();
            pixelFont30 = new Font("Arial", Font.BOLD, 30);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (leftPressed) player.moveLeft();
        if (rightPressed) player.moveRight();
        player.update();

        if (punching && System.currentTimeMillis() - punchStartTime > punchDuration) {
            punching = false;
        }

        if (!player.isFastFalling()) {
            for (Platform platform : platforms) {
                if (platform.isUnderPlayer(player)) {
                    player.landOn(platform.y);
                    break;
                }
            }
        }
        long currentTime = System.currentTimeMillis();

        if (!countdownStarted && !lavaSpawned && showPlayer1Text) {
            lavaVisible = true;
        } else {
            lavaVisible = false;
        }

        if (lavaVisible && player.getY() + player.getSize() >= HEIGHT - 50) {
            player.respawn(100, 100);
            player.takeDamage(50);

            if (player.getHealth() <= 0 && !player.isGameOver()) {
                player.setGameWin(false);
                player.setGameOverTime(System.currentTimeMillis());
            }
                    
        }
        long elapsedTime = currentTime - gameStartTime;

        int interval = elapsedTime > 50_000 ? 4000 : platformRefreshInterval;  // 4s after 50s

        if (currentTime - lastPlatformRefreshTime > interval) {
            generatePlatforms();
            lastPlatformRefreshTime = currentTime;
        }


        for (Fireball f : new ArrayList<>(fireballs)) {
            f.update();
            if (f.isOffScreen(WIDTH)) {
                fireballs.remove(f);
            }
        }

        if (fireballs.isEmpty()) {
            long now = System.currentTimeMillis();
            int increments = (int) ((now - gameStartTime) / fireballInterval);
            fireballCount = Math.min(3 + increments, 13);

            for (int i = 0; i < fireballCount; i++) {
                boolean fromLeft = rand.nextBoolean();
                fireballs.add(new Fireball(HEIGHT, fromLeft, fireballImage));
            }
        }

        for (Fireball f : new ArrayList<>(fireballs)) {
            f.update();

            if (f.isOffScreen(WIDTH)) {
                fireballs.remove(f);
                continue;
            }

            if (f.getBounds().intersects(player.getBounds())) {
                int direction = f.fromLeft ? 1 : -1; 
                player.startKnockback(direction);
                fireballs.remove(f);
                player.takeDamage(10);   
                showHitEffect = true;
                hitEffectStartTime = System.currentTimeMillis();

                if (player.getHealth() <= 0 && !player.isGameOver()) {
                    player.setGameWin(false);
                    player.setGameOverTime(System.currentTimeMillis());
                }
            }
        }

        if (player.isGameOver() && currentTime - player.getGameOverTime() >= gameRestartDelay) {
            restartGame();
        }

        long elapsedGameTime = currentTime - gameTimerStartTime;
        long remainingTime = gameTimerDuration - elapsedGameTime;

        if (remainingTime <= 0 && !player.isGameOver() && !player.isGameWin()) {
            if (player.getHealth() > 0) {
                player.setGameWin(true);
            } else {
                player.setGameWin(false);
            }
            player.setGameOverTime(currentTime);  
        }

        if (player.isGameWin() && currentTime - player.getGameOverTime() >= gameRestartDelay) {
            restartGame();
        }

        if (showHitEffect) {
            long elapsedHitEffect = System.currentTimeMillis() - hitEffectStartTime;
            if (elapsedHitEffect > hitEffectDuration) {
                showHitEffect = false;
            }
        }

        repaint();
    }

    private void restartGame() {
        player.reset();
        fireballs.clear();
        fireballCount = 3;
        player.respawn(100, HEIGHT - 100);
        generatePlatforms();
        lastPlatformRefreshTime = System.currentTimeMillis();     
        gameTimerStartTime = System.currentTimeMillis(); 
        gameStartTime = System.currentTimeMillis();
        musicPlayer.restart();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, this);

        for (Platform p : platforms) {
            p.draw(g2);
        }
        player.draw(g2, this);

        if (punching) {
            int centerX = player.getCenterX();
            int centerY = player.getCenterY();

            double angle = Math.atan2(mouseY - centerY, mouseX - centerX);
            double distance = Math.min(350, Point.distance(centerX, centerY, mouseX, mouseY)); 

            int handWidth = (int) distance;
            int handHeight = 40;

            int handX = centerX + (int)(Math.cos(angle) * distance / 2) - handWidth / 2;
            int handY = centerY + (int)(Math.sin(angle) * distance / 2) - handHeight / 2;

            Graphics2D g2Rotated = (Graphics2D) g2.create();
            g2Rotated.rotate(angle, handX + handWidth / 2.0, handY + handHeight / 2.0);

            if (mouseX < centerX) {
                g2Rotated.translate(handX + handWidth, handY);
                g2Rotated.scale(-1, 1);
                g2Rotated.drawImage(handImage, 0, 0, handWidth, handHeight, this);
            } else {
                g2Rotated.drawImage(handImage, handX, handY, handWidth, handHeight, this);
            }

            g2Rotated.dispose();
        }

         for (Fireball f : fireballs) {
            f.draw(g2, this);
        }

        if (showHitEffect) {
            int sparkSize = 50;
            int sparkX;
            int playerCenterX = player.getCenterX();
            int playerCenterY = player.getCenterY();

            sparkX = playerCenterX  - sparkSize / 2;
            int sparkY = playerCenterY - sparkSize / 2;

            g.drawImage(sparkImage, sparkX, sparkY, sparkSize, sparkSize, this);
        }

        long currentTime = System.currentTimeMillis();

        if (countdownStarted && !lavaSpawned) {
            long elapsed = currentTime - countdownStartTime;
            int secondsLeft = (int) Math.ceil((countdownDuration - elapsed) / 1000.0);

            Color overlayBlack = new Color(0, 0, 0, (int)(255 * 0.5));
            g2.setColor(overlayBlack);
            g2.fillRect(0, 0, WIDTH, HEIGHT);

            if (secondsLeft > 0) {

                String countdownText = "Lava spawns in: " + secondsLeft;
                g2.setFont(pixelFont30);
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(countdownText);
                int textHeight = fm.getAscent();

                int x = (WIDTH - textWidth) / 2;
                int y = (HEIGHT + textHeight) / 2;

                if (secondsLeft > 5) {
                    g2.setColor(Color.WHITE);
                } else if (secondsLeft <= 5 && secondsLeft > 3) {
                    g2.setColor(Color.YELLOW);
                } else {
                    Color DarkRed = new Color(204, 51, 0); 
                    g2.setColor(DarkRed);
                }

                g2.drawString(countdownText, x, y);
            } else {
                lavaSpawned = true;
                lavaSpawnedStartTime = currentTime;
            }
        } else if (lavaSpawned) {
            long animElapsed = currentTime - lavaSpawnedStartTime;
            float progress = Math.min(animElapsed / (float) lavaSpawnedAnimationDuration, 1.0f);

            float scale = 1.0f + 0.2f * progress;

            int alpha = (int) (255 * (1.0f - progress));
            alpha = Math.max(alpha, 0);

            Font baseFont = pixelFont30;
            float baseSize = baseFont.getSize2D();
            Font scaledFont = baseFont.deriveFont(baseSize * scale);
            g2.setFont(scaledFont);

            String lavaText = "Lava Spawned!";
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(lavaText);
            int textHeight = fm.getAscent();

            int x = (WIDTH - textWidth) / 2;
            int y = (HEIGHT + textHeight) / 2;

            g2.setColor(new Color(255, 69, 0, alpha));

            g2.drawString(lavaText, x, y);

            if (progress >= 1.0f) {
                countdownStarted = false;
                lavaSpawned = false;
                showPlayer1Text = true;
            }
        }

        if (showPlayer1Text) {
            String player1Text = " ";
            g2.setFont(pixelFont30);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(player1Text);
            int padding = 20;
            int x = WIDTH - textWidth - padding;
            int y = fm.getAscent() + padding;

            g2.setColor(Color.WHITE);
            g2.drawString(player1Text, x, y);
        }

    
        if (lavaVisible) {
            g2.drawImage(lavaImage, 0, HEIGHT - 50, WIDTH, 50, this);
        }

        int health = player.getHealth();
        int healthBarWidth = (int) (200 * Math.max(0, health / 500.0));
        g2.setColor(health > 250 ? Color.GREEN : health > 100 ? Color.ORANGE : Color.RED);
        g2.fillRect(20, 20, healthBarWidth, 30);
        g2.setColor(Color.WHITE);
        g2.setFont(pixelFont30.deriveFont(18f));
        g2.drawString("HP: " + health, 230, 43);

        if (player.isGameOver()) {
            g2.setFont(pixelFont30);
            String loseText = "YOU LOSE";
            String restartingText = "Restarting in " + (5 - (System.currentTimeMillis() - player.getGameOverTime()) / 1000) + "...";
            FontMetrics fm = g2.getFontMetrics();
            int x = (WIDTH - fm.stringWidth(loseText)) / 2;
            int y = HEIGHT / 2;
            g2.setColor(Color.RED);
            g2.drawString(loseText, x, y);
            g2.setColor(Color.WHITE);
            g2.drawString(restartingText, x, y + 40);
            gameTimerStartTime = System.currentTimeMillis();

        }

        if (!player.isGameOver() && !player.isGameWin()) {
            long elapsedGameTime = System.currentTimeMillis() - gameTimerStartTime;
            long remainingTime = Math.max(0, gameTimerDuration - elapsedGameTime);
            int seconds = (int) (remainingTime / 1000);
            int minutesPart = seconds / 60;
            int secondsPart = seconds % 60;
            String timeText = String.format("%d:%02d", minutesPart, secondsPart);

            g2.setFont(pixelFont30);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(timeText);
            int padding = 20;
            int x = WIDTH - textWidth - padding;
            int y = fm.getAscent() + padding;

            g2.setColor(Color.WHITE);
            g2.drawString(timeText, x, y);
        }

        if (player.isGameWin()) {
            g2.setFont(pixelFont30);
            String winText = "YOU WIN";
            String restartingText = "Restarting in " + (5 - (System.currentTimeMillis() - player.getGameOverTime()) / 1000) + "...";
            FontMetrics fm = g2.getFontMetrics();
            int x = (WIDTH - fm.stringWidth(winText)) / 2;
            int y = HEIGHT / 2;
            g2.setColor(Color.GREEN);
            g2.drawString(winText, x, y);
            g2.setColor(Color.WHITE);
            g2.drawString(restartingText, x, y + 40);

        }

        g2.dispose();
    }

    private void generatePlatforms() {
        platforms.clear();

        long elapsedTime = System.currentTimeMillis() - gameStartTime;
        boolean lateGame = elapsedTime > 50_000;

        int num = lateGame ? 1 + rand.nextInt(3) : 2 + rand.nextInt(5);
        int attempts = 0;
        int maxAttempts = 100;

        int minSpacing = lateGame ? 150 : 80;
        int minWidth = lateGame ? 60 : 100;
        int maxWidth = lateGame ? 120 : 300;
    
      
        while (platforms.size() < num && attempts < maxAttempts) {
            int width = minWidth + rand.nextInt(maxWidth - minWidth + 1);
            int x = rand.nextInt(WIDTH - width);
            int y = 100 + rand.nextInt(HEIGHT - 200); 

            boolean tooClose = false;
            for (Platform existing : platforms) {
                int dx = Math.abs(existing.x - x);
                int dy = Math.abs(existing.y - y);

                if (dx < minSpacing && dy < minSpacing) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                platforms.add(new Platform(x, y, width));
            }

            attempts++;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> leftPressed = true;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> rightPressed = true;
            case KeyEvent.VK_SPACE, KeyEvent.VK_W -> player.jump();
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> player.fastFall(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> leftPressed = false;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> rightPressed = false;
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> player.fastFall(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public void startGame() {
        countdownStarted = true;
        countdownStartTime = System.currentTimeMillis();
        gameStartTime = countdownStartTime;  
        showStartOverlay = true;
        lavaVisible = false;
        lavaSpawned = false;
        showPlayer1Text = false;
        timer.start();
    }
} 
