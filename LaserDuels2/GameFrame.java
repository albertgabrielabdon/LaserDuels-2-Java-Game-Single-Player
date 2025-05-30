import javax.swing.JFrame;

public class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Laser Duels V2 | The Floor is Lava");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GameCanvas canvas = new GameCanvas();
        add(canvas);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
        canvas.startGame();
    }

    public static void main(String[] args) {
        new GameFrame();
    }
}
