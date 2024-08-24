import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.io.File;

public class LassyBlock extends JPanel implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;
    private final Point[][][] Tetrominoes = {
            // I-Piece
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
                    { new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3) },
                    { new Point(0, 2), new Point(1, 2), new Point(2, 2), new Point(3, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) }
            },
            // J-Piece
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0) }
            },
            // L-Piece
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) }
            },
            // O-Piece
            {
                    { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
                    { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
                    { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
                    { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) }
            },
            // S-Piece
            {
                    { new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
                    { new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
                    { new Point(1, 1), new Point(2, 1), new Point(0, 2), new Point(1, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(2, 2) }
            },
            // T-Piece
            {
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) }
            },
            // Z-Piece
            {
                    { new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) },
                    { new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
                    { new Point(2, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) }
            }
    };

    private final Color[] tetrominoColors = { Color.cyan, Color.blue, Color.orange, Color.yellow, Color.green, Color.magenta, Color.red };
    private Point pieceOrigin;
    private int currentPiece;
    private int rotation;
    private Timer timer;
    private int[][] well;
    private int score = 0;
    private int linesCleared = 0;
    private boolean gameOver = false;
    private Clip clip;

    // Creates a border around the well and initializes the dropping piece
    private void init() {
        well = new int[12][24];
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 23; j++) {
                if (i == 0 || i == 11 || j == 22) {
                    well[i][j] = 1;
                } else {
                    well[i][j] = 0;
                }
            }
        }
        newPiece();
    }

    private void playSoundtrack(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                throw new RuntimeException("Sound file not found: " + filePath);
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);  // Loop the soundtrack continuously
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file format: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopSoundtrack() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void newPiece() {
        pieceOrigin = new Point(5, 2);
        rotation = 0;
        currentPiece = (int) (Math.random() * 7);
        if (collidesAt(pieceOrigin.x, pieceOrigin.y, rotation)) {
            gameOver = true;
            timer.stop();
            stopSoundtrack();
        }
    }

    private boolean collidesAt(int x, int y, int rotation) {
        for (Point p : Tetrominoes[currentPiece][rotation]) {
            if (well[p.x + x][p.y + y] != 0) {
                return true;
            }
        }
        return false;
    }

    public void rotate(int i) {
        if (gameOver) return;

        int newRotation = (rotation + i) % 4;
        if (newRotation < 0) {
            newRotation = 3;
        }
        if (!collidesAt(pieceOrigin.x, pieceOrigin.y, newRotation)) {
            rotation = newRotation;
        }
        repaint();
    }

    public void move(int i) {
        if (gameOver) return;

        if (!collidesAt(pieceOrigin.x + i, pieceOrigin.y, rotation)) {
            pieceOrigin.x += i;
        }
        repaint();
    }

    public void dropDown() {
        if (gameOver) return;

        if (!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
            pieceOrigin.y += 1;
        } else {
            fixToWell();
        }
        repaint();
    }

    public void fixToWell() {
        for (Point p : Tetrominoes[currentPiece][rotation]) {
            well[p.x + pieceOrigin.x][p.y + pieceOrigin.y] = currentPiece + 1;
        }
        clearRows();
        newPiece();
    }

    public void deleteRow(int row) {
        for (int j = row - 1; j > 0; j--) {
            for (int i = 1; i < 11; i++) {
                well[i][j + 1] = well[i][j];
            }
        }
    }

    public void clearRows() {
        boolean gap;
        int numClears = 0;

        for (int j = 21; j > 0; j--) {
            gap = false;
            for (int i = 1; i < 11; i++) {
                if (well[i][j] == 0) {
                    gap = true;
                    break;
                }
            }
            if (!gap) {
                deleteRow(j);
                j += 1;
                numClears += 1;
            }
        }

        switch (numClears) {
            case 1:
                score += 100;
                linesCleared += 1;
                break;
            case 2:
                score += 300;
                linesCleared += 2;
                break;
            case 3:
                score += 500;
                linesCleared += 3;
                break;
            case 4:
                score += 800;
                linesCleared += 4;
                break;
        }
    }

    private void drawPiece(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (Point p : Tetrominoes[currentPiece][rotation]) {
            int x = (p.x + pieceOrigin.x) * 26;
            int y = (p.y + pieceOrigin.y) * 26;
            GradientPaint gradient = new GradientPaint(x, y, Color.white, x + 25, y + 25, tetrominoColors[currentPiece]);
            g2d.setPaint(gradient);
            g2d.fillRect(x, y, 25, 25);
            g2d.setColor(Color.black);
            g2d.drawRect(x, y, 25, 25);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the title at the top
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), 50);
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Lassy Block", getWidth() / 2 - 100, 40);

        if (gameOver) {
            g.setColor(Color.black);
            g.fillRect(0, 50, getWidth(), getHeight() - 50);
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over", getWidth() / 2 - 100, getHeight() / 2 - 50);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Final Score: " + score, getWidth() / 2 - 90, getHeight() / 2);
            return;
        }

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 23; j++) {
                g.setColor(Color.black);
                g.fillRect(26 * i, 26 * j + 50, 25, 25);
                if (well[i][j] != 0) {
                    int pieceIndex = well[i][j] - 1;
                    int x = 26 * i;
                    int y = 26 * j + 50;
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(x, y, Color.white, x + 25, y + 25, tetrominoColors[pieceIndex]);
                    g2d.setPaint(gradient);
                    g2d.fillRect(x, y, 25, 25);
                    g2d.setColor(Color.black);
                    g2d.drawRect(x, y, 25, 25);
                }
            }
        }

        drawPiece(g);

        // Draw the scoreboard
        int scoreboardX = 12 * 26 + 30;
        g.setColor(Color.black);
        g.fillRect(scoreboardX - 10, 100, 160, 150);
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score", scoreboardX, 130);
        g.drawString(String.valueOf(score), scoreboardX, 160);
        g.drawString("Lines", scoreboardX, 190);
        g.drawString(String.valueOf(linesCleared), scoreboardX, 220);

        // Draw the instructions at the bottom
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.black);
        g.fillRect(0, getHeight() - 60, getWidth(), 60);
        g.setColor(Color.white);
        g.drawString("Instructions:", 10, getHeight() - 45);
        g.drawString("Up Arrow: Rotate | Left Arrow: Move Left | Right Arrow: Move Right | Down Arrow: Drop", 10, getHeight() - 25);
        g.drawString("Press Spacebar to drop the piece quickly", 10, getHeight() - 5);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            dropDown();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                rotate(-1);
                break;
            case KeyEvent.VK_DOWN:
                rotate(1);
                break;
            case KeyEvent.VK_LEFT:
                move(-1);
                break;
            case KeyEvent.VK_RIGHT:
                move(1);
                break;
            case KeyEvent.VK_SPACE:
                dropDown();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // No action required on key release
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No action required on key typed
    }

    private void startGame() {
        timer.start();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Lassy Block");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(12 * 26 + 200, 26 * 23 + 100); // Increased height to accommodate title and instructions
        f.setResizable(false);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(screenSize.width / 2 - f.getSize().width / 2, screenSize.height / 2 - f.getSize().height / 2);

        LassyBlock game = new LassyBlock();
        f.add(game);
        f.addKeyListener(game);
        f.setVisible(true);
        game.startGame();
    }

    public LassyBlock() {
        timer = new Timer(500, this);
        init();
        playSoundtrack("C:\\Users\\sethl\\OneDrive\\Desktop\\music-for-puzzle-game-146738.wav"); // Replace with the correct path to your WAV file
    }
}

