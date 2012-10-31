package madscience;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.util.HashSet;
import java.util.Set;
import madscience.sprites.PlayerSprite;

/**
 *
 * @author Richard Kakaš
 */
public final class GameCanvas extends Canvas implements Runnable, ComponentListener, KeyListener {
    private static final double LIFE_INDICATOR_WIDTH = 275;
    private static final double LIFE_INDICATOR_MARGIN = 6;

    private BufferStrategy buffer;

    private Thread loopThread;
    private final Object loopLock = new Object();
    private boolean loopRunning = false;
    private boolean gamePaused = false;
    private boolean gameEnded = false;

    private Set<Integer> pressedKeys = new HashSet<Integer>();
    private Game game = null;
    private int nextGameLevel = 1;

    public GameCanvas() {
        setVisible(false);
        setIgnoreRepaint(true);
        addComponentListener(this);
        addKeyListener(this);
    }

    public int getGameWidth() {
        return getWidth();
    }

    public int getGameHeight() {
        return getHeight() - 25;
    }

    public void startGame(Game newGame) {
        synchronized (loopLock) {
            game = newGame;
            gamePaused = false;
            gameEnded = false;
        }
    }

    public void startNextGame() {
        int score = 0;
        if (game == null || game.isLost()) nextGameLevel = 1;
        else score = game.getPlayerScore();
        Game newGame = GameFactory.createGame(getGameWidth(), getGameHeight(), nextGameLevel++);
        newGame.addPlayerScore(score);
        startGame(newGame);
    }

    public void pauseGame(boolean val) {
        synchronized (loopLock) {
            gamePaused = val;
        }
    }

    protected void update(double sec) {
        if (game != null) {
            synchronized (loopLock) {
                if (!gamePaused && !gameEnded) {
                    if (game.isWon() || game.isLost()) {
                        gameEnded = true;
                    }
                    else {
                        game.update(sec);
                        donePressedKeys();
                    }
                }
                else if (gameEnded) {
                    startNextGame();
                }
            }
        }
    }

    protected void draw(Graphics gAbs) {
        Graphics2D g = (Graphics2D) gAbs;
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, getWidth(), getHeight() - getGameHeight());

        double onePlayerLifeWidth = LIFE_INDICATOR_WIDTH / PlayerSprite.MAX_LIVES;
        int currPlayerLives = (game == null) ? 0 : game.getPlayerSprite().getLives();
        g.setColor(Color.RED);
        for (int i = 0; i < currPlayerLives; i++) {
            g.fill(new Rectangle2D.Double(i*onePlayerLifeWidth + LIFE_INDICATOR_MARGIN,
                                          LIFE_INDICATOR_MARGIN,
                                          onePlayerLifeWidth,
                                          getHeight() - getGameHeight() - 2*LIFE_INDICATOR_MARGIN));
        }

        int bossMaxLives = (game == null) ? 1 : game.getBossMaxLives();
        if (bossMaxLives == 0) bossMaxLives = 1;
        double oneBossLifeWidth = LIFE_INDICATOR_WIDTH / bossMaxLives;
        int currBossLives = (game == null || game.getBossSprite() == null) ? 0 :
                                    game.getBossSprite().getLives();
        g.setColor(Color.RED);
        for (int i = 0; i < currBossLives; i++) {
            g.fill(new Rectangle2D.Double(getWidth() - LIFE_INDICATOR_WIDTH - LIFE_INDICATOR_MARGIN + i*oneBossLifeWidth,
                                          LIFE_INDICATOR_MARGIN,
                                          oneBossLifeWidth,
                                          getHeight() - getGameHeight() - 2*LIFE_INDICATOR_MARGIN));
        }

        int score = (game == null) ? 0 : game.getPlayerScore();
        String scoreStr = Integer.toString(score);
        g.setColor(Color.WHITE);
        g.setFont(new Font(null, 0, getHeight() - getGameHeight()));
        FontMetrics fontMetrics = g.getFontMetrics();

        g.drawString(scoreStr, getWidth() / 2.0f - fontMetrics.stringWidth(scoreStr) / 2,
                               9.0f*(getHeight() - getGameHeight()) / 10.0f);

        g.setColor(Color.BLACK);
        g.fillRect(0, getHeight() - getGameHeight(), getGameWidth(), getGameHeight());

        AffineTransform af = g.getTransform();
        g.translate(0, getHeight() - getGameHeight());
        if (game != null) {
            synchronized (loopLock) {
                game.draw(g);
            }
        }
        g.setTransform(af);
    }

    @Override
    public void run() {
        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

        long lastLoopTime = System.nanoTime();

        while (loopRunning) {
            long now = System.nanoTime();
            double delta = now - lastLoopTime;
            lastLoopTime = now;

            update(delta / 1000000000);

            Graphics g = null;
            try {
                g = buffer.getDrawGraphics();
                draw(g);
            }
            finally {
                if (g != null) g.dispose();
            }
            if (!buffer.contentsLost()) buffer.show();
            Toolkit.getDefaultToolkit().sync();

            try {
                Thread.sleep((lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000);
            }
            catch(Exception e) { }
        }
    }

    public synchronized void donePressedKeys() {
        double speedX = 0;
        double speedY = 0;

        boolean isSpace = false;
        for (Integer key : pressedKeys) {
            switch (key) {
                case KeyEvent.VK_UP:
                    speedY = -game.getPlayerSetSpeed();
                    break;
                case KeyEvent.VK_DOWN:
                    speedY = game.getPlayerSetSpeed();
                    break;
                case KeyEvent.VK_SPACE:
                    isSpace = true;
                    break;
            }
        }
        game.setPlayerSpriteSpeedXY(speedX, speedY);
        game.setPlayerShooting(isSpace);
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public synchronized void keyPressed(KeyEvent ke) {
        pressedKeys.add(ke.getKeyCode());
    }

    @Override
    public synchronized void keyReleased(KeyEvent ke) {
        pressedKeys.remove(ke.getKeyCode());
    }

    @Override
    public void componentResized(ComponentEvent ce) {
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
        createBufferStrategy(2);
        buffer = getBufferStrategy();

        if (loopThread == null || loopThread.getState() == Thread.State.TERMINATED) {
            loopThread = new Thread(this);
            loopRunning = true;
            loopThread.start();
        }
        startNextGame();
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
        if (loopThread == null) return;
        loopRunning = false;
        boolean joined = false;
        while (!joined) {
            try {
                loopThread.join();
                System.out.println("Joined");
                joined = true;
            }
            catch (InterruptedException ex) { }
        }
    }

}
