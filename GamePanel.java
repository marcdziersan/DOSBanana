package dosbanana;

import dosbanana.entity.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GamePanel extends JPanel implements KeyListener {
    private static final int W = 800, H = 600;
    private static final int TICK_MS = 16; // ~60fps
    private static final int START_LIVES = 3; // hearts

    private final dosbanana.gfx.BackgroundWall bg = new dosbanana.gfx.BackgroundWall();

    private final List<String> levels;
    private int levelIdx = 0;

    private World world;
    private Timer timer;

    private boolean left, right, jump;
    private boolean shoot;

    private enum ScreenState {
        START,
        PLAYING,
        WIN,
        GAME_OVER
    }

    private ScreenState state = ScreenState.START;

    // Meta / Endscreen-Info
    private static final String GAME_TITLE = "DOS Banana";
    private static final String GAME_VERSION = "1.1.0";
    private static final String GAME_AUTHOR = "Marcus Dziersan";
    private static final String GAME_COPYRIGHT = "\u00A9 2026 Marcus Dziersan";

    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);

        levels = LevelLoader.discoverLevelFiles("levels");

        if (levels.isEmpty()) {
            // Fallback: versuch levels\\level1.txt
            world = new World("levels\\level1.txt", 0, START_LIVES);
        } else {
            world = new World(levels.get(levelIdx), 0, START_LIVES);
        }

        timer = new Timer(TICK_MS, e -> {
            // In nicht-PLAYING Screens: nur repaint, keine Simulation
            if (state != ScreenState.PLAYING) {
                repaint();
                return;
            }

            handleInput();
            world.update(1.0 / 60.0);

            // Game Over?
            if (world.getHealthHalf() <= 0) {
                state = ScreenState.GAME_OVER;
                repaint();
                timer.stop();
                return;
            }

            // Next level / Win?
            if (world.consumeExitTriggered()) {
                tryNextLevelOrWin();
            }

            repaint();
        });
    }

    public void start() {
        requestFocusInWindow();
        timer.start();
    }

    private void tryNextLevelOrWin() {
        if (levels.isEmpty()) {
            state = ScreenState.WIN;
            repaint();
            timer.stop();
            return;
        }

        int next = levelIdx + 1;
        if (next < levels.size() && LevelLoader.existsFile(levels.get(next))) {
            int carryScore = world.getScore();
            int carryLives = world.getMaxHearts();
            int carryHealthHalf = world.getHealthHalf();

            levelIdx = next;
            world = new World(levels.get(levelIdx), carryScore, carryLives, carryHealthHalf);
        } else {
            state = ScreenState.WIN;
            repaint();
            timer.stop();
        }
    }

    private void restartGame() {
        timer.stop();

        // input flags reset
        left = right = jump = shoot = false;

        state = ScreenState.START;
        levelIdx = 0;

        if (levels.isEmpty()) {
            world = new World("levels\\level1.txt", 0, START_LIVES);
        } else {
            world = new World(levels.get(levelIdx), 0, START_LIVES);
        }

        timer.start();
        repaint();
    }

    private void handleInput() {
        Player p = world.getPlayer();
        if (p == null) return;

        p.setMoveLeft(left);
        p.setMoveRight(right);

        if (jump) p.requestJump();
        jump = false;

        if (shoot) p.requestShoot();
        shoot = false;
    }

    private void renderStartScreen(Graphics2D g2) {
        // DOS-like overlay box
        int boxW = 660;
        int boxH = 410;
        int x = (getWidth() - boxW) / 2;
        int y = (getHeight() - boxH) / 2;

        drawOverlayBox(g2, x, y, boxW, boxH);

        g2.setFont(new Font("Monospaced", Font.BOLD, 34));
        g2.setColor(Color.GREEN);
        g2.drawString(GAME_TITLE, x + 30, y + 60);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2.drawString("Collect all bananas to open the door.", x + 30, y + 105);
        g2.drawString("Avoid enemies — stomp them from above.", x + 30, y + 130);

        // gameplay additions
        g2.drawString("Power-up: Collect 'P' to unlock Flame Shot.", x + 30, y + 160);
        g2.drawString("Fall damage: Big falls (higher than a normal jump) hurt (1/2 heart).", x + 30, y + 185);

        g2.drawString("Controls:", x + 30, y + 230);
        g2.drawString("  Left/Right  : A/D or Arrow Keys", x + 30, y + 255);
        g2.drawString("  Jump        : SPACE / W / UP", x + 30, y + 280);
        g2.drawString("  Flame Shot  : F or CTRL (after power-up)", x + 30, y + 305);
        g2.drawString("  Start       : ENTER or SPACE", x + 30, y + 330);
        g2.drawString("  Restart     : ESC (hard reset)", x + 30, y + 355);
        g2.drawString("  Quit        : Q", x + 30, y + 380);
    }

    private void renderEndScreen(Graphics2D g2, boolean win) {
        int boxW = 680;
        int boxH = 420;
        int x = (getWidth() - boxW) / 2;
        int y = (getHeight() - boxH) / 2;

        drawOverlayBox(g2, x, y, boxW, boxH);

        g2.setFont(new Font("Monospaced", Font.BOLD, 34));
        g2.setColor(win ? Color.GREEN : Color.RED);
        g2.drawString(win ? "YOU WIN!" : "GAME OVER", x + 30, y + 60);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2.setColor(Color.GREEN);
        g2.drawString("Score: " + world.getScore(), x + 30, y + 95);

        // Program / credits
        g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
        g2.drawString("Program Info:", x + 30, y + 140);
        g2.drawString("  Title    : " + GAME_TITLE, x + 30, y + 165);
        g2.drawString("  Version  : " + GAME_VERSION, x + 30, y + 185);
        g2.drawString("  Author   : " + GAME_AUTHOR, x + 30, y + 205);
        g2.drawString("  Copyright: " + GAME_COPYRIGHT, x + 30, y + 225);
        g2.drawString("  Tech     : Java + Swing", x + 30, y + 245);

        g2.drawString("Keys:", x + 30, y + 290);
        g2.drawString("  R   Restart", x + 30, y + 315);
        g2.drawString("  ESC Restart (hard reset)", x + 30, y + 335);
        g2.drawString("  Q   Quit", x + 30, y + 355);
    }

    private void drawOverlayBox(Graphics2D g2, int x, int y, int w, int h) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.70f));
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(x, y, w, h, 18, 18);
        g2.setComposite(old);

        g2.setColor(Color.GREEN);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 18, 18);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        bg.paint(g2, getWidth(), getHeight());

        // START screen: nur Overlay
        if (state == ScreenState.START) {
            renderStartScreen(g2);
            g2.dispose();
            return;
        }

        // PLAY/WIN/GO: World einfrieren und als Hintergrund stehen lassen
        world.render(g2);

        if (state == ScreenState.WIN) {
            renderEndScreen(g2, true);
        } else if (state == ScreenState.GAME_OVER) {
            renderEndScreen(g2, false);
        }

        g2.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right = true;

            case KeyEvent.VK_SPACE -> {
                if (state == ScreenState.START) {
                    state = ScreenState.PLAYING;
                    repaint();
                } else if (state == ScreenState.PLAYING) {
                    jump = true;
                }
            }

            case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                if (state == ScreenState.PLAYING) jump = true;
            }

            case KeyEvent.VK_F, KeyEvent.VK_CONTROL -> {
                if (state == ScreenState.PLAYING) shoot = true;
            }

            case KeyEvent.VK_ENTER -> {
                if (state == ScreenState.START) {
                    state = ScreenState.PLAYING;
                    repaint();
                }
            }

            case KeyEvent.VK_Q -> System.exit(0);

            case KeyEvent.VK_ESCAPE -> restartGame();

            case KeyEvent.VK_R -> {
                if (state == ScreenState.WIN || state == ScreenState.GAME_OVER) restartGame();
            }

            default -> {}
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right = false;
            default -> {}
        }
    }
}