package madscience;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

/**
 *
 * @author Richard Kakaš
 */
public final class GameCanvas extends Canvas implements Runnable, ComponentListener, KeyListener {

    private BufferStrategy buffer;

    private Thread loopThread;
    private boolean loopRunning = false;
    private boolean gamePaused = false;
    private boolean gameEnded = false;

    private Game game;

    public GameCanvas() {
        setVisible(false);
        setIgnoreRepaint(true);
        addComponentListener(this);
        addKeyListener(this);
    }

    public void startGame(Game newGame) {
        synchronized (this) {
            game = newGame;
            gamePaused = false;
            gameEnded = false;
        }
    }

    public void pauseGame(boolean val) {
        synchronized (this) {
            gamePaused = val;
        }
    }

    protected void update(double sec) {
        if (game != null) {
            synchronized (this) {
                if (!gamePaused && !gameEnded) {
                    if (game.isWon() || game.isLost()) {
                        gameEnded = true;
                    }
                    game.update(sec);
                }
            }
        }
    }

    protected void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (game != null) {
            synchronized (this) {
                game.draw((Graphics2D) g);
            }
        }
    }

    public void run() {
        final double GAME_HERTZ = 30.0;
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

    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
        if (game == null) return;
        synchronized(this) {
            double speedX = game.getPlayerSpriteSpeedX();
            double speedY = game.getPlayerSpriteSpeedY();

            if (ke.getKeyCode() == KeyEvent.VK_LEFT)
                speedX = -game.getPlayerSetSpeed();
            else if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
                speedX = game.getPlayerSetSpeed();
            else if (ke.getKeyCode() == KeyEvent.VK_UP)
                speedY = -game.getPlayerSetSpeed();
            else if (ke.getKeyCode() == KeyEvent.VK_DOWN)
                speedY = game.getPlayerSetSpeed();
            else if (ke.getKeyCode() == KeyEvent.VK_SPACE)
                game.getPlayerSprite().setShooting(200);

            game.setPlayerSpriteSpeedXY(speedX, speedY);
        }
    }

    public void keyReleased(KeyEvent ke) {
        if (game == null) return;
        synchronized(this) {
            double speedX = game.getPlayerSpriteSpeedX();
            double speedY = game.getPlayerSpriteSpeedY();

            if (ke.getKeyCode() == KeyEvent.VK_LEFT || ke.getKeyCode() == KeyEvent.VK_RIGHT)
                speedX = 0;
            else if (ke.getKeyCode() == KeyEvent.VK_UP || ke.getKeyCode() == KeyEvent.VK_DOWN)
                speedY = 0;
            else if (ke.getKeyCode() == KeyEvent.VK_SPACE)
                game.getPlayerSprite().setShooting(false);

            game.setPlayerSpriteSpeedXY(speedX, speedY);
        }
    }

    public void componentResized(ComponentEvent ce) {
    }

    public void componentMoved(ComponentEvent ce) {
    }

    public void componentShown(ComponentEvent ce) {
        createBufferStrategy(2);
        buffer = getBufferStrategy();

        if (loopThread == null || loopThread.getState() == Thread.State.TERMINATED) {
            loopThread = new Thread(this);
            loopRunning = true;
            loopThread.start();
        }
        startGame(new Game(getWidth(), getHeight()));
    }

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
