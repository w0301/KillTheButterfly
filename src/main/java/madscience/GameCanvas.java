package madscience;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Richard Kakaš
 */
public final class GameCanvas extends Canvas implements Runnable, ComponentListener, KeyListener {

    private BufferStrategy buffer;

    private Thread loopThread;
    private final Object loopLock = new Object();
    private boolean loopRunning = false;
    private boolean gamePaused = false;
    private boolean gameEnded = false;

    private Set<Integer> pressedKeys = new HashSet<Integer>();
    private Game game = null;

    public GameCanvas() {
        setVisible(false);
        setIgnoreRepaint(true);
        addComponentListener(this);
        addKeyListener(this);
    }

    public void startGame(Game newGame) {
        synchronized (loopLock) {
            game = newGame;
            gamePaused = false;
            gameEnded = false;
        }
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
            }
        }
    }

    protected void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (game != null) {
            synchronized (loopLock) {
                game.draw((Graphics2D) g);
            }
        }
    }

    @Override
    public void run() {
        final double GAME_HERTZ = 60.0;
        final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;
        final int MAX_UPDATES_BEFORE_RENDER = 5;

        final double TARGET_FPS = 60;
        final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

        long lastUpdateTime = System.nanoTime();
        long lastRenderTime = System.nanoTime();

        while (loopRunning) {
            long now = System.nanoTime();
            int updateCount = 0;

            while (now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER) {
                double elapsed = (now - lastUpdateTime) / 1000000000.0;
                update(elapsed);

                lastUpdateTime += TIME_BETWEEN_UPDATES;
                updateCount++;
            }
            if (now - lastUpdateTime > TIME_BETWEEN_UPDATES) {
               lastUpdateTime = (long) (now - TIME_BETWEEN_UPDATES);
            }

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

            lastRenderTime = now;
            while (now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS &&
                   now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
                Thread.yield();
                try {
                    Thread.sleep(1);
                }
                catch(Exception e) { }
                now = System.nanoTime();
            }
        }
    }

    public void donePressedKeys() {
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
        startGame(GameFactory.createGame(getWidth(), getHeight(), 1));
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
